package com.midterm.truongtuyettrinh;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.midterm.truongtuyettrinh.Note;
import com.midterm.truongtuyettrinh.NoteAdapter;
import com.midterm.truongtuyettrinh.NoteDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {



    private RecyclerView rvNotes;
    private Button btnSave,btnDelete, btnUpdate;;
    private FloatingActionButton btnAdd;
    private NoteAdapter adapter;
    private ArrayList<Note> noteList;
    private NoteDatabaseHelper dbHelper;
    private EditText edtTitle, edtContent;
    private TextView tvDate;

    private String noteId;
    private String currentDate;


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

        dbHelper = new NoteDatabaseHelper(this);
        loadNotes();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNoteDialog();
            }
        });
        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                showEditDeleteDialog(note);
            }

        });

        adapter.setOnItemActionListener(new NoteAdapter.OnItemActionListener() {
            @Override
            public void onEdit(Note note) {
                showEditDeleteDialog(note); // Dùng lại dialog đã có
            }

            @Override
            public void onDelete(Note note) {
                deleteNoteFromDb(note.getId());
            }
        });

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
        Button btnDelete = view.findViewById(R.id.btn_delete); // 👉 THÊM DÒNG NÀY

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

        btnDelete.setOnClickListener(v -> { // 👉 THAY THẾ CHO dialog.setButton
            deleteNoteFromDb(note.getId());
            dialog.dismiss();
        });

        dialog.show();
    }


    private void updateNoteInDb(String id, String title, String content) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        db.update("notes", values, "id=?", new String[]{id});
        loadNotes();
    }

    private void deleteNoteFromDb(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("notes", "id=?", new String[]{id});
        loadNotes();
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

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edtTitle.getText().toString();
                String content = edtContent.getText().toString();
                String date = currentDate;

                if (!title.isEmpty()) {
                    saveNoteToDb(title, content, date);
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }



    private void saveNoteToDb(String title, String content, String date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("date", date);
        db.insert("notes", null, values);
        loadNotes();
    }

    private void loadNotes() {
        noteList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("notes", null, null, null, null, null, "id DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                noteList.add(new Note(id, title, content, date));
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.bar_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Khi người dùng nhấn nút tìm kiếm
                filterNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Khi người dùng đang gõ
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