package com.midterm.truongtuyettrinh;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private ArrayList<Note> noteList;
    private OnItemClickListener clickListener;
    private OnItemActionListener actionListener;

    public NoteAdapter(ArrayList<Note> noteList, MainActivity mainActivity) {
        this.noteList = noteList;
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public interface OnItemActionListener {
        void onEdit(Note note);
        void onDelete(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());
        holder.tvDate.setText(note.getDate());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(note);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEdit(note);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDelete(note);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void updateList(ArrayList<Note> newList) {
        this.noteList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        TextView btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
