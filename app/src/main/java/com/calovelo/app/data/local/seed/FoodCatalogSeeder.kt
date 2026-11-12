// app/src/main/java/com/calovelo/app/data/local/seed/FoodCatalogSeeder.kt
package com.calovelo.app.data.local.seed

import android.content.Context
import com.calovelo.app.data.local.dao.FoodCatalogDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Populates the food catalog from assets/nutrition_seed.json the first time the app runs (and
 * after a v2 -> v3 upgrade, which creates the table empty). Idempotent: once the table has rows
 * it does nothing, so it never overwrites anything. Both the app start-up and the repository call
 * [ensureSeeded], and the repository awaits it before reading, so the catalog is never observed empty.
 */
@Singleton
class FoodCatalogSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: FoodCatalogDao
) {
    private val mutex = Mutex()

    @Volatile
    private var seeded = false

    suspend fun ensureSeeded() {
        if (seeded) return
        mutex.withLock {
            if (seeded) return
            withContext(Dispatchers.IO) {
                val currentCount = dao.count()
                val text = context.assets.open(SEED_ASSET).bufferedReader().use { it.readText() }
                val parsed = NutritionSeedParser.parse(text)
                if (currentCount < parsed.size) {
                    dao.upsertAll(parsed)
                }
            }
            seeded = true
        }
    }

    suspend fun forceReseed() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                dao.deleteAll()
                val text = context.assets.open(SEED_ASSET).bufferedReader().use { it.readText() }
                val parsed = NutritionSeedParser.parse(text)
                dao.upsertAll(parsed)
            }
            seeded = true
        }
    }

    companion object {
        const val SEED_ASSET = "nutrition_seed.json"
    }
}
