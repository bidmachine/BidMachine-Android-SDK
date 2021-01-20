-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.adcolony.** { *; }
-dontwarn com.adcolony.**
-dontwarn android.app.Activity
