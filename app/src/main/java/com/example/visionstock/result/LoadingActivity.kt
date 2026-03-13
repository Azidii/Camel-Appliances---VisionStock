package com.example.visionstock

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.visionstock.login.LoginActivity
import com.example.visionstock.result.ResultActivity
import com.example.visionstock.scanner.CamelAIClassifier
import com.example.visionstock.helper.DialogHelper
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoadingActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        supportActionBar?.hide()

        val scannedUri = intent.getStringExtra("scanned_image_uri")
        val itemId = intent.getStringExtra("item_id")
        val itemName = intent.getStringExtra("item_name")
        val itemCategory = intent.getStringExtra("item_category")
        val itemLocation = intent.getStringExtra("item_location")
        val itemQuantity = intent.getIntExtra("item_quantity", 0)

        if (scannedUri == null) {
            window.decorView.postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 1500)
            return
        }

        // Manual search - go directly, NO scan history
        if (itemId != null) {
            navigateToResult(scannedUri, itemId, itemName, itemCategory, itemLocation, itemQuantity)
            return
        }

        classifyAndLookup(scannedUri)
    }

    private fun classifyAndLookup(scannedUri: String) {
        Thread {
            try {
                val uri = Uri.parse(scannedUri)
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    runOnUiThread {
                        DialogHelper.showError(this@LoadingActivity, "Scan Error",
                            "Could not read the captured image.") { finish() }
                    }
                    return@Thread
                }

                // ============ TIER 1: YOLO TFLite Model ============
                var predictedName = "Unknown"
                var predictedCategory = "Others"
                var aiConfidence = 0f

                try {
                    val classifier = CamelAIClassifier(this@LoadingActivity)
                    val result = classifier.classify(bitmap)
                    classifier.close()
                    predictedName = result.label
                    predictedCategory = result.category
                    aiConfidence = result.confidence
                    Log.d(TAG, "TIER 1: $predictedName ($aiConfidence)")
                } catch (e: Exception) {
                    Log.e(TAG, "TIER 1 failed: ${e.message}", e)
                }

                // Fetch ALL inventory items
                runOnUiThread {
                    db.collection("inventory").get()
                        .addOnSuccessListener { snapshot ->
                            val allNames = mutableListOf<String>()
                            for (doc in snapshot) {
                                val n = doc.getString("name") ?: ""
                                if (n.isNotEmpty()) allNames.add(n)
                            }

                            // TIER 1 match check - UPDATED TO 85% CONFIDENCE
                            if (predictedName != "Unknown" && aiConfidence >= 0.85f) {
                                for (doc in snapshot) {
                                    val name = doc.getString("name") ?: ""
                                    if (name.equals(predictedName, ignoreCase = true)) {
                                        Log.d(TAG, "TIER 1 MATCH: $name")
                                        showFoundAndSave(doc, scannedUri, predictedCategory)
                                        return@addOnSuccessListener
                                    }
                                }
                                Log.d(TAG, "TIER 1: '$predictedName' not in DB, trying Gemini...")
                            }

                            // ============ TIER 2: GEMINI VISION API ============
                            if (allNames.isEmpty()) {
                                DialogHelper.showError(this@LoadingActivity, "No Items",
                                    "Inventory is empty. Add items first.") { finish() }
                                return@addOnSuccessListener
                            }

                            Log.d(TAG, "TIER 2: Calling Gemini with ${allNames.size} items")
                            Thread {
                                val geminiResult = callGeminiVision(bitmap, allNames)
                                Log.d(TAG, "Gemini result: ${geminiResult.matchedName} (${geminiResult.status})")

                                runOnUiThread {
                                    if (geminiResult.matchedName != null) {
                                        for (doc in snapshot) {
                                            val name = doc.getString("name") ?: ""
                                            if (name.equals(geminiResult.matchedName, ignoreCase = true)) {
                                                Log.d(TAG, "TIER 2 MATCH: $name")
                                                showFoundAndSave(doc, scannedUri, "Others")
                                                return@runOnUiThread
                                            }
                                        }
                                        DialogHelper.showError(this@LoadingActivity, "Item Not Found",
                                            "Gemini identified \"${geminiResult.matchedName}\" but it was not found in the database.") { finish() }
                                    } else if (geminiResult.status == "api_error_429") {
                                        // RATE LIMIT HANDLER
                                        DialogHelper.showError(this@LoadingActivity, "Scanner Cooling Down",
                                            "You are scanning too fast for the free API plan. Please wait 30 seconds and try again.") { finish() }
                                    } else {
                                        DialogHelper.showError(this@LoadingActivity, "Item Not Found",
                                            "This item could not be matched to any item in the inventory.") { finish() }
                                    }
                                }
                            }.start()
                        }
                        .addOnFailureListener { e ->
                            DialogHelper.showError(this@LoadingActivity, "Database Error",
                                "Could not connect to the database.") { finish() }
                        }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.e(TAG, "Error: ${e.message}", e)
                    DialogHelper.showError(this@LoadingActivity, "Scan Error",
                        "Something went wrong. Please try again.") { finish() }
                }
            }
        }.start()
    }

    // ============ GEMINI VISION API ============

    data class GeminiResult(val matchedName: String?, val status: String)

    private fun callGeminiVision(bitmap: Bitmap, itemNames: List<String>): GeminiResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            Log.e(TAG, "Gemini API key not set")
            return GeminiResult(null, "no_api_key")
        }

        // BUMP SIZE TO 512 SO GEMINI 2.5 CAN ACCURATELY SEE THE WRENCH
        val base64Img = bitmapToBase64(bitmap, 512)
        val itemList = itemNames.joinToString(", ")

        // SMARTER PROMPT TO PREVENT "NONE" RESPONSES
        val prompt = """What is the main object in this image?
If the object is a "Wrench", reply EXACTLY: Wrench
If it is a "Hammer", reply EXACTLY: Hammer
If you absolutely do not know, reply: NONE
List of allowed answers to choose from: [$itemList]"""


        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", "image/jpeg")
                                put("data", base64Img)
                            })
                        })
                    })
                })
            })
        }

        // UPDATED TO GEMINI-2.5-FLASH
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection

        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 30000

            val w = OutputStreamWriter(conn.outputStream)
            w.write(body.toString())
            w.flush()
            w.close()

            val code = conn.responseCode
            Log.d(TAG, "Gemini HTTP $code")

            if (code != 200) {
                val err = conn.errorStream?.bufferedReader()?.readText() ?: ""
                Log.e(TAG, "Gemini error: $err")
                return GeminiResult(null, "api_error_$code")
            }

            val resp = conn.inputStream.bufferedReader().readText()
            Log.d(TAG, "Gemini response: $resp")

            val json = JSONObject(resp)
            val parts = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
            if (parts.length() > 0) {
                val text = parts.getJSONObject(0).getString("text").trim()
                Log.d(TAG, "Gemini says: '$text'")

                if (text.equals("NONE", ignoreCase = true)) return GeminiResult(null, "no_match")

                val exact = itemNames.firstOrNull { it.equals(text, ignoreCase = true) }
                if (exact != null) return GeminiResult(exact, "matched")

                val fuzzy = itemNames.firstOrNull { text.contains(it, ignoreCase = true) }
                if (fuzzy != null) return GeminiResult(fuzzy, "fuzzy")

                val rev = itemNames.firstOrNull { it.contains(text, ignoreCase = true) }
                if (rev != null) return GeminiResult(rev, "reverse")

                return GeminiResult(null, "unrecognized: $text")
            }
            return GeminiResult(null, "empty")
        } catch (e: Exception) {
            Log.e(TAG, "Gemini failed: ${e.message}", e)
            return GeminiResult(null, "exception")
        } finally {
            conn.disconnect()
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap, maxSize: Int): String {
        val r = Math.min(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
        val resized = Bitmap.createScaledBitmap(bitmap, (bitmap.width * r).toInt(), (bitmap.height * r).toInt(), true)
        val out = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    // ============ SUCCESS: save to history + navigate ============

    private fun showFoundAndSave(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        scannedUri: String, fallbackCat: String
    ) {
        val name = doc.getString("name") ?: "Unknown"
        // FIXED DATABASE FIELD NAMES TO MATCH ADD ITEM
        val img = doc.getString("itemPicture") ?: ""
        val imgShow = if (img.isNotEmpty()) img else scannedUri
        val mId = doc.getString("itemID") ?: "N/A"
        val mCat = doc.getString("itemCategory") ?: fallbackCat
        val mLoc = doc.getString("itemLocation") ?: "Not Specified"
        val mQty = doc.getLong("quantity")?.toInt() ?: 0

        // Save ONLY successful scans
        db.collection("camel_scan_history").add(hashMapOf(
            "name" to name, "category" to mCat, "itemId" to mId,
            "location" to mLoc, "quantity" to mQty,
            "imageUrl" to imgShow, "timestamp" to System.currentTimeMillis()
        ))

        DialogHelper.showSuccess(this@LoadingActivity, "Item Found!", "Matched: $name") {
            navigateToResult(imgShow, mId, name, mCat, mLoc, mQty)
        }
    }

    // ============ Navigate (no history save) ============

    private fun navigateToResult(
        imageUri: String, itemId: String?, itemName: String?,
        itemCategory: String?, itemLocation: String?, itemQuantity: Int
    ) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("scanned_image_uri", imageUri)
        intent.putExtra("item_id", itemId)
        intent.putExtra("item_name", itemName)
        intent.putExtra("item_category", itemCategory)
        intent.putExtra("item_location", itemLocation)
        intent.putExtra("item_quantity", itemQuantity)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "LoadingActivity"
    }
}
