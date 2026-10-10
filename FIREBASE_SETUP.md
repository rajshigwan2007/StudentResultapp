# Teacher Portal + Firebase Setup

The app now uses **Firebase Authentication (Email/Password)** for teacher login.
Passwords are not stored in Realtime Database.

## 1. Enable Teacher Login

In Firebase Console:

1. Open the project `studentresultapp-19798`.
2. Go to **Authentication -> Sign-in method**.
3. Enable **Email/Password**.
4. Go to **Authentication -> Users**.
5. Create each teacher account with an email and password.
6. Copy the generated **UID** for each teacher.

## 2. Add the teacher role in Realtime Database

For every teacher UID, create:

```text
Teachers
  <TEACHER_UID>
    role: "teacher"
    active: true
```

Example:

```json
{
  "Teachers": {
    "abc123UID": {
      "role": "teacher",
      "active": true
    }
  }
}
```

The app checks this record after Firebase Authentication succeeds. A normal Firebase user without `role: teacher` cannot open the teacher dashboard.

## 3. Student result structure

Teacher uploads results under the existing path used by your project:

```text
Students
  Students
    101
      name: "Student Name"
      rollNo: "101"
      dob: "01/01/2010"
      className: "10th"
      examName: "Final Exam"
      subjects
        Marathi: 80
        English: 75
        Mathematics: 90
      totalMarks: 245
      maxMarks: 300
      percentage: 81.6667
      grade: "A"
      status: "PASS"
      remarks: "Good performance"
      published: true
      uploadedBy: "TEACHER_UID"
      updatedAt: 1234567890
```

## 4. Teacher features

- Teacher login/logout
- Add new student result
- Search existing student by roll number
- Edit/update result
- Delete result
- Enter subject-wise marks
- Automatic total and percentage
- Automatic grade
- Automatic PASS/FAIL
- Remarks
- Publish result / keep draft

## 5. Important grading rule

The current default rule is:

- 90+ = A+
- 80-89 = A
- 70-79 = B+
- 60-69 = B
- 50-59 = C
- 35-49 = D
- Below 35 = F

A student is marked **PASS** only when every entered subject is at least 35 and the overall percentage is at least 35.

If your college/school uses a different grading system, change `calculateGrade()` and the pass rule in `TeacherDashboardActivity.java`.

## 6. Realtime Database rules

A starter rules file is included as `firebase-database-rules.json`.

It protects teacher writes so only authenticated users registered under `Teachers/<uid>` can create/update/delete student results. Student reads remain available for the existing student verification flow.

Review the rules before production deployment because the current student-side verification does not authenticate the student account.
