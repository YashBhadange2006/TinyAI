# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Prevent R8 from breaking Google LiteRT-LM JNI boundaries
-keep class com.google.ai.edge.litertlm.** { *; }

# Prevent R8 from breaking MediaPipe GenAI & Tasks Infrastructure
-keep class com.google.mediapipe.** { *; }
-keep class org.tensorflow.lite.** { *; }

# Protect all native JNI communication methods across your code
-keepclasseswithmembernames class * {
    native <methods>;
}

# Room Database Optimization Rules (Since you are using Room)
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Serialization Protection (Since you use kotlinx.serialization)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.google.auto.value.extension.memoized.Memoized
-dontwarn com.google.mediapipe.proto.CalculatorProfileProto$CalculatorProfile
-dontwarn com.google.mediapipe.proto.GraphTemplateProto$CalculatorGraphTemplate