package com.example.studentresultapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TeacherLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference teachersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        etEmail = findViewById(R.id.etTeacherEmail);
        etPassword = findViewById(R.id.etTeacherPassword);

        btnLogin = findViewById(R.id.btnTeacherLogin);
        tvRegister = findViewById(R.id.tvTeacherRegister);

        mAuth = FirebaseAuth.getInstance();
        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");

        // Login
        btnLogin.setOnClickListener(v -> loginTeacher());

        // Register
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TeacherLoginActivity.this,
                    TeacherRegisterActivity.class
            );
            startActivity(intent);
        });
    }

    private void loginTeacher() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        if (mAuth.getCurrentUser() == null) {
                            showLoginError("Login failed");
                            return;
                        }

                        String uid = mAuth.getCurrentUser().getUid();

                        // Check teacher role
                        teachersRef.child(uid)
                                .addListenerForSingleValueEvent(
                                        new ValueEventListener() {

                                            @Override
                                            public void onDataChange(
                                                    DataSnapshot snapshot) {

                                                if (snapshot.exists()) {

                                                    String role = snapshot
                                                            .child("role")
                                                            .getValue(String.class);

                                                    if ("teacher".equalsIgnoreCase(role)) {

                                                        Toast.makeText(
                                                                TeacherLoginActivity.this,
                                                                "Login successful",
                                                                Toast.LENGTH_SHORT
                                                        ).show();

                                                        Intent intent =
                                                                new Intent(
                                                                        TeacherLoginActivity.this,
                                                                        TeacherDashboardActivity.class
                                                                );

                                                        startActivity(intent);
                                                        finish();

                                                    } else {

                                                        mAuth.signOut();
                                                        showLoginError(
                                                                "You are not registered as a teacher"
                                                        );
                                                    }
                                                } else {

                                                    mAuth.signOut();
                                                    showLoginError(
                                                            "Teacher account not found"
                                                    );
                                                }
                                            }

                                            @Override
                                            public void onCancelled(
                                                    DatabaseError error) {

                                                showLoginError(
                                                        "Database error: "
                                                                + error.getMessage()
                                                );
                                            }
                                        }
                                );

                    } else {

                        String message = "Login failed";

                        if (task.getException() != null) {
                            message = task.getException().getMessage();
                        }

                        showLoginError(message);
                    }
                });
    }

    private void showLoginError(String message) {

        btnLogin.setEnabled(true);
        btnLogin.setText("LOGIN");

        Toast.makeText(
                TeacherLoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
