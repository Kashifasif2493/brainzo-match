# Assembly notes

Selected files from Stitch ZIP:
- `app/build.gradle.kts`: `app_build.gradle.kts_2.txt` (version-catalog plugin aliases and KSP at module root); signing block adjusted to avoid debug builds requiring secrets.
- `BrainzoNavHost.kt`: `brainzonavhost.kt_complete_8_game_wiring.txt` (full 8-game wiring variant).
- `MainActivity.kt`: `mainactivity.kt_2.txt`; `BrainzoApplication.kt`: `brainzoapplication.kt_2.txt`.
- Workflow was rewritten to remove the original zero-byte placeholder creation and to use installed Gradle 8.4 because the wrapper JAR was absent.
- Asset script checks actual non-empty resource files and deliberately does not require a wrapper JAR for the cloud workflow.
- Audio files are synthetic test tones generated during assembly; not production audio.

No compilation was performed. The exported code should be treated as a candidate project pending CI build and fixes.

## Revision 2 (completion pass)
- Added the missing `ui/screens/home/HomeScreen.kt` (home dashboard + 8-game hub, live Room progress).
- Added missing `ic_launcher_round` (adaptive XML + PNG fallbacks for Android 7.x built from `design/` icon).
- Added `gradle.properties` (`android.useAndroidX=true` was missing and would fail the build).
- Upgraded to compileSdk/targetSdk 36 (Google Play requirement since 31 Aug 2026), AGP 8.9.1, Gradle 8.11.1.
- Fixed: player, parental-controls and per-game progress rows were never created, so coins/stars/progress updates saved nothing. Added `BrainzoRepository.ensureDefaults()`, called at app start.
- Fixed: default parent PIN hash did not match "1234". Removed fake demo values (1450 coins, 42 minutes played, etc.).
- Still to do before release: replace test-tone audio, real AdMob IDs, Play Console subscription `vip_monthly`, privacy policy URL, replace placeholder game visuals (Find the Difference, Jigsaw, Hidden Objects), build and fix compile errors via GitHub Actions.
