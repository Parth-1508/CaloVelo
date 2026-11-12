package com.calovelo.app.data.vision

import android.graphics.Bitmap
import com.calovelo.app.BuildConfig
import com.calovelo.app.data.local.dao.FoodCatalogDao
import com.calovelo.app.data.local.mapper.toDomain
import com.calovelo.app.domain.model.BoundingBox
import com.calovelo.app.domain.model.DetectedFoodCandidate
import com.calovelo.app.domain.model.FoodCategory
import com.calovelo.app.domain.model.FoodItem
import com.calovelo.app.domain.model.FoodScanResult
import com.calovelo.app.domain.model.MealType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiVisionService @Inject constructor(
    private val catalogDao: FoodCatalogDao
) {
    private companion object {
        private val MODEL_NAMES = listOf(
            "gemini-2.5-flash",
            "gemini-1.5-flash",
            "gemini-2.0-flash-exp",
            "gemini-1.5-pro"
        )
    }

    private fun getAllApiKeys(): List<String> {
        return listOfNotNull(
            BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() },
            BuildConfig.GEMINI_BURNER_KEY_1.takeIf { it.isNotBlank() },
            BuildConfig.GEMINI_BURNER_KEY_2.takeIf { it.isNotBlank() },
            BuildConfig.GEMINI_BURNER_KEY_3.takeIf { it.isNotBlank() },
            BuildConfig.GEMINI_BURNER_KEY_4.takeIf { it.isNotBlank() },
            BuildConfig.GEMINI_BURNER_KEY_5.takeIf { it.isNotBlank() },
            BuildConfig.GEMINI_BURNER_KEY_6.takeIf { it.isNotBlank() },
            BuildConfig.GEMINI_BURNER_KEY_7.takeIf { it.isNotBlank() }
        ).distinct()
    }

    fun isConfigured(): Boolean = getAllApiKeys().isNotEmpty()

    suspend fun analyze(bitmap: Bitmap): FoodScanResult = withContext(Dispatchers.IO) {
        val keys = getAllApiKeys()
        if (keys.isEmpty()) {
            return@withContext FoodScanResult.Failure(
                "Gemini API Key is missing. Add GEMINI_API_KEY in local.properties to enable Cloud Vision fallback."
            )
        }

        val prompt = """
            You are a nutrition expert AI. Analyze this image of food or a meal plate.
            Identify all distinct food items present. For each food item, provide accurate estimates:
            - food_name: Clean name (e.g. "White Bread with Butter", "Egg Omelette", "Steamed Rice")
            - serving_size_grams: Portion weight in grams (e.g. 100)
            - calories: Total calories for this portion
            - protein_g: Protein in grams
            - carbs_g: Carbohydrates in grams
            - fat_g: Fat in grams
            - is_veg: boolean
            - is_vegan: boolean
            - is_egg: boolean
            - is_non_veg: boolean
            - contains_lactose: boolean

            Return ONLY a JSON array with this exact structure, with no markdown tags or code blocks:
            [
              {
                "food_name": "White Bread with Butter",
                "serving_size_grams": 80,
                "calories": 250,
                "protein_g": 5.5,
                "carbs_g": 32.0,
                "fat_g": 11.0,
                "is_veg": true,
                "is_vegan": false,
                "is_egg": false,
                "is_non_veg": false,
                "contains_lactose": true
              }
            ]
        """.trimIndent()

        var lastError: String? = null

        // Key & Model Rotation Loop (matching Watch Order Engine / FreshKeep resiliency)
        for (modelName in MODEL_NAMES) {
            for (key in keys) {
                try {
                    val generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = key
                    )

                    val inputContent = content {
                        image(bitmap)
                        text(prompt)
                    }

                    val response = generativeModel.generateContent(inputContent)
                    val text = response.text?.trim() ?: continue

                    val cleanJson = cleanJsonResponse(text)
                    val jsonArray = JSONArray(cleanJson)

                    if (jsonArray.length() == 0) {
                        return@withContext FoodScanResult.NothingDetected()
                    }

                    val candidates = mutableListOf<DetectedFoodCandidate>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val foodName = obj.optString("food_name", "Food Item")
                        val servingGrams = obj.optInt("serving_size_grams", 100).coerceAtLeast(10)
                        val totalCalories = obj.optDouble("calories", 150.0)
                        val totalProtein = obj.optDouble("protein_g", 5.0)
                        val totalCarbs = obj.optDouble("carbs_g", 20.0)
                        val totalFat = obj.optDouble("fat_g", 4.0)

                        val factor = 100.0 / servingGrams
                        val kcalPer100g = totalCalories * factor
                        val proteinPer100g = totalProtein * factor
                        val carbsPer100g = totalCarbs * factor
                        val fatPer100g = totalFat * factor

                        val isVeg = obj.optBoolean("is_veg", true)
                        val isVegan = obj.optBoolean("is_vegan", false)
                        val isEgg = obj.optBoolean("is_egg", false)
                        val isNonVeg = obj.optBoolean("is_non_veg", false)
                        val containsLactose = obj.optBoolean("contains_lactose", false)

                        val catalogMatch = catalogDao.searchFiltered(
                            query = foodName,
                            token1 = "%",
                            token2 = "%",
                            token3 = "%",
                            isVegOnly = 0,
                            isVeganOnly = 0,
                            isEggOnly = 0,
                            isLactoseFreeOnly = 0,
                            limit = 1
                        ).firstOrNull()

                        val foodItem = if (catalogMatch != null) {
                            catalogMatch.toDomain()
                        } else {
                            FoodItem(
                                labelKey = "gemini_" + foodName.lowercase().replace(" ", "_").take(30),
                                displayName = foodName,
                                category = FoodCategory.fromKey("mixed_meal"),
                                defaultServingGrams = servingGrams,
                                kcalPer100g = kcalPer100g,
                                proteinPer100g = proteinPer100g,
                                carbsPer100g = carbsPer100g,
                                fatPer100g = fatPer100g,
                                fiberPer100g = 2.0,
                                suitableMealTypes = MealType.entries.toSet(),
                                isVeg = isVeg,
                                isVegan = isVegan,
                                isEgg = isEgg,
                                isNonVeg = isNonVeg,
                                containsLactose = containsLactose
                            )
                        }

                        candidates.add(
                            DetectedFoodCandidate(
                                food = foodItem,
                                confidence = 0.95f,
                                box = BoundingBox(0.1f, 0.1f, 0.9f, 0.9f),
                                portionMultiplier = (servingGrams.toFloat() / foodItem.defaultServingGrams).coerceIn(0.2f, 5.0f)
                            )
                        )
                    }

                    return@withContext FoodScanResult.Detected(
                        candidates = candidates,
                        imageWidth = bitmap.width,
                        imageHeight = bitmap.height
                    )
                } catch (e: Exception) {
                    lastError = e.localizedMessage ?: e.javaClass.simpleName
                    // On rate limit / quota / model error, continue to next key or model
                    continue
                }
            }
        }

        FoodScanResult.Failure("Gemini Vision failed after trying all API keys & models. Last error: $lastError")
    }

    private fun cleanJsonResponse(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }
}
