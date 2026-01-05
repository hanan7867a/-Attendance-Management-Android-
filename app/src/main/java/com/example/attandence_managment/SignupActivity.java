package com.example.attandence_managment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etStudentId;
    private Button btnRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etStudentId = findViewById(R.id.etStudentId);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        List<String> selectedCourses = getSelectedCourses();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        if(selectedCourses.isEmpty()) {
            Toast.makeText(this, "Select at least one course", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = auth.getCurrentUser().getUid();

                    // Save user profile
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("email", email);
                    userMap.put("name", name);
                    userMap.put("role", "student");
                    userMap.put("studentId", studentId);
                    userMap.put("profilePic",
                            "https://wallpapers.com/images/hd/professional-profile-pictures-1080-x-1080-460wjhrkbwdcp1ig.jpg");
                    userMap.put("enrolledCourses", selectedCourses);

                    db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener(aVoid -> {
                                // Enroll student in each course
                                enrollStudentInCourses(studentId, selectedCourses);
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, Dashborad.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private List<String> getSelectedCourses() {
        List<String> courses = new ArrayList<>();
        if(((CheckBox)findViewById(R.id.cs101)).isChecked()) courses.add("CS101-A");
        if(((CheckBox)findViewById(R.id.mth201)).isChecked()) courses.add("MTH201-B");
        if(((CheckBox)findViewById(R.id.phy111)).isChecked()) courses.add("PHY111-A");
        if(((CheckBox)findViewById(R.id.eng102)).isChecked()) courses.add("ENG102-C");
        if(((CheckBox)findViewById(R.id.se305)).isChecked()) courses.add("SE305-B");
        return courses;
    }

    private void enrollStudentInCourses(String studentId, List<String> courses) {
        for(String courseCode : courses) {
            db.collection("courses").document(courseCode)
                    .update("students", FieldValue.arrayUnion(studentId))
                    .addOnSuccessListener(aVoid -> Log.d("ENROLL", "Student added to " + courseCode))
                    .addOnFailureListener(e -> Log.e("ENROLL_ERROR", e.getMessage()));
        }
    }
}
