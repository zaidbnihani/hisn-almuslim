# تطبيق حصن المسلم - أذكاري (Azkari)

تطبيق أندرويد إسلامي شامل مبني بلغة Kotlin و Jetpack Compose يحتوي على الأذكار، السبحة الإلكترونية، ومواقيت الصلاة الدقيقة مع الأذان.

## إعدادات البناء والتوقيع (Signing Config)

يستخدم مشروع أندرويد إعداد التوقيع الخاص بالتطوير (`debug`):

```kotlin
signingConfig = signingConfigs.getByName("debug")
```

تأكد من عدم تغيير اسم الإعداد إلى "debugConfig" لضمان توافق البناء مع إعدادات Gradle في `app/build.gradle.kts`.
