package com.example.tictactoe.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tictactoe.R;
import com.example.tictactoe.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistroActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword;
    String name, email, password;
    private Button btnRegistro;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ProgressBar pbRegistro;
    private ScrollView formRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegistro = findViewById(R.id.btnRegistro);
        formRegistro = findViewById(R.id.formRegistro);
        pbRegistro = findViewById(R.id.pbRegistro);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        changeRegistroFormVisibility(true);
        eventos();
    }

    private void eventos() {
        btnRegistro.setOnClickListener(v -> {
            name = etName.getText().toString();
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();
            if (name.isEmpty())
                etName.setError("El nombre es obligatorio");
            else if (email.isEmpty())
                etEmail.setError("El email es obligatorio");
            else if (password.isEmpty())
                etPassword.setError("La contraseña es obligatoria.");
            else {
                //TODO realizar registro firebase
                createUser();
            }
        });
    }

    private void createUser() {
        changeRegistroFormVisibility(false);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(RegistroActivity.this,
                                "Error en el registro.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            //Almacenar info del usuario en FireStore

            User nuevoUsuario = new User(name, 0, 0);
            db.collection("users")
                    .document(user.getUid())
                    .set(nuevoUsuario)
                    .addOnSuccessListener(unused -> {
                        //Navegar a la siguiente pantalla de la app
                        finish();
                        Intent intent = new Intent(RegistroActivity.this, EncontrarJugadaActivity.class);
                        startActivity(intent);
                    });
        } else {
            changeRegistroFormVisibility(true);
            etPassword.setError("Nombre, email y/o contrasela incorrectos.");
            etPassword.requestFocus();
        }
    }

    private void changeRegistroFormVisibility(boolean showForm) {
        pbRegistro.setVisibility(showForm ? View.GONE : View.VISIBLE);
        formRegistro.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }
}