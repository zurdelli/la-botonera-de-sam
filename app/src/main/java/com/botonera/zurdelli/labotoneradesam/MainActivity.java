package com.botonera.zurdelli.labotoneradesam;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private List<String> names;
    private final int PERM_CODE = 100;

    InputStream inputStream;
    FileOutputStream fileOutputStream;

    // Crea el canal de notificacion
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NotificationChannel.DEFAULT_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Forzar y cargar icono en el actionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        listView = (ListView) findViewById(R.id.listaView);

        // Enviar
//         Si tiene el SDK > 25 (Marshmallow)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Si el permiso no ha sido aceptado
            if (!CheckPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // requestPermissions es el metodo para pedir permisos.
                // requestPermissions (String[] permisos requeridos, int Codigo/s de permiso/s)
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_CODE);


                // shouldShow... es un metodo que sirve para insistir en que acepte el permiso
                // -> Si habiamos preguntado y rechaza = true
                // -> Si marca que nunca mas le preguntemos = false
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Creamos un AlertDialog que sirve para mostrar un mensaje emergente
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setMessage("Para enviar los audios por Whatsapp acepta los permisos");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_CODE);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }

        // Datos a mostrar
        names = new ArrayList<String>();
        names.add("Sound1");
        names.add("Sound2");
        names.add("Sound3");
        names.add("Sound4");
        names.add("Sound5");

        // Enlazamos con nuestro adaptador personalizado
        AdapterCategory adappter = new AdapterCategory(this,R.layout.item, names);
        listView.setAdapter(adappter);
    }

    public boolean CheckPermission (String permission){
        int result = this.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }


}
