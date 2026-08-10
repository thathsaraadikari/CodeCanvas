# 📱 CodeCanvas

CodeCanvas is a fast, highly-capable, native Android text editor built specifically for coding on mobile devices. Developed from the ground up using **Kotlin** and **Jetpack Compose**, CodeCanvas provides a clean, classic text-editor experience while packing advanced under-the-hood features like delta-based version control and real-time syntax highlighting.

Developed as the mini-project for **IS2205 Mobile Application Design & Development**.

---

## ✨ Key Features

- **Classic Editor Interface:** A minimalist, distraction-free environment complete with a dedicated line-number gutter, classic Top App Bar, and easy-to-use drawer navigation.
- **Delta-Based Version Control:** Say goodbye to massive file sizes. CodeCanvas tracks your edit history using an intelligent `java-diff-utils` engine that calculates the exact lines changed and saves them as tiny "patches" in a local SQLite Room database, allowing you to rollback to previous versions instantly.
- **Real-Time Syntax Highlighting:** Employs a highly efficient Regex-based text transformer to actively scan and colorize your Kotlin and Markdown syntax as you type without dropping frames.
- **Background Crash Prevention:** Automatically saves your active editor state to a hidden `.crash_recovery.tmp` cache file every 10 seconds. If your app closes unexpectedly, your unsaved work is instantly restored upon relaunch.
- **Advanced Editing Tools:** Features full Undo/Redo stacks, Search & Replace, Word Wrap toggles, and Read-Only lock modes to protect your code.
- **Dynamic Theming:** Seamlessly integrates with Android's system-wide Dark/Light modes for optimal viewing in any environment.

## 🛠️ Architecture & Tech Stack

CodeCanvas is engineered following modern Android best practices:

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a fully declarative, functional UI.
- **Architecture**: **MVVM (Model-View-ViewModel)**. The app cleanly separates UI logic (`MainScreen.kt`), State/Business logic (`EditorViewModel.kt`), and Data persistence (`VersionControlRepository.kt`).
- **Database**: Room (SQLite) for managing delta-patch version tracking.
- **State Management**: Kotlin `StateFlow` and Coroutines for asynchronous background tasks.

## 🚀 Getting Started

To run CodeCanvas locally on your machine:

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Latest version recommended)
- Android SDK (API 34+)

### Installation
1. Clone this repository:
   ```bash
   git clone https://github.com/thathsaraadikari/CodeCanvas.git
   ```
2. Open the project folder in Android Studio.
3. Allow Gradle to sync and download all necessary dependencies.
4. Connect an Android physical device via USB/Wi-Fi or start an Android Emulator.
5. Click **Run** (`Shift + F10`) to compile and deploy the app!

## 🤝 The Team
Developed collaboratively by:
- **Thathsara Adikari**
- **Sithmini**
- **Ashi**
