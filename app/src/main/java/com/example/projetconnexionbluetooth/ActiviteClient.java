package com.example.projetconnexionbluetooth;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;

public class ActiviteClient extends AppCompatActivity {

    // Implémentation de la télécommande client
    // Lister l'ensemble des appareils connectés avec une requête envoyée au serveur

    ConnectedThread thread_bluetooth;

    // Liste des Devices
    private final Map<Integer, Device> ListeDevice = new HashMap<Integer, Device>();

    // Liste des views
    private final Map<Integer, View> Liste_Device_Views = new HashMap<Integer, View>();

    // Liste des boutons
    private final  Map<Integer, Button> Liste_Boutons = new HashMap<Integer, Button>();

    private LinearLayout Global_L_Layout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("ActiviteClient", "Activité Client lancée");

        //on rajoute à la varibale globale le layout
        Global_L_Layout = findViewById(R.id.linearLayout2);

        Log.d("ActiviteClient", "Instance thread bluetooth");
        thread_bluetooth = ConnectedThread.getInstance();

        importDevices();



        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_activite_client);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void importDevices() {
        // Implémentation de la méthode qui fait une requête bluetooth au serveur pour import les devices
    }

    public View createDeviceView(Device dev) {

        // Création du layout
        RelativeLayout layout = new RelativeLayout(this );

        // Récupération des information du device
        int autonomy = dev.getAutonomy();
        String nom = dev.getName();
        Boolean etat = dev.getState();
        String modele = dev.getModele();
        String data = dev.getData();
        String type = dev.getType();
        String marque = dev.getBrand();
        int ID = dev.getID();

        String Setat;
        if (etat) {
            Setat = "ON";
        } else {
            Setat = "OFF";
        }

        String letexte = "[" + modele + "]" + " " + nom;

        // Paramètres Titre
        RelativeLayout.LayoutParams paramsTitle = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsTitle.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        //paramsTopLeft.addRule(RelativeLayout.ABOVE, RelativeLayout.TRUE);

        TextView titleview = new TextView(this );
        titleview.setText(letexte);
        titleview.setTypeface(null, Typeface.BOLD);
        titleview.setId(("titleView" + dev.getID()).hashCode());
        //paramsTitle.setMargins(20, 20, 20, 20);
        layout.addView(titleview, paramsTitle);

        String texte_info;
        boolean b = !(data.isEmpty()) || data.contentEquals("NoData"); // Il y a de l'information associée au device
        if (autonomy != -1) {
            if (b) {
                texte_info = "Type : " + type + " Data : " + " " + data + " " + " Autonomy : " + autonomy + "%";
            } else {
                texte_info = "Type : " + type + " Autonomy : " + autonomy + "%";
            }
        } else {
            if (b) {
                texte_info = "Type : " + type + " Data : " + " " + data + " ";
            } else {
                texte_info = "Type : " + type;
            }
        }

        // Paramètres pour positionner les informations en dessous du titre
        RelativeLayout.LayoutParams paramsInfo = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsInfo.addRule(RelativeLayout.BELOW, titleview.getId());
        paramsInfo.addRule(RelativeLayout.BELOW, RelativeLayout.TRUE);
        paramsInfo.setMargins(0, 70, 0, 100);


        TextView infoView = new TextView(this);
        infoView.setText(texte_info);
        infoView.setId(("infoView" + dev.getID()).hashCode());
        layout.addView(infoView, paramsInfo);

        // On ajoute la view à la liste des views
        Liste_Device_Views.put(dev.getID(), layout);

        // Création du bouton
        Button bouton_etat = new Button(this);
        bouton_etat.setText(Setat);
        bouton_etat.setId(("stateButton" + dev.getID()).hashCode());
        Liste_Boutons.put(dev.getID(), bouton_etat);

        // Paramètres pour positionner le bouton à droite du texte
        RelativeLayout.LayoutParams paramsTopRight = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        paramsTopRight.addRule(RelativeLayout.BELOW, titleview.getId());
        layout.addView(bouton_etat, paramsTopRight);

        // Écouteur de clic pour le bouton
        bouton_etat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Switch Mode Device", "Clique sur le bouton Maj du device numero "
                        + dev.getID());
                SwitchDeviceState(dev.getID());
                // IMPLÉMENTATION DE LA REQUÊTE BLUETOOTH
            }
        });

        return layout ;
    }

    public void SwitchDeviceState(int device_id) {
        // A REMPLIR
    }
}