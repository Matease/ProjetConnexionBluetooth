package com.example.projetconnexionbluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

// Cette classe permet de gérer la connexion entre le client et le serveur

public class ConnectedThread extends Thread {
    private static ConnectedThread instance = null;

    BluetoothSocket monSocket; // Socket commun entre le client et le serveur
    InputStream monInputStream;
    OutputStream monOutputStream;

    private Handler handler;

    public ConnectedThread(Handler handler) {
        this.handler = handler;
        this.monSocket = ThreadClient.socket;
    }

    public static synchronized ConnectedThread getInstance() {
        Log.d("ConnectedThread", "Récupération de l'instance");
        if (instance == null) {
            instance = new ConnectedThread(MainActivity.handler);
        }
        return instance;
    }

    @Override
    public void run() {
        Log.d("ConnectedThread", "RUN du thread de connexion lancé");
        byte[] buffer = new byte[1024]; // Buffer pour la lecture des données

        InputStream InputStreamTemporaire = null;
        OutputStream OutputStreamTemporaire = null;
        while (true) {
            // On récupère les flux d'entrée (du client) du socket
            try {
                monInputStream = ThreadClient.socket.getInputStream();
                Log.d("ConnectedThread", "Récupération du flux d'entrée");
                int nombre_bits_lus = monInputStream.read(buffer);
                Log.d("ConnectedThread", "Message reçu de " + nombre_bits_lus + " bits");

                // On affiche le message reçu
                String messagerecu = new String(buffer, 0, nombre_bits_lus);
                Log.d("ConnectedThread", "Message reçu : " + messagerecu);

                // On envoie le message reçu au Handler pour qu'il soit traité
                Log.d("ConnectedThread", "Envoi du message au Handler");
                Message message = handler.obtainMessage();
                message.obj = buffer;
                Log.d("ConnectedThread", "Message envoyé au Handler" + message.obj.toString());
                handler.sendMessage(message);

                // Écriture dans l'OuputStream du socket
                Log.d("ConnectedThread", "Ecriture dans le flux de sortie");
                write(buffer);
            } catch (IOException e) {
                Log.e("ConnectedThread", "Erreur lors de la récupération du flux d'entrée", e);
            }
        }


    }

    // On utilise cette méthode pour écrire des données sur le socket
    public void write(byte[] bytes) {
        try {
            ThreadServeur.socket.getOutputStream().write(bytes);
        } catch (IOException e) {
            Log.e("ConnectedThread", "Problème pendant l'écriture", e);
        }
    }

    // On utilise cette méthode pour fermer la connexion depuis l'activité principale
    public void cancel() {
        try {
            monSocket.close();
        } catch (IOException e) {
            Log.e("ConnectedThread", "close() du socket échoué", e);
        }
    }
}
