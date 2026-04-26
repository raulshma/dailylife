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

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
