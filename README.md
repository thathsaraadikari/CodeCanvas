# 📱 CodeCanvas

CodeCanvas is a fast, lightweight, native Android text editor built specifically for developers and technical writers. Developed from the ground up using **Kotlin** and **Jetpack Compose**, it delivers a clean, classic IDE experience on mobile while packing advanced features like delta-based version control, real-time syntax highlighting, and invisible crash recovery.

Developed as the mini-project for **IS2205 — Mobile Application Design & Development**.

---

## ✨ Features

### Editor Engine
- **File Management:** Create new files, save, and Save As with encoding selection (UTF-8, ASCII, ISO-8859-1, UTF-16).
- **Sidebar Navigation:** A slide-out drawer listing all recent files. Tap to open, long-press to delete.
- **Undo / Redo:** A dual-stack memory system tracking up to 50 granular edits per session.
- **Search & Replace:** Dialog-based search with real-time yellow highlighting. Highlights auto-clear when you tap back into the editor.
- **Word Wrap Toggle:** Enable or disable horizontal scrolling for long lines.
- **Code Formatting:** One-tap auto-indentation engine for Kotlin code.
- **Read-Only Lock:** Lock any file to prevent accidental edits. Persisted in the database.

### Syntax Highlighting
- **Kotlin:** Keywords, strings, single-line comments, and annotations (`@Composable`, etc.) are dynamically colored via a Regex-based `VisualTransformation` engine.
- **Markdown:** Headers (`#`), bold (`**text**`), italic (`*text*`), and inline code (`` `code` ``) are styled in real-time with theme-adaptive colors for both dark and light modes.

### Delta-Based Version Control
- **Incremental Versioning:** The first save stores the full file. Every subsequent save calculates only the changed lines (delta patch) using `java-diff-utils` and stores the lightweight patch string — never duplicating the full file.
- **Version History:** View all saved snapshots with timestamps. Expand any version to inspect the raw diff patch.
- **Rollback:** Instantly restore any previous version. The engine reconstructs the file by sequentially applying patches from the base version.
- **Room Database:** All version metadata and patches are persisted locally via SQLite (Room Persistence Library).

### Fault Tolerance
- **Crash Prevention:** A background Kotlin Coroutine silently caches the active editor buffer to a hidden `.crash_recovery_cache.txt` file every 10 seconds.
- **Auto-Recovery:** On app relaunch, if unsaved cached data is detected, it is automatically loaded into the editor as `Recovered_File.txt`.

### Theming
- **Dark / Light Mode:** Toggle between a premium Obsidian dark theme and a clean Frost light theme, both with carefully curated color palettes.

---

## 🛠️ Architecture

CodeCanvas follows the **MVVM (Model-View-ViewModel)** architecture:

```
┌─────────────────────────────────────────────────┐
│  VIEW (UI Layer)                                │
│  MainScreen.kt — Jetpack Compose UI             │
│  SyntaxHighlighter.kt — VisualTransformation    │
├─────────────────────────────────────────────────┤
│  VIEWMODEL (Logic Layer)                        │
│  EditorViewModel.kt — State, Undo/Redo, I/O    │
├─────────────────────────────────────────────────┤
│  MODEL (Data Layer)                             │
│  VersionControlRepository.kt — Delta engine     │
│  AppDatabase.kt / FileVersionDao.kt — Room DB   │
└─────────────────────────────────────────────────┘
```

### Tech Stack
| Component | Technology |
|:---|:---|
| Language | Kotlin |
| UI Toolkit | Jetpack Compose (Material 3) |
| Architecture | MVVM with StateFlow |
| Database | Room (SQLite) |
| Diff Engine | java-diff-utils |
| Build System | Gradle with KSP |

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Latest version)
- Android SDK (API 26+, target API 37)

### Build & Run
```bash
git clone https://github.com/thathsaraadikari/CodeCanvas.git
```
1. Open the project folder in Android Studio.
2. Let Gradle sync and download dependencies.
3. Connect a physical Android device or start an emulator.
4. Click **Run** (`Shift + F10`).

### Generate Signed APK
1. In Android Studio: **Build** → **Generate Signed Bundle / APK** → **APK**
2. Create or select a keystore, choose the **release** variant.
3. The signed APK will be at `app/release/app-release.apk`.

---

## 🤝 The Team
Developed collaboratively by:
- **Thathsara Adikari**
- **Sithmini**
- **Ashi**
