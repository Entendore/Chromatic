# Minimal — grown only on smoke failures (v15 §4 policy):
-keep class com.example.chromatic.HiltTestRunner { *; }
-keep @org.junit.runner.RunWith class * { *; }
-keep class * extends androidx.test.runner.AndroidJUnitRunner { *; }
-keep class dagger.hilt.internal.** { *; }
-dontwarn org.junit.**
