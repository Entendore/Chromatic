# Crash traces: file + line survive obfuscation (v14).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-repackageclasses ''
-allowaccessmodification

# Consumer-rules audit (verified — do NOT add keeps speculatively, v14 policy):
#   Hilt, Room, Compose, kotlinx-serialization, kotlinx-datetime, DataStore,
#   Glance, WorkManager, Timber — all ship their own.
# Evidence-first keeps only: if the release smoke (§7 connected test) ever fails on
# SavedPalette deserialization, add the keep BELOW with the failing stack trace:
# -keep class com.example.chromatic.core.data.model.SavedPalette { *; }
