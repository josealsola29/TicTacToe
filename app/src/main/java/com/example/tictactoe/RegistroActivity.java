package com.example.tictactoe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword;
    private Button btnRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegistro = findViewById(R.id.btnRegistro);
        eventos();
    }

    private void eventos() {
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                if(name.isEmpty())
                    etName.setError("El nombre es obligatorio");
                else if (email.isEmpty())
                    etEmail.setError("El email es obligatorio");
                else if (password.isEmpty())
                    etPassword.setError("La contraseña es obligatoria.");
                else{
                    //TODO realizar registro firebase
                }
            }
        });
    }
}