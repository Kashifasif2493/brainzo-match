# Brainzo Match — Android project (revision 2)

This folder was assembled from the uploaded Stitch ZIP. Source text files were placed into conventional Android project paths. This is an assembly pass, NOT a successful Gradle build.

## Important limitations
- The Stitch export did not contain `gradle/wrapper/gradle-wrapper.jar`. The GitHub workflow therefore installs Gradle 8.11.1 and runs `gradle` directly; `gradlew`/`gradlew.bat` are included but will not work locally until the official wrapper JAR is added.
- Ten WAV files have been generated as simple TEST TONES so the resource names exist and the project can reach compilation checks. They are not final sound effects or background music. Replace them with properly licensed, child-friendly audio before release.
- No Android build was run in this environment. Kotlin/Compose source may still contain compile errors or unresolved cross-file references. GitHub Actions is the next real build check.
- The module Gradle file uses the source ZIP's dependency versions and has a guarded signing setup. Review it against the actual repository secrets before a release build.
- Some Stitch files were duplicated/older. The selected versions are noted in `docs/ASSEMBLY_NOTES.md`.

## Build on GitHub
Upload the contents of this folder to the root of an empty GitHub repository. Push/commit to `main`; the Debug APK workflow will run. Release AAB is a separate manual workflow job and requires all four signing secrets.
