# ProGuard rules for Azkari (Athkar) Android App
# Add project specific ProGuard rules here.

-keep class com.example.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
