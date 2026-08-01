# Pulse Application R8 / ProGuard Rules

# ----------------------------------------------------------------------
# 1. Jetpack Compose
# ----------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**

-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ----------------------------------------------------------------------
# 2. Hilt / Dependency Injection
# ----------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class **_HiltModules_* { *; }
-keep class **_HiltComponents_* { *; }
-keep class **Hilt_* { *; }
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *

# ----------------------------------------------------------------------
# 3. Room Database
# ----------------------------------------------------------------------
-keep class androidx.room.** { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# ----------------------------------------------------------------------
# 4. Media3 / ExoPlayer
# ----------------------------------------------------------------------
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ----------------------------------------------------------------------
# 5. Retrofit + OkHttp + Moshi
# ----------------------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*

-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
# Moshi codegen (KSP) discovers generated adapters by class name at runtime.
-keep class **JsonAdapter { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep interface * {
    @retrofit2.http.* <methods>;
}

# ----------------------------------------------------------------------
# 6. Kotlin Serialization & Coroutines
# ----------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ----------------------------------------------------------------------
# 7. Wear OS Components
# ----------------------------------------------------------------------
-keep class androidx.wear.** { *; }
-dontwarn androidx.wear.**

# ----------------------------------------------------------------------
# 8. Application Components
# ----------------------------------------------------------------------
-keep public class com.example.PulseApplication
-keep public class com.example.MainActivity

-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Activity

# ----------------------------------------------------------------------
# 9. Firebase / Google (declared but unused at runtime; strip safely)
# ----------------------------------------------------------------------
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.mlkit.**

# ----------------------------------------------------------------------
# 10. Generic R8 / Proguard optimizations
# ----------------------------------------------------------------------
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-allowaccessmodification
