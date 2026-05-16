# Default Android optimize rules are inherited from proguard-android-optimize.txt.
# Module-specific rules go here. Keep this file small and explicit.

# --- Kotlinx serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.snapdoc.app.**$$serializer { *; }
-keepclassmembers class com.snapdoc.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.snapdoc.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.* { *; }

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# --- Firebase / Crashlytics ---
-keepattributes SourceFile,LineNumberTable
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- AdMob ---
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# --- Play Billing ---
-keep class com.android.billingclient.** { *; }

# --- ML Kit ---
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }
-dontwarn com.google.mlkit.**

# --- Timber ---
-dontwarn org.jetbrains.annotations.**

# --- Coroutines ---
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
