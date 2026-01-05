package com.example.attandence_managment.grades;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class GradeBookOptionsActivity extends AppCompatActivity {

    LinearLayout assignmentCard, quizCard, submissionCard, midsCard, finalCard;

    private String studentId;
    private String subjectId;
    String name,emial, profilePic;
    ImageView profileImage;
    TextView studentName,studentEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grade_book_options);
        name=getIntent().getStringExtra("name");
        emial=getIntent().getStringExtra("email");
        profilePic =getIntent().getStringExtra("image");
        profileImage=findViewById(R.id.profile_image);





        // Receive subjectId from intent
        subjectId = getIntent().getStringExtra("subject_id");
        if (subjectId == null) {
            Toast.makeText(this, "Subject not selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize cards
        assignmentCard = findViewById(R.id.assignment_card);
        quizCard = findViewById(R.id.quiz_card);
        midsCard = findViewById(R.id.mids_card);
        finalCard = findViewById(R.id.final_card);
        submissionCard = findViewById(R.id.submission_card);
        studentName=findViewById(R.id.name_of_student);
        studentEmail=findViewById(R.id.std_email);

 studentName.setText(name);
 studentEmail.setText(emial);
        Glide.with(this)
                .load(profilePic)
                .placeholder(R.drawable.profilepicture)
                .circleCrop()
                .into(profileImage);

        // Get studentId from current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            studentId = doc.getString("studentId");
                        } else {
                            Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Set click listeners
        assignmentCard.setOnClickListener(v -> openMarks("assignments"));
        quizCard.setOnClickListener(v -> openMarks("quizzes"));
        midsCard.setOnClickListener(v -> openMarks("mids"));
        finalCard.setOnClickListener(v -> openMarks("finals"));

        submissionCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubmissionPageActivity.class);
            intent.putExtra("subject_id", subjectId);
            startActivity(intent);
        });
    }

    private void openMarks(String fieldName) {
        if (studentId == null) {
            Toast.makeText(this, "Loading student data...", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MarksResultActivity.class);
        intent.putExtra("studentId", studentId);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("fieldName", fieldName);
        intent.putExtra("name", name);
        intent.putExtra("email", emial);
        intent.putExtra("image", profilePic);

        startActivity(intent);
    }
}
