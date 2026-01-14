-dontwarn **

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep interface * extends androidx.room.RoomDatabase$Callback
-keep class * extends androidx.room.migration.Migration

# ── Koin ──────────────────────────────────────────────────────────────────────
-keepnames class * extends org.koin.core.module.Module
-keep class com.miletracker.** {
    @org.koin.core.annotation.* <fields>;
}

# ── Kotlin serialization ──────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ── OSMdroid ─────────────────────────────────────────────────────────────────
-keep class org.osmdroid.** { *; }
-keep class org.osmdroid.tileprovider.** { *; }

# ── Coil ─────────────────────────────────────────────────────────────────────
-keep class coil.** { *; }
-keep interface coil.** { *; }

# ── DataStore / Protobuf ─────────────────────────────────────────────────────
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── SQLCipher ─────────────────────────────────────────────────────────────────
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# ── Application classes ───────────────────────────────────────────────────────
-keep class com.miletracker.** extends android.app.Application
-keep class com.miletracker.**.dao.** { *; }
-keep class com.miletracker.**.model.** { *; }
-keep class com.miletracker.**.di.** { *; }
