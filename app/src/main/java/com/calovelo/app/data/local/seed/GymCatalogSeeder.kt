// app/src/main/java/com/calovelo/app/data/local/seed/GymCatalogSeeder.kt
package com.calovelo.app.data.local.seed

import android.content.Context
import com.calovelo.app.data.local.dao.ExerciseCatalogDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Populates the exercise catalog from assets/gym_seed.json the first time the app runs (and after a
 * v3 -> v4 upgrade, which creates the table empty). Idempotent: once the table has rows it does
 * nothing, so it never overwrites anything. App start-up and the repository both call [ensureSeeded],
 * and the repository awaits it before reading, so the catalog is never observed empty. The insert is
 * one transaction: either the whole catalog is saved or none of it, and a failed attempt is retried
 * on the next call.
 */
@Singleton
class GymCatalogSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ExerciseCatalogDao
) {
    private val mutex = Mutex()

    @Volatile
    private var seeded = false

    suspend fun ensureSeeded() {
        if (seeded) return
        mutex.withLock {
            if (seeded) return
            withContext(Dispatchers.IO) {
                if (dao.count() == 0) {
                    val text = context.assets.open(SEED_ASSET).bufferedReader().use { it.readText() }
                    dao.upsertAll(GymSeedParser.parse(text))
                }
            }
            seeded = true
        }
    }

    companion object {
        const val SEED_ASSET = "gym_seed.json"
    }
}
