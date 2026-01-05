package com.example.attandence_managment.teacher_side;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attandence_managment.R;

public class UploadAssignmentActivity extends AppCompatActivity {

    EditText etTitle, etDescription;
    LinearLayout uploadFileBtn, submitAssignmentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_assignment);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        uploadFileBtn = findViewById(R.id.uploadFileBtn);
        submitAssignmentBtn = findViewById(R.id.submitAssignmentBtn);

        uploadFileBtn.setOnClickListener(v -> {
            // For now just show Toast
            Toast.makeText(this, "Upload file clicked", Toast.LENGTH_SHORT).show();
        });

        submitAssignmentBtn.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();

            if(title.isEmpty() || desc.isEmpty()){
                Toast.makeText(this, "Please enter title and description", Toast.LENGTH_SHORT).show();
                return;
            }

            // For now, just show Toast
            Toast.makeText(this, "Assignment submitted:\n" + title, Toast.LENGTH_SHORT).show();

            // Here you can add your logic to upload to server/database
        });
    }
}
