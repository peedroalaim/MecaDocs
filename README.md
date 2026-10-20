# MecaDocs (Modo Expert) ⚡

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-purple.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**MecaDocs** is an advanced mobile engineering lab station and AI-powered datasheet generator designed for electronics engineers, hobbyists, and makers. Search for any electronic component (*ESP32*, *Arduino Uno*, *L298N*, *HC-SR04*, etc.) and instantly generate or retrieve comprehensive technical datasheets complete with electrical specifications, pinouts, bench safety warnings, and ready-to-use microcontroller test code.

---

## 🚀 Key Features

- **🧠 Multi-Provider AI Pool & Rotation**: Automatic fallback and round-robin load balancing across top AI providers (*OpenRouter*, *Google Gemini Flash*, *Groq Llama 3.3*, *MecaDocs Primary Gateway*) with intelligent 60s cooldown handling and network resilience.
- **💾 Dual Room DB Persistence**:
  - **Favorites & Search History**: Bookmark essential components and track recent searches.
  - **Dynamic AI Result Caching**: Any datasheet successfully retrieved online is automatically cached locally, allowing offline access to newly searched components beyond the built-in mocks.
- **🔌 Offline Expert Mocks**: Built-in fallback database containing fully detailed datasheets and sample code for essential components (*ESP32*, *Arduino Uno*, *L298N*, *STM32 Blue Pill*, *HC-SR04*).
- **🧮 Bench Tools**: Integrated Voltage Divider Calculator for quick electronics calculations on the workbench.
- **🎨 Modern Material 3 UI**: Engineered with Jetpack Compose, Edge-to-Edge support, smooth animations, and a custom technical design system.

---

## 🛠️ Tech Stack

- **Language**: 100% Kotlin
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: MVVM + Repository Pattern with `StateFlow`
- **Networking**: Retrofit 2 + OkHttp 3 (with custom `ApiRotationInterceptor`)
- **JSON Parsing**: Moshi with KSP code generation
- **Local Database**: Room Database (version 2)
- **Testing**: JUnit 4, Kotlin Coroutines Test

---

## 📦 Getting Started & Building

1. **Clone the repository**:
   ```bash
   git clone https://github.com/peedroalaim/MecaDocs.git
   ```
2. **Open in Android Studio**:
   - Open Android Studio, select **Open**, and choose the cloned `MecaDocs` folder.
   - Let Gradle sync dependencies.
3. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```
4. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📦 Publishing to GitHub (Step-by-Step)

If you want to upload this project to GitHub so others can try it, follow these steps in your terminal or Git client:

1. **Initialize Git** (if not already initialized):
   ```bash
   git init
   git branch -M main
   ```
2. **Add all project files**:
   ```bash
   git add .
   git commit -m "Initial commit: MecaDocs Android App v1.0"
   ```
3. **Create a new repository on GitHub** (e.g., named `MecaDocs`).
4. **Link your local repository to GitHub and push**:
   ```bash
   git remote add origin https://github.com/peedroalaim/MecaDocs.git
   git push -u origin main
   ```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).

---
*Powered by AI • Lab Station • By: Softworld*
