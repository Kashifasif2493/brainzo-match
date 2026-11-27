# Brainzo Match — Final Google Play Store Release Checklist
**App Name:** Brainzo Match: Kids Brain Games  
**Package:** `com.brainzomatch.app`  
**Target Category:** Educational / Puzzle  
**Target Audience:** Ages 5–8, Ages 9–12 (Designed for Families)

---

## 1. Project Compilation & Testing (Mandatory)
- [ ] **Android Studio Clean Build:** `./gradlew clean build` completes with `BUILD SUCCESSFUL` and 0 errors.
- [ ] **Lint Verification:** Run `./gradlew lintRelease` to check for missing XML resources, broken translations, or hardcoded strings.
- [ ] **Unit & Instrumentation Tests:** Run `./gradlew test` to ensure all 8 game engines compute scores, difficulty grids, and word spellings accurately.
- [ ] **No Hardcoded API Keys or Test Credentials:** Confirm production AdMob IDs and real Google Play Billing product IDs are configured.

---

## 2. Release Keystore & App Signing (Mandatory)
- [ ] **Production Keystore Generation:**
  ```bash
  keytool -genkey -v -keystore brainzo-release.jks \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -alias brainzo_key
  ```
- [ ] **Secure Storage:** Store `brainzo-release.jks` and passwords in a secure password manager. (Losing this key prevents future app updates).
- [ ] **Generate Signed Android App Bundle (AAB):**
  ```bash
  ./gradlew bundleRelease
  # Output file: app/build/outputs/bundle/release/app-release.aab
  ```
- [ ] **Verify Bundle Size:** Confirm base download size is under 30 MB for instant installation.

---

## 3. Visual Assets & Store Metadata (Mandatory)
- [ ] **App Icon:** 512 × 512 px, 32-bit PNG with alpha channel, no transparent border.
- [ ] **Feature Graphic:** 1024 × 500 px, JPEG or 24-bit PNG, centered title and mascot.
- [ ] **Phone Screenshots:** At least 4 distinct screenshots (1080 × 1920 or 1080 × 2400 px, 16:9 or 20:9) showcasing Home Dashboard, Memory Match, Math Blast, and Dressing Room.
- [ ] **7-Inch Tablet Screenshots:** At least 1 tablet screenshot (required for Families program eligibility).
- [ ] **10-Inch Tablet Screenshots:** At least 1 widescreen screenshot (required for Families program eligibility).
- [ ] **Store Title:** `Brainzo Match: Kids Brain Games` (30/30 characters).
- [ ] **Short Description:** `Fun memory puzzles, math blasts & brain teasers designed for kids ages 5–12!` (77/80 characters).
- [ ] **Full Description:** Structured with key features, educational benefits, offline capabilities, and COPPA notice.

---

## 4. Policy, Privacy & Families Compliance (Mandatory)
- [ ] **Public HTTPS Privacy Policy URL:** Host `Brainzo Match Child-Safe Privacy Policy` on GitHub Pages or custom domain (e.g., `https://brainzomatch.com/privacy`).
- [ ] **Data Safety Form Declarations:**
  - *Data Collected by Developer:* Declare **None**.
  - *Data Collected by Third-Party SDKs (AdMob):* Declare ephemeral diagnostic device telemetry.
  - *Data Encrypted in Transit:* Yes (HTTPS).
  - *Account Creation:* Not required.
- [ ] **Target Audience & Content:**
  - Target Ages: Check **Ages 5 and under**, **Ages 6–8**, and **Ages 9–12**.
  - Neutral Age Screen: Confirm adult math gate protects external links and purchases.
- [ ] **Child-Directed AdMob Settings:**
  - Global flags in `BrainzoApplication.kt`: `TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE`, `TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE`, and `MAX_AD_CONTENT_RATING_G`.
  - Non-Personalized Ads flag enabled (`npa=1`).
  - No `AD_ID` permission declared in `AndroidManifest.xml`.
- [ ] **Parental Gate Verification:** Test adult challenge before billing flow (`BillingManager.kt`).
- [ ] **Content Rating Questionnaire:** Completed via IARC (Target rating: ESRB Everyone, PEGI 3).

---

## 5. Testing Tracks & Launch Workflow (Mandatory)
- [ ] **Internal Testing:** Upload `app-release.aab` to verify installation on physical Android 10, 12, 13, and 14 devices.
- [ ] **Pre-Launch Report Review:** Inspect Firebase Test Lab automated crawl results in Play Console for crashes, ANRs, or layout clipping.
- [ ] **Closed Testing Track:** Invite at least 20 opted-in testers for 14 continuous days (mandatory requirement for personal developer accounts).
- [ ] **Production Rollout Strategy:**
  - Day 1: 10% Staged Rollout.
  - Day 3: 25% Staged Rollout.
  - Day 5: 50% Staged Rollout.
  - Day 7: 100% Full Production Release.
- [ ] **Post-Launch Android Vitals Monitoring:** Monitor crash rate (< 1.09%) and ANR rate (< 0.47%) in Google Play Console.
