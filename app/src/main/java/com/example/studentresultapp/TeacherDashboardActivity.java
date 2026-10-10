package com.example.studentresultapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class TeacherDashboardActivity extends AppCompatActivity {

    private EditText etName,
            etRoll,
            etDob,
            etExam,
            etRemarks;

    private Spinner spClass;

    private CheckBox cbPublish;

    private Button btnSave,
            btnDelete,
            btnClear,
            btnSearch,
            btnLogout,
            btnManageSubjects,
            btnViewAllResults;

    private TextView tvTeacherEmail,
            tvFormTitle;

    private LinearLayout subjectInputsContainer;

    private final ArrayList<EditText> subjectInputs =
            new ArrayList<>();

    private final ArrayList<String> subjects =
            new ArrayList<>();

    private final ArrayList<Long> maxMarksList =
            new ArrayList<>();

    private final ArrayList<Long> passingMarksList =
            new ArrayList<>();

    private DatabaseReference studentsReference;
    private DatabaseReference subjectsReference;

    private FirebaseAuth auth;

    private String pendingSearchRollNo = null;
    private boolean autoSearchDone = false;

    private static final String DATABASE_URL =
            "https://studentresultapp-19798-default-rtdb.firebaseio.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_dashboard
        );

        bindViews();

        setupClassSpinner();

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

        subjectsReference =
                FirebaseDatabase
                        .getInstance(DATABASE_URL)
                        .getReference("subjects");

        tvTeacherEmail.setText(
                auth.getCurrentUser().getEmail()
        );

        etDob.setOnClickListener(
                v -> openDatePicker()
        );

        btnSave.setOnClickListener(
                v -> saveResult()
        );

        btnSearch.setOnClickListener(
                v -> searchStudent()
        );

        btnDelete.setOnClickListener(
                v -> deleteStudent()
        );

        btnClear.setOnClickListener(
                v -> clearForm()
        );

        btnLogout.setOnClickListener(
                v -> logoutTeacher()
        );

        btnManageSubjects.setOnClickListener(
                v -> showManageSubjectsDialog()
        );

        btnViewAllResults.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            TeacherDashboardActivity.this,
                            ViewAllResultsActivity.class
                    );

            startActivity(intent);
        });

        /*
         * View All Results मधून result click केल्यावर
         * हा Roll No मिळेल.
         */
        pendingSearchRollNo =
                getIntent().getStringExtra(
                        "searchRollNo"
                );

        loadSubjects();
    }


    // =========================================================
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        etName =
                findViewById(
                        R.id.etTeacherStudentName
                );

        etRoll =
                findViewById(
                        R.id.etTeacherRoll
                );

        etDob =
                findViewById(
                        R.id.etTeacherDob
                );

        etExam =
                findViewById(
                        R.id.etTeacherExam
                );

        etRemarks =
                findViewById(
                        R.id.etTeacherRemarks
                );

        spClass =
                findViewById(
                        R.id.spTeacherClass
                );

        cbPublish =
                findViewById(
                        R.id.cbPublishResult
                );

        btnSave =
                findViewById(
                        R.id.btnSaveResult
                );

        btnDelete =
                findViewById(
                        R.id.btnDeleteStudent
                );

        btnClear =
                findViewById(
                        R.id.btnClearForm
                );

        btnSearch =
                findViewById(
                        R.id.btnSearchStudent
                );

        btnLogout =
                findViewById(
                        R.id.btnTeacherLogout
                );

        btnManageSubjects =
                findViewById(
                        R.id.btnManageSubjects
                );

        btnViewAllResults =
                findViewById(
                        R.id.btnViewAllResults
                );

        tvTeacherEmail =
                findViewById(
                        R.id.tvTeacherEmail
                );

        tvFormTitle =
                findViewById(
                        R.id.tvTeacherFormTitle
                );

        subjectInputsContainer =
                findViewById(
                        R.id.subjectInputsContainer
                );
    }


    // =========================================================
    // CLASS SPINNER
    // =========================================================

    private void setupClassSpinner() {

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
    }


    // =========================================================
    // SUBJECT LOADING
    // =========================================================

    private void loadSubjects() {

        subjectsReference.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot) {

                        subjects.clear();
                        maxMarksList.clear();
                        passingMarksList.clear();

                        if (!snapshot.exists()) {

                            addDefaultSubjects();

                        } else {

                            for (DataSnapshot child :
                                    snapshot.getChildren()) {

                                String name =
                                        child.child("name")
                                                .getValue(
                                                        String.class
                                                );

                                Long maxMarks =
                                        child.child("maxMarks")
                                                .getValue(
                                                        Long.class
                                                );

                                Long passingMarks =
                                        child.child(
                                                        "passingMarks"
                                                )
                                                .getValue(
                                                        Long.class
                                                );

                                if (name == null ||
                                        name.trim().isEmpty()) {
                                    continue;
                                }

                                if (maxMarks == null ||
                                        maxMarks <= 0) {
                                    maxMarks = 100L;
                                }

                                if (passingMarks == null ||
                                        passingMarks < 0 ||
                                        passingMarks > maxMarks) {

                                    passingMarks =
                                            Math.min(
                                                    35L,
                                                    maxMarks
                                            );
                                }

                                subjects.add(name);
                                maxMarksList.add(maxMarks);
                                passingMarksList.add(
                                        passingMarks
                                );
                            }

                            setupSubjectFields();
                        }
                    }

                    @Override
                    public void onCancelled(
                            DatabaseError error) {

                        toast(
                                "Could not load subjects: "
                                        + error.getMessage()
                        );
                    }
                }
        );
    }


    // =========================================================
    // DEFAULT SUBJECTS
    // =========================================================

    private void addDefaultSubjects() {

        String[] defaults = {
                "Marathi",
                "English",
                "Mathematics",
                "Science",
                "Social Science",
                "Hindi",
                "Physics",
                "Chemistry",
                "Biology",
                "Computer"
        };

        for (String subject : defaults) {

            String key =
                    subjectsReference
                            .push()
                            .getKey();

            if (key == null) {
                continue;
            }

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put(
                    "name",
                    subject
            );

            data.put(
                    "maxMarks",
                    100L
            );

            data.put(
                    "passingMarks",
                    35L
            );

            subjectsReference
                    .child(key)
                    .setValue(data);

            subjects.add(subject);
            maxMarksList.add(100L);
            passingMarksList.add(35L);
        }

        setupSubjectFields();
    }


    // =========================================================
    // SUBJECT UI
    // =========================================================

    private void setupSubjectFields() {

        subjectInputs.clear();

        subjectInputsContainer.removeAllViews();

        for (int i = 0;
             i < subjects.size();
             i++) {

            TextView label =
                    new TextView(this);

            label.setText(
                    subjects.get(i)
                            + "  (Max: "
                            + maxMarksList.get(i)
                            + ", Pass: "
                            + passingMarksList.get(i)
                            + ")"
            );

            label.setTextSize(14);

            label.setTextColor(
                    getResources()
                            .getColor(
                                    R.color.text_primary
                            )
            );

            label.setTypeface(
                    null,
                    Typeface.BOLD
            );

            label.setPadding(
                    0,
                    14,
                    0,
                    6
            );

            subjectInputsContainer.addView(
                    label
            );

            EditText input =
                    new EditText(this);

            input.setHint(
                    "Enter marks"
            );

            input.setTextSize(15);

            input.setSingleLine(true);

            input.setInputType(
                    InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_DECIMAL
            );

            input.setPadding(
                    15,
                    0,
                    15,
                    0
            );

            input.setBackgroundResource(
                    R.drawable.bg_input
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            52
                    );

            params.setMargins(
                    0,
                    0,
                    0,
                    5
            );

            subjectInputsContainer.addView(
                    input,
                    params
            );

            subjectInputs.add(input);
        }

        /*
         * View All Results मधून Dashboard उघडल्यावर
         * subjects load झाल्यानंतरच result search करतो.
         */
        if (pendingSearchRollNo != null &&
                !pendingSearchRollNo.trim().isEmpty() &&
                !autoSearchDone) {

            autoSearchDone = true;

            etRoll.setText(
                    pendingSearchRollNo
            );

            etRoll.postDelayed(
                    () -> searchStudent(),
                    300
            );
        }
    }


    // =========================================================
    // MANAGE SUBJECTS
    // =========================================================

    private void showManageSubjectsDialog() {

        String[] options = {
                "➕ Add Subject",
                "✏️ Edit Subject",
                "🗑️ Remove Subject",
                "🎯 Passing Marks"
        };

        new AlertDialog.Builder(this)
                .setTitle("Manage Subjects")
                .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                showAddSubjectDialog();

                            } else if (which == 1) {

                                showEditSubjectDialog();

                            } else if (which == 2) {

                                showRemoveSubjectDialog();

                            } else if (which == 3) {

                                showPassingMarksDialog();
                            }
                        }
                )
                .setNegativeButton(
                        "CLOSE",
                        null
                )
                .show();
    }


    // =========================================================
    // ADD SUBJECT
    // =========================================================

    private void showAddSubjectDialog() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                25,
                5,
                25,
                5
        );

        EditText subjectInput =
                new EditText(this);

        subjectInput.setHint(
                "Subject Name"
        );

        subjectInput.setSingleLine(true);

        layout.addView(
                subjectInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        55
                )
        );

        EditText maxMarksInput =
                new EditText(this);

        maxMarksInput.setHint(
                "Max Marks (e.g. 100)"
        );

        maxMarksInput.setSingleLine(true);

        maxMarksInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        LinearLayout.LayoutParams maxParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        55
                );

        maxParams.topMargin = 10;

        layout.addView(
                maxMarksInput,
                maxParams
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Add Subject")
                        .setView(layout)
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "ADD",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            Button addButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            addButton.setOnClickListener(v -> {

                String name =
                        subjectInput
                                .getText()
                                .toString()
                                .trim();

                String maxText =
                        maxMarksInput
                                .getText()
                                .toString()
                                .trim();

                if (name.isEmpty()) {

                    subjectInput.setError(
                            "Enter subject name"
                    );

                    return;
                }

                if (maxText.isEmpty()) {

                    maxMarksInput.setError(
                            "Enter max marks"
                    );

                    return;
                }

                long maxMarks;

                try {

                    maxMarks =
                            Long.parseLong(
                                    maxText
                            );

                } catch (Exception e) {

                    maxMarksInput.setError(
                            "Invalid max marks"
                    );

                    return;
                }

                if (maxMarks <= 0) {

                    maxMarksInput.setError(
                            "Max marks must be greater than 0"
                    );

                    return;
                }

                if (subjectExists(name)) {

                    subjectInput.setError(
                            "Subject already exists"
                    );

                    return;
                }

                addSubject(
                        name,
                        maxMarks
                );

                dialog.dismiss();
            });
        });

        dialog.show();
    }


    private void addSubject(
            String name,
            long maxMarks) {

        String key =
                subjectsReference
                        .push()
                        .getKey();

        if (key == null) {

            toast(
                    "Could not create subject"
            );

            return;
        }

        long passingMarks =
                Math.min(
                        35L,
                        maxMarks
                );

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "name",
                name
        );

        data.put(
                "maxMarks",
                maxMarks
        );

        data.put(
                "passingMarks",
                passingMarks
        );

        subjectsReference
                .child(key)
                .setValue(data)
                .addOnCompleteListener(
                        task -> {

                            if (task.isSuccessful()) {

                                subjects.add(name);

                                maxMarksList.add(
                                        maxMarks
                                );

                                passingMarksList.add(
                                        passingMarks
                                );

                                setupSubjectFields();

                                toast(
                                        "Subject added successfully"
                                );

                            } else {

                                toast(
                                        "Could not add subject"
                                );
                            }
                        }
                );
    }


    // =========================================================
    // EDIT SUBJECT
    // =========================================================

    private void showEditSubjectDialog() {

        if (subjects.isEmpty()) {

            toast(
                    "No subjects available"
            );

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Subject")
                .setItems(
                        subjects.toArray(
                                new String[0]
                        ),
                        (dialog, which) ->
                                showEditSubjectForm(
                                        which
                                )
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .show();
    }


    private void showEditSubjectForm(
            int index) {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                25,
                5,
                25,
                5
        );

        EditText subjectInput =
                new EditText(this);

        subjectInput.setSingleLine(true);

        subjectInput.setText(
                subjects.get(index)
        );

        subjectInput.setHint(
                "Subject Name"
        );

        layout.addView(
                subjectInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        55
                )
        );

        EditText maxMarksInput =
                new EditText(this);

        maxMarksInput.setSingleLine(true);

        maxMarksInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        maxMarksInput.setText(
                String.valueOf(
                        maxMarksList.get(index)
                )
        );

        maxMarksInput.setHint(
                "Max Marks"
        );

        LinearLayout.LayoutParams maxParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        55
                );

        maxParams.topMargin = 10;

        layout.addView(
                maxMarksInput,
                maxParams
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Edit Subject")
                        .setView(layout)
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "SAVE",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            Button saveButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            saveButton.setOnClickListener(v -> {

                String newName =
                        subjectInput
                                .getText()
                                .toString()
                                .trim();

                String maxText =
                        maxMarksInput
                                .getText()
                                .toString()
                                .trim();

                if (newName.isEmpty()) {

                    subjectInput.setError(
                            "Enter subject name"
                    );

                    return;
                }

                if (maxText.isEmpty()) {

                    maxMarksInput.setError(
                            "Enter max marks"
                    );

                    return;
                }

                long maxMarks;

                try {

                    maxMarks =
                            Long.parseLong(
                                    maxText
                            );

                } catch (Exception e) {

                    maxMarksInput.setError(
                            "Invalid max marks"
                    );

                    return;
                }

                if (maxMarks <= 0) {

                    maxMarksInput.setError(
                            "Invalid max marks"
                    );

                    return;
                }

                for (int i = 0;
                     i < subjects.size();
                     i++) {

                    if (i != index &&
                            subjects.get(i)
                                    .equalsIgnoreCase(
                                            newName
                                    )) {

                        subjectInput.setError(
                                "Subject already exists"
                        );

                        return;
                    }
                }

                findAndUpdateSubject(
                        index,
                        newName,
                        maxMarks
                );

                dialog.dismiss();
            });
        });

        dialog.show();
    }


    private void findAndUpdateSubject(
            int index,
            String newName,
            long maxMarks) {

        String oldName =
                subjects.get(index);

        subjectsReference
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot) {

                                for (DataSnapshot child :
                                        snapshot.getChildren()) {

                                    String name =
                                            child.child("name")
                                                    .getValue(
                                                            String.class
                                                    );

                                    if (name != null &&
                                            name.equals(
                                                    oldName
                                            )) {

                                        Long oldPassing =
                                                child.child(
                                                        "passingMarks"
                                                ).getValue(
                                                        Long.class
                                                );

                                        if (oldPassing == null) {
                                            oldPassing = 35L;
                                        }

                                        long newPassing =
                                                Math.min(
                                                        oldPassing,
                                                        maxMarks
                                                );

                                        Map<String, Object>
                                                updates =
                                                new LinkedHashMap<>();

                                        updates.put(
                                                "name",
                                                newName
                                        );

                                        updates.put(
                                                "maxMarks",
                                                maxMarks
                                        );

                                        updates.put(
                                                "passingMarks",
                                                newPassing
                                        );

                                        child.getRef()
                                                .updateChildren(
                                                        updates
                                                )
                                                .addOnCompleteListener(
                                                        task -> {

                                                            if (task.isSuccessful()) {

                                                                subjects.set(
                                                                        index,
                                                                        newName
                                                                );

                                                                maxMarksList.set(
                                                                        index,
                                                                        maxMarks
                                                                );

                                                                passingMarksList.set(
                                                                        index,
                                                                        newPassing
                                                                );

                                                                setupSubjectFields();

                                                                toast(
                                                                        "Subject updated"
                                                                );

                                                            } else {

                                                                toast(
                                                                        "Update failed"
                                                                );
                                                            }
                                                        }
                                                );

                                        return;
                                    }
                                }

                                toast(
                                        "Subject not found"
                                );
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error) {

                                toast(
                                        "Update failed: "
                                                + error.getMessage()
                                );
                            }
                        }
                );
    }


    // =========================================================
    // REMOVE SUBJECT
    // =========================================================

    private void showRemoveSubjectDialog() {

        if (subjects.isEmpty()) {

            toast(
                    "No subjects available"
            );

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove Subject")
                .setItems(
                        subjects.toArray(
                                new String[0]
                        ),
                        (dialog, which) -> {

                            String selectedSubject =
                                    subjects.get(which);

                            confirmRemoveSubject(
                                    selectedSubject
                            );
                        }
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .show();
    }


    private void confirmRemoveSubject(
            String subjectName) {

        new AlertDialog.Builder(this)
                .setTitle("Remove Subject")
                .setMessage(
                        "Remove \""
                                + subjectName
                                + "\" from result form?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "REMOVE",
                        (dialog, which) ->
                                findAndDeleteSubject(
                                        subjectName
                                )
                )
                .show();
    }


    private void findAndDeleteSubject(
            String subjectName) {

        subjectsReference
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot) {

                                for (DataSnapshot child :
                                        snapshot.getChildren()) {

                                    String name =
                                            child.child("name")
                                                    .getValue(
                                                            String.class
                                                    );

                                    if (name != null &&
                                            name.equals(
                                                    subjectName
                                            )) {

                                        child.getRef()
                                                .removeValue()
                                                .addOnCompleteListener(
                                                        task -> {

                                                            if (task.isSuccessful()) {

                                                                int index =
                                                                        subjects.indexOf(
                                                                                subjectName
                                                                        );

                                                                if (index >= 0) {

                                                                    subjects.remove(
                                                                            index
                                                                    );

                                                                    maxMarksList.remove(
                                                                            index
                                                                    );

                                                                    passingMarksList.remove(
                                                                            index
                                                                    );
                                                                }

                                                                setupSubjectFields();

                                                                toast(
                                                                        "Subject removed"
                                                                );

                                                            } else {

                                                                toast(
                                                                        "Remove failed"
                                                                );
                                                            }
                                                        }
                                                );

                                        return;
                                    }
                                }

                                toast(
                                        "Subject not found"
                                );
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error) {

                                toast(
                                        "Remove failed: "
                                                + error.getMessage()
                                );
                            }
                        }
                );
    }


    // =========================================================
    // PASSING MARKS
    // =========================================================

    private void showPassingMarksDialog() {

        if (subjects.isEmpty()) {

            toast(
                    "No subjects available"
            );

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Subject")
                .setItems(
                        subjects.toArray(
                                new String[0]
                        ),
                        (dialog, which) ->
                                showPassingMarksInput(
                                        which
                                )
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .show();
    }


    private void showPassingMarksInput(
            int index) {

        EditText input =
                new EditText(this);

        input.setHint(
                "Passing marks"
        );

        input.setSingleLine(true);

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        input.setText(
                String.valueOf(
                        passingMarksList.get(index)
                )
        );

        input.setPadding(
                25,
                10,
                25,
                10
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Passing Marks - "
                                        + subjects.get(index)
                        )
                        .setView(input)
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "SAVE",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            Button saveButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            saveButton.setOnClickListener(v -> {

                String text =
                        input.getText()
                                .toString()
                                .trim();

                if (text.isEmpty()) {

                    input.setError(
                            "Enter passing marks"
                    );

                    return;
                }

                long passing;

                try {

                    passing =
                            Long.parseLong(
                                    text
                            );

                } catch (Exception e) {

                    input.setError(
                            "Invalid marks"
                    );

                    return;
                }

                long max =
                        maxMarksList.get(index);

                if (passing < 0 ||
                        passing > max) {

                    input.setError(
                            "Enter 0 to " + max
                    );

                    return;
                }

                updatePassingMarks(
                        index,
                        passing
                );

                dialog.dismiss();
            });
        });

        dialog.show();
    }


    private void updatePassingMarks(
            int index,
            long passingMarks) {

        String subjectName =
                subjects.get(index);

        subjectsReference
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot) {

                                for (DataSnapshot child :
                                        snapshot.getChildren()) {

                                    String name =
                                            child.child("name")
                                                    .getValue(
                                                            String.class
                                                    );

                                    if (name != null &&
                                            name.equals(
                                                    subjectName
                                            )) {

                                        child.getRef()
                                                .child(
                                                        "passingMarks"
                                                )
                                                .setValue(
                                                        passingMarks
                                                )
                                                .addOnCompleteListener(
                                                        task -> {

                                                            if (task.isSuccessful()) {

                                                                passingMarksList.set(
                                                                        index,
                                                                        passingMarks
                                                                );

                                                                setupSubjectFields();

                                                                toast(
                                                                        "Passing marks updated"
                                                                );

                                                            } else {

                                                                toast(
                                                                        "Could not update passing marks"
                                                                );
                                                            }
                                                        }
                                                );

                                        return;
                                    }
                                }

                                toast(
                                        "Subject not found"
                                );
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error) {

                                toast(
                                        "Update failed: "
                                                + error.getMessage()
                                );
                            }
                        }
                );
    }


    private boolean subjectExists(
            String name) {

        for (String subject : subjects) {

            if (subject.equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void openDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, day) ->
                                etDob.setText(
                                        String.format(
                                                Locale.getDefault(),
                                                "%02d/%02d/%04d",
                                                day,
                                                month + 1,
                                                year
                                        )
                                ),
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.getDatePicker()
                .setMaxDate(
                        System.currentTimeMillis()
                );

        dialog.show();
    }


    // =========================================================
    // SAVE / UPDATE RESULT
    // =========================================================

    private void saveResult() {

        String name =
                etName.getText()
                        .toString()
                        .trim();

        String roll =
                etRoll.getText()
                        .toString()
                        .trim();

        String dob =
                etDob.getText()
                        .toString()
                        .trim();

        String className =
                spClass.getSelectedItem()
                        .toString();

        String exam =
                etExam.getText()
                        .toString()
                        .trim();

        String remarks =
                etRemarks.getText()
                        .toString()
                        .trim();


        if (name.isEmpty()) {

            etName.setError(
                    "Enter student name"
            );

            etName.requestFocus();

            return;
        }


        if (roll.isEmpty()) {

            etRoll.setError(
                    "Enter roll number"
            );

            etRoll.requestFocus();

            return;
        }


        if (dob.isEmpty()) {

            etDob.setError(
                    "Select date of birth"
            );

            etDob.requestFocus();

            return;
        }


        if ("Select Class".equals(
                className
        )) {

            toast(
                    "Select class"
            );

            return;
        }


        if (exam.isEmpty()) {

            etExam.setError(
                    "Enter exam name"
            );

            etExam.requestFocus();

            return;
        }


        if (subjects.isEmpty()) {

            toast(
                    "Add at least one subject"
            );

            return;
        }


        LinkedHashMap<String, Long> marksMap =
                new LinkedHashMap<>();

        long total = 0;

        long maxTotal = 0;

        boolean allPassed = true;


        for (int i = 0;
             i < subjects.size();
             i++) {

            String value =
                    subjectInputs
                            .get(i)
                            .getText()
                            .toString()
                            .trim();


            if (value.isEmpty()) {
                continue;
            }


            try {

                double parsed =
                        Double.parseDouble(
                                value
                        );

                long maxMarks =
                        maxMarksList.get(i);

                long passingMarks =
                        passingMarksList.get(i);


                if (parsed < 0 ||
                        parsed > maxMarks ||
                        parsed != Math.floor(parsed)) {

                    subjectInputs
                            .get(i)
                            .setError(
                                    "Enter whole marks 0-"
                                            + maxMarks
                            );

                    subjectInputs
                            .get(i)
                            .requestFocus();

                    return;
                }


                long marks =
                        (long) parsed;


                marksMap.put(
                        subjects.get(i),
                        marks
                );


                total += marks;

                maxTotal += maxMarks;


                if (marks < passingMarks) {

                    allPassed = false;
                }


            } catch (NumberFormatException e) {

                subjectInputs
                        .get(i)
                        .setError(
                                "Invalid marks"
                        );

                subjectInputs
                        .get(i)
                        .requestFocus();

                return;
            }
        }


        if (marksMap.isEmpty()) {

            toast(
                    "Enter marks for at least one subject"
            );

            return;
        }


        double percentage =
                maxTotal == 0
                        ? 0
                        : (total * 100.0)
                          / maxTotal;


        String grade =
                calculateGrade(
                        percentage
                );


        String status =
                allPassed
                        ? "PASS"
                        : "FAIL";


        if (remarks.isEmpty()) {

            remarks =
                    status.equals("PASS")
                            ? "Good performance"
                            : "Needs improvement";
        }


        Map<String, Object> data =
                new LinkedHashMap<>();


        data.put(
                "name",
                name
        );

        data.put(
                "rollNo",
                roll
        );

        data.put(
                "dob",
                dob
        );

        data.put(
                "className",
                className
        );

        data.put(
                "examName",
                exam
        );

        data.put(
                "subjects",
                marksMap
        );

        data.put(
                "totalMarks",
                total
        );

        data.put(
                "maxMarks",
                maxTotal
        );

        data.put(
                "percentage",
                percentage
        );

        data.put(
                "grade",
                grade
        );

        data.put(
                "status",
                status
        );

        data.put(
                "remarks",
                remarks
        );

        /*
         * Publish checkbox:
         * checked = published
         * unchecked = draft
         */
        data.put(
                "published",
                cbPublish.isChecked()
        );


        data.put(
                "updatedAt",
                System.currentTimeMillis()
        );


        if (auth.getCurrentUser() != null) {

            data.put(
                    "uploadedBy",
                    auth.getCurrentUser()
                            .getUid()
            );
        }


        setButtonsEnabled(false);


        studentsReference
                .child(roll)
                .setValue(data)
                .addOnCompleteListener(
                        task -> {

                            setButtonsEnabled(true);


                            if (task.isSuccessful()) {

                                tvFormTitle.setText(
                                        "Result Saved / Updated"
                                );


                                if (cbPublish.isChecked()) {

                                    toast(
                                            "Result uploaded and published successfully"
                                    );

                                } else {

                                    toast(
                                            "Result saved as draft"
                                    );
                                }


                            } else {

                                toast(
                                        "Could not save result: "
                                                +
                                                (
                                                        task.getException()
                                                                == null
                                                                ? "Unknown error"
                                                                : task.getException()
                                                                .getMessage()
                                                )
                                );
                            }
                        }
                );
    }


    // =========================================================
    // SEARCH STUDENT
    // =========================================================

    private void searchStudent() {

        String roll =
                etRoll.getText()
                        .toString()
                        .trim();


        if (roll.isEmpty()) {

            etRoll.setError(
                    "Enter roll number first"
            );

            etRoll.requestFocus();

            return;
        }


        setButtonsEnabled(false);


        studentsReference
                .child(roll)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot) {

                                setButtonsEnabled(true);


                                if (!snapshot.exists()) {

                                    clearFieldsExceptRoll();

                                    toast(
                                            "Student not found. You can add a new result."
                                    );

                                    return;
                                }


                                loadStudent(snapshot);


                                toast(
                                        "Student result loaded"
                                );
                            }


                            @Override
                            public void onCancelled(
                                    DatabaseError error) {

                                setButtonsEnabled(true);

                                toast(
                                        "Search failed: "
                                                + error.getMessage()
                                );
                            }
                        }
                );
    }


    // =========================================================
    // LOAD STUDENT
    // =========================================================

    private void loadStudent(
            DataSnapshot snapshot) {

        etName.setText(
                getString(
                        snapshot,
                        "name"
                )
        );


        String savedRoll =
                getString(
                        snapshot,
                        "rollNo"
                );


        etRoll.setText(
                savedRoll.isEmpty()
                        ? snapshot.getKey()
                        : savedRoll
        );


        etDob.setText(
                getString(
                        snapshot,
                        "dob"
                )
        );


        etExam.setText(
                getString(
                        snapshot,
                        "examName"
                )
        );


        etRemarks.setText(
                getString(
                        snapshot,
                        "remarks"
                )
        );


        selectClass(
                getString(
                        snapshot,
                        "className"
                )
        );


        /*
         * IMPORTANT:
         * Firebase मधील actual publish status load करतो.
         */
        Boolean published =
                snapshot.child("published")
                        .getValue(
                                Boolean.class
                        );


        cbPublish.setChecked(
                published != null && published
        );


        DataSnapshot subjectSnapshot =
                snapshot.child(
                        "subjects"
                );


        for (int i = 0;
             i < subjects.size();
             i++) {

            Object value =
                    subjectSnapshot
                            .child(
                                    subjects.get(i)
                            )
                            .getValue();


            subjectInputs
                    .get(i)
                    .setText(
                            value == null
                                    ? ""
                                    : String.valueOf(
                                    value
                            )
                    );
        }


        tvFormTitle.setText(
                "Edit Student Result"
        );
    }


    // =========================================================
    // DELETE RESULT
    // =========================================================

    private void deleteStudent() {

        String roll =
                etRoll.getText()
                        .toString()
                        .trim();


        if (roll.isEmpty()) {

            toast(
                    "Enter roll number first"
            );

            return;
        }


        new AlertDialog.Builder(this)
                .setTitle(
                        "Delete Result"
                )
                .setMessage(
                        "Delete result for Roll No. "
                                + roll
                                + "?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "DELETE",
                        (dialog, which) -> {

                            studentsReference
                                    .child(roll)
                                    .removeValue()
                                    .addOnCompleteListener(
                                            task -> {

                                                if (task.isSuccessful()) {

                                                    clearForm();

                                                    toast(
                                                            "Student result deleted"
                                                    );

                                                } else {

                                                    toast(
                                                            "Delete failed"
                                                    );
                                                }
                                            }
                                    );
                        }
                )
                .show();
    }


    // =========================================================
    // CLEAR FORM
    // =========================================================

    private void clearForm() {

        etName.setText("");

        etRoll.setText("");

        etDob.setText("");

        etExam.setText("");

        etRemarks.setText("");

        spClass.setSelection(0);


        /*
         * New result default = DRAFT
         */
        cbPublish.setChecked(false);


        for (EditText input :
                subjectInputs) {

            input.setText("");
        }


        tvFormTitle.setText(
                "Upload Student Result"
        );
    }


    private void clearFieldsExceptRoll() {

        etName.setText("");

        etDob.setText("");

        etExam.setText("");

        etRemarks.setText("");

        spClass.setSelection(0);


        /*
         * New result default = DRAFT
         */
        cbPublish.setChecked(false);


        for (EditText input :
                subjectInputs) {

            input.setText("");
        }
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private void selectClass(
            String className) {

        for (int i = 0;
             i < spClass.getCount();
             i++) {

            if (spClass
                    .getItemAtPosition(i)
                    .toString()
                    .equalsIgnoreCase(
                            className
                    )) {

                spClass.setSelection(i);

                return;
            }
        }
    }


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


    private String calculateGrade(
            double percentage) {

        if (percentage >= 90)
            return "A+";

        if (percentage >= 80)
            return "A";

        if (percentage >= 70)
            return "B+";

        if (percentage >= 60)
            return "B";

        if (percentage >= 50)
            return "C";

        if (percentage >= 35)
            return "D";

        return "F";
    }


    private void setButtonsEnabled(
            boolean enabled) {

        btnSave.setEnabled(enabled);

        btnDelete.setEnabled(enabled);

        btnClear.setEnabled(enabled);

        btnSearch.setEnabled(enabled);

        btnManageSubjects.setEnabled(enabled);

        btnViewAllResults.setEnabled(enabled);
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    private void logoutTeacher() {

        auth.signOut();

        finish();
    }


    private void toast(
            String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}