package com.example.projetconnexionbluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ThreadServeur extends AsyncTask<Void, Void, String> {

    private Context context;

    public ThreadServeur(Context context) {
        this.context = context;
    }

    private static final String SERVICE_NAME = "MonServeur";
    private static final UUID SERVICE_UUID = UUID.fromString("a48ffb46-4f3e-43be-9e60-bbf7f1e396d1");
    // UUID généré sur Internet : identifiant unique de connexion pour le client et le serveur de cette application
    private static final String TODO = "Connexion refuséee";

    public boolean serveur_connecte;

    public static BluetoothServerSocket serverSocket; // Utilisé pour écouter les demandes entrantes de connexion du serveur

    public static BluetoothSocket socket; // Socket utilisé pour pour envoyer des informations entre le client et le serveur

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected String doInBackground(Void... voids) {
        serveur_connecte = false; // Dans un premier temps le serveur n'est pas connecté, on attend qu'un client se connecte
        Log.d("Server Thread", "Démarrage du thread serveur");

        // On récupère l'adaptateur bluetooth du téléphone
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        try {
            // On crée un socket pour écouter les demandes de connexion entrantes
            // On vérifie que l'application a bien les permissions pour utiliser le bluetooth
            Log.d("Server Thread", "Vérification des permissions pour le bluetooth");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Si on ne l'a pas, on demande la permission
                Log.d("Server Thread", "Demande de permission pour le bluetooth");
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                Log.d("Server Thread", "Permission pour le bluetooth accordée");
            } else {
                Log.d("Server Thread", "Permission pour le bluetooth déjà accordée");
            }

            serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);

        } catch (IOException e) {
            Log.e("Server Thread", "Erreur lors de la création du socket serveur", e);
            throw new RuntimeException(e);
        }

        while (!serveur_connecte) {
            try {
                // On attend qu'un client se connecte, le socket est en mode écoute
                Log.d("Server Thread", "En attente d'un client");
                socket = serverSocket.accept();
                serveur_connecte = true; // On a trouvé un client, on peut sortir de la boucle
                Log.d("Server Thread", "Un client s'est connecté");
                //Toast.makeText(context, "Un client s'est connecté", Toast.LENGTH_SHORT).show();

                // On ferme le socket serveur une fois
                serverSocket.close();
                return "Connexion établie";
            } catch (IOException e) {
                Log.e("Server Thread", "Erreur lors de l'acceptation de la connexion", e);
                throw new RuntimeException(e);
            }
        }

        return null;

    }

    protected void onPostExecute(String result) {
        if (serveur_connecte) {
            MainActivity.bouton_serveur.setText("Connexion établie");
            MainActivity.bouton_serveur.setEnabled(false); // On désactive le bouton client
            MainActivity.bouton_serveur.setBackgroundColor(Color.GREEN);
            MainActivity.description.setText("Connecté avec le client");
            MainActivity.description.setTextColor(Color.GREEN); // On change la couleur du texte en vert
        } else {
            MainActivity.bouton_serveur.setText("ERROR !");
            MainActivity.bouton_serveur.setBackgroundColor(Color.RED);
            MainActivity.description.setText("Erreur lors de la connexion");
            MainActivity.description.setTextColor(Color.RED); // On change la couleur du texte en rouge
        }
        Log.d("Server Thread", "Pause de 2 secondes avant lancement ActiServeur");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Lancement de l'activité serveur après la pause de 2 secondes
                Log.d("Server Thread", "Lancement de l'activité serveur");
                Intent intent = new Intent(context, ActiviteServeur.class);
                context.startActivity(intent);
            }
        }, 2000); // Pause de 2 secondes

    }
}

