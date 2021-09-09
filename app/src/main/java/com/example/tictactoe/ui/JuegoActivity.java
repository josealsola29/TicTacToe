package com.example.tictactoe.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tictactoe.R;
import com.example.tictactoe.app.Constantes;
import com.example.tictactoe.databinding.ActivityJuegoBinding;
import com.example.tictactoe.model.Jugada;
import com.example.tictactoe.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JuegoActivity extends AppCompatActivity {
    private List<ImageView> casillas;
    private TextView tvPlayer1, tvPlayer2;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String uid, jugadaId = "", playerOneName = "", playerTwoName = "", ganadorId = "";
    private Jugada jugada;
    private ListenerRegistration listenerRegistration = null;
    private ActivityJuegoBinding binding;
    private FirebaseUser firebaseUser;
    String nombreJugador;
    private User userPlayer1, userPlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityJuegoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

/*        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        initViews();
        initGame();
    }

    private void initGame() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        uid = Objects.requireNonNull(firebaseUser).getUid();

        Bundle extras = getIntent().getExtras();

        jugadaId = extras.getString(Constantes.EXTRA_JUGADA_ID);

    }

    private void initViews() {
        tvPlayer1 = findViewById(R.id.tvPlayer1);
        tvPlayer2 = findViewById(R.id.tvPlayer2);
        casillas = new ArrayList<>();

        casillas.add((ImageView) findViewById(R.id.iv0));
        casillas.add((ImageView) findViewById(R.id.iv1));
        casillas.add((ImageView) findViewById(R.id.iv2));
        casillas.add((ImageView) findViewById(R.id.iv3));
        casillas.add((ImageView) findViewById(R.id.iv4));
        casillas.add((ImageView) findViewById(R.id.iv5));
        casillas.add((ImageView) findViewById(R.id.iv6));
        casillas.add((ImageView) findViewById(R.id.iv7));
        casillas.add((ImageView) findViewById(R.id.iv8));
    }

    @Override
    protected void onStart() {
        super.onStart();
        jugadaListener();
    }

    private void jugadaListener() {
        listenerRegistration = db.collection("jugadas")
                .document(jugadaId)
                .addSnapshotListener(JuegoActivity.this, (snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(JuegoActivity.this, "Error al obtener los datos " +
                                "de la jugada.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String source = snapshot != null
                            && snapshot.getMetadata().hasPendingWrites() ? "Local" : "Server";
                    if (Objects.requireNonNull(snapshot).exists() && source.equals("Server")) {
                        jugada = snapshot.toObject(Jugada.class);
                        if (playerOneName.isEmpty() || playerTwoName.isEmpty()) {
                            getPlayerNames();
                        }
                        updateUI();
                    }
                    updatePlayerUI();
                });
    }

    private void updatePlayerUI() {
        if (jugada.isTurnoJugadorUno()) {
            tvPlayer1.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            tvPlayer2.setTextColor(ContextCompat.getColor(this, R.color.gris));
        } else {
            tvPlayer1.setTextColor(ContextCompat.getColor(this, R.color.gris));
            tvPlayer2.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        }

        if (!jugada.getGanadorId().isEmpty()) {
            ganadorId = jugada.getGanadorId();
            mostrarDialogoGameOver();
        }
    }

    private void updateUI() {
        for (int i = 0; i < 9; i++) {
            int casilla = jugada.getCeldasSeleccionadas().get(i);
            ImageView ivCasillaActual = casillas.get(i);

            if (casilla == 0) {
                ivCasillaActual.setImageResource(R.drawable.ic_black_square_svgrepo_com);
            } else if (casilla == 1) {
                ivCasillaActual.setImageResource(R.drawable.ic_cross_svgrepo_com);
            } else {
                ivCasillaActual.setImageResource(R.drawable.ic_circle_svgrepo_com);
            }
        }
    }

    private void getPlayerNames() {
        db.collection("users")
                .document(jugada.getJugadorUnoId())
                .get()
                .addOnSuccessListener(JuegoActivity.this, documentSnapshot -> {
                    userPlayer1 = documentSnapshot.toObject(User.class);
                    playerOneName = Objects.requireNonNull(documentSnapshot.get("name")).toString();
                    tvPlayer1.setText(playerOneName);
                    if (jugada.getJugadorUnoId().equals(uid)) {
                        nombreJugador = playerOneName;
                    }
                });

        //Obtener el nombre del player 2

        db.collection("users")
                .document(jugada.getJugadorDosId())
                .get()
                .addOnSuccessListener(JuegoActivity.this, documentSnapshot -> {
                    userPlayer2 = documentSnapshot.toObject(User.class);
                    playerTwoName = Objects.requireNonNull(documentSnapshot.get("name")).toString();
                    tvPlayer2.setText(playerOneName);
                    if (jugada.getJugadorDosId().equals(uid)) {
                        nombreJugador = playerTwoName;
                    }
                });
    }

    @Override
    protected void onStop() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        super.onStop();
    }

    public void casillaSeleccionada(View view) {
        if (!jugada.getGanadorId().isEmpty()) {
            Toast.makeText(this, "La partida ha terminado. ", Toast.LENGTH_SHORT).show();
        } else {
            if (jugada.isTurnoJugadorUno() && jugada.getJugadorUnoId().equals(uid)) {
                //Está jugando el jugador 1
                actualizarJugada(view.getTag().toString());
            } else if (!jugada.isTurnoJugadorUno() && jugada.getJugadorDosId().equals(uid)) {
                //Está jugando el jugador 2
                actualizarJugada(view.getTag().toString());

            } else {
                Toast.makeText(this, "No es tu turno aún.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void actualizarJugada(String numeroCasilla) {
        int posicionCasilla = Integer.parseInt(numeroCasilla);

        if (jugada.getCeldasSeleccionadas().get(posicionCasilla) != 0) {
            Toast.makeText(this, "Seleccione una casilla libre.", Toast.LENGTH_SHORT).show();
        } else {
            if (jugada.isTurnoJugadorUno()) {
                casillas.get(posicionCasilla).setImageResource(R.drawable.ic_cross_svgrepo_com);
                jugada.getCeldasSeleccionadas().set(posicionCasilla, 1);
            } else {
                casillas.get(posicionCasilla).setImageResource(R.drawable.ic_circle_svgrepo_com);
                jugada.getCeldasSeleccionadas().set(posicionCasilla, 2);
            }

            if (existeSolucion()) {
                jugada.setGanadorId(uid);
                Toast.makeText(this, "Hay solución.", Toast.LENGTH_SHORT).show();
            } else if (existeEmpate()) {
                jugada.setGanadorId("EMPATE");
                Toast.makeText(this, "Hay empate.", Toast.LENGTH_SHORT).show();
            } else {
                cambioTurno();
            }

            //Acutalizar en Firestore los datos de la jugada
            db.collection("jugadas")
                    .document(jugadaId)
                    .set(jugada)
                    .addOnSuccessListener(JuegoActivity.this, unused -> {

                    }).addOnFailureListener(JuegoActivity.this,
                    e -> Log.w("Error", "Error al guardar la jugada"));
        }
    }

    private void cambioTurno() {
        //Cambio de turno
        jugada.setTurnoJugadorUno(!jugada.isTurnoJugadorUno());
    }

    private boolean existeEmpate() {
        boolean existe = false;
        //Empate
        boolean hayCasillaLibre = false;
        for (int i = 0; i < 9; i++) {
            if (jugada.getCeldasSeleccionadas().get(i) == 0) {
                hayCasillaLibre = true;
                break;
            }
        }

        if (!hayCasillaLibre) { //Empate
            existe = true;
        }
        return existe;
    }

    private boolean existeSolucion() {
        boolean existe = false;

        List<Integer> selectedCells = jugada.getCeldasSeleccionadas();
        if (selectedCells.get(0).equals(selectedCells.get(1))
                && selectedCells.get(1).equals(selectedCells.get(2))
                && selectedCells.get(2) != 0) { //0 - 1 - 2
            existe = true;
        } else if (selectedCells.get(3).equals(selectedCells.get(4))
                && selectedCells.get(4).equals(selectedCells.get(5))
                && selectedCells.get(5) != 0) {
            existe = true;
        } else if (selectedCells.get(6).equals(selectedCells.get(7))
                && selectedCells.get(7).equals(selectedCells.get(8))
                && selectedCells.get(8) != 0) {
            existe = true;
        } else if (selectedCells.get(0).equals(selectedCells.get(3))
                && selectedCells.get(3).equals(selectedCells.get(6))
                && selectedCells.get(6) != 0) {
            existe = true;
        } else if (selectedCells.get(1).equals(selectedCells.get(4))
                && selectedCells.get(4).equals(selectedCells.get(7))
                && selectedCells.get(7) != 0) {
            existe = true;
        } else if (selectedCells.get(2).equals(selectedCells.get(5))
                && selectedCells.get(5).equals(selectedCells.get(8))
                && selectedCells.get(8) != 0) {
            existe = true;
        } else if (selectedCells.get(0).equals(selectedCells.get(4))
                && selectedCells.get(4).equals(selectedCells.get(7))
                && selectedCells.get(8) != 0) {
            existe = true;
        } else if (selectedCells.get(2).equals(selectedCells.get(4))
                && selectedCells.get(4).equals(selectedCells.get(6))
                && selectedCells.get(6) != 0) {
            existe = true;
        }
        return existe;
    }

    public void mostrarDialogoGameOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game over");
        builder.setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.dialogo_game_over, null);
        //Obtener las referencias a los View componentes
        TextView tvPuntos = view.findViewById(R.id.tvPuntos);
        TextView tvInformacion = view.findViewById(R.id.tvInformacion);
        LottieAnimationView gameoverAnimationView = view.findViewById(R.id.animation_view);
        builder.setView(view);

        if (ganadorId.equals("EMPATE")) {
            actualizarPuntuacion(1);
            tvInformacion.setText(String.format("¡%s has empatado el juego y has conseguido 1 punto!",
                    nombreJugador));
            tvPuntos.setText("+1 punto");
        } else if (ganadorId.equals(uid)) {
            actualizarPuntuacion(3);
            tvInformacion.setText(String.format("¡%s has ganado el juego y has conseguido 3 puntos!",
                    nombreJugador));
            tvPuntos.setText("+3 puntos");
        } else {
            actualizarPuntuacion(0);
            tvInformacion.setText(MessageFormat.format("¡{0} has perdido la partida, no has ganado" +
                    " nigún punto!", nombreJugador));
            tvPuntos.setText("0 puntos");
            gameoverAnimationView.setAnimation("lose_ani.json");
        }

        gameoverAnimationView.playAnimation();

        builder.setPositiveButton("OK", (dialog, which) -> finish());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void actualizarPuntuacion(int puntosConseguidos) {
        User jugadorActualizar = null;
        if (nombreJugador.equals(userPlayer1.getName())) {
            userPlayer1.setPoints(userPlayer1.getPoints() + puntosConseguidos);
            userPlayer1.setPartidasJugadas(userPlayer1.getPartidasJugadas() + 1);
            jugadorActualizar = userPlayer1;
        } else {
            userPlayer2.setPoints(userPlayer2.getPoints() + puntosConseguidos);
            userPlayer2.setPartidasJugadas(userPlayer2.getPartidasJugadas() + 1);
            jugadorActualizar = userPlayer2;
        }

        db.collection("users")
                .document(uid)
                .set(jugadorActualizar)
                .addOnSuccessListener(JuegoActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                })
                .addOnFailureListener(JuegoActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}