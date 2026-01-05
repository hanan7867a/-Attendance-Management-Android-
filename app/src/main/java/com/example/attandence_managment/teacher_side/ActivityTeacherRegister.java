package com.example.attandence_managment.teacher_side;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.R;
import com.example.attandence_managment.teacher_side.TeacherUser;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ActivityTeacherRegister extends AppCompatActivity {

    private ShapeableImageView profilePicImageView;
    private TextView nameTextView, roleTextView;
    private TextView emailTextView, idTextView;
    private MaterialAutoCompleteTextView courseAutoCompleteTextView;
    private MaterialButton addCourseButton;
    private RecyclerView coursesRecyclerView;
    private TextView noCoursesTextView;
    private FloatingActionButton saveFab;

    private Uri selectedImageUri;
    private String uploadedImageUrl = null; // Will hold URL from your API after upload
    private List<String> enrolledCourses = new ArrayList<>();
    private CoursesAdapter coursesAdapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    // Replace with your actual available courses (fetch from your API or Firestore courses collection)
    private final List<String> availableCourses = Arrays.asList(
            "CS101-A", "CS102-A", "ENG102-C", "MATH201-B", "PHY101-A", "CHEM105"
    );

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            // Show selected image immediately using Glide
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .into(profilePicImageView);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_register); // Your provided layout

        initFirebase();
        initViews();
        setupToolbar();
        setupCourseDropdown();
        setupRecyclerView();
        loadOrPrepareUserData();

        saveFab.setOnClickListener(v -> saveTeacherProfile());
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();
    }

    private void initViews() {
        profilePicImageView = findViewById(R.id.profilePicImageView);
        nameTextView = findViewById(R.id.nameTextView);
        roleTextView = findViewById(R.id.roleTextView);
        emailTextView = findViewById(R.id.emailTextView);
        idTextView = findViewById(R.id.idTextView);

        courseAutoCompleteTextView = findViewById(R.id.courseAutoCompleteTextView);
        addCourseButton = findViewById(R.id.addCourseButton);
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        noCoursesTextView = findViewById(R.id.noCoursesTextView);
        saveFab = findViewById(R.id.editFab);

        // Click handlers for editing
        profilePicImageView.setOnClickListener(v -> openImagePicker());
        nameTextView.setOnClickListener(v -> editName());
        emailTextView.setOnClickListener(v -> editField("Email", emailTextView));
        idTextView.setOnClickListener(v -> editField("University ID", idTextView));

        addCourseButton.setOnClickListener(v -> addSelectedCourse());
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        getSupportActionBar().setTitle("Complete Your Profile");
    }

    private void setupCourseDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, availableCourses);
        courseAutoCompleteTextView.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        coursesAdapter = new CoursesAdapter(enrolledCourses, this::removeCourse);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        coursesRecyclerView.setAdapter(coursesAdapter);
    }

    private void loadOrPrepareUserData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        TeacherUser teacher = documentSnapshot.toObject(TeacherUser.class);
                        if (teacher != null) {
                            populateFields(teacher);
                        }
                    } else {
                        // First time registration - set defaults
                        roleTextView.setText("Teacher");
                        Glide.with(this)
                                .load("https://wallpapers.com/images/hd/professional-profile-pictures-1080-x-1080-460wjhrkbwdcp1ig.jpg")
                                .circleCrop()
                                .into(profilePicImageView);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void populateFields(TeacherUser teacher) {
        nameTextView.setText(teacher.getName() != null ? teacher.getName() : "");
        emailTextView.setText(teacher.getEmail() != null ? teacher.getEmail() : "");
        idTextView.setText(teacher.getStudentId() != null ? teacher.getStudentId() : "");

        Glide.with(this)
                .load(teacher.getProfilePic())
                .placeholder(R.drawable.profile_circle)
                .circleCrop()
                .into(profilePicImageView);

        enrolledCourses.clear();
        if (teacher.getEnrolledCourses() != null) {
            enrolledCourses.addAll(teacher.getEnrolledCourses());
        }
        coursesAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void editName() {
        TextInputEditText input = new TextInputEditText(this);
        input.setText(nameTextView.getText());
        input.setHint("Full Name");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        nameTextView.setText(newName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editField(String title, TextView textView) {
        TextInputEditText input = new TextInputEditText(this);
        input.setText(textView.getText());
        input.setHint(title);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit " + title)
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        textView.setText(text);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addSelectedCourse() {
        String course = courseAutoCompleteTextView.getText().toString().trim();
        if (!course.isEmpty() && availableCourses.contains(course)) {
            if (!enrolledCourses.contains(course)) {
                enrolledCourses.add(course);
                coursesAdapter.notifyItemInserted(enrolledCourses.size() - 1);
                updateEmptyState();
                courseAutoCompleteTextView.setText("");
            } else {
                Toast.makeText(this, "Course already added", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select a valid course", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCourse(String course) {
        int position = enrolledCourses.indexOf(course);
        if (position != -1) {
            enrolledCourses.remove(position);
            coursesAdapter.notifyItemRemoved(position);
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        noCoursesTextView.setVisibility(enrolledCourses.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // =============================================================================
    // MAIN SAVE LOGIC - CALL YOUR API TO UPLOAD IMAGE FIRST, THEN SAVE TO FIRESTORE
    // =============================================================================
    private void saveTeacherProfile() {
        String name = nameTextView.getText().toString().trim();
        String email = emailTextView.getText().toString().trim();
        String teacherId = idTextView.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || teacherId.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_LONG).show();
            return;
        }

        // If user selected a new image → upload it via your API first
        if (selectedImageUri != null) {
            uploadImageToYourApiAndSave(name, email, teacherId);
        } else {
            // No new image → use existing or default
            saveToFirestore(name, email, teacherId, null);
        }
    }

    private void uploadImageToYourApiAndSave(String name, String email, String teacherId) {
        // TODO: Replace this with your actual image upload API call
        // Example using Retrofit/Volley/OkHttp - here using a placeholder method

        uploadImageViaYourApi(selectedImageUri, new ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                uploadedImageUrl = imageUrl;
                saveToFirestore(name, email, teacherId, imageUrl);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ActivityTeacherRegister.this,
                        "Image upload failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Placeholder for your API call
    private void uploadImageViaYourApi(Uri imageUri, ImageUploadCallback callback) {
        // Implement your API upload logic here (Retrofit, Volley, etc.)
        // On success → callback.onSuccess(returnedUrl)
        // On failure → callback.onFailure("message")
    }

    private void saveToFirestore(String name, String email, String teacherId, String profilePicUrl) {
        TeacherUser teacher = new TeacherUser();
        teacher.setName(name);
        teacher.setEmail(email);
        teacher.setStudentId(teacherId);
        teacher.setRole("teacher");
        teacher.setEnrolledCourses(new ArrayList<>(enrolledCourses));

        // Use uploaded URL if available, otherwise keep current (loaded from DB)
        if (profilePicUrl != null) {
            teacher.setProfilePic(profilePicUrl);
        } else {
            // If no new upload, we keep whatever was already loaded (or default)
            // You can store current URL in a field if needed
        }

        db.collection("users")
                .document(userId)
                .set(teacher)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back or to dashboard
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
    // Add this method in any Activity (e.g., ActivityTeacherRegister or a new AdminActivity)



    // Callback interface for your API
    interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }
}