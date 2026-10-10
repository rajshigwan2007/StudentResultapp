# Student Result App - Teacher Portal Upgrade

## Open the project correctly

1. Extract this ZIP completely.
2. Open Android Studio.
3. Choose **File > Open**.
4. Select the folder that contains **settings.gradle.kts** and the **app** folder.
5. Do NOT open the `app`, `src`, or `java` folder by itself.
6. If Android Studio asks for a Gradle JDK, select **Embedded JDK 17**.
7. Wait for Gradle Sync to finish.
8. After successful sync, the left Project panel should show the **Android** view and the top toolbar should show the **Run (▶)** button.

## If Android Studio shows "Link Gradle Project"

Choose **Link Gradle Project** and select the project's `settings.gradle.kts` file.

## Firebase

The existing `app/google-services.json` is retained. Teacher login uses Firebase Authentication and the Teacher role is checked under `Teachers/<uid>`.

See `FIREBASE_SETUP.md` for the Firebase setup steps.

## Teacher features

- Teacher login
- Add/upload student result
- Search result by roll number
- Edit/update result
- Delete result
- Subject-wise marks
- Automatic total, percentage, grade and pass/fail
- Publish or save as draft
- Teacher logout
