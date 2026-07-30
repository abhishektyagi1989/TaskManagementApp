# TaskMaster: Modern Task Management Android Application

TaskMaster is a complete, production-ready Android application designed using modern Android development best practices, Clean Architecture, and offline-first synchronization.

## Architecture & Principles
The project follows **Clean Architecture** separated into three distinct layers:
- **Domain Layer**: Contains pure Kotlin business logic, models, repository interfaces, and UseCases (e.g. `LoginUseCase`, `GetTasksUseCase`, `SyncTasksUseCase`). It is completely independent of external libraries and frameworks, making it easily testable.
- **Data Layer**: Manages data coordination. It implements the repository contract, handles networking via Retrofit/OkHttp, caches data locally using Room database, manages preferences via DataStore, and coordinates background workers.
- **Presentation Layer (MVVM)**: Implements Jetpack Compose Material 3 UI layouts. ViewModels expose `UiState` via `StateFlow` and publish one-time events (like showing Snackbars or Navigating) via `SharedFlow`.

### Core Highlights
1. **Offline-First Synchronization**:
   - Every read and write operation interacts directly with the local Room Cache.
   - Database entries include a `syncState` enum (`SYNCED`, `PENDING_INSERT`, `PENDING_UPDATE`, `PENDING_DELETE`).
   - A `SyncWorker` managed by WorkManager synchronizes the cache with JSONPlaceholder API once network connectivity is recovered.
2. **Biometric Authentication**:
   - Integrates biometric prompt authentication (using fingerprint or face scan) for seamless login once user session credentials have been established.
3. **Deep Linking**:
   - Supports deep link routing directly to specific task details via `taskmaster://task/detail/{taskId}`.
4. **Firebase Cloud Messaging (FCM)**:
   - Registers a push receiver service that triggers background synchronization when a sync payload (`"action": "sync"`) is received.

---

## Folder Structure

```
com.taskmaster
│
├── di/                     # Hilt Modules (AppModule, DatabaseModule, NetworkModule)
│
├── domain/                 # Pure Business Logic
│   ├── model/              # Domain Models (Task, Priority, Status, SyncState)
│   ├── repository/         # Repository contracts
│   └── usecase/            # Use Cases (GetTasks, CreateTask, UpdateTask, DeleteTask, SyncTasks, Login, Logout)
│
├── data/                   # Data Access & Operations
│   ├── local/              # Local Cache (Room DB, DAOs, Entities, DataStore Preferences)
│   ├── remote/             # Network API (Retrofit, DTOs, ApiResult wrapper)
│   ├── repository/         # Repository implementations (TaskRepositoryImpl)
│   ├── sync/               # WorkManager workers (SyncWorker)
│   └── fcm/                # Push notifications receiver service
│
├── ui/                     # Jetpack Compose UI
│   ├── login/              # Login screen, ViewModel, and UiStates
│   ├── dashboard/          # Dashboard statistics overview screen and VM
│   ├── task/               # Task CRUD forms, details, and VMs
│   ├── components/         # Reusable widgets (LoadingSkeletons, TaskCards, dialogs)
│   └── theme/              # Material 3 styling (Colors, Typography, Themes)
│
├── navigation/             # SetupNavGraph & Screen routes definitions
│
└── utils/                  # Helper utilities (DateFormatter, NetworkHelper)
```

---

## Technical Stack & Libraries
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Dependency Injection**: Dagger Hilt (`hilt-android`)
- **Local Database**: Room Database (`androidx.room`)
- **API Networking**: Retrofit + OkHttp
- **Preferences**: DataStore Preferences
- **Async Execution**: Kotlin Coroutines & Flow (StateFlow, SharedFlow)
- **Image Loading**: Coil (`coil-compose`)
- **Logging**: Timber (`timber`)
- **Background Execution**: WorkManager (`androidx.work-runtime-ktx`)
- **System Biometrics**: Biometric Manager (`androidx.biometric`)
- **Testing**: JUnit 4, Mockito Kotlin, kotlinx-coroutines-test, Turbine

---

## How to Run & Build

### Running the App
1. Import the project in Android Studio (version Iguana or newer recommended).
2. Build and run the app on a connected emulator or physical device.
3. Log in using the mock credentials:
   - **Email**: `admin@test.com`
   - **Password**: `123456`

### Running Unit Tests
To execute all VM, UseCase, and Repository tests:
```bash
./gradlew testDebugUnitTest
```

### APK Generation
To generate the release or debug APK:
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (needs configuration signing in app/build.gradle.kts)
./gradlew assembleRelease
```
The generated APKs will be located under `app/build/outputs/apk/`.

---

## Future Improvements
- **True Auth Integration**: Replace the mock authentication module with Firebase Auth or OAuth 2.0.
- **WebSocket Sync**: Implement WebSockets or Server-Sent Events (SSE) to push tasks dynamically in real time instead of periodic scheduling.
- **Advanced Rich Editor**: Support rich text annotations, subtasks list, and attachments (images, voice notes) in task descriptions.
