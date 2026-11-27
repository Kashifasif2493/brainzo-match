#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
errors=0
check_file() {
  if [[ ! -s "$1" ]]; then echo "MISSING/EMPTY: $1"; errors=$((errors+1)); else echo "OK: $1"; fi
}
for f in app/src/main/AndroidManifest.xml app/build.gradle.kts settings.gradle.kts build.gradle.kts gradle/libs.versions.toml gradle/wrapper/gradle-wrapper.properties app/proguard-rules.pro app/src/main/res/values/strings.xml app/src/main/res/values/colors.xml app/src/main/res/values/themes.xml app/src/main/res/xml/backup_rules.xml app/src/main/res/xml/backup_rules_legacy.xml app/src/main/res/xml/network_security_config.xml app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml app/src/main/res/drawable/ic_launcher_background.xml app/src/main/res/drawable/ic_launcher_foreground.xml app/src/main/java/com/brainzomatch/app/MainActivity.kt app/src/main/java/com/brainzomatch/app/BrainzoApplication.kt app/src/main/java/com/brainzomatch/app/ui/navigation/BrainzoNavHost.kt; do check_file "$f"; done
for f in button_click.wav card_flip.wav correct_answer.wav wrong_answer.wav victory.wav coin_collect.wav timer_tick.wav puzzle_snap.wav reward_unlock.wav; do check_file "app/src/main/res/raw/$f"; done
bg=0; [[ -s app/src/main/res/raw/brainzo_background.wav ]] && bg=$((bg+1)); [[ -s app/src/main/res/raw/brainzo_background.ogg ]] && bg=$((bg+1))
if [[ "$bg" -ne 1 ]]; then echo "ERROR: exactly one non-empty background file (.wav OR .ogg) is required"; errors=$((errors+1)); fi
if [[ "$errors" -gt 0 ]]; then echo "Asset verification failed: $errors issue(s)."; exit 1; fi
echo "Asset verification passed. Note: audio files currently generated in this package are TEST TONES, not production audio."
