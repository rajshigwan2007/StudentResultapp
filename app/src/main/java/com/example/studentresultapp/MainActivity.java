package com.example.studentresultapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText etName;
    private Spinner spClass;
    private EditText etRoll;
    private EditText etDob;
    private Button btnShow;

    private DatabaseReference studentsReference;

    private static final String DATABASE_URL =
            "https://studentresultapp-19798-default-rtdb.firebaseio.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        spClass = findViewById(R.id.spClass);
        etRoll = findViewById(R.id.etRoll);
        etDob = findViewById(R.id.etDob);
        btnShow = findViewById(R.id.btnShow);
        TextView btnTeacherLogin = findViewById(R.id.btnTeacherLogin);

        // Class dropdown
        String[] classes = {
                "Select Class",
                "8th",
                "9th",
                "10th",
                "11th",
                "12th"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        classes
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spClass.setAdapter(adapter);

        /*
         * Firebase structure:
         *
         * Students
         *    └── Students
         *          └── 101
         *
         * म्हणून second Students खाली reference घेतला आहे.
         */
        studentsReference = FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference("Students")
                .child("Students");

        etDob.setOnClickListener(v -> openDatePicker());

        btnShow.setOnClickListener(v -> checkResult());
        btnTeacherLogin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TeacherLoginActivity.class)));
    }

    private void openDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    String dob = String.format(
                            "%02d/%02d/%04d",
                            day,
                            month + 1,
                            year
                    );

                    etDob.setText(dob);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMaxDate(
                System.currentTimeMillis()
        );

        dialog.show();
    }

    private void checkResult() {

        String inputName =
                etName.getText().toString().trim();

        String inputClass =
                spClass.getSelectedItem().toString().trim();

        String inputRoll =
                etRoll.getText().toString().trim();

        String inputDob =
                etDob.getText().toString().trim();

        if (inputName.isEmpty()) {
            etName.setError("Enter Student Name");
            etName.requestFocus();
            return;
        }

        if (inputClass.equals("Select Class")) {
            Toast.makeText(
                    this,
                    "Please select your Class",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (inputRoll.isEmpty()) {
            etRoll.setError("Enter Roll Number");
            etRoll.requestFocus();
            return;
        }

        if (inputDob.isEmpty()) {
            etDob.setError("Select Date of Birth");
            etDob.requestFocus();
            return;
        }

        btnShow.setEnabled(false);
        btnShow.setText("CHECKING...");

        studentsReference
                .child(inputRoll)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot
                            ) {

                                if (!snapshot.exists()) {

                                    resetButton();

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Roll Number "
                                                    + inputRoll
                                                    + " not found",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                Object publishedValue = snapshot.child("published").getValue();
                                if (publishedValue instanceof Boolean && !((Boolean) publishedValue)) {
                                    resetButton();
                                    Toast.makeText(
                                            MainActivity.this,
                                            "This result has not been published by the teacher yet",
                                            Toast.LENGTH_LONG
                                    ).show();
                                    return;
                                }

                                String firebaseName =
                                        getString(
                                                snapshot,
                                                "name"
                                        );

                                String firebaseClass =
                                        getString(
                                                snapshot,
                                                "className"
                                        );

                                String firebaseDob =
                                        getString(
                                                snapshot,
                                                "dob"
                                        );

                                boolean nameMatch =
                                        normalize(inputName)
                                                .equals(
                                                        normalize(
                                                                firebaseName
                                                        )
                                                );

                                boolean classMatch =
                                        normalizeClass(inputClass)
                                                .equals(
                                                        normalizeClass(
                                                                firebaseClass
                                                        )
                                                );

                                boolean dobMatch =
                                        normalizeDob(inputDob)
                                                .equals(
                                                        normalizeDob(
                                                                firebaseDob
                                                        )
                                                );

                                if (!nameMatch) {

                                    resetButton();

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Student Name incorrect",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                if (!classMatch) {

                                    resetButton();

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Class incorrect",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                if (!dobMatch) {

                                    resetButton();

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Date of Birth incorrect",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                openResult(snapshot);

                                resetButton();
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error
                            ) {

                                resetButton();

                                Toast.makeText(
                                        MainActivity.this,
                                        "Firebase Error: "
                                                + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    private String normalizeClass(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    private String normalizeDob(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replace("-", "/")
                .replace(".", "/");
    }

    private String getString(
            DataSnapshot snapshot,
            String key
    ) {

        Object value =
                snapshot.child(key).getValue();

        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }

    private void openResult(
            DataSnapshot snapshot
    ) {

        Intent intent = new Intent(
                MainActivity.this,
                ResultActivity.class
        );

        intent.putExtra(
                "name",
                getString(snapshot, "name")
        );

        String rollNo =
                getString(snapshot, "rollNo");

        if (rollNo.isEmpty()) {
            rollNo = snapshot.getKey();
        }

        intent.putExtra(
                "rollNo",
                rollNo
        );

        intent.putExtra(
                "dob",
                getString(snapshot, "dob")
        );

        intent.putExtra(
                "className",
                getString(snapshot, "className")
        );

        intent.putExtra(
                "examName",
                getString(snapshot, "examName")
        );

        intent.putExtra(
                "grade",
                getString(snapshot, "grade")
        );

        intent.putExtra(
                "status",
                getString(snapshot, "status")
        );

        intent.putExtra(
                "remarks",
                getString(snapshot, "remarks")
        );

        DataSnapshot subjects =
                snapshot.child("subjects");

        for (DataSnapshot subject :
                subjects.getChildren()) {

            String subjectName =
                    subject.getKey();

            Object value =
                    subject.getValue();

            if (subjectName == null || value == null) {
                continue;
            }

            long marks;

            if (value instanceof Number) {

                marks = ((Number) value).longValue();

            } else {

                try {

                    marks = Long.parseLong(
                            String.valueOf(value)
                    );

                } catch (Exception e) {
                    continue;
                }
            }

            intent.putExtra(
                    "subject_" + subjectName,
                    marks
            );

            intent.putExtra(
                    "subject_exists_" + subjectName,
                    true
            );
        }

        startActivity(intent);
    }

    private void resetButton() {

        btnShow.setEnabled(true);
        btnShow.setText("SHOW RESULT");
    }
}