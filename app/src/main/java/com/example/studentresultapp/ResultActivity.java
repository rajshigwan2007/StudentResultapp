package com.example.studentresultapp;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private LinearLayout subjectsContainer;

    private TextView tvName;
    private TextView tvRoll;
    private TextView tvDob;
    private TextView tvClass;
    private TextView tvExam;

    private TextView tvTotal;
    private TextView tvPercentage;
    private TextView tvGrade;
    private TextView tvStatus;
    private TextView tvRemarks;

    private Button btnBack;

    private DatabaseReference studentsReference;
    private DatabaseReference subjectsReference;

    private static final String DATABASE_URL =
            "https://studentresultapp-19798-default-rtdb.firebaseio.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_result
        );


        tvName =
                findViewById(
                        R.id.tvName
                );

        tvRoll =
                findViewById(
                        R.id.tvRoll
                );

        tvDob =
                findViewById(
                        R.id.tvDob
                );

        tvClass =
                findViewById(
                        R.id.tvClass
                );

        tvExam =
                findViewById(
                        R.id.tvExam
                );


        subjectsContainer =
                findViewById(
                        R.id.subjectsContainer
                );


        tvTotal =
                findViewById(
                        R.id.tvTotal
                );

        tvPercentage =
                findViewById(
                        R.id.tvPercentage
                );

        tvGrade =
                findViewById(
                        R.id.tvGrade
                );

        tvStatus =
                findViewById(
                        R.id.tvStatus
                );

        tvRemarks =
                findViewById(
                        R.id.tvRemarks
                );


        btnBack =
                findViewById(
                        R.id.btnBack
                );


        studentsReference =
                FirebaseDatabase
                        .getInstance(DATABASE_URL)
                        .getReference("Students")
                        .child("Students");


        subjectsReference =
                FirebaseDatabase
                        .getInstance(DATABASE_URL)
                        .getReference("subjects");


        btnBack.setOnClickListener(
                v -> finish()
        );


        loadStudentDetails();
    }


    // =========================================================
    // LOAD RESULT
    // =========================================================

    private void loadStudentDetails() {

        String rollNo =
                getIntent().getStringExtra(
                        "rollNo"
                );


        if (rollNo == null ||
                rollNo.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Roll number not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        studentsReference
                .child(rollNo.trim())
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot) {

                                /*
                                 * Result does not exist
                                 */
                                if (!snapshot.exists()) {

                                    Toast.makeText(
                                            ResultActivity.this,
                                            "Result not found.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    finish();

                                    return;
                                }


                                /*
                                 * IMPORTANT:
                                 * Only published result is visible
                                 * to student.
                                 */
                                Boolean published =
                                        snapshot.child(
                                                        "published"
                                                )
                                                .getValue(
                                                        Boolean.class
                                                );


                                if (published == null ||
                                        !published) {

                                    Toast.makeText(
                                            ResultActivity.this,
                                            "Result is not published yet.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    finish();

                                    return;
                                }


                                /*
                                 * Published result असल्यामुळे
                                 * आता result display करा.
                                 */
                                displayResult(
                                        snapshot
                                );
                            }


                            @Override
                            public void onCancelled(
                                    DatabaseError error) {

                                Toast.makeText(
                                        ResultActivity.this,
                                        "Could not load result: "
                                                + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                                finish();
                            }
                        }
                );
    }


    // =========================================================
    // DISPLAY RESULT
    // =========================================================

    private void displayResult(
            DataSnapshot snapshot) {

        String name =
                getString(
                        snapshot,
                        "name"
                );


        String rollNo =
                getString(
                        snapshot,
                        "rollNo"
                );


        if (rollNo.isEmpty()) {
            rollNo = snapshot.getKey();
        }


        String dob =
                getString(
                        snapshot,
                        "dob"
                );


        String className =
                getString(
                        snapshot,
                        "className"
                );


        String examName =
                getString(
                        snapshot,
                        "examName"
                );


        String grade =
                getString(
                        snapshot,
                        "grade"
                );


        String status =
                getString(
                        snapshot,
                        "status"
                );


        String remarks =
                getString(
                        snapshot,
                        "remarks"
                );


        tvName.setText(
                valueOrDash(name)
        );


        tvRoll.setText(
                "Roll No: "
                        + valueOrDash(rollNo)
        );


        tvDob.setText(
                "DOB: "
                        + valueOrDash(dob)
        );


        tvClass.setText(
                "Class: "
                        + valueOrDash(className)
        );


        tvExam.setText(
                valueOrDash(examName)
        );


        /*
         * Clear old subject rows
         */
        subjectsContainer.removeAllViews();


        DataSnapshot subjectSnapshot =
                snapshot.child(
                        "subjects"
                );


        if (!subjectSnapshot.exists()) {

            addSubjectRow(
                    "Marks",
                    0,
                    100
            );

        } else {

            /*
             * First load subject max marks
             */
            loadSubjectRows(
                    snapshot,
                    subjectSnapshot,
                    grade,
                    status,
                    remarks
            );

            return;
        }


        /*
         * If no subjects
         */
        showFinalResult(
                snapshot,
                grade,
                status,
                remarks,
                0,
                100
        );
    }


    // =========================================================
    // LOAD DYNAMIC SUBJECT MAX MARKS
    // =========================================================

    private void loadSubjectRows(
            DataSnapshot resultSnapshot,
            DataSnapshot resultSubjects,
            String grade,
            String status,
            String remarks) {

        subjectsReference
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot subjectDefinitions) {

                                long calculatedTotal = 0;
                                long calculatedMax = 0;

                                for (DataSnapshot resultSubject :
                                        resultSubjects.getChildren()) {

                                    String subjectName =
                                            resultSubject.getKey();

                                    if (subjectName == null ||
                                            subjectName.trim().isEmpty()) {
                                        continue;
                                    }


                                    Object marksValue =
                                            resultSubject.getValue();


                                    long marks =
                                            getLongValue(
                                                    marksValue
                                            );


                                    long maxMarks =
                                            100;


                                    /*
                                     * Find subject's current max marks
                                     */
                                    for (DataSnapshot definition :
                                            subjectDefinitions.getChildren()) {

                                        String definitionName =
                                                definition.child("name")
                                                        .getValue(
                                                                String.class
                                                        );


                                        if (definitionName != null &&
                                                definitionName.equals(
                                                        subjectName
                                                )) {

                                            Long savedMax =
                                                    definition.child(
                                                                    "maxMarks"
                                                            )
                                                            .getValue(
                                                                    Long.class
                                                            );


                                            if (savedMax != null &&
                                                    savedMax > 0) {

                                                maxMarks =
                                                        savedMax;
                                            }

                                            break;
                                        }
                                    }


                                    addSubjectRow(
                                            subjectName,
                                            marks,
                                            (int) maxMarks
                                    );


                                    calculatedTotal += marks;

                                    calculatedMax += maxMarks;
                                }


                                /*
                                 * Use Firebase saved total/max if available.
                                 * This keeps old result calculations intact.
                                 */
                                long savedTotal =
                                        getLongValue(
                                                resultSnapshot
                                                        .child(
                                                                "totalMarks"
                                                        )
                                                        .getValue()
                                        );


                                long savedMax =
                                        getLongValue(
                                                resultSnapshot
                                                        .child(
                                                                "maxMarks"
                                                        )
                                                        .getValue()
                                        );


                                if (savedTotal > 0) {
                                    calculatedTotal =
                                            savedTotal;
                                }


                                if (savedMax > 0) {
                                    calculatedMax =
                                            savedMax;
                                }


                                showFinalResult(
                                        resultSnapshot,
                                        grade,
                                        status,
                                        remarks,
                                        calculatedTotal,
                                        calculatedMax
                                );
                            }


                            @Override
                            public void onCancelled(
                                    DatabaseError error) {

                                /*
                                 * Even if subject definitions fail,
                                 * show result using saved Firebase values.
                                 */

                                long total =
                                        getLongValue(
                                                resultSnapshot
                                                        .child(
                                                                "totalMarks"
                                                        )
                                                        .getValue()
                                        );


                                long max =
                                        getLongValue(
                                                resultSnapshot
                                                        .child(
                                                                "maxMarks"
                                                        )
                                                        .getValue()
                                        );


                                for (DataSnapshot resultSubject :
                                        resultSubjects.getChildren()) {

                                    String subjectName =
                                            resultSubject.getKey();


                                    long marks =
                                            getLongValue(
                                                    resultSubject.getValue()
                                            );


                                    addSubjectRow(
                                            subjectName == null
                                                    ? "Subject"
                                                    : subjectName,
                                            marks,
                                            100
                                    );
                                }


                                showFinalResult(
                                        resultSnapshot,
                                        grade,
                                        status,
                                        remarks,
                                        total,
                                        max
                                );
                            }
                        }
                );
    }


    // =========================================================
    // FINAL RESULT
    // =========================================================

    private void showFinalResult(
            DataSnapshot snapshot,
            String grade,
            String status,
            String remarks,
            long totalMarks,
            long maxMarks) {

        /*
         * Firebase saved percentage असेल तर तेच वापरतो.
         */
        Object percentageValue =
                snapshot.child(
                        "percentage"
                ).getValue();


        double percentage;


        if (percentageValue != null) {

            percentage =
                    getDoubleValue(
                            percentageValue
                    );

        } else {

            percentage =
                    maxMarks == 0
                            ? 0
                            : (totalMarks * 100.0)
                              / maxMarks;
        }


        tvTotal.setText(
                totalMarks
                        + " / "
                        + maxMarks
        );


        tvPercentage.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f%%",
                        percentage
                )
        );


        tvGrade.setText(
                valueOrDash(grade)
        );


        String finalStatus =
                valueOrDash(status);


        tvStatus.setText(
                finalStatus
        );


        if (finalStatus.equalsIgnoreCase(
                "PASS"
        )) {

            tvStatus.setBackgroundResource(
                    R.drawable.bg_status_pass
            );

        } else if (finalStatus.equalsIgnoreCase(
                "FAIL"
        )) {

            tvStatus.setBackgroundResource(
                    R.drawable.bg_status_fail
            );
        }


        tvRemarks.setText(
                valueOrDash(remarks)
        );
    }


    // =========================================================
    // SUBJECT ROW
    // =========================================================

    private void addSubjectRow(
            String subject,
            long marks,
            int maxMarks) {

        LinearLayout row =
                new LinearLayout(this);


        row.setOrientation(
                LinearLayout.HORIZONTAL
        );


        row.setGravity(
                Gravity.CENTER_VERTICAL
        );


        row.setPadding(
                14,
                14,
                14,
                14
        );


        TextView tvSubject =
                new TextView(this);


        tvSubject.setText(
                subject
        );


        tvSubject.setTextSize(
                15
        );


        tvSubject.setTextColor(
                getResources()
                        .getColor(
                                R.color.text_primary
                        )
        );


        tvSubject.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        TextView tvMarks =
                new TextView(this);


        tvMarks.setText(
                marks
                        + " / "
                        + maxMarks
        );


        tvMarks.setTextSize(
                15
        );


        tvMarks.setGravity(
                Gravity.END
        );


        tvMarks.setTextColor(
                getResources()
                        .getColor(
                                R.color.text_primary
                        )
        );


        row.addView(
                tvSubject,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );


        row.addView(
                tvMarks,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.45f
                )
        );


        subjectsContainer.addView(
                row
        );


        View divider =
                new View(this);


        divider.setBackgroundColor(
                getResources()
                        .getColor(
                                R.color.divider
                        )
        );


        subjectsContainer.addView(
                divider,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );
    }


    // =========================================================
    // VALUE HELPERS
    // =========================================================

    private String getString(
            DataSnapshot snapshot,
            String key) {

        Object value =
                snapshot.child(key)
                        .getValue();


        return value == null
                ? ""
                : String.valueOf(
                value
        ).trim();
    }


    private long getLongValue(
            Object value) {

        if (value == null) {
            return 0;
        }


        if (value instanceof Long) {
            return (Long) value;
        }


        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }


        if (value instanceof Double) {
            return ((Double) value).longValue();
        }


        if (value instanceof Float) {
            return ((Float) value).longValue();
        }


        try {

            return Long.parseLong(
                    String.valueOf(value)
            );

        } catch (Exception e) {

            return 0;
        }
    }


    private double getDoubleValue(
            Object value) {

        if (value == null) {
            return 0;
        }


        if (value instanceof Double) {
            return (Double) value;
        }


        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }


        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }


        try {

            return Double.parseDouble(
                    String.valueOf(value)
            );

        } catch (Exception e) {

            return 0;
        }
    }


    private String valueOrDash(
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "-";
        }


        return value;
    }
}