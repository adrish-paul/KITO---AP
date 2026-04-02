
# Kito – Intelligent Academic Assistant

Kito differs significantly from standard academic utilities by prioritizing **privacy, local-first data ownership, and seamless user experience**. It serves as a sophisticated frontend for your academic life, bridging the gap between legacy university portals (SAP) and modern mobile standards.

> [!NOTE]
> **Privacy Architecture**: Kito operates on a zero-knowledge principle regarding your credentials. All sensitive data is processed locally on your device or transmitted directly to the university portal via a secure, transient link. No intermediary servers store your login information.

📲 **Available on Google Play**
👉 [https://play.google.com/store/apps/details?id=com.kito](https://play.google.com/store/apps/details?id=com.kito)
🍎 **Available on App Store**
👉 [https://apps.apple.com/us/app/kiito/id6759354457](https://apps.apple.com/us/app/kiito/id6759354457)

---

## 🏗️ Architectural Excellence

Kito is built on a robust, scalable foundation designed for reliability and maintainability.

### **Core Architecture: Clean Architecture + MVVM**
The application strictly adheres to **Clean Architecture** principles, separating concerns into three distinct layers:
*   **Presentation Layer**: Built with **Jetpack Compose** (Material 3), utilizing **MVVM (Model-View-ViewModel)** for reactive state management. UI events are decoupled from business logic, ensuring a responsive and crash-resistant interface.
*   **Domain Layer**: Contains pure Kotlin business logic and use cases (e.g., `AppSyncUseCase`, `CalculateAttendanceUseCase`). This layer is platform-agnostic, paving the way for full Multiplatform (KMP) support.
*   **Data Layer**: Manages data sources through the **Repository Pattern**. It arbitrates between the local database (Room), **DataStore Preferences** (Key-Value storage), and network data sources.

### **Kotlin Multiplatform (KMP) Readiness**
The project is structured for **Kotlin Multiplatform**, separating code into `commonMain` (business logic) and `androidMain` (platform-specific implementations). This forward-looking design ensures that the core logic can be shared across iOS and Desktop targets in the future.

---

## 🚀 Key Features & Engineering Highlights

### 1. **Secure Direct-Link Integration (SAP)**
Instead of relying on unstable APIs, Kito implements a custom **Secure Direct-Link** engine.
*   **Mechanism**: The app establishes a direct, encrypted session with the university's SAP portal.
*   **Optimization**: Advanced parsing algorithms transform raw portal data into structured, queryable local objects in milliseconds.
*   **Security**: Authentication tokens are handled in-memory and discarded post-session.

### 2. **Offline-First & Local Persistence**
Kito treats the local device as the source of truth.
*   **Room Database**: Complex relational data (Students, Sections, Attendance) is stored in a normalized SQL database using **Room**. This allows for complex queries, such as "attendance trends over time" or "subjects with low attendance," to be executed instantly without network calls.
*   **Proto DataStore**: User preferences and session configurations are strictly typed and stored using **Protocol Buffers**, ensuring type safety and modifying settings without UI jank.
*   **DataStore Preferences**: Utilized for lightweight key-value storage of application state, flags, and simple user settings, replacing the legacy SharedPreferences with a modern, asynchronous solution.

### 3. **Intelligent Background Synchronization**
*   **WorkManager Integration**: The app employs Android's **WorkManager** exclusively for reliable, deferrable background tasks such as widget updates.
*   **Smart Scheduling**: Sync jobs are optimized to run only when network conditions are favorable, conserving battery while keeping attendance data up-to-date.
*   **AlarmManager**: utilized strictly for precise notification delivery.
*   **Notification Pipeline**: A custom notification controller manages alerts for upcoming classes and attendance thresholds.

### 4. **Comprehensive Academic Suite**
Kito goes beyond attendance to manage your entire academic life:
*   **Smart Calendar**: Integrated academic calendar view.
*   **Exam Central**: dedicated section for upcoming exam schedules and seating arrangements.
*   **Faculty Directory**: detailed faculty search and information system.

### 5. **User-Centric Customization**
*   **Attendance Targets**: Set your own required attendance threshold (e.g., 75%) and get personalized alerts when you drop below it.
*   **Profile Management**: Easily manage your student profile details.
*   **Feedback System**: Direct in-app support channel.
*   **Seamless Updates**: Integrated Google Play In-App Updates ensure you're always running the latest version with the newest features and security patches.

### 6. **Supabase Integration (Static Data)**
*   **Hybrid Data Model**: While sensitive user data comes from SAP, static academic data (Faculty details, Holiday lists) is fetched from **Supabase**. This hybrid approach ensures the app remains lightweight while providing rich contextual information.

### 7. **Modern UI/UX with Jetpack Compose**
*   **Declarative UI**: 100% Kotlin-based UI definition.
*   **Rich Animations**: Integrated **Lottie** animations for a delightful and interactive user experience.
*   **Haze & Glassmorphism**: Utilizes the **Haze** library for high-performance, real-time blurring effects (frosted glass) on dialogs and overlays.
*   **Glance Widgets**: Home screen widgets are exclusively built using **Jetpack Glance**, allowing users to view their schedule without opening the app in a performant, battery-efficient manner.

---

## 🛠️ Technology Stack

| Category | Technologies |
| :--- | :--- |
| **Language** | Kotlin 2.3+ |
| **UI Toolkit** | Jetpack Compose, Material 3 |
| **Architecture** | Clean Architecture, MVVM |
| **Dependency Injection** | Hilt (Dagger) |
| **Asynchronous** | Kotlin Coroutines, Flow |
| **Networking** | OkHttp, Retrofit (Supabase/Rest) |
| **Persistence** | Room (SQLite), Proto DataStore |
| **Background Jobs** | WorkManager |
| **Security** | Android Keystore, EncryptedSharedPreferences |
| **Tools** | Gradle (Kotlin DSL), Version Catalog |

---

## ⚙️ Setup & Build Instructions

This project uses the Gradle build system and is configured for Android Studio Koala or newer.

### Prerequisites
*   Android Studio Koala+
*   JDK 17

### Building the Project

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-repo/kito.git
    cd kito
    ```

2.  **Configure Local Properties**:
    Create a `local.properties` file in the root directory if it doesn't exist (usually auto-generated by Android Studio).

3.  **Build the APK**:
    ```bash
    ./gradlew assembleDebug
    ```

4.  **Run Tests**:
    ```bash
    ./gradlew testDebugUnitTest
    ```

---

## 🔒 Security & Privacy Manifesto

Kito is engineered with a **"Trust No One"** architecture.

*   **No Commercial Tracking**: We do not use Google Analytics, Firebase Analytics, or any third-party behavioral trackers.
*   **Ephemeral Credentials**: Your password is never written to disk. It is used solely for the active session authentication handshake.
*   **Sandboxed Storage**: All local data is stored in the app's private sandbox, inaccessible to other applications without root access.


## 📄 License & Legal

**Kito is proprietary software.**

Copyright (c) 2026 Kito. All rights reserved.

Permission is hereby granted to any person to **view and inspect** the source code of this repository for **transparency and security review purposes only**.

*   **No Commercial Use**: You may not use this code to create competing products or services.
*   **No Redistribution**: No permission is granted to copy, modify, merge, publish, distribute, sublicense, or sell copies of the Software.

The software is provided "AS IS", without warranty of any kind.

---

> **Disclaimer**: Kito is an official application affiliated with ELABS and the School of Electronics.
