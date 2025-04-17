package com.midterm.truongtuyettrinh;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNotes;
    private FloatingActionButton btnAdd;
    private NoteAdapter adapter;
    private ArrayList<Note> noteList;
    private NoteDatabase noteDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("TODO App");

        rvNotes = findViewById(R.id.rv_notes);
        btnAdd = findViewById(R.id.btn_add);

        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        noteList = new ArrayList<>();
        adapter = new NoteAdapter(noteList, this);
        rvNotes.setAdapter(adapter);

        noteDatabase = NoteDatabase.getInstance(this);

        loadNotes();

        btnAdd.setOnClickListener(v -> showAddNoteDialog());

        adapter.setOnItemClickListener(note -> showEditDeleteDialog(note));

        adapter.setOnItemActionListener(new NoteAdapter.OnItemActionListener() {
            @Override
            public void onEdit(Note note) {
                showEditDeleteDialog(note);
            }

            @Override
            public void onDelete(Note note) {
                deleteNoteFromDb(note.getId());
            }
        });
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.addnew_note, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        EditText edtTitle = view.findViewById(R.id.edt_title);
        EditText edtContent = view.findViewById(R.id.edt_content);
        TextView tvDate = view.findViewById(R.id.tv_date);
        Button btnSave = view.findViewById(R.id.btn_save);

        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText("Date: " + currentDate);

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString();
            String content = edtContent.getText().toString();

            if (!title.isEmpty()) {
                saveNoteToDb(title, content, currentDate);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showEditDeleteDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.addnew_note, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        EditText edtTitle = view.findViewById(R.id.edt_title);
        EditText edtContent = view.findViewById(R.id.edt_content);
        TextView tvDate = view.findViewById(R.id.tv_date);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnDelete = view.findViewById(R.id.btn_delete);

        edtTitle.setText(note.getTitle());
        edtContent.setText(note.getContent());
        tvDate.setText("Date: " + note.getDate());

        btnSave.setText("Update");

        btnSave.setOnClickListener(v -> {
            String updatedTitle = edtTitle.getText().toString();
            String updatedContent = edtContent.getText().toString();

            if (!updatedTitle.isEmpty()) {
                updateNoteInDb(note.getId(), updatedTitle, updatedContent);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            deleteNoteFromDb(note.getId());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveNoteToDb(String title, String content, String date) {
        Note note = new Note(title, content, date);
        noteDatabase.noteDao().insert(note);
        loadNotes();
    }

    private void updateNoteInDb(int id, String title, String content) {
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        Note note = new Note(title, content, date);
        note.setId(id); // Quan trọng để update đúng note
        noteDatabase.noteDao().update(note);
        loadNotes();
    }

    private void deleteNoteFromDb(int id) {
        Note note = new Note("", "", ""); // Dummy values
        note.setId(id); // Quan trọng để Room biết xoá note nào
        noteDatabase.noteDao().delete(note);
        loadNotes();
    }


    private void loadNotes() {
        noteList.clear();
        noteList.addAll(noteDatabase.noteDao().getAllNotes());
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });

        return true;
    }

    private void filterNotes(String query) {
        ArrayList<Note> filteredList = new ArrayList<>();
        for (Note note : noteList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    note.getContent().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(note);
            }
        }
        adapter.updateList(filteredList);
    }
}
