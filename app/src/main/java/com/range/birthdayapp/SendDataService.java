package com.range.birthdayapp;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.util.Date;

public class SendDataService extends Service {
    private final LocalBinder mBinder = new LocalBinder();
    protected Handler handler;
    protected Toast mToast;
    private String CHANNEL_ID = "123";

    public class LocalBinder extends Binder {
        public SendDataService getService() {
            return SendDataService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                //  if(intent.getIntExtra("key",0)==1)
                // Toast.makeText(SendDataService.this, "70 seconds elapsed", Toast.LENGTH_SHORT).show();
                // else if(intent.getIntExtra("key",0)==2){

                //Toast.makeText(SendDataService.this, "100 seconds elapsed,data = "+intent.getIntExtra("key",-1), Toast.LENGTH_SHORT).show();


                if (intent != null) {
                    PendingIntent pwintent=null;
                    try {
                        String text = "Happy Birthday!!!";// Replace with your message.
                        String number=getSharedPreferences("myprefs", MODE_PRIVATE).getString(intent.getStringExtra("name"),"919600097849");
                        StringBuilder tN=new StringBuilder();
                        if(number.length()<=10)
                        {
                            tN.append("91"+number);
                        }
                        else if(number.length()>12)
                        {
                            tN.append(number.replace("+",""));
                        }



                        //String toNumber = getSharedPreferences("myprefs", MODE_PRIVATE).getString(intent.getStringExtra("name"),"919789068365"); // Replace with mobile phone number without +Sign or leading zeros.
                        String toNumber=tN.toString();


                        Intent wintent = new Intent(Intent.ACTION_VIEW);
                        wintent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
                        pwintent=PendingIntent.getActivity(getApplicationContext(),0,wintent,0);
                        //startActivity(wintent);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    SharedPreferences timepref=getSharedPreferences("timing",MODE_PRIVATE);


                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setContentTitle("Birthday")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(intent.getStringExtra("name") + "'s Birthday is in "+(24-timepref.getInt("hour",21))+" Hours. " + "Be The First To Wish Them"))
                            .setSmallIcon(R.mipmap.iconroundbirth)
                            .setContentIntent(pwintent)
                            .setAutoCancel(true);

                    // mBuilder.setOngoing(true);

                    //mBuilder.build();

                    int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                    NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
                    //Toast.makeText(SendDataService.this, "hello "+intent.getLongExtra("phn",0), Toast.LENGTH_SHORT).show();
                    nm.notify(m, mBuilder.build());
                /*PendingIntent pintent=PendingIntent.getService(getApplicationContext(),intent.getIntExtra("rand",0), intent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(pintent);*/
                    //AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    //PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
                    //alarm.cancel(pintent);
                    // }

// write your code to post content on server
                }
            }
        });
        return android.app.Service.START_STICKY;
    }

}