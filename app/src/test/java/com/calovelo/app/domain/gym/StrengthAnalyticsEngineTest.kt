// app/src/test/java/com/calovelo/app/domain/gym/StrengthAnalyticsEngineTest.kt
package com.calovelo.app.domain.gym

import com.calovelo.app.domain.model.GymSet
import com.calovelo.app.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthAnalyticsEngineTest {

    private val engine = StrengthAnalyticsEngine()

    private fun set(
        weight: Double,
        reps: Int,
        exercise: String = "bench_press",
        type: SetType = SetType.NORMAL,
        index: Int = 1
    ) = GymSet(
        setId = "s-$exercise-$index-$weight-$reps",
        sessionId = "session",
        exerciseId = exercise,
        setIndex = index,
        weightKg = weight,
        reps = reps,
        rpe = null,
        type = type
    )

    // ---- Volume -------------------------------------------------------------------------------

    @Test
    fun setVolume_isWeightTimesReps() {
        assertEquals(500.0, engine.setVolume(100.0, 5), 1e-9)
        assertEquals(187.5, engine.setVolume(37.5, 5), 1e-9)
    }

    @Test
    fun totalSessionVolume_sumsWeightTimesRepsAcrossAllSets() {
        val sets = listOf(
            set(100.0, 5, index = 1),
            set(100.0, 5, index = 2),
            set(80.0, 8, exercise = "squat", index = 1)
        )
        // 500 + 500 + 640
        assertEquals(1_640.0, engine.totalSessionVolume(sets), 1e-9)
    }

    @Test
    fun totalSessionVolume_ofNoSets_isZero() {
        assertEquals(0.0, engine.totalSessionVolume(emptyList()), 1e-9)
    }

    @Test
    fun totalSessionVolume_includesWarmupsByDefault_andCanExcludeThem() {
        val sets = listOf(
            set(40.0, 10, type = SetType.WARMUP, index = 1), // 400
            set(100.0, 5, index = 2),                        // 500
            set(90.0, 4, type = SetType.DROPSET, index = 3)  // 360
        )
        assertEquals(1_260.0, engine.totalSessionVolume(sets), 1e-9)
        assertEquals(860.0, engine.totalSessionVolume(sets, includeWarmups = false), 1e-9)
    }

    @Test
    fun totalSessionVolume_ignoresInvalidSets() {
        val sets = listOf(
            set(100.0, 5),
            set(100.0, 0),            // no reps
            set(-20.0, 10),           // negative weight
            set(Double.NaN, 5),
            set(Double.POSITIVE_INFINITY, 5),
            set(0.0, 12)              // bodyweight entry with no added load adds nothing
        )
        assertEquals(500.0, engine.totalSessionVolume(sets), 1e-9)
    }

    @Test
    fun volumeByExercise_groupsInFirstAppearanceOrder_andSumsToTotal() {
        val sets = listOf(
            set(100.0, 5, exercise = "bench_press", index = 1),
            set(80.0, 8, exercise = "squat", index = 1),
            set(100.0, 3, exercise = "bench_press", index = 2)
        )
        val byExercise = engine.volumeByExercise(sets)
        assertEquals(listOf("bench_press", "squat"), byExercise.keys.toList())
        assertEquals(800.0, byExercise.getValue("bench_press"), 1e-9)
        assertEquals(640.0, byExercise.getValue("squat"), 1e-9)
        assertEquals(engine.totalSessionVolume(sets), byExercise.values.sum(), 1e-9)
    }

    @Test
    fun volumeByExercise_canExcludeWarmups() {
        val sets = listOf(
            set(40.0, 10, type = SetType.WARMUP, index = 1),
            set(100.0, 5, index = 2)
        )
        assertEquals(500.0, engine.volumeByExercise(sets, includeWarmups = false).getValue("bench_press"), 1e-9)
    }

    // ---- Epley 1RM ----------------------------------------------------------------------------

    @Test
    fun estimateOneRepMax_followsEpley() {
        assertEquals(100.0 * (1.0 + 5.0 / 30.0), engine.estimateOneRepMax(100.0, 5), 1e-9) // 116.666...
        assertEquals(80.0, engine.estimateOneRepMax(60.0, 10), 1e-9)
        assertEquals(200.0, engine.estimateOneRepMax(100.0, 30), 1e-9)
    }

    @Test
    fun estimateOneRepMax_ofASingleRep_appliesTheLiteralFormula() {
        assertEquals(100.0 * (1.0 + 1.0 / 30.0), engine.estimateOneRepMax(100.0, 1), 1e-9)
    }

    @Test
    fun estimateOneRepMax_isZeroForInvalidInput() {
        assertEquals(0.0, engine.estimateOneRepMax(100.0, 0), 0.0)
        assertEquals(0.0, engine.estimateOneRepMax(100.0, -3), 0.0)
        assertEquals(0.0, engine.estimateOneRepMax(0.0, 5), 0.0)
        assertEquals(0.0, engine.estimateOneRepMax(-50.0, 5), 0.0)
        assertEquals(0.0, engine.estimateOneRepMax(Double.NaN, 5), 0.0)
    }

    @Test
    fun estimateOneRepMax_growsWithWeightAndWithReps() {
        assertTrue(engine.estimateOneRepMax(105.0, 5) > engine.estimateOneRepMax(100.0, 5))
        assertTrue(engine.estimateOneRepMax(100.0, 6) > engine.estimateOneRepMax(100.0, 5))
    }

    @Test
    fun estimateOneRepMax_ofASet_matchesTheScalarOverload() {
        val s = set(72.5, 6)
        assertEquals(engine.estimateOneRepMax(72.5, 6), engine.estimateOneRepMax(s), 0.0)
    }

    @Test
    fun bestEstimatedOneRepMax_picksTheStrongestWorkingSet() {
        val sets = listOf(
            set(100.0, 5, index = 1),  // 116.67
            set(110.0, 3, index = 2),  // 121.0
            set(90.0, 10, index = 3)   // 120.0
        )
        assertEquals(121.0, engine.bestEstimatedOneRepMax(sets)!!, 1e-9)
    }

    @Test
    fun bestEstimatedOneRepMax_ignoresWarmups_andReturnsNullWhenNothingQualifies() {
        val warmupOnly = listOf(set(200.0, 10, type = SetType.WARMUP))
        assertNull(engine.bestEstimatedOneRepMax(warmupOnly))
        assertNull(engine.bestEstimatedOneRepMax(emptyList()))
        assertNull(engine.bestEstimatedOneRepMax(listOf(set(100.0, 0))))

        val mixed = listOf(set(200.0, 10, type = SetType.WARMUP), set(100.0, 5, index = 2))
        assertEquals(100.0 * (1.0 + 5.0 / 30.0), engine.bestEstimatedOneRepMax(mixed)!!, 1e-9)
    }

    @Test
    fun weightForReps_invertsEpley() {
        val oneRm = engine.estimateOneRepMax(100.0, 5)
        assertEquals(100.0, engine.weightForReps(oneRm, 5), 1e-9)
        assertNotNull(engine.weightForReps(oneRm, 8))
        assertTrue(engine.weightForReps(oneRm, 8) < engine.weightForReps(oneRm, 5))
        assertEquals(0.0, engine.weightForReps(0.0, 5), 0.0)
        assertEquals(0.0, engine.weightForReps(120.0, 0), 0.0)
    }
}
