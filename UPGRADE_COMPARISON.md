# Upgrade comparison

## Kept from the original project
- Existing Android Java/XML project structure
- Existing student result search/verification flow
- Existing ResultActivity result display
- Existing Firebase Realtime Database data path
- Existing `google-services.json`
- Existing launcher/resources/tests

## Added/changed
- Teacher Login screen using Firebase Authentication
- Teacher Dashboard
- Teacher role verification using `Teachers/<uid>`
- Add/search/edit/delete student result operations
- Subject marks entry
- Automatic total/max marks/percentage/grade/status
- Publish/Draft result flag
- Student-side check for unpublished results
- Teacher logout
- Firebase Auth dependency
- Teacher-focused database write rules file
- Android Studio opening instructions

## Gradle sync cleanup
- Removed the generated `gradle/gradle-daemon-jvm.properties` file that requested a Java 25 toolchain.
- Removed the Foojay toolchain resolver plugin from `settings.gradle.kts` so the project can use Android Studio's Embedded JDK 17 without an extra toolchain resolver.
- Kept the project as a standard Gradle Android project with `settings.gradle.kts`, `build.gradle.kts`, `app/`, and the Gradle wrapper.
