# CaloVelo 🚴🏋️🥗

**CaloVelo** is an offline-first, AI-enhanced Android fitness, nutrition, and social workout tracker built with Kotlin and Jetpack Compose.

---

## 📋 Master Plans & Roadmap

For a detailed breakdown of our architectural plans, phase execution, AI vision pipeline, and gym strength tracker design, please view our dedicated [Master Plans & Roadmap Document](docs/PLANS.md).

---

## 🚀 Features & Architecture

- **Phase 1 – Core Persistence & Health Sync**:
  - Room Database (`calovelo_db` v3) for local-first reliability.
  - Android Health Connect SDK integration for seamless background read/write of steps, heart rate, and active calories.
  - Hydration Tracker with WorkManager smart reminders and notification quick-add actions.

- **Phase 2 – Live GPS Workout Recording**:
  - Foreground Service (`foregroundServiceType="location"`) for uninterrupted live GPS tracking.
  - Polyline route encoding (Google reference algorithm).
  - MET-based calorie estimation & post-workout RPE (Rating of Perceived Exertion) bottom sheet feedback.

- **Phase 3 – On-Device TFLite Food Scanner & Nutrition Suite**:
  - CameraX viewfinder with bounding box object detection and local Room seeded nutrition catalog (INDB / USDA dataset).
  - Macro tracking (Protein, Carbs, Fats, Fiber) and daily meal planner.

- **Gym Strength Tracker & AI Analytics Engine**:
  - Muscle group categorization (Chest, Back, Shoulders, Arms, Legs, Core).
  - Set logging (Weight $\times$ Reps $\times$ RPE), volume calculations, 1RM estimates, and progressive overload recommendations.

---

## 🛠️ Tech Stack

- **Language**: 100% Kotlin
- **UI Toolkit**: Jetpack Compose with Material 3 Theme & Glassmorphism
- **Dependency Injection**: Hilt
- **Local Storage**: Room Persistence Library & DataStore Preferences
- **Machine Learning**: TensorFlow Lite (LiteRT) for on-device food vision
- **Asynchronous**: Kotlin Coroutines & Flow
