# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.godaplayer.app.data.local.database.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Media3
-keep class androidx.media3.** { *; }
