package com.botonera.zurdelli.labotoneradesam;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by zurdelli on 13/2/2018.
 */

// Esta es la clase encargada de mostrar los elementos. Extiende de la clase BaseAdapter,
// uno de los mas basicos pero muy personalizables
public class AdapterCategory extends BaseAdapter {

    private Context context;
    private int layout;
    private List<String> names;
    private final int PERM_CODE = 100;
    int soundList[] = {R.raw.sound1, R.raw.sound2, R.raw.sound3, R.raw.sound4, R.raw.sound5, R.raw.sound6};


    public AdapterCategory(Context context, int layout, List<String> names) {
        this.context = context;
        this.layout = layout;
        this.names = names;
    }

    // getCount nos proporciona el size de la lista
    @Override
    public int getCount() {
        return this.names.size();
    }

    // Devuelve un item del array a la que le damos una position
    @Override
    public Object getItem(int position) {
        return this.names.get(position);
    }

    // Devuelve el ID del item
    @Override
    public long getItemId(int id) {
        return id;
    }


    // Aqui esta la chicha, nos sirve para "inflar" o personalizar nuestra lista
    // convertView : Vista que se va a transformar, por defecto esta vacia
    // viewGroup: Grupo de Vistas
    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {

        // View holder pattern. Usamos esto para dar un mejor rendimiento a la app
        final ViewHolder holder;

        // Si convertView nunca ha sido inflada la inflamos
        if (convertView == null) {
            // Tomamos un objeto de tipo LayoutInflater ("Inflador de layout"?) del contexto
            // y lo usamos para inflar la vista que venia por defecto con el layout que creamos
            // (R.item_category)
            LayoutInflater layoutInflater = LayoutInflater.from(this.context);
            convertView = layoutInflater.inflate(R.layout.item, null);

            // Creamos el objeto holder
            holder = new ViewHolder();

            // lo llenamos
            holder.nameTextView = (TextView) convertView.findViewById(R.id.category);
            holder.togglePlay = convertView.findViewById(R.id.toggleplay);
            holder.play = convertView.findViewById(R.id.play);
            holder.share = (ImageButton) convertView.findViewById(R.id.share);
            holder.download = (ImageButton) convertView.findViewById(R.id.download);

            final MediaPlayer mediaPlayer = MediaPlayer.create(AdapterCategory.this.context, soundList[position]);

            holder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        holder.play.setImageResource(R.drawable.play);
                    } else {
                        mediaPlayer.start();
                        //holder.play.setImageResource(R.drawable.pause);
                    }
                    // holder.play.setImageResource(R.drawable.play);
                }

            });

//
//            holder.togglePlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    // Creamos el media player fuera del boton asi no se superpone
//
//                    if (holder.togglePlay.isChecked()) {
//                        mediaPlayer.start();
//                        //holder.play.setImageResource(R.drawable.play);
//                    } else {
//                        mediaPlayer.pause();
//                        //holder.play.setImageResource(R.drawable.pause);
//                    }
//                }
//            });

            holder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_SEND);

                    // set flag to give temporary permission to external app to use your FileProvider
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    File archivo = new File(copyFiletoCacheStorage(soundList[position], position));
                    //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/Download/samSounds"+position+".mp3")))
                    Uri path = FileProvider.getUriForFile(context, "com.botonera.zurdelli.labotoneradesam", archivo);

                    intent.putExtra(Intent.EXTRA_STREAM, path);
                    intent.setType("audio/mp3");
                    context.startActivity(Intent.createChooser(intent, "Share sound"));
                }
            });

            holder.download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new File(copyFiletoExternalStorage(soundList[position], position));
                    Toast.makeText(AdapterCategory.this.context, "Descargando... " + Environment.getExternalStorageDirectory() + "/Music", Toast.LENGTH_LONG).show();

                    createNotification(position);

                }
            });


            // setTag guarda el tag en noseque
            convertView.setTag(holder);

            // Si ya ha sido inflada solo la llamamos de nuevo
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Traemos el valor actual dependiente de la posicion
        String currentName = names.get(position);

        // Referenciamos el objeto a modificar y lo rellenamos
        holder.nameTextView.setText(currentName);

        //devolvemos la lista inflada
        return convertView;
    }

    static class ViewHolder {
        private TextView nameTextView;
        private ToggleButton togglePlay;
        private ImageButton play;
        private ImageButton share;
        private ImageButton download;
    }

    private String copyFiletoCacheStorage(int resourceId, int position) {

        File file = new File(this.context.getCacheDir(), "samsounds" + position + ".mp3");
        try {
            InputStream in = context.getResources().openRawResource(resourceId);
            FileOutputStream out = new FileOutputStream(file);

            byte[] buff = new byte[1024];
            int read = 0;
            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    private String copyFiletoExternalStorage(int resourceId, int position) {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "samsounds" + position + ".mp3");
        try {
            InputStream in = context.getResources().openRawResource(resourceId);
            FileOutputStream out = new FileOutputStream(file);

            byte[] buff = new byte[1024];
            int read = 0;
            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    private void createNotification(int i){
        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.context.getApplicationContext(), "notify_001");
        Intent ii = new Intent(this.context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(Environment.getExternalStorageState() + "Music/samsounds"+i+".mp3");
        bigText.setBigContentTitle("Tu archivo se guardara en la ruta");
        bigText.setSummaryText("Text in detail");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.notification_icon);
        mBuilder.setContentTitle("Que me contas guachin");
        mBuilder.setContentText("todo bien y vos ?");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        mNotificationManager =
                (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "YOUR_CHANNEL_ID";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());}
}

