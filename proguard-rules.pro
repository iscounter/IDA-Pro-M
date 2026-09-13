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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep native methods and JNI classes
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep JNI data classes
-keep class com.idapro.mobile.native.** { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep data model classes
-keep class com.idapro.mobile.data.model.** { *; }
-keep class com.idapro.mobile.data.database.entities.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.flow.**

# Keep Android architecture components
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.lifecycle.AndroidViewModel

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Optimize and obfuscate
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-repackageclasses ''

# Keep application class
-keep public class com.idapro.mobile.IdaProMobileApplication

# Keep main activity
-keep public class com.idapro.mobile.MainActivity

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep generic signatures
-keepattributes Signature

# Keep annotations
-keepattributes *Annotation*

# Keep crash reporting
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile

# Security - Remove debug information in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# IDA Pro Mobile specific rules
# Keep binary analysis result classes
-keep class com.idapro.mobile.data.model.DisassemblyInstruction { *; }
-keep class com.idapro.mobile.data.model.Function { *; }
-keep class com.idapro.mobile.data.model.BinaryFile { *; }
-keep class com.idapro.mobile.data.model.Annotation { *; }

# Keep utility classes that might be called via reflection
-keep class com.idapro.mobile.utils.BinaryUtils { *; }
-keep class com.idapro.mobile.utils.FileUtils { *; }

# Keep ViewModels
-keep class com.idapro.mobile.viewmodel.** { *; }

# Preserve parameter names for better debugging
-keepparameternames

# Keep line numbers for crash reports
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Aggressive optimization for release builds
-optimizationpasses 5
-overloadaggressively
-repackageclasses ''
-allowaccessmodification

# Remove unnecessary code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkParameterIsNotNull(...);
    public static void checkNotNullParameter(...);
    public static void checkExpressionValueIsNotNull(...);
    public static void checkNotNullExpressionValue(...);
    public static void checkReturnedValueIsNotNull(...);
    public static void checkFieldIsNotNull(...);
}
