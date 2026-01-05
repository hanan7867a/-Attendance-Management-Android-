package com.example.attandence_managment.teacher_side;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.attandence_managment.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateAssignment extends AppCompatActivity {

    private static final String TAG = "CreateAssignment";
    private static final int PICK_FILE_REQUEST = 101;

    private EditText titleEditText, descEditText;
    private MaterialButton submitBtn;
    private CardView uploadFileBtn;
    private ImageView profileImage;

    private Uri selectedFileUri;
    private String uploadedFileUrl;
    private String teacherImageUrl;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_assignment);

        initViews();
        db = FirebaseFirestore.getInstance();

        teacherImageUrl = getIntent().getStringExtra("image");
        loadImage(profileImage, teacherImageUrl);

        uploadFileBtn.setOnClickListener(v -> pickFile());
        submitBtn.setOnClickListener(v -> submitAssignment());
    }

    private void initViews() {
        titleEditText = findViewById(R.id.etTitle);
        descEditText = findViewById(R.id.etDescription);
        submitBtn = findViewById(R.id.submitAssignmentBtn);
        uploadFileBtn = findViewById(R.id.uploadFileBtn);
        profileImage = findViewById(R.id.teacherimage);
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        });
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            Toast.makeText(this, "File Selected", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "File selected: " + selectedFileUri);
        }
    }

    private void submitAssignment() {
        String titleText = titleEditText.getText().toString().trim();

        if (titleText.isEmpty() || selectedFileUri == null) {
            Toast.makeText(this, "Title or file missing", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchImageKitAuth();
    }

    private void fetchImageKitAuth() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://springbootapi-production-cda6.up.railway.app/api/imagekit/auth")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ImageKit auth failed", e);
                runOnUiThread(() -> Toast.makeText(CreateAssignment.this, "Auth API failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Auth API error: " + response.code());
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    String token = json.getString("token");
                    String signature = json.getString("signature");
                    long expire = json.getLong("expire");

                    uploadFileToImageKit(token, signature, expire);
                } catch (Exception e) {
                    Log.e(TAG, "Auth JSON parse error", e);
                }
            }
        });
    }

    private void uploadFileToImageKit(String token, String signature, long expire) {
        try {
            byte[] fileBytes = readBytesFromUri(selectedFileUri);
            String base64File = Base64.encodeToString(fileBytes, Base64.NO_WRAP);

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", base64File)
                    .addFormDataPart("fileName", "assignment_" + System.currentTimeMillis())
                    .addFormDataPart("publicKey", "public_Es0vI4aIZOpz+Pa4KLc0GI6jVcA=")
                    .addFormDataPart("signature", signature)
                    .addFormDataPart("token", token)
                    .addFormDataPart("expire", String.valueOf(expire))
                    .addFormDataPart("useUniqueFileName", "true")
                    .addFormDataPart("folder", "/assignments")
                    .build();

            Request request = new Request.Builder()
                    .url("https://upload.imagekit.io/api/v1/files/upload")
                    .post(requestBody)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Upload failed", e);
                    runOnUiThread(() -> Toast.makeText(CreateAssignment.this, "Upload failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Upload error: " + response.body().string());
                        return;
                    }

                    JSONObject json = null;
                    try {
                        json = new JSONObject(response.body().string());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        uploadedFileUrl = json.getString("url");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    saveAssignmentInFirestore(uploadedFileUrl);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "File read/upload error", e);
        }
    }

    private void saveAssignmentInFirestore(String fileUrl) {
        String courseSection = "CS101-A"; // dynamic later
        String teacherId = "teacher987";  // dynamic later

        AssignmentModel assignment = new AssignmentModel(
                titleEditText.getText().toString().trim(),
                descEditText.getText().toString().trim(),
                10, // totalMarks, can be dynamic
                fileUrl,
                teacherId,
                System.currentTimeMillis()
        );

        db.collection("courses")
                .document(courseSection)
                .collection("assignments")
                .add(assignment)
                .addOnSuccessListener(docRef -> runOnUiThread(() -> {
                    Toast.makeText(CreateAssignment.this, "Assignment Created Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    Log.e(TAG, "Firestore save failed", e);
                    Toast.makeText(CreateAssignment.this, "Failed to save assignment", Toast.LENGTH_SHORT).show();
                }));
    }

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

    private void loadImage(ImageView imageView, String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.profilepicture)
                .error(R.drawable.profilepicture)
                .circleCrop()
                .into(imageView);
    }
}
