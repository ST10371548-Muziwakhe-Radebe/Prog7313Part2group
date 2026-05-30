# Budget Tracker App — PROG7313 Part 2

## Description
A personal budget tracker Android app built with Kotlin and RoomDB. 
The app helps users track their spending habits and savings goals.

## Features
- User login and registration
- Create expense categories
- Add expense entries with date, time, description and category
- Optionally attach a photo to each expense entry
- Set minimum and maximum monthly budget goals
- View all expenses within a selected period
- View total spending per category within a selected period

## Tech Stack
- Kotlin
- Android Studio
- RoomDB (local offline database)
- GitHub Actions (automated CI/CD builds)

## Team Members & Division of Labor (Part 3)
- **Member 1 (ST10371548) - Firebase Setup & Online Database (25 Marks)**:
  - Designed and implemented the unified database layer (`FirebaseHelper.kt`) supporting seamless switching between local RoomDB and online Firebase Realtime Database.
  - Built robust coroutine-driven synchronization pipelines to automatically migrate local user profile, custom categories, and expense transaction entries to Firebase.
  - Configured project-level and app-level `build.gradle.kts` and added support for the google-services Gradle plugin.
  - Implemented detailed logging and structured try-catch mechanisms to handle database switches gracefully without app crashes.
- **Member 2 (ST10440606)**: Categories, Entries & Photo Feature
- **Member 3 (ST10377293)**: Goals, Reports & Period Views
- **Member 4 (ST10440322)**: UI/UX & Demo Video

---

## ☁️ Firebase Integration & Setup Instructions (Member 1)

### 1. Database Switcher UI
A premium interactive toggle switch has been added to the dashboard header:
- **☁️ Online Sync (Firebase)**: Toggling this switch instantly toggles all reads, writes, and authentication checks from local Room DB to online Firebase Realtime Database.
- **Migrate Local Data to Firebase**: Triggering this button launches a background sync coroutine, fetches all categories, user profile credentials, and transaction entries from the local SQLite/Room DB, and uploads them to Firebase.

### 2. Connect Your Own Firebase Project
To test the Firebase features on your own Google account:
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new Firebase Project named `MoneyGoals`.
3. Add a new Android App to the project and enter the package name: `com.example.prog7313appupdated`.
4. Download the `google-services.json` file.
5. Replace the placeholder `google-services.json` inside the `app/` directory of this project with your newly downloaded file.
6. Enable **Realtime Database** in the Firebase console and set your security rules to read/write:
   ```json
   {
     "rules": {
       ".read": "auth != null || true",
       ".write": "auth != null || true"
     }
   }
   ```
7. Build and run the app. Enable the online sync switch, register or log in, and you will see the JSON objects populate instantly under `/users`, `/categories`, and `/entries` nodes in the Firebase Realtime Database console!

### 3. Database Architecture & Sync
The sync architecture is designed with the Repository Pattern in mind:
- **`FirebaseHelper.kt`**: Exposes secure coroutine-based functions to read, write, and update user authentication profiles, custom spending categories, and transaction entries.
- **Backwards Compatibility**: The switcher stores user preference in SharedPreferences. If online mode is toggled, all subsequent screen interactions (signing up, logging in, adding expenses, listing entries) interact with Firebase Realtime Database. If switched off, the app falls back instantly to the local offline Room database.

---

## Demo Video
Link coming soon...

## How to Run
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on emulator or Android device (API 27+)

## GitHub Actions
This project uses GitHub Actions for automated builds.
The workflow triggers on every push to the `main` or `release` branch.
