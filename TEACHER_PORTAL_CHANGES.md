# Teacher Portal Upgrade

The former Admin-style management functionality has been renamed and implemented as a **Teacher Portal**.

## Added files

- `TeacherLoginActivity.java`
- `TeacherDashboardActivity.java`
- `activity_teacher_login.xml`
- `activity_teacher_dashboard.xml`
- `FIREBASE_SETUP.md`
- `firebase-database-rules.json`

## Updated files

- `MainActivity.java`
- `activity_main.xml`
- `Student.java`
- `AndroidManifest.xml`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

## Teacher workflow

`Student App -> Teacher Login -> Firebase Authentication -> Teacher role check -> Teacher Dashboard -> Search/Add/Edit/Delete -> Calculate Result -> Publish -> Student Result`
