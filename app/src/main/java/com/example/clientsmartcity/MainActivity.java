package com.example.clientsmartcity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";


    private Button iniciarButton;
    private ListView proximosList;

    private static String gatewayIP = "192.168.10.43";
    private static int gatewayPort = 5500;
    private ArrayList<String> proximos = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniciarButton = findViewById(R.id.btn_iniciar);

        uniqueID = getUUID(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
            return;
        }

        updateListView(proximos);
    }

    private void updateListView(ArrayList<String> proximos) {

        proximosList = findViewById(R.id.proximosList);
        ArrayAdapter<String> proximosAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, proximos);
        proximosList.setAdapter(proximosAdapter);

    }

    public void startButton(View view) {
        Intent i = new Intent(this, ConnectionService.class);
        i.putExtra("UUID", uniqueID);
        i.putExtra("IP", gatewayIP);
        i.putExtra("Port", gatewayPort);

        this.startService(i);

        System.out.println("Serviço iniciado");

        Toast.makeText(getBaseContext(), "Iniciando leitura GPS", Toast.LENGTH_LONG).show();

    }

    public void stopButton(View view) {
        stopService(new Intent(getBaseContext(), ConnectionService.class));
        Toast.makeText(getBaseContext(), "Finalizando leitura GPS", Toast.LENGTH_LONG).show();
    }

    public synchronized static String getUUID(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        return uniqueID;
    }


}
