# Project-specific R8 / ProGuard rules.

# WorkManager creates its Room database implementation by generated-name lookup.
# Keep the abstract database and generated implementation stable in minified candidate/release builds.
-keep class androidx.work.impl.WorkDatabase { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.room.RoomDatabase { *; }
