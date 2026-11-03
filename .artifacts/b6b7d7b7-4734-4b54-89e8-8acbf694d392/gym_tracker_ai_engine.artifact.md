# CaloVelo Gym Workout Tracker & AI Analytics Engine

> Elevating gym tracking beyond basic chat logs into a professional, data-driven strength training and AI coaching ecosystem.

---

## 1. Gym Tracker Feature Set (Inspired by ChatGPT Workout Logs + Hevy/Strong)

### A. Structured Exercise Logging
- **Muscle Group Sections**: Grouped by Chest, Back, Shoulders, Biceps, Triceps, Quads, Hamstrings, Calves, Core.
- **Set Types**: Normal, Warm-up, Drop set, Failure.
- **Per-Set Metrics**:
  - Weight (kg / lbs)
  - Reps completed
  - RPE (Rating of Perceived Exertion, 1–10) or RIR (Reps in Reserve)
  - Notes per set (e.g. *"Right shoulder discomfort"*, *"Felt explosive"*)
- **Quick-Fill from Previous Session**: Automatically populates previous weight and reps so you can instantly beat your last performance (Progressive Overload).

---

## 2. Advanced Statistics & Analytics (What makes CaloVelo superior)

1. **Total Volume Calculation**: $\sum (\text{Weight} \times \text{Reps})$ per exercise and per session.
2. **Estimated One-Rep Max (1RM)**: Calculated using the Epley formula:
   $$\text{1RM} = \text{Weight} \times \left(1 + \frac{\text{Reps}}{30}\right)$$
3. **Personal Records (PR) Engine**: Instantly flags new PRs with trophy badges (e.g. *Heaviest Bench Press*, *Max Volume Deadlift*).
4. **Muscle Fatigue Heatmap**: Tracks which muscle groups were hit recently and estimates recovery percentage.

---

## 3. In-App AI Analytics & Progressive Overload Engine

Instead of cloud API costs, CaloVelo uses an on-device rule-based ML & statistical analyzer to evaluate workout history and generate actionable recommendations:

### AI Recommendation Pipeline
1. **Performance Trend Analysis**:
   - Compares current set completion against the last 3 sessions for the same exercise.
2. **Progressive Overload Rule**:
   - If user completes all target reps with RPE $\le 8$, the AI recommends: **"Increase weight by 2.5 kg next session."**
   - If reps fell short or RPE was 10 (Failure), the AI recommends: **"Maintain weight and focus on form."**
3. **Injury / Discomfort Guardrail**:
   - If user notes indicate pain (e.g. *"shoulder discomfort"*), the AI flags the exercise and suggests a joint-friendly alternative (e.g., substituting Flat Barbell Bench Press with Dumbbell Floor Press or Cable Flyes).
4. **Next Workout Generator**:
   - Automatically schedules and plans the next push/pull/legs or upper/lower split based on recovery intervals.

---

## 4. Room Database Schema (Version 4)

```kotlin
@Entity(tableName = "exercise_catalog")
data class ExerciseCatalogEntity(
    @PrimaryKey val exerciseId: String,
    val name: String,
    val muscleGroup: String, // CHEST, BACK, SHOULDERS, etc.
    val equipmentType: String // BARBELL, DUMBBELL, MACHINE, CABLE, BODYWEIGHT
)

@Entity(tableName = "workout_exercises")
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long, // Linked to WorkoutSessionEntity
    val exerciseId: String,
    val notes: String?
)

@Entity(tableName = "exercise_sets")
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true) val setId: Long = 0,
    val workoutExerciseId: Long,
    val setIndex: Int,
    val weightKg: Float,
    val reps: Int,
    val rpe: Int?,
    val setType: String, // NORMAL, WARMUP, DROPSET, FAILURE
    val isCompleted: Boolean,
    val setNote: String?
)
```
