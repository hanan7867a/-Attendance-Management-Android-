package com.example.attandence_managment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attandence_managment.teacher_side.TeacherDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private LinearLayout loginButton;
    private TextView forgotPasswordText;
    TextView btnSignup ;


    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        // ✅ If already logged in, skip login
//        if (auth.getCurrentUser() != null) {
//            startActivity(new Intent(this, Dashborad.class));
//            finish();
//            return;
//        }

        setContentView(R.layout.loginpage);
        initViews();
        setupListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginbtn);
        forgotPasswordText = findViewById(R.id.r76t3y8a28ml);
        btnSignup = findViewById(R.id.btnSignup);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> loginUser());
        btnSignup.setOnClickListener(v->{
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });


        forgotPasswordText.setOnClickListener(v ->
                Toast.makeText(this,
                        "Password recovery will be added later",
                        Toast.LENGTH_SHORT).show());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        loginButton.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        goToDashboard();
                    } else {
                        Toast.makeText(this,
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Login failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return false;
        }

        return true;
    }

    private void goToDashboard() {
        String userId = auth.getCurrentUser().getUid();

        // Fetch user profile to get role
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");

                        if (role == null) role = "student"; // default

                        Intent intent;
                        if (role.equalsIgnoreCase("teacher")) {
                            intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, Dashborad.class);
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to get user role: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

}
