package com.example.tictactoe.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tictactoe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnLoginRegistro;
    private ScrollView formLogin;
    private ProgressBar pbLogin;
    private FirebaseAuth firebaseAuth;
    private String email, password;
    private boolean trylogin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        formLogin = findViewById(R.id.formLogin);
        pbLogin = findViewById(R.id.pbLogin);
        btnLoginRegistro = findViewById(R.id.btnLoginRegistro);

        firebaseAuth = FirebaseAuth.getInstance();
        changeLoginFormVisibility(true);
        eventos();
    }

    private void eventos() {
        btnLogin.setOnClickListener(v -> {
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();
            if (email.isEmpty())
                etEmail.setError("El email es obligatorio");
            else if (password.isEmpty())
                etPassword.setError("La contraseña es obligatoria.");
            else {
                changeLoginFormVisibility(false);
                loginUser();
            }

        });

        btnLoginRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    trylogin = true;
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.e("TAG", "Sigin Error.", task.getException());
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            //Almacenar info del usuario en FireStore
            //TODO
            //Navegar a la siguiente pantalla de la app
            Intent intent = new Intent(LoginActivity.this, EncontrarJugadaActivity.class);
            startActivity(intent);
        } else {
            changeLoginFormVisibility(true);
            if (trylogin) {
                etPassword.setError("Nombre, email y/o contrasela incorrectos.");
                etPassword.requestFocus();
            }
        }
    }

    private void changeLoginFormVisibility(boolean showForm) {
        pbLogin.setVisibility(showForm ? View.GONE : View.VISIBLE);
        formLogin.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }
}