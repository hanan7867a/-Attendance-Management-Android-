package com.example.attandence_managment.grades;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attandence_managment.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class StudentFetchAssignmentAdapter extends RecyclerView.Adapter<StudentFetchAssignmentAdapter.ViewHolder> {

    private final List<AssignmentFetchModel> assignmentList;
    private final Activity activity;
    private final OnUploadClickListener uploadListener;

    public interface OnUploadClickListener {
        void onUploadClicked(String assignmentId);
    }

    public StudentFetchAssignmentAdapter(Activity activity, List<AssignmentFetchModel> list, OnUploadClickListener listener) {
        this.activity = activity;
        this.assignmentList = list;
        this.uploadListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment_submission, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignmentFetchModel model = assignmentList.get(position);

        holder.title.setText(model.getTitle());
        holder.description.setText(model.getDescription() != null ? model.getDescription() : "No description");

        if (model.isSubmitted()) {
            holder.fileName.setText("Submitted Successfully ✓");
            holder.uploadBtn.setEnabled(false);
            holder.uploadBtn.setText("Submitted");
        } else {
            holder.fileName.setText("Not Submitted");
            holder.uploadBtn.setEnabled(true);
            holder.uploadBtn.setText("Upload");
        }

        // View/Download teacher's assignment file
        holder.downloadBtn.setOnClickListener(v -> {
            if (model.getUrl() != null && !model.getUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(model.getUrl()));
                activity.startActivity(intent);
            } else {
                Toast.makeText(activity, "No assignment file attached", Toast.LENGTH_SHORT).show();
            }
        });

        // Upload student's submission
        holder.uploadBtn.setOnClickListener(v -> {
            if (uploadListener != null) {
                uploadListener.onUploadClicked(model.getAssignmentId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, fileName;
        MaterialButton downloadBtn, uploadBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.assignment_title);
            description = itemView.findViewById(R.id.assignment_description);
            fileName = itemView.findViewById(R.id.submission_status);
            downloadBtn = itemView.findViewById(R.id.download_button);
            uploadBtn = itemView.findViewById(R.id.uploadFileBtn);
        }
    }
}