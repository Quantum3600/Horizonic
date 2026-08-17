# 🌊 Horizonic

**Find your equilibrium in a moving world.**

Horizonic is a modern Android application designed to alleviate motion sickness (kinetosis) through synchronized sensory feedback. By bridging the gap between what your eyes see and what your inner ear feels, Horizonic helps you reclaim your journey—whether you're reading on a train, working in a car, or browsing on a bus.

---

## 🧠 The Science: Why we get sick
Motion sickness is often triggered by **vestibular-ocular mismatch**. Your inner ear (vestibular system) senses the acceleration and curves of a vehicle, but your eyes—focused on a static screen or book—tell your brain you are stationary. This sensory conflict causes the brain to trigger a nausea response.

## ⚖️ The Solution: Two-Fold Harmony

### 1. Visual Motion Cues (Physical Stabilizers)
Horizonic projects a subtle, gyro-stabilized particle overlay across your entire screen using Android's **Accessibility Service**. These particles react in real-time to the vehicle's movement, providing your peripheral vision with the "horizon" it needs to stay anchored, even while you use other apps.

### 2. Vestibular Harmony (Acoustic Relief)
The app generates a pure **100Hz bass sine tone**. This specific frequency is designed to stimulate the vestibular system and provide a consistent rhythmic anchor, helping the brain resolve the sensory mismatch more efficiently.

---

## ✨ Key Features

- **Zen-Inspired UI**: A minimalist, calming interface built with Jetpack Compose, featuring dynamic "Zen Orbs" and ripple animations.
- **Intelligent Auto-Detection**: Uses the device's linear accelerometer to detect vehicular motion and proactively suggest starting a relief session.
- **System-Wide Overlay**: Particles stay visible over any app, ensuring continuous relief while you work or play.
- **Quick Access**: Control your sessions via Home Screen Widgets or the Quick Settings Tile.
- **Deep Customization**: Adjust particle sensitivity, density, and color themes to suit your comfort level.

---

## 🛠️ Tech Stack

- **UI**: 100% [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative interface.
- **Language**: [Kotlin](https://kotlinlang.org/) with Coroutines and Flow for reactive state management.
- **Services**: 
    - **Accessibility Service** for system-wide visual overlays.
    - **Foreground Services** for continuous motion detection and audio playback.
- **Sensors**: High-frequency Gyroscope and Accelerometer integration for low-latency feedback.
- **Architecture**: Clean architecture principles with a focus on modularity and testability.

---

## 🚀 Getting Started

### Prerequisites
- Android device running **Android 8.0 (Oreo)** or higher.
- Headphones (Required for the 100Hz Vestibular Harmony feature).

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/trishit/horizonic.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Build and run the `:app` module on your device.

### Permissions
To provide system-wide relief, Horizonic requires:
- **Accessibility Service**: To draw the stabilizing particles over other apps.
- **Post Notifications**: For session control and auto-detection alerts.

---

## 🎨 Design Philosophy
Horizonic isn't just a tool; it's an experience. The UI is designed to be "invisible"—lowering cognitive load and providing a sense of calm through soft gradients, light typography, and organic motion.

---

*Made with 💙 for everyone who loves the journey but hates the ride.*
