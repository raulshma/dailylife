-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepattributes Signature
-keepattributes Exception
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

-dontwarn dagger.hilt.**

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

-dontwarn okhttp3.**
-dontwarn okio.**

-keep class org.osmdroid.** { *; }
-keep class org.osmdroid.views.** { *; }
-keep class org.osmdroid.tileprovider.** { *; }
-dontwarn org.osmdroid.**

-keep class com.google.ai.edge.litertlm.** { *; }
-dontwarn com.google.ai.edge.litertlm.**

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.raulshma.dailylife.domain.** { *; }
-keep class com.raulshma.dailylife.data.backup.** { *; }

-keep class com.squareup.moshi.** { *; }
-keep class com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory { *; }
-dontwarn com.squareup.moshi.**

-keep class androidx.savedstate.** { *; }
-dontwarn androidx.savedstate.**

-keep class androidx.lifecycle.SavedStateHandle** { *; }
-keep class androidx.lifecycle.viewmodel.** { *; }
-dontwarn androidx.lifecycle.viewmodel.**

-keep class dagger.hilt.android.internal.lifecycle.** { *; }
-dontwarn dagger.hilt.android.internal.lifecycle.**

-keep class androidx.compose.ui.savedstate.** { *; }
-dontwarn androidx.compose.ui.savedstate.**
