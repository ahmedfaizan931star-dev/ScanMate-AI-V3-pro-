# ScanMate AI Pro safe release rules.
# Minification is currently disabled to avoid breaking CameraX/ML Kit/Room/Compose while production QA is ongoing.
# These keep rules are safe if you enable R8 later.

-keep class com.synthbyte.scanmate.data.** { *; }
-keep class com.synthbyte.scanmate.domain.** { *; }
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }
-keep class androidx.camera.** { *; }
-keep class androidx.room.** { *; }
-keep class com.squareup.moshi.** { *; }
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes InnerClasses,EnclosingMethod
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.**
-dontwarn okio.**
