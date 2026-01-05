package com.example.attandence_managment.grades;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attandence_managment.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SubmissionPageActivity extends AppCompatActivity {

    private static final String TAG = "SubmissionPageActivity";
    private static final int PICK_FILE_REQUEST = 101;

    RecyclerView recyclerView;
    StudentFetchAssignmentAdapter adapter;
    List<AssignmentFetchModel> assignmentList;
    FirebaseFirestore firestore;

    String courseId;
    Uri selectedFileUri;
    String currentAssignmentId; // Track which assignment the student is uploading for

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_page);

        firestore = FirebaseFirestore.getInstance();
        courseId = getIntent().getStringExtra("courseId");

        if (courseId == null || courseId.isEmpty()) {
            Toast.makeText(this, "No course selected!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.assignmentsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        assignmentList = new ArrayList<>();
        adapter = new StudentFetchAssignmentAdapter(this, assignmentList, this::onUploadClicked);
        recyclerView.setAdapter(adapter);

        loadAssignments();
    }

    // Called from Adapter when upload button clicked
    private void onUploadClicked(String assignmentId) {
        currentAssignmentId = assignmentId;
        pickFile();
    }

    // ================= FILE PICK =================
    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimetypes = {"application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                Toast.makeText(this, "File selected, uploading...", Toast.LENGTH_SHORT).show();
                uploadFileToImageKit();
            }
        }
    }

    // ================= UPLOAD FILE TO IMAGEKIT =================
    private void uploadFileToImageKit() {
        if (selectedFileUri == null) return;

        new Thread(() -> {
            try {
                byte[] fileBytes = readBytesFromUri(selectedFileUri);
                String base64File = Base64.encodeToString(fileBytes, Base64.NO_WRAP);

                OkHttpClient client = new OkHttpClient();

                // Get ImageKit auth token from backend
                Request authRequest = new Request.Builder()
                        .url("https://springbootapi-production-cda6.up.railway.app/api/imagekit/auth")
                        .get()
                        .build();

                client.newCall(authRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Auth API failed", e);
                        runOnUiThread(() -> Toast.makeText(SubmissionPageActivity.this, "Auth API failed", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) return;

                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String token = json.getString("token");
                            String signature = json.getString("signature");
                            long expire = json.getLong("expire");

                            MultipartBody requestBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("file", base64File)
                                    .addFormDataPart("fileName", "submission_" + System.currentTimeMillis())
                                    .addFormDataPart("publicKey", "public_Es0vI4aIZOpz+Pa4KLc0GI6jVcA=")
                                    .addFormDataPart("signature", signature)
                                    .addFormDataPart("token", token)
                                    .addFormDataPart("expire", String.valueOf(expire))
                                    .addFormDataPart("useUniqueFileName", "true")
                                    .addFormDataPart("folder", "/submissions/" + currentAssignmentId)
                                    .build();

                            Request uploadRequest = new Request.Builder()
                                    .url("https://upload.imagekit.io/api/v1/files/upload")
                                    .post(requestBody)
                                    .build();

                            client.newCall(uploadRequest).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e(TAG, "Upload failed", e);
                                    runOnUiThread(() -> Toast.makeText(SubmissionPageActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String responseBody = response.body() != null ? response.body().string() : "";
                                    if (!response.isSuccessful()) {
                                        Log.e(TAG, "Upload error: " + responseBody);
                                        runOnUiThread(() -> Toast.makeText(SubmissionPageActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
                                        return;
                                    }
                                    runOnUiThread(() -> Toast.makeText(SubmissionPageActivity.this, "File uploaded successfully to ImageKit!", Toast.LENGTH_SHORT).show());
                                }
                            });

                        } catch (Exception e) {
                            Log.e(TAG, "Auth JSON parse error", e);
                            runOnUiThread(() -> Toast.makeText(SubmissionPageActivity.this, "Upload failed: Auth error", Toast.LENGTH_SHORT).show());
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "File read error", e);
                runOnUiThread(() -> Toast.makeText(SubmissionPageActivity.this, "Failed to read file", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ================= FILE READ HELPER =================
    private byte[] readBytesFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        inputStream.close();
        return buffer.toByteArray();
    }

    // ================= LOAD ASSIGNMENTS =================
    private void loadAssignments() {
        firestore.collection("courses")
                .document(courseId)
                .collection("assignments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    assignmentList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        AssignmentFetchModel model = doc.toObject(AssignmentFetchModel.class);
                        if (model != null) assignmentList.add(model);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load assignments", Toast.LENGTH_SHORT).show());
    }
}
