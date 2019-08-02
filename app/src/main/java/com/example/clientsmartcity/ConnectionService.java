package com.example.clientsmartcity;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.UUID;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;

public class ConnectionService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private volatile Boolean keepRunning;
    private volatile Boolean isConnected;

    private MrUdpNodeConnection connection;
    private UUID myUUID;
    private Bundle extras;
    private static String ip;
    private static int port;
    private GoogleApiClient mGoogleApiClient;

    private SocketAddress socket;
    private Thread t;
    private LocalBroadcastManager broadcastManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;

    private ArrayList<String> msg = new ArrayList<>();
    EventBus eb = EventBus.getDefault();


    @Override
    public void onCreate() {
        t = null;
        keepRunning = true;
        isConnected = false;

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        EventBus.getDefault().register(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        extras = intent.getExtras();

        myUUID = UUID.fromString(extras.getString("UUID"));
        ip = extras.getString("IP");
        port = extras.getInt("Port");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        return START_STICKY;
    }

    private void startThread(final String valor) {
        t = new Thread(new Runnable() {
            public void run() {
                try {
                    /* Create a new MR-UDP connection */
                    connection = new MrUdpNodeConnection(myUUID);
                    connection.addNodeConnectionListener(new MListener());
                    socket = new InetSocketAddress(ip, port);
                    connection.connect(socket);

                    isConnected = true;
                    while (keepRunning) {
                        //System.out.println("Executando");
                        /* Disconnect and set the thread to null */
                        if (!isConnected) {
                            keepRunning = false;
                            connection.disconnect();
                            stopThread(t);
                        }
                        synchronized (t) {
                            Thread.sleep(4000);
                        }


                        ApplicationMessage message = new ApplicationMessage();
                        System.out.println("enviando: "+valor);
                        message.setContentObject(valor);
                        try {
                            connection.sendMessage(message);
                        } catch (Exception e) {
                            System.err.println(e);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }


    private synchronized void stopThread(Thread t) {
        keepRunning = false;

        try {
            connection.disconnect();
        } catch (Exception e) {
            System.err.println(e);
        }

        if (t != null) {
            t = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        String valor = event.getData();
        startThread(valor);
        teste(valor);
    }

    //testar se ta recebendo mensagem pelo event bus
    private void teste(String valor) {
        System.out.println("send: "+ valor);
    }

    @Override
    public void onDestroy() {
        stopThread(t);
        eb.unregister(this);
        System.out.println("Serviço parado");
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("onConnected" + bundle);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (l != null) {
            System.out.println("lat " + l.getLatitude() + "lng " + l.getLongitude());
        }

        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("onConnectionSuspended " + i);

    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("lat " + location.getLatitude() + ", lng " + location.getLongitude());
        String localizacao = location.getLatitude() + "," + location.getLongitude();
        EventBus.getDefault().post(new MessageEvent(localizacao));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("onConnectionFailed ");

    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void startLocationUpdate() {
        initLocationRequest();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

}
