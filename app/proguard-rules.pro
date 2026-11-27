# ==============================================================================
# Brainzo Match — Production ProGuard / R8 Optimization & Obfuscation Rules
# File: app/proguard-rules.pro
# Target AGP: 8.3.2 | Gradle: 8.4 | JDK: 17
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Jetpack Compose & General Kotlin Rules
# ------------------------------------------------------------------------------
# Preserve generic type signatures, inner classes, and annotations required by reflection and runtime tooling
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Composable method signatures intact
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# ------------------------------------------------------------------------------
# 2. Room Database v2 (Version 2.6.1 with KSP)
# ------------------------------------------------------------------------------
# Keep the generated RoomDatabase implementation classes
-keep class * extends androidx.room.RoomDatabase

# Keep all Room @Entity annotated model classes and their fields/constructors
-keep @androidx.room.Entity class * { *; }

# Keep all Room @Dao interfaces and implementations
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep specific Room TypeConverter implementation
-keep class com.brainzomatch.app.core.data.local.BrainzoTypeConverters {
    public *;
}

# Suppress optional/missing Room Paging warnings if paging is not on classpath
-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# 3. Google Gson (Version 2.10.1)
# ------------------------------------------------------------------------------
# Preserve Gson classes, annotations, and generic signatures
-keep class com.google.gson.** { *; }

# Keep entity fields that are serialized to/from JSON in database converters
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}

# Keep classes referenced in TypeToken queries inside BrainzoTypeConverters
-keep class com.brainzomatch.app.core.data.local.entities.** { *; }

# ------------------------------------------------------------------------------
# 4. Google Mobile Ads SDK (Version 23.0.0 — COPPA & Families Compliant)
# ------------------------------------------------------------------------------
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public class com.google.ads.** {
    public *;
}
-dontwarn com.google.android.gms.ads.**

# Keep JavaScript interface methods if custom web view ads are integrated
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ------------------------------------------------------------------------------
# 5. Google Play Billing Library v6 (Version 6.2.1)
# ------------------------------------------------------------------------------
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.**

# ------------------------------------------------------------------------------
# 6. Kotlin Coroutines
# ------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
