package com.example.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.tictactoe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class EncontrarJugadaActivity extends AppCompatActivity {
    private TextView tvLoadingMessage;
    private ProgressBar pbJugadas;
    private ScrollView layoutProgressBar, layoutMenuJuego;
    private Button btnJugar, btnRanking;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encontrar_jugada);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        layoutMenuJuego = findViewById(R.id.layoutMenuJuego);

        btnJugar = findViewById(R.id.btnJugar);
        btnRanking = findViewById(R.id.btnRanking);

        initProgressBar();
        initFireBase();
        eventos();
    }

    private void initFireBase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid= Objects.requireNonNull(firebaseUser).getUid();
    }

    private void eventos() {
        btnJugar.setOnClickListener(v -> {
            changeMenuVisibility(false);
            buscarJugadaLibre();
        });

        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
    }

    private void buscarJugadaLibre() {
        tvLoadingMessage.setText("Buscando una jugada libre ...");
        db.collection("jugadas")
                .whereEqualTo("jugadorDosId","")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size() == 0){

                            //TODO   no existen partidas libres
                        }else{
                            DocumentSnapshot docJugada = task.getResult().getDocuments().get(0);
                        }
                    }
                });
    }

    private void initProgressBar() {

        tvLoadingMessage = findViewById(R.id.textViewLoading);
        pbJugadas = findViewById(R.id.pbJugadas);

        pbJugadas.setIndeterminate(true);
        tvLoadingMessage.setText("Cargando ...");

        changeMenuVisibility(true);
    }

    private void changeMenuVisibility(boolean showMenu) {
        layoutProgressBar.setVisibility(showMenu ? View.GONE : View.VISIBLE);
        layoutMenuJuego.setVisibility(showMenu ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeMenuVisibility(true);
    }
}