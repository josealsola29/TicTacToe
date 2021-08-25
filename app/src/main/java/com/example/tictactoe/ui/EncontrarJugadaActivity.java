package com.example.tictactoe.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tictactoe.R;
import com.example.tictactoe.app.Constantes;
import com.example.tictactoe.model.Jugada;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;

public class EncontrarJugadaActivity extends AppCompatActivity {
    private TextView tvLoadingMessage;
    private ProgressBar pbJugadas;
    private ScrollView layoutProgressBar, layoutMenuJuego;
    private Button btnJugar, btnRanking;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String uid, jugadaId;
    private ListenerRegistration listenerRegistration = null;
    private LottieAnimationView animationView;

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
        uid = Objects.requireNonNull(firebaseUser).getUid();
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
        animationView.playAnimation();
        db.collection("jugadas")
                .whereEqualTo("jugadorDosId", "")
                .get()
                .addOnCompleteListener(task -> {
                    if (Objects.requireNonNull(task.getResult()).size() == 0) {

                        //TODO   no existen partidas libres crear una nueva
                        crearNuevaJugada();
                    } else {
                        boolean encontrado = false;

                        for (DocumentSnapshot docJugada : task.getResult().getDocuments()) {
                            if (!docJugada.get("jugadorUnoId").equals("uid")) {
                                encontrado = true;
                                jugadaId = docJugada.getId();
                                Jugada jugada = docJugada.toObject(Jugada.class);
                                Objects.requireNonNull(jugada).setJugadorDosId(uid);

                                db.collection("jugadas")
                                        .document(jugadaId)
                                        .set(jugada)
                                        .addOnSuccessListener(unused -> {
                                            tvLoadingMessage.setText("¡Jugada libre encontrada! Comienza la partida");
                                            animationView.setRepeatCount(0);
                                            animationView.setAnimation("chekedAnim.json");
                                            animationView.playAnimation();

                                            final Handler handler = new Handler();
                                            final Runnable runnable = this::startGame;
                                            handler.postDelayed(runnable, 1500);
                                        }).addOnFailureListener(e -> {
                                    changeMenuVisibility(true);
                                    Toast.makeText(EncontrarJugadaActivity.this,
                                            "Hubo algún error al entrar en la jugada.",
                                            Toast.LENGTH_SHORT).show();
                                });
                                break;
                            }

                            if(!encontrado) crearNuevaJugada();
                        }
                    }
                });
    }

    private void crearNuevaJugada() {
        tvLoadingMessage.setText("Creando una jugada nueva ...");
        Jugada nuevaJugada = new Jugada(uid);

        db.collection("jugadas")
                .add(nuevaJugada)
                .addOnSuccessListener(documentReference -> {
                    jugadaId = documentReference.getId();
                    //TODO
                    esperarJugador();
                }).addOnFailureListener(e -> {
            changeMenuVisibility(true);
            Toast.makeText(EncontrarJugadaActivity.this,
                    "Error al crear la nueva jugada.", Toast.LENGTH_SHORT).show();
        });
    }

    private void esperarJugador() {
        tvLoadingMessage.setText("Esperando a otro jugador...");

        listenerRegistration = db.collection("jugadas")
                .document(jugadaId)
                .addSnapshotListener((value, error) -> {
                    if (Objects.equals(Objects.requireNonNull(value).get("jugadorDosId"), "")) {
                        tvLoadingMessage.setText("¡Ya ha llegado llegado un jugador! Comienza la partida");
                        animationView.setRepeatCount(0);
                        animationView.setAnimation("chekedAnim.json");
                        animationView.playAnimation();

                        final Handler handler = new Handler();
                        final Runnable runnable = this::startGame;
                        handler.postDelayed(runnable, 1500);
                    }
                });
    }

    private void startGame() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        Intent intent = new Intent(EncontrarJugadaActivity.this, JuegoActivity.class);
        intent.putExtra(Constantes.EXTRA_JUGADA_ID, jugadaId);
        startActivity(intent);
        jugadaId = "";
    }

    private void initProgressBar() {
        animationView = findViewById(R.id.animation_view);
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
        if (!jugadaId.equals("")) {
            changeMenuVisibility(false);
            esperarJugador();
        } else {
            changeMenuVisibility(true);
        }
        changeMenuVisibility(true);
    }

    @Override
    protected void onStop() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        if (!jugadaId.equals("")) {
            db.collection("jugadas")
                    .document(jugadaId)
                    .delete()
                    .addOnCompleteListener(task -> jugadaId = "");
        }
        super.onStop();
    }
}