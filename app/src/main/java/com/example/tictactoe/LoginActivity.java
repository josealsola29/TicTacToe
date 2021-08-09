package com.example.tictactoe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnLoginRegistro;
    private ScrollView formLogin;
    private ProgressBar pbLogin;

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
        changeLoginFormVisibility(true);
        eventos();
    }

    private void eventos() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            if (email.isEmpty())
                etEmail.setError("El email es obligatorio");
            else if (password.isEmpty())
                etPassword.setError("La contraseña es obligatoria.");
            else {
                changeLoginFormVisibility(false);
            }

        });

        btnLoginRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    private void changeLoginFormVisibility(boolean showForm) {
        pbLogin.setVisibility(showForm ? View.GONE : View.VISIBLE);
        formLogin.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }
}