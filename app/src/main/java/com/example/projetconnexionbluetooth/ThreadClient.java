package com.example.projetconnexionbluetooth;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class ThreadClient extends AsyncTask<Void, Void, String> {


    private Context context;
    public static BluetoothSocket socket;
    private BluetoothDevice serveur_device;

    private static final UUID SERVICE_UUID
            = UUID.fromString("a48ffb46-4f3e-43be-9e60-bbf7f1e396d1");

    boolean connecte_au_serveur = false;

    public ThreadClient(Context context) {
        this.context = context;
    }

    private static final String SERVICE_NAME = "MonClient";

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected String doInBackground(Void... voids) {
        // Récupération de l'adaptateur Bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Recherche du périphérique Bluetooth
        try {
            // On vérifie que l'application a bien les permissions pour se connecter en Bluetooth
            Log.d("Server Client", "Vérification des permissions pour le bluetooth");
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Si on ne l'a pas, on demande la permission
                Log.d("Server Client", "Demande de permission pour le bluetooth");
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
                Log.d("Server Client", "Permissions pour le bluetooth accordée");
            } else {
                Log.d("Server Client", "Permissions pour le bluetooth déja accordée");
            }

            // On récupère l'ensmeble des téléphones appairés disponibles
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            StringBuilder names_devices = new StringBuilder();

            if (!bondedDevices.isEmpty()) {
                for (BluetoothDevice device : bondedDevices) {
                    names_devices.append(" ").append(device.getName());
                    // CHANGER LES NOMS DES DEVICES EN FONCTION DE LA CONFIGURATION
                    if (device.getName().equals("Bluetooth1") || device.getName().equals("Bluetooth2")
                            || device.getName().equals("Bluetooth3")
                            || device.getName().equals("MacBook Air de Mathis")) {
                        serveur_device = device;
                        break;
                    }
                }
                Log.d("Client Thread", "Périphérique(s) trouvé(s) : " + names_devices);
            } else {
                Log.d("Client Thread", "Aucun périphérique connecté");
            }

            if (serveur_device == null) {
                Log.e("Client Thread", "Périphérique non trouvé");
                //Toast.makeText(context, "Périphérique non trouvé", Toast.LENGTH_SHORT).show();
                return "Périphérique non trouvé";
            }

            // Demande d'accès du socket du device serveur
            socket = serveur_device.createRfcommSocketToServiceRecord(SERVICE_UUID);
            socket.connect(); // Établissement de la connexion

        } catch (IOException e) {
            Log.e("Client Thread", "Erreur lors de la connexion au serveur", e);
            return "Erreur lors de la connexion au serveur";
        }

        // On est connecté au serveur
        Log.d("Client Thread", "Connecté au serveur");
        connecte_au_serveur = true;
        return "Connecté au serveur";
    }

    @SuppressLint("SetTextI18n")
    protected void onPostExecute(String result) {

        if(connecte_au_serveur) {
            MainActivity.bouton_client.setText("Connecté au serveur");
            MainActivity.bouton_client.setEnabled(false); // On désactive le bouton client
            MainActivity.bouton_client.setBackgroundColor(Color.GREEN);
            MainActivity.description.setText("Connecté au serveur");
            MainActivity.description.setTextColor(Color.GREEN); // On change la couleur du texte en vert
            //MainActivity.monitor_server.setEnabled(true);
        }
        else {
            MainActivity.bouton_client.setText("ERROR!");
            MainActivity.bouton_client.setBackgroundColor(Color.RED);
            MainActivity.description.setText("Erreur lors de la connexion");
            MainActivity.description.setTextColor(Color.RED); // On change la couleur du texte en rouge
            connecte_au_serveur = false;
        }

        Log.d("Client Thread", "Pause de 2 secondes avant lancement de l'activité client");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (connecte_au_serveur) {
                    // Lancement de l'activité serveur après la pause de 2 secondes
                    Log.d("Server Thread", "Lancement de l'activité Client");
                    Intent intent = new Intent(context, ActiviteClient.class);
                    context.startActivity(intent);
                }

            }
        }, 2000); // Pause de 2 secondes

    }
}
