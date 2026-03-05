# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.calendar.data.entities.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep native method
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom view
-keepclassmembers class * extends android.view.View {
   void set*(***);
   *** get*();
}
