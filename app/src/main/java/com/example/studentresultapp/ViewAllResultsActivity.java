package com.example.studentresultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ViewAllResultsActivity extends AppCompatActivity {

    private ListView listResults;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseAuth auth;
    private DatabaseReference studentsReference;

    private final ArrayList<String> resultKeys =
            new ArrayList<>();

    private final ArrayList<String> resultDisplayList =
            new ArrayList<>();

    private ArrayAdapter<String> adapter;

    private static final String DATABASE_URL =
            "https://studentresultapp-19798-default-rtdb.firebaseio.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_view_all_results
        );

        bindViews();

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {

            finish();
            return;
        }

        studentsReference =
                FirebaseDatabase
                        .getInstance(DATABASE_URL)
                        .getReference("Students")
                        .child("Students");

        setupList();

        loadAllResults();
    }

    private void bindViews() {

        listResults =
                findViewById(R.id.listResults);

        progressBar =
                findViewById(R.id.progressBarResults);

        tvEmpty =
                findViewById(R.id.tvEmptyResults);
    }

    private void setupList() {

        adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        resultDisplayList
                );

        listResults.setAdapter(adapter);

        listResults.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position >= resultKeys.size()) {
                        return;
                    }

                    String rollNo =
                            resultKeys.get(position);

                    Intent intent =
                            new Intent(
                                    ViewAllResultsActivity.this,
                                    TeacherDashboardActivity.class
                            );

                    intent.putExtra(
                            "searchRollNo",
                            rollNo
                    );

                    startActivity(intent);
                }
        );
    }

    private void loadAllResults() {

        showLoading(true);

        studentsReference
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot) {

                                resultKeys.clear();
                                resultDisplayList.clear();

                                for (DataSnapshot child :
                                        snapshot.getChildren()) {

                                    String rollNo =
                                            getString(
                                                    child,
                                                    "rollNo"
                                            );

                                    if (rollNo.isEmpty()) {

                                        rollNo =
                                                child.getKey();
                                    }

                                    if (rollNo == null ||
                                            rollNo.isEmpty()) {
                                        continue;
                                    }

                                    String name =
                                            getString(
                                                    child,
                                                    "name"
                                            );

                                    String className =
                                            getString(
                                                    child,
                                                    "className"
                                            );

                                    String examName =
                                            getString(
                                                    child,
                                                    "examName"
                                            );

                                    String total =
                                            getNumberString(
                                                    child,
                                                    "totalMarks"
                                            );

                                    String maxMarks =
                                            getNumberString(
                                                    child,
                                                    "maxMarks"
                                            );

                                    String percentage =
                                            getPercentage(
                                                    child
                                            );

                                    String grade =
                                            getString(
                                                    child,
                                                    "grade"
                                            );

                                    String status =
                                            getString(
                                                    child,
                                                    "status"
                                            );

                                    Boolean published =
                                            child.child(
                                                            "published"
                                                    )
                                                    .getValue(
                                                            Boolean.class
                                                    );

                                    String publishText;

                                    if (published != null &&
                                            published) {

                                        publishText =
                                                "PUBLISHED";

                                    } else {

                                        publishText =
                                                "DRAFT";
                                    }

                                    String display =
                                            "Student: "
                                                    + name
                                                    + "\n"
                                                    + "Roll No: "
                                                    + rollNo
                                                    + "   |   Class: "
                                                    + className
                                                    + "\n"
                                                    + "Exam: "
                                                    + examName
                                                    + "\n"
                                                    + "Total: "
                                                    + total
                                                    + " / "
                                                    + maxMarks
                                                    + "   |   "
                                                    + percentage
                                                    + "%\n"
                                                    + "Grade: "
                                                    + grade
                                                    + "   |   Status: "
                                                    + status
                                                    + "\n"
                                                    + "Result: "
                                                    + publishText;

                                    resultKeys.add(
                                            rollNo
                                    );

                                    resultDisplayList.add(
                                            display
                                    );
                                }

                                adapter.notifyDataSetChanged();

                                showLoading(false);

                                if (resultDisplayList.isEmpty()) {

                                    tvEmpty.setVisibility(
                                            View.VISIBLE
                                    );

                                    listResults.setVisibility(
                                            View.GONE
                                    );

                                } else {

                                    tvEmpty.setVisibility(
                                            View.GONE
                                    );

                                    listResults.setVisibility(
                                            View.VISIBLE
                                    );
                                }
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error) {

                                showLoading(false);

                                Toast.makeText(
                                        ViewAllResultsActivity.this,
                                        "Could not load results: "
                                                + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private String getString(
            DataSnapshot snapshot,
            String key) {

        Object value =
                snapshot.child(key)
                        .getValue();

        return value == null
                ? ""
                : String.valueOf(value);
    }

    private String getNumberString(
            DataSnapshot snapshot,
            String key) {

        Object value =
                snapshot.child(key)
                        .getValue();

        if (value == null) {
            return "0";
        }

        if (value instanceof Double) {

            double number =
                    (Double) value;

            if (number ==
                    Math.floor(number)) {

                return String.valueOf(
                        (long) number
                );
            }
        }

        return String.valueOf(value);
    }

    private String getPercentage(
            DataSnapshot snapshot) {

        Object value =
                snapshot.child("percentage")
                        .getValue();

        if (value == null) {
            return "0";
        }

        if (value instanceof Double) {

            double percentage =
                    (Double) value;

            return String.format(
                    java.util.Locale.getDefault(),
                    "%.2f",
                    percentage
            );
        }

        return String.valueOf(value);
    }

    private void showLoading(
            boolean loading) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (studentsReference != null) {
            loadAllResults();
        }
    }
}