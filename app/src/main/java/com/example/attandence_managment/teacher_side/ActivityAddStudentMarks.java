package com.example.attandence_managment.teacher_side;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for adding marks for students.
 */
public class ActivityAddStudentMarks extends AppCompatActivity {

    private static final String TAG = "AddStudentMarks";

    private AutoCompleteTextView typeSpinner;
    private TextInputEditText obtainedEditText, assignmentEditText;
    private MaterialButton saveBtn;
    private ImageView teacherImage;

    private FirebaseFirestore db;
    private String courseId;
    private String teacherImgUrl;

    private final String[] types = {"Assignment", "Quiz", "Mid Term", "Final Exam"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student_marks);

        // Get data from intent
        courseId = getIntent().getStringExtra("courseId");
        teacherImgUrl = getIntent().getStringExtra("image");

        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinner();
        setupListeners();
        loadTeacherImage();
    }

    /**
     * Initialize UI components
     */
    private void initViews() {
        typeSpinner = findViewById(R.id.type_spinner);
        obtainedEditText = findViewById(R.id.obtained_edit);
        assignmentEditText = findViewById(R.id.assignment_edit);
        saveBtn = findViewById(R.id.save_button);
        teacherImage = findViewById(R.id.teacherimage);
    }

    /**
     * Setup marks type dropdown
     */
    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                types
        );
        typeSpinner.setAdapter(adapter);
    }

    /**
     * Setup button click listeners
     */
    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveMarks());
    }

    /**
     * Load teacher image using Glide
     */
    private void loadTeacherImage() {
        if (teacherImgUrl != null && !teacherImgUrl.isEmpty()) {
            Glide.with(this)
                    .load(teacherImgUrl)
                    .placeholder(R.drawable.profilepicture)
                    .error(R.drawable.profilepicture)
                    .circleCrop()
                    .into(teacherImage);
        } else {
            teacherImage.setImageResource(R.drawable.profilepicture);
        }
    }

    /**
     * Save marks in Firestore
     */
    private void saveMarks() {
        String type = typeSpinner.getText().toString().trim();
        String marksStr = obtainedEditText.getText().toString().trim();
        String numberStr = assignmentEditText.getText().toString().trim();

        if (type.isEmpty() || marksStr.isEmpty() || numberStr.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int marks;
        try {
            marks = Integer.parseInt(marksStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid marks entered", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine Firestore document, map field, and key prefix based on type
        String docName, mapFieldName, keyPrefix;
        switch (type) {
            case "Assignment":
                docName = "assignments";
                mapFieldName = "assignmentsmarks";
                keyPrefix = "assignment";
                break;
            case "Quiz":
                docName = "quizzes";
                mapFieldName = "quizzesmarks";
                keyPrefix = "quiz";
                break;
            case "Mid Term":
                docName = "mids";
                mapFieldName = "midsmarks";
                keyPrefix = "question";
                break;
            case "Final Exam":
                docName = "finals";
                mapFieldName = "finalmarks";
                keyPrefix = "question";
                break;
            default:
                Toast.makeText(this, "Invalid type", Toast.LENGTH_SHORT).show();
                return;
        }

        String key = keyPrefix + numberStr; // e.g., quiz1, assignment2, question3

        saveMarksTransaction(docName, mapFieldName, key, marks);
    }

    /**
     * Save marks atomically using Firestore transaction
     */
    private void saveMarksTransaction(String docName, String mapFieldName, String key, int marks) {
        DocumentReference docRef = db.collection("courses")
                .document(courseId)
                .collection("marks")
                .document(docName);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    DocumentSnapshot snapshot = transaction.get(docRef);

                    Map<String, Object> marksMap = (Map<String, Object>) snapshot.get(mapFieldName);
                    if (marksMap == null) {
                        marksMap = new HashMap<>();
                    }

                    marksMap.put(key, marks);
                    transaction.set(docRef, Collections.singletonMap(mapFieldName, marksMap), SetOptions.merge());

                    return null;
                }).addOnSuccessListener(aVoid -> Toast.makeText(this, "Marks saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving marks", e);
                    Toast.makeText(this, "Error saving marks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
