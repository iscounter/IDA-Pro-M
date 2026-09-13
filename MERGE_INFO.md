# Merge Information — IDA Pro Mobile + KTIMAZ-REV

This repository is the result of merging **two projects** with a strict rule:
**no code was changed, and nothing was removed.** Every original file is
byte-identical to its source project (verified with recursive diff).

---

## What was merged

| Project | Original archive | Role | Package |
|---|---|---|---|
| **IDA Pro Mobile** | `IDA_PRO-Mobile-main.zip` | **Main project** (repository root) | `com.idapro.mobile` |
| **KTIMAZ-REV** | `KTIMAZ-REV-master.zip` | Nested project (kept fully intact) | `com.imtiaz.ktimazrev` |

## Repository structure

```
IDA_PRO-Mobile-merged/
├── app/                        ← IDA Pro Mobile app module (untouched)
├── gradle/                     ← IDA Pro Mobile wrapper + config (untouched)
├── build.gradle.kts            ← IDA Pro Mobile root build (untouched)
├── settings.gradle.kts         ← IDA Pro Mobile settings (untouched)
├── .github/, .replit, ...      ← IDA Pro Mobile docs & metadata (untouched)
└── KTIMAZ-REV/                 ← KTIMAZ-REV project, 100% verbatim copy
    ├── app/                    ← KTIMAZ-REV app module (untouched, incl.
    │                             Ktimazstudio.keystore and dict.txt)
    ├── build.gradle.kts        ← untouched
    ├── settings.gradle.kts     ← untouched
    ├── gradle/wrapper/         ← its own Gradle 8.14.3 wrapper (untouched)
    └── README.md, LICENSE, .github/ ... ← all preserved
```

## Why the projects are nested instead of sharing one Gradle build

Both apps are complete, independent Android applications with different
application IDs and — critically — **incompatible toolchains**:

| | IDA Pro Mobile (main) | KTIMAZ-REV |
|---|---|---|
| Android Gradle Plugin | 8.2.0 | 8.2.2 |
| Kotlin | **1.9.0** (legacy `composeOptions` 1.5.4) | **2.0.21** (new `kotlin.plugin.compose`) |
| Version catalog | none (direct deps) | `gradle/libs.versions.toml` |
| Gradle wrapper | 8.4 | 8.14.3 |

A single Gradle build requires one shared plugin classpath, so combining the
modules would force one Kotlin version onto both — which would require
editing the build files of at least one project (Kotlin 1.9 cannot apply
`org.jetbrains.kotlin.plugin.compose`; Kotlin 2.0 rejects
`composeOptions.kotlinCompilerExtensionVersion`). That would violate the
"change nothing" rule, so it was **not** done.

Binding KTIMAZ-REV with `includeBuild("KTIMAZ-REV")` (composite build) was
also **not** applied, because KTIMAZ-REV currently fails Gradle
configuration on its own (see known issues below) — that failure would
propagate to every Gradle invocation from the root and break the main
project too. Verified error from a live run of the original KTIMAZ-REV:

```
* What went wrong:
Build was configured to prefer settings repositories over project
repositories but repository 'Google' was added by build file
'build.gradle.kts'
```

## How to build

**IDA Pro Mobile (main project)** — from the repository root:

```bash
./gradlew :app:assembleDebug
```

**KTIMAZ-REV** — from its own folder (it keeps its own wrapper and settings):

```bash
cd KTIMAZ-REV
./gradlew :app:assembleDebug
```

Both projects can still be opened independently in Android Studio
(`File → Open`). The root opens the main project; `KTIMAZ-REV/` can be
opened as a second window/project.

## Known pre-existing issues (present in the originals, intentionally left untouched)

1. **KTIMAZ-REV**: `settings.gradle.kts` sets
   `RepositoriesMode.FAIL_ON_PROJECT_REPOS`, while the root
   `build.gradle.kts` (line 19) declares `allprojects { repositories { … } }`.
   Gradle refuses this combination. If you want to fix it later, either
   remove that `allprojects` block or change the mode to `PREFER_SETTINGS` —
   then you may also enable `includeBuild("KTIMAZ-REV")` in the root
   `settings.gradle.kts` to build both apps from one command.
2. **IDA Pro Mobile**: the source archive contains no
   `app/src/main/res/` folder, although `AndroidManifest.xml` references
   resources (`@mipmap/ic_launcher`, `@string/app_name`,
   `@style/Theme.IdaProMobile`, `@xml/data_extraction_rules`,
   `@xml/backup_rules`). Resource linking will fail until those resources
   exist. Preserved exactly as uploaded.
3. **KTIMAZ-REV**: the release signing config looks for the keystore via
   `rootProject.file("Ktimazstudio.keystore")`, but the keystore file lives
   inside `app/`. This is identical to the original project's behavior and
   was not altered.

## Changes made for the merge (complete list)

1. Added the folder `KTIMAZ-REV/` (verbatim copy of `KTIMAZ-REV-master.zip`).
2. Added this `MERGE_INFO.md`.
3. Set the executable bit on both `gradlew` scripts (standard Gradle wrapper
   permission; no content change).

Nothing else was added, changed, or removed.
