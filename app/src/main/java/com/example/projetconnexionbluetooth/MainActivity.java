package com.example.projetconnexionbluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.BreakIterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static Button bouton_serveur;
    static Button bouton_client;
    static TextView description;

    //thread commun permettant au client d'envoyer des données au serveur
    ConnectedThread connectedThread;

    public static Handler handler;

    //private Button monitor_server;
    //private Button monitor_processes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        this.bouton_client = (Button) findViewById((R.id.BoutonClient));
        bouton_client.setOnClickListener((View.OnClickListener) this);

        this.bouton_serveur = (Button) findViewById((R.id.BoutonServeur));
        bouton_serveur.setOnClickListener((View.OnClickListener) this);

        description = findViewById(R.id.textViewActionEnCours);
        description.setText("Bienvenue sur l'application de connexion Bluetooth");

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                // On peut mettre le traitement du message ici
                return true;
            }
        });

        // On créer le thread de connexion
        connectedThread = new ConnectedThread(handler);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClick(View view) {

        if (view.getId() == R.id.BoutonClient) {
            bouton_client.setText("Démarrage mode Client...");
            bouton_serveur.setEnabled(false); // On désactive le bouton serveur
            description.setText("Connexion au Serveur...");

            // Création d'un thread client permettant de se connecter au serveur
            ThreadClient clientthread= new ThreadClient(this);
            Log.d("ClientThread","Lancement ThreadClient");
            //on execute le thread créé
            clientthread.execute();
        }

        if (view.getId() == R.id.BoutonServeur) {
            bouton_serveur.setText("Démarrage mode serveur...");
            bouton_client.setEnabled(false); // On désactive le bouton client
            description.setText("Attente d'un Client...");

            // Création d'un thread serveur permettant d'établir la connexion bluetooth
            ThreadServeur serverthread= new ThreadServeur(this);
            Log.d("ServerThread","Lancement ThreadServeur");
            //on execute le thread créé
            serverthread.execute();
        }
    }
}