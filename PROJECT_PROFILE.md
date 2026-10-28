# ملف تعريف المشروع | Project Profile

## العربية

### الغرض والنطاق
حصن المسلم تطبيق Android أصلي مكتوب بـ Kotlin، يجمع شاشات الأذكار والتسبيح ومواقيت الصلاة والأدعية والقرآن/الراديو ومحوّل التاريخ والإعدادات. يستند هذا الوصف إلى الشاشات والبيانات والأنشطة والخدمات الموجودة في `app/src/main/java/com/example/`، وإلى إعداد التطبيق في `app/build.gradle.kts`؛ كما تصف `PRD.MD` المتطلبات والميزات المقصودة، لكن عند اختلاف وصفها عن التنفيذ تُقدَّم ملفات المصدر والإعداد الحالية بوصفها دليل الحالة المنفذة. ملف `README.md` الحالي لا يحتوي سوى عنوان وهوية بصرية، ولا يشرح الإعداد أو التطوير.

### المكوّنات والتدفقات الأساسية
- نقطة الدخول والتنقل وعرض الواجهات في `MainActivity.kt`؛ حالة التطبيق ومنطق الإجراءات في `ui/MainViewModel.kt`، المنشور عبر `StateFlow`.
- بيانات الأذكار والنماذج والحسابات في `data/AthkarData.kt` و`data/DhikrModels.kt` و`data/PrayerCalculator.kt`.
- شاشات Compose في `ui/screens/`: `HomeScreen.kt` للأذكار، `TasbihScreen.kt` للتسبيح، `PrayerTimesScreen.kt` للمواقيت، `FortressScreen.kt` للأدعية، `QuranRadioScreen.kt` للراديو، `DateConverterScreen.kt` للتاريخ، `SettingsScreen.kt` للإعدادات، و`PrivacyConsentScreen.kt` للموافقة على سياسة الخصوصية.
- الخدمات والمستقبلات في `service/` و`receiver/` تتعامل مع جدولة/تشغيل الأذان والراديو واستعادة العمل بعد الإقلاع. `update/` يحوي فحص الإصدارات وتنزيل/تثبيت APK، و`data/FirebaseBroadcastManager.kt` يتعامل مع رسائل Firebase Realtime Database.
- التدفق العام الذي تدعمه الشاشات: فتح التطبيق ثم التنقل بين ميزاته، وتحديث الحالة المحفوظة، مع واجهات منفصلة للتفضيلات والخصوصية والتحديثات. تفاصيل السلوك الدقيقة موثقة جزئيًا في `PRD.MD`، ويلزم الرجوع إلى الكود عند تغييره.

### شجرة مختصرة
```text
.
├── README.md                         # عنوان/واجهة مختصرة؛ لا يتضمن تعليمات تطوير
├── PRD.MD                            # وثيقة متطلبات ونطاق موصوف للمشروع
├── build.gradle.kts                  # إضافات Gradle على مستوى المشروع
├── settings.gradle.kts               # المستودعات ووحدة :app واسم المشروع
├── gradle/libs.versions.toml         # كتالوج نسخ الإضافات والتبعيات
└── app/
    ├── build.gradle.kts              # إعداد Android والتبعيات والنسخ المستهدفة
    ├── proguard-rules.pro            # قواعد ProGuard المشار إليها في إعداد release
    └── src/main/
        ├── AndroidManifest.xml       # النشاط والصلاحيات والخدمات والمستقبلات
        ├── assets/adhan/             # ملفات صوت أذان محلية بصيغة MP3
        ├── java/com/example/         # النشاط والبيانات وواجهة Compose والمنطق
        └── res/                      # موارد Android والأيقونات والأنماط
```

### ملفات محورية
- `app/src/main/java/com/example/MainActivity.kt`: مدخل النشاط وتركيب واجهات التطبيق.
- `app/src/main/java/com/example/ui/MainViewModel.kt`: حالة UI ومنطق الأذكار والمواقيت والتفضيلات والخصوصية والتحديثات.
- `app/src/main/java/com/example/data/AthkarData.kt` و`PrayerCalculator.kt`: بيانات المحتوى وحسابات الصلاة/التاريخ.
- `app/src/main/AndroidManifest.xml`: تعريف الصلاحيات والنشاط والخدمات والمستقبلات.
- `app/build.gradle.kts` و`gradle/libs.versions.toml`: إعداد SDK والنسخة والتبعيات وإصدارات أدوات البناء.
- `PRD.MD`: مرجع نطاق وقرارات المنتج؛ لا يُعد بديلاً عن التحقق من المصدر الحالي.

### التقنية والإصدارات
الإعداد المعلن في `app/build.gradle.kts` يحدد Android Gradle Plugin `8.7.2` وKotlin `2.0.21` (من `gradle/libs.versions.toml`)، وCompose مع BOM `2024.10.01`، وNavigation Compose `2.8.3`، وAndroidX Core KTX `1.15.0`، Lifecycle `2.8.7`، Activity Compose `1.9.3`، وMaterial 3. كما يسجل الكتالوج Adhan `1.2.1` وDataStore Preferences `1.1.1` وPlay Services Location `21.3.0` وRetrofit `2.11.0` وGson `2.11.0`. يضيف ملف التطبيق أيضًا Media3 ExoPlayer/Session `1.4.1` وAndroidX Media `1.7.0` وCoil Compose `2.7.0` وFirebase BOM `33.10.0` مع Analytics وRealtime Database، وإضافات Google Services `4.4.2` في `build.gradle.kts` الجذري. إعداد التطبيق يحدد `namespace` باسم `com.example`، ومعرّف تطبيق `com.aistudio.athkar.azkari`، و`compileSdk` و`targetSdk` بقيمة `36`، و`minSdk` بقيمة `26`، ونسخة التطبيق `2.2.7` ورمز بناء `8`؛ ويضبط مصدر وهدف Java وKotlin على `21` (`app/build.gradle.kts`). هذه معلومات إعداد المشروع، وليست ضمانًا لتوافقها مع بيئة بناء بعينها.

### الإعداد والتشغيل والاختبار والبناء
تُظهر `settings.gradle.kts` وحدة Gradle الوحيدة `:app` ومستودعات Google وMaven Central. لم يُعثر في الملفات المتتبعة على `gradlew`/`gradlew.bat` أو تعليمات تشغيل/بناء في README؛ لذلك لا يحدد المستودع أمرًا موثقًا يمكن نسخه لتشغيل البناء، ولا أفترض أوامر غير موجودة. يدرج `app/build.gradle.kts` اعتماديات JUnit وAndroidX JUnit وEspresso وCompose UI tests، لكن لا توجد ملفات اختبار في `app/src/test` أو `app/src/androidTest`، ولا ملفات CI تحت `.github/workflows/` ضمن الشجرة. من ثم لا توجد نتيجة اختبار أو بناء مؤكدة في هذا الملف. قد يتطلب إعداد Firebase تهيئة خارجية بسبب إضافة Google Services واستخدام `FirebaseBroadcastManager`؛ ملفات المستودع التي فُحصت لا تحسم متطلبات تهيئة بيئة المطور، لذا تحقق منها قبل تنفيذ بناء محلي.

### معلومات عملية ونقاط غير محسومة
- التخزين الموثق في المصدر يستخدم `SharedPreferences` باسم `athkar_user_data` داخل `ui/MainViewModel.kt`، بينما DataStore موجود كتَبعية في الكتالوج؛ تحقق من الاستخدام الفعلي قبل تغيير آلية التخزين.
- تتضمن `AndroidManifest.xml` صلاحيات وخدمات ومستقبلات للأذان والراديو والموقع والإشعارات. وجود تعريف الصلاحية وحده لا يثبت اكتمال سلوك المنتج؛ راجع `service/` و`receiver/` والشاشات ذات الصلة.
- `README.md` لا يقدم خطوات للمساهمين، ولا توجد اختبارات أو أتمتة CI ظاهرة وفق الفحص. لا توجد هنا إجراءات إصدار أو بيانات إعداد خارجية مؤكدة.
- راجع `PRD.MD` لمعرفة القيود المسجلة فيه، وتحقق من مطابقتها للكود قبل الاعتماد عليها؛ فهي وثيقة وصفية وليست نتيجة اختبار مستقلة.

## English

### Purpose and scope
Hisn Al-Muslim is a native Android application written in Kotlin. It brings together screens for daily remembrances, tasbih, prayer times, supplications, Quran/radio, date conversion, and settings. This description is grounded in the activities, screens, data, and services under `app/src/main/java/com/example/` and the app configuration in `app/build.gradle.kts`; `PRD.MD` describes intended requirements and features, but when that description differs from implementation, the current source and build files are the evidence of implemented state. The current `README.md` contains only a title and visual identity, with no setup or development guidance.

### Main components and flows
- The entry activity, navigation, and UI composition are in `MainActivity.kt`; app state and action logic are in `ui/MainViewModel.kt`, exposed through `StateFlow`.
- Remembrance data, models, and calculations are in `data/AthkarData.kt`, `data/DhikrModels.kt`, and `data/PrayerCalculator.kt`.
- Compose screens under `ui/screens/` include `HomeScreen.kt` for remembrances, `TasbihScreen.kt` for tasbih, `PrayerTimesScreen.kt` for prayer times, `FortressScreen.kt` for supplications, `QuranRadioScreen.kt` for radio, `DateConverterScreen.kt` for date conversion, `SettingsScreen.kt` for settings, and `PrivacyConsentScreen.kt` for privacy consent.
- Services and receivers under `service/` and `receiver/` handle adhan scheduling/playback, radio playback, and restoration after boot. `update/` contains version checking and APK download/installation; `data/FirebaseBroadcastManager.kt` handles Firebase Realtime Database messages.
- The general flow supported by the screens is to open the app and navigate among its features while updating persisted state, with separate interfaces for preferences, privacy, and updates. Detailed behavior is only partly documented in `PRD.MD`; consult the code when changing it.

### Compact tree
```text
.
├── README.md                         # Minimal title/visual; no development instructions
├── PRD.MD                            # Product requirements and described scope
├── build.gradle.kts                  # Project-level Gradle plugins
├── settings.gradle.kts               # Repositories, :app module, project name
├── gradle/libs.versions.toml         # Plugin and dependency version catalog
└── app/
    ├── build.gradle.kts              # Android configuration, dependencies, SDKs
    ├── proguard-rules.pro            # ProGuard rules referenced by release config
    └── src/main/
        ├── AndroidManifest.xml       # Activity, permissions, services, receivers
        ├── assets/adhan/             # Local MP3 adhan audio assets
        ├── java/com/example/         # Activity, data, Compose UI, and logic
        └── res/                      # Android resources, icons, and styles
```

### Key files
- `app/src/main/java/com/example/MainActivity.kt`: app entry activity and UI composition.
- `app/src/main/java/com/example/ui/MainViewModel.kt`: UI state and remembrance, prayer-time, preference, privacy, and update logic.
- `app/src/main/java/com/example/data/AthkarData.kt` and `PrayerCalculator.kt`: content data and prayer/date calculations.
- `app/src/main/AndroidManifest.xml`: permissions, activity, services, and receivers.
- `app/build.gradle.kts` and `gradle/libs.versions.toml`: Android SDK, app version, dependencies, and build-tool versions.
- `PRD.MD`: product-scope and decision reference; it does not replace verification against current source.

### Technology and versions
The configuration in `app/build.gradle.kts` specifies Android Gradle Plugin `8.7.2` and Kotlin `2.0.21` (from `gradle/libs.versions.toml`), Compose with BOM `2024.10.01`, Navigation Compose `2.8.3`, AndroidX Core KTX `1.15.0`, Lifecycle `2.8.7`, Activity Compose `1.9.3`, and Material 3. The catalog also lists Adhan `1.2.1`, DataStore Preferences `1.1.1`, Play Services Location `21.3.0`, Retrofit `2.11.0`, and Gson `2.11.0`. The app build file additionally uses Media3 ExoPlayer/Session `1.4.1`, AndroidX Media `1.7.0`, Coil Compose `2.7.0`, and Firebase BOM `33.10.0` with Analytics and Realtime Database; the root `build.gradle.kts` declares Google Services plugin `4.4.2`. The app configuration sets namespace `com.example`, application ID `com.aistudio.athkar.azkari`, `compileSdk` and `targetSdk` `36`, `minSdk` `26`, app version `2.2.7`, and version code `8`; Java and Kotlin source/target levels are set to `21` (`app/build.gradle.kts`). These are repository configuration facts, not a guarantee of compatibility with a particular local build environment.

### Setup, run, test, and build
`settings.gradle.kts` shows a single Gradle module, `:app`, and Google/Maven Central repositories. No tracked `gradlew`/`gradlew.bat` or README build/run instructions were found; therefore the repository does not specify a documented copyable build command, and this profile does not assume one. `app/build.gradle.kts` declares JUnit, AndroidX JUnit, Espresso, and Compose UI test dependencies, but no test files were found under `app/src/test` or `app/src/androidTest`, and no CI files were found under `.github/workflows/` in the tree. Accordingly, this profile does not assert a successful test or build. Firebase setup may require external configuration because the Google Services plugin is declared and `FirebaseBroadcastManager` is used; the inspected repository files do not settle developer-environment setup requirements, so verify them before attempting a local build.

### Practical notes and open questions
- Source code uses `SharedPreferences` named `athkar_user_data` in `ui/MainViewModel.kt`; DataStore is also a catalog dependency, so verify actual usage before changing persistence.
- `AndroidManifest.xml` declares permissions, services, and receivers for adhan, radio, location, and notifications. A declared permission alone does not prove a complete product flow; inspect the relevant `service/`, `receiver/`, and screen code.
- `README.md` provides no contributor steps, and no tests or CI automation were visible in this inspection. No confirmed release process or external configuration values are documented here.
- Consult `PRD.MD` for limitations recorded there, and verify they still match the code before relying on them; it is descriptive documentation, not an independent test result.
