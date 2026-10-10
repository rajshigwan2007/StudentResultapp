package com.example.studentresultapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class TeacherRegisterActivity extends AppCompatActivity {

    private EditText etTeacherName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference teachersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_register);

        // Find views
        etTeacherName = findViewById(R.id.etTeacherName);
        etEmail = findViewById(R.id.etTeacherEmail);
        etPassword = findViewById(R.id.etTeacherPassword);
        etConfirmPassword = findViewById(R.id.etTeacherConfirmPassword);

        btnRegister = findViewById(R.id.btnTeacherRegister);
        tvLogin = findViewById(R.id.tvTeacherLogin);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");

        // Register button
        btnRegister.setOnClickListener(v -> registerTeacher());

        // Already registered? Login
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TeacherRegisterActivity.this,
                    TeacherLoginActivity.class
            );
            startActivity(intent);
            finish();
        });
    }

    private void registerTeacher() {

        String name = etTeacherName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etTeacherName.setError("Enter teacher name");
            etTeacherName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Disable button while registering
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        // Create Firebase Authentication account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        if (mAuth.getCurrentUser() == null) {
                            btnRegister.setEnabled(true);
                            btnRegister.setText("Register");
                            Toast.makeText(
                                    TeacherRegisterActivity.this,
                                    "Registration failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        String uid = mAuth.getCurrentUser().getUid();

                        // Teacher profile
                        Map<String, Object> teacherData = new HashMap<>();
                        teacherData.put("name", name);
                        teacherData.put("email", email);
                        teacherData.put("role", "teacher");

                        // Save teacher data in Realtime Database
                        teachersRef.child(uid)
                                .setValue(teacherData)
                                .addOnCompleteListener(databaseTask -> {

                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Register");

                                    if (databaseTask.isSuccessful()) {

                                        Toast.makeText(
                                                TeacherRegisterActivity.this,
                                                "Teacher registered successfully",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        // Go to Teacher Login
                                        Intent intent = new Intent(
                                                TeacherRegisterActivity.this,
                                                TeacherLoginActivity.class
                                        );

                                        // Remove register screen from back stack
                                        intent.addFlags(
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                        );

                                        startActivity(intent);
                                        finish();

                                    } else {

                                        // If database save fails, remove auth account
                                        if (mAuth.getCurrentUser() != null) {
                                            mAuth.getCurrentUser().delete();
                                        }

                                        Toast.makeText(
                                                TeacherRegisterActivity.this,
                                                "Failed to save teacher profile: "
                                                        + databaseTask.getException(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });

                    } else {

                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");

                        String errorMessage = "Registration failed";

                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }

                        Toast.makeText(
                                TeacherRegisterActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
