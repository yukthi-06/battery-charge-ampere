# Tree Notes

A hierarchical, recursive, and tree-based notes management application designed exclusively in Java for Android devices. Organize your thoughts, projects, tasks, and ideas with unlimited nesting depth and absolute control over local file storage.

---

## 🌟 Features

- **Recursive Hierarchical Notes**: Infinite depth of sub-notes and folders.
- **Dynamic Leaf vs Folder Views**: Items automatically transition between "leaf note editor" and "sub-children folders" when child nodes are added.
- **Universal Search**: Instantly find notes and titles at any depth level.
- **Drag-free Reordering**: Dynamically arrange nodes up and down within any level.
- **Configurable File Storage**: Choose where your data lives (local app sandbox or any custom user-selected folder via Android Storage Access Framework (SAF)).
- **Soft Delete Mechanism**: Deleted notes/trees are renamed and moved safely, rather than permanently deleted instantly.
- **Backup & Restore**: Easily package all JSON trees into a single ZIP archive, or import them back with a single tap.
- **Dynamic Themes**: Sleek Light, Dark, or System Default material aesthetics.

---

## 🏗️ Architecture & Clean Package Structure

The project strictly follows Android production guidelines, clean architecture, and modern material design tokens in Java:

```
app/src/main/java/com/vypeensoft/todo/
├── MainActivity.java           # Home screen with drawer, list of master trees
├── TreeListActivity.java       # Hierarchical level navigator, recursively launched
├── NodeEditorActivity.java     # Note viewer & multi-line editor with auto-save
├── SettingsActivity.java       # Preference manager (Theme, SAF storage, ZIP)
├── HelpActivity.java           # Beautiful visual usage documentation
├── AboutActivity.java          # App versioning and build information
│
├── TreeNode.java               # Model representing a node recursively
├── TreeDocument.java           # Model representing a Master Tree file
│
├── MasterTreeAdapter.java      # Recycler adapter for master trees
├── TreeNodeAdapter.java        # Recycler adapter for nodes (folder/note views)
│
├── JsonStorageManager.java     # Local sandbox / SAF JSON stream persistence
├── FileUtils.java              # ZIP archive import/export manager
└── ThemeUtils.java             # System, Light, and Dark app theme toggles
```

### 🗃️ Storage Schema

Every Master Tree document represents a single separate JSON file recursively capturing the complete sub-tree.

#### JSON Structure Example:

```json
{
  "id": "e4f8d55b-43d9-48aa-b541-6927bf7c8585",
  "name": "My Work Projects",
  "rootNodes": [
    {
      "id": "a1c2-3d4e",
      "title": "Project Alpha",
      "content": "Focus on backend architecture.",
      "createdDate": 1716480164000,
      "updatedDate": 1716480195000,
      "children": [
        {
          "id": "f5g6-7h8i",
          "title": "Database Schema",
          "content": "PostgreSQL layout details here...",
          "createdDate": 1716480210000,
          "updatedDate": 1716480210000,
          "children": []
        }
      ]
    }
  ]
}
```

---

## 🛠️ Build and Compilation Instructions

### 1. Requirements
- **Java**: JDK 17 (Temurin recommended)
- **Gradle**: Wrapper (configured automatically)
- **Android Studio**: Ladybug / Jellyfish or higher

### 2. Standard Local Compilation
To compile locally using Gradle wrapper:

- **Windows**:
  ```powershell
  ./gradlew.bat assembleDebug
  ```
- **macOS / Linux**:
  ```bash
  chmod +x gradlew
  ./gradlew assembleDebug
  ```

Once compiled, the output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🚀 CI/CD Automation (GitHub Actions)

The app is fully integrated with a automated GitHub Actions workflow configured at `.github/workflows/android.yml`:
- Installs **Java 17**.
- Caches Gradle builds for blistering fast performance.
- Automatically handles Windows vs UNIX gradlew execution formatting using `dos2unix`.
- Compiles the project, renames the output to `TreeNotes-[TIMESTAMP].apk`, and uploads it as a downloadable release artifact on every push to the `main` branch.