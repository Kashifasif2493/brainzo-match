# PRIVACY POLICY FOR BRAINZO MATCH

**Last Updated:** February 24, 2025  
**Effective Date:** February 24, 2025  
**Application:** Brainzo Match ("the Application", "the App")  
**Target Audience:** Children (Ages 5–12) and Families  
**Developer / Studio Name:** `[DEVELOPER_STUDIO_NAME]`  
**Contact Email:** `[DEVELOPER_CONTACT_EMAIL]`  
**Hosted Privacy Policy URL:** `[PRIVACY_POLICY_HTTPS_URL]`

---

## 1. INTRODUCTION & OUR COMMITMENT TO CHILD SAFETY

Welcome to Brainzo Match! We create educational, playful brain-training puzzles and memory games designed for children ages 5 to 12. Because our primary audience includes young children, protecting children's privacy is our highest priority. 

This Privacy Policy explains what information is stored on your device, how third-party services operate within the Application, and your rights as a parent or legal guardian.

This Application complies with:
- The **Children's Online Privacy Protection Act (COPPA)** (United States)
- The **Google Play Families Policy**
- The **General Data Protection Regulation (GDPR / GDPR-K)** (European Union & United Kingdom)
- Applicable global children’s data protection legislation

We do **not** condition a child’s participation in any activity or game on the disclosure of more personal information than is reasonably necessary.

---

## 2. INFORMATION STORED LOCALLY ON YOUR DEVICE

Brainzo Match is designed to work offline. All primary game progression and personalization data is stored strictly on your local device storage using a local Room/SQLite database (`brainzo_match_database.db`). 

This local data includes:
- **Game Progress:** Highest level completed (Levels 1–100), stars earned per level (1–3 stars), high scores, and game accuracy percentages.
- **Player Customization:** In-game nickname/gamer tag (e.g., "Leo the Thinker"), selected avatar, aura mood color, and equipped wardrobe items.
- **In-Game Currency:** Virtual coins and stars collected through puzzle completion (virtual currency has no real-world monetary value).
- **Parental Controls Settings:** Screen time daily minutes limit, bedtime lockout schedule, and a 4-digit PIN stored as a cryptographic hash (`SHA-256`).
- **Sensory Settings:** Audio volume sliders for background music, sound effects, and tactile haptic preferences.

**No Account Registration:** Brainzo Match does not require children to create an account, log in with social media, or provide names, physical addresses, telephone numbers, or email addresses to play. Local game data remains on the device and is removed when the Application is uninstalled.

---

## 3. ADVERTISING PRACTICES & GOOGLE ADMOB (CHILD-SAFE)

Brainzo Match features optional advertisements delivered via **Google AdMob**, an approved ad network under the Google Play Families program.

We enforce strict child-directed advertising configurations in code:
1. **Child-Directed Flagging:** All ad requests are globally tagged with `TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE` and `TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE`.
2. **Maximum G Rating:** All ad creative content is capped at `MAX_AD_CONTENT_RATING_G` (appropriate for all audiences, strictly free of adult content, violence, alcohol, or sensitive themes).
3. **No Behavioral Remarketing or Tracking:** Personalized advertising is disabled (`npa=1`). The Google Advertising ID (`AAID` / `AD_ID`) is **not** collected or requested by the Application (`com.google.android.gms.permission.AD_ID` is omitted from our manifest).
4. **Child-Safe Placement:** Ads are never displayed during live puzzle play. Interstitial ads are limited by an automatic 3-minute cooldown between completed levels to protect the child's focus.
5. **Ad-Free VIP Option:** Parents may permanently disable all advertisements by subscribing to Brainzo VIP Gold.

*Note on Third-Party SDK Data Collection:* Google AdMob may automatically collect ephemeral, non-identifying technical data (such as coarse device type, operating system version, and general IP address for diagnostic and geo-routing purposes) strictly to fulfill basic ad delivery, prevent fraud, and calculate publisher earnings in compliance with COPPA safe harbor guidelines. Please review [Google's Privacy Policy](https://policies.google.com/privacy) for details.

---

## 4. IN-APP PURCHASES & GOOGLE PLAY BILLING

Brainzo Match offers an optional **Brainzo VIP Gold** subscription providing unlimited levels, mythic wardrobe items, and an ad-free experience.

1. **Parental Verification Gate:** All purchase attempts, billing pages, and external web navigation are protected by an adult verification challenge (e.g., solving a two-digit multiplication problem or entering the parental PIN). A child cannot initiate a transaction on their own.
2. **Payment Processing:** All transactions are handled directly and securely by the **Google Play Billing API**. We do **not** collect, process, or store credit card numbers, billing addresses, or banking details. Google provides our Application only with an anonymous digital purchase token verifying active subscription status.

---

## 5. DEVICE INFORMATION & DIAGNOSTICS

To ensure high performance and crash prevention across thousands of Android device models, the Application may utilize Google Play Console automated diagnostic services (Pre-launch reports and Android Vitals):
- **Crash Logs:** Aggregated stack traces in the event of an unexpected app crash.
- **Performance Metrics:** Frame render rates and battery consumption telemetry.

These diagnostics are collected in the aggregate without associating data with individual children or devices.

---

## 6. DATA SHARING & THIRD-PARTY DISCLOSURE

We do not sell, rent, trade, or monetize personal information from children or adults. 

Information is only shared under the following limited circumstances:
- **Service Providers:** Trusted platform SDKs (Google Play Services, Google AdMob, and Google Play In-App Billing) solely to provide platform functionality, child-safe advertising, and subscription management as described in this policy.
- **Legal Compliance:** If required by law, subpoena, or legal process to protect our rights, prevent fraud, or ensure the safety of our users.

---

## 7. DATA RETENTION & SECURITY

- **Local Data:** All gameplay records, avatar customizations, and parental controls remain on your device until you clear application storage or uninstall the App.
- **Security in Transit:** All external communication (ad delivery, billing verification) is encrypted using industry-standard Transport Layer Security (**HTTPS / TLS 1.3**). Cleartext HTTP traffic is disabled system-wide via our Android network security configuration.

---

## 8. PARENTAL RIGHTS & CONTROLS

Under COPPA, GDPR-K, and applicable privacy regulations, parents and legal guardians have the right to:
1. Review any information collected about their child (if any).
2. Direct the developer to delete any personal information collected from their child.
3. Refuse to permit further collection or use of their child's information.
4. Manage in-app screen time, bedtime lockout hours, and sound preferences directly within the in-app **Parent Analytics & Controls** dashboard.

Because we do not maintain user accounts or store child data on remote cloud servers, you can instantly erase all stored gameplay records, coins, and customizations at any time by navigating to:
**Android Settings > Apps > Brainzo Match > Storage > Clear Storage / Clear Data**.

To exercise parental rights, ask questions about our advertising partners, or submit inquiries, please contact our Data Protection Officer at:  
📧 **Email:** `[DEVELOPER_CONTACT_EMAIL]`  
🏢 **Developer:** `[DEVELOPER_STUDIO_NAME]`

---

## 9. CHANGES TO THIS PRIVACY POLICY

We may update this Privacy Policy periodically to reflect new game features, updated SDK implementations, or evolving legal requirements. When changes are made, we will update the "Last Updated" date at the top of this document. We encourage parents to review this policy periodically.

---

## 10. GOOGLE PLAY CONSOLE PRIVACY VERIFICATION CHECKLIST

For the developer submitting Brainzo Match to Google Play Console:
- **Target Age Selection:** Select **Ages 5 and under**, **Ages 6–8**, and **Ages 9–12**.
- **Families Policy Confirmation:** Confirm enrollment in the Google Play Families program.
- **Privacy Policy URL:** Host this document on an HTTPS page (e.g., `https://[YOUR_DOMAIN]/privacy-policy` or GitHub Pages) and enter the URL in **App Content > Privacy Policy**.
- **Data Safety Form Declaration:**
  - *Data collected:* None collected by the first-party developer.
  - *Third-party ads/analytics:* Declare AdMob non-personalized child-directed usage.
  - *Data encryption in transit:* Yes.
  - *Request for deletion mechanism:* Yes (via device data clearance).
