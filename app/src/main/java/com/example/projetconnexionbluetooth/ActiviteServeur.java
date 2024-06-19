package com.example.projetconnexionbluetooth;


import static com.example.projetconnexionbluetooth.ConnectedThread.getInstance;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ActiviteServeur extends AppCompatActivity {
    // EXPLICATIONS : on retrouve l'application du TP sans les listenerOnClick des boutons
    // La gestion Serveur et base de donnée avec HTTP est géré ici
    // On rajoute le côté connexion client avec les requêtes bluetooth

    // CE QU'IL FAUT FAIRE :
    // rajouter l'envoyer via le thread qui gère le socket lorsqu'il y a une maj
    // rajouter la gestion des requêtes bluetooth pour les clients
    // Faire des toast pour montrer les requêtes bluetooth reçues
    // SI le temps : séparer l'écran en deux pour

    // Récupération du thread bluetooth
    ConnectedThread thread_bluetooth;


    // Numéro de la maison donnée
    public int NumeroMaison = 29;

    // URL pour récupérer les données
    private final String url = "https://www.bde.enseeiht.fr/~bailleq/smartHouse/api/v1/devices/"
            + NumeroMaison;

    // JSONArray récupéré
    private JSONArray data_recup;

    // String des logs
    private String string_recup;

    // Liste des Devices
    private final Map<Integer, Device> ListeDevice = new HashMap<Integer, Device>();

    // Liste des views
    private final Map<Integer, View> Liste_Device_Views = new HashMap<Integer, View>();

    // Liste des boutons
    private final  Map<Integer, Button> Liste_Boutons = new HashMap<Integer, Button>();

    private LinearLayout Global_L_Layout;

    private RequestQueue queue;

    private boolean imported = false; // Pour savoir si la BDD a déja été importé ou non

    private ScheduledExecutorService scheduler; // Pour les la maj périodique

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_activite_serveur);

        Log.d("ActiviteServeur", "Instance thread bluetooth créée");
        thread_bluetooth = getInstance();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView description = findViewById(R.id.DescriptionServeur);
        description.setText("Serveur de la maison " + NumeroMaison);
        description.setTypeface(null, Typeface.BOLD);

        //on rajoute à la varibale globale le layout
        Global_L_Layout = findViewById(R.id.linearLayout);

        refresh_ALL_Data(imported); // On récupère les données depuis le serveur (imported = false)
        imported = true; // On a importé les données

        //On peut maintenant lancer le thread du socket bluetooth
        //Log.d("ActiviteServeur", "Lancement du thread bluetooth");
        //thread_bluetooth.start();


        // Mise à jour des données toutes 5 les secondes
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.d("Handler", "Mise à jour des données");
                refresh_ALL_Data(imported);
            }
        }, 0, 5, TimeUnit.SECONDS);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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

        // PAS DE LISTENER POUR LES BOUTONS
        /*
        bouton_etat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Switch Mode Device", "Clique sur le bouton Maj du device numero "
                        + dev.getID());
                SwitchDeviceState(dev.getID());
            }
        });
        */
        return layout ;
    }

    private void SwitchDeviceState(int deviceId) {
        StringRequest sr = new StringRequest(
                Request.Method.POST,
                "https://www.bde.enseeiht.fr/~bailleq/smartHouse/api/v1/devices/29/" + String.valueOf(deviceId),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(getApplicationContext(), "Switch Mode du device Réussie !" + String.valueOf(deviceId), Toast.LENGTH_SHORT).show();
                        refresh_device(deviceId); // Récupération des données depuis une requête HTTP car on a changé l'état d'un device
                        // Il se peut que le device ne puisse pas être switché -> par de changement en mode local mais par le serveur !
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Requête POST echouée", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("deviceId", String.valueOf(deviceId));
                params.put("houseId", String.valueOf(NumeroMaison));
                params.put("action", "turnOnOff");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type",
                        "application/x-www-form-urlencoded; charset=utf-8");
                return headers;
            }
        };
        // Ajout de la requête à la file d'attente
        this.queue.add(sr);
        Log.d("SwitchDeviceMode", "queue.add pour le dev num " + String.valueOf(deviceId));
    }


    // Méthode pour OnOff un device lors d'un clic
    private void SwitchDeviceState2(int deviceId) {
        StringRequest sr = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(getApplicationContext(), "Switch Mode du device " +
                                String.valueOf(deviceId), Toast.LENGTH_SHORT).show();
                        Log.d("OnResponseSwitchDevice",s);
                        refresh_device(deviceId);
                    }
                }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(),
                        "Requête POST echouée", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("deviceId", String.valueOf(deviceId));
                params.put("houseId", String.valueOf(NumeroMaison));
                params.put("action", "turnOnOff");
                Log.d("getParams" + String.valueOf(deviceId), "getParams de la requête HTTP");

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "Application/x−www−form−urlencoded");

                return params;
            }
        };
        this.queue.add(sr);
        Log.d("SwitchDeviceMode", "queue.add pour le dev num " + String.valueOf(deviceId));
    }

    public void refresh_ALL_Data(boolean imported) {
        // Récupération depuis requête HTTP des données
        // et rajout dans les views
        this.queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,
                null,
                refreshALLData_request_Array_Success_Listener(imported),
                refreshALLData_requestArrayErrorListener());
        this.queue.add(jsonArrayRequest);
    }

    private Response.Listener<JSONArray> refreshALLData_request_Array_Success_Listener(boolean imported) {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.d("refreshContextData_RequestArraySuccessListener", jsonArray.toString());
                data_recup = jsonArray;

                try {
                    for (int i = 0; i < data_recup.length(); i++) {
                        JSONObject device = data_recup.getJSONObject(i);

                        // On rajoute les éléments récupérés dans un objet de type device
                        Device device_a_rajouter = new Device();

                        device_a_rajouter.setID(device.getInt("ID"));
                        device_a_rajouter.setModele(device.getString("MODEL"));
                        device_a_rajouter.setBrand(device.getString("BRAND"));
                        device_a_rajouter.setName(device.getString("NAME"));
                        device_a_rajouter.setType(device.getString("TYPE"));
                        device_a_rajouter.setAutonomy(device.getInt("AUTONOMY"));
                        device_a_rajouter.setState(device.getString("STATE").equals("1") ? Boolean.TRUE : Boolean.FALSE);
                        device_a_rajouter.setData(device.getString("DATA"));

                        // On rajoute le device à la liste des device
                        ListeDevice.put(device_a_rajouter.getID(), device_a_rajouter);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // Créer les views pour chaque device
                if (!imported) { // Première importation : création des views
                    for (Device dev : ListeDevice.values()) {
                        View view_device = createDeviceView(dev);
                        int dev_id = dev.getID();
                        Global_L_Layout.addView(view_device);
                    }
                } else {
                    for (Device dev : ListeDevice.values()) {
                        updateDeviceView(dev);
                    }
                }
            }
        };
    }

    private ErrorListener refreshALLData_requestArrayErrorListener() {
        return new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("refreshContextData_ErrorResponse", Objects.requireNonNull(volleyError.getMessage()));
            }
        };
    }

    public void refresh_device(int device_ID) {
        Log.d("refresh_device","Récupération état du device" + String.valueOf(device_ID));
        this.queue = Volley.newRequestQueue(this);
        JsonObjectRequest json_refresh_device = new JsonObjectRequest(Request.Method.GET,
                url + "/" + String.valueOf(device_ID), // Limiter la requête à un seul device
                null, refresh_device_request_Array_Success_Listener(),
                refreshALLData_requestArrayErrorListener());
        this.queue.add(json_refresh_device);
    }
    private Response.Listener<JSONObject> refresh_device_request_Array_Success_Listener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d("refresh_device_RequestArraySuccessListener", jsonObject.toString());

                Map<String, String> map_elements_device = new HashMap<>();
                Iterator<String> keys = jsonObject.keys();

                while (keys.hasNext()) {
                    String key = keys.next(); // On récupère la clé
                    String value;
                    try {
                        value = jsonObject.getString(key);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    map_elements_device.put(key, value);
                }

                Device device_a_rajouter = new Device();

                device_a_rajouter.setID(Integer.parseInt(Objects.requireNonNull(map_elements_device.get("ID"))));
                device_a_rajouter.setModele(map_elements_device.get("MODEL"));
                device_a_rajouter.setBrand(map_elements_device.get("BRAND"));
                device_a_rajouter.setName(map_elements_device.get("NAME"));
                device_a_rajouter.setType(map_elements_device.get("TYPE"));
                device_a_rajouter.setAutonomy(Integer.parseInt(Objects.requireNonNull(map_elements_device.get("AUTONOMY"))));

                Log.d("AUTONOMY", Objects.requireNonNull(map_elements_device.get("AUTONOMY")));

                device_a_rajouter.setState(Objects.equals(map_elements_device.get("STATE"), "1") ? Boolean.TRUE : Boolean.FALSE);
                device_a_rajouter.setData(map_elements_device.get("DATA"));

                updateDeviceView(device_a_rajouter);
            }
        };
    }

    private void updateDeviceView(Device device) {
        // Récupérez la vue existante
        View existingView = Liste_Device_Views.get(device.getID());

        if (existingView != null) {
            // Mettez à jour les informations de la vue
            TextView titleView = existingView.findViewById(("titleView" + device.getID()).hashCode());
            TextView infoView = existingView.findViewById(("infoView" + device.getID()).hashCode());
            Button stateButton = existingView.findViewById(("stateButton" + device.getID()).hashCode());

            // Mettez à jour le titre
            String titleText = "[" + device.getModele() + "]" + " " + device.getName();
            if (titleView != null) {
                titleView.setText(titleText); // On met à jour le titre du device
            } else {
                Log.d("updateDeviceView", "titleView est null");
            }

            // Mettez à jour les informations
            String texte_info;
            boolean b = !(device.getData().isEmpty())
                    || device.getData().contentEquals("NoData"); // Il y a de l'information associée au device
            if (device.getAutonomy() != -1) {
                if (b) {
                    texte_info = "Type : " + device.getType() + " Data : " + " " + device.getData() +
                            " " + " Autonomy : " + device.getAutonomy() + "%";
                } else {
                    texte_info = "Type : " + device.getType() + " Autonomy : "
                            + device.getAutonomy() + "%";
                }
            } else {
                if (b) {
                    texte_info = "Type : " + device.getType() + " Data : "
                            + " " + device.getData() + " ";
                } else {
                    texte_info = "Type : " + device.getType();
                }
            }

            if (infoView != null) {
                infoView.setText(texte_info); // On met à jour les informations
            } else {
                Log.d("updateDeviceView", "infoView est null");
            }

            // Mettez à jour l'état du bouton
            String stateText = device.getState() ? "ON" : "OFF";
            if (stateButton != null) {
                stateButton.setText(stateText); // On met à jour l'état du device
            } else {
                Log.d("updateDeviceView", "stateButton est null");
            }

        }
    }

    @Override
    protected  void onPause() { // On arrête le scheduler lorsque l'activité est en pause
        super.onPause();
        if (scheduler != null) {
            Log.d("onPause", "Arrêt du scheduler On Pause");
            scheduler.shutdown();
        } else {
            Log.d("onPause", "scheduler est null");
        }
    }

    @Override
    protected void onResume() { // On redémarre le scheduler lorsque l'activité est en pause
        super.onResume();
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            // Si le scheduler est null, arrêté ou terminé, on le redémarre
            Log.d("onResume", "Redémarrage du scheduler");
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Log.d("Handler", "Mise à jour des données");
                    refresh_ALL_Data(imported);
                }
            }, 0, 5, TimeUnit.SECONDS);
        } else {
            Log.d("onResume", "scheduler est déjà démarré");
        }
    }

    @Override
    protected void onDestroy() { // On arrête le scheduler lorsque l'activité est détruite
        super.onDestroy();
        if (scheduler != null) {
            Log.d("onDestroy", "Arrêt du scheduler OnDestroy");
            scheduler.shutdown();
        } else {
            Log.d("onDestroy", "scheduler est null");
        }
    }
}