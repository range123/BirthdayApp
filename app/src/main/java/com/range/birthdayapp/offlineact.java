package com.range.birthdayapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class offlineact extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SetAlarm();
    }

    /* public class YourService extends Service
        {
            Alarm alarm = new Alarm();
            public void onCreate()
            {
                super.onCreate();
            }

            @Override
            public int onStartCommand(Intent intent, int flags, int startId)
            {
                alarm.setAlarm(this);
                return START_STICKY;
            }

            @Override
            public void onStart(Intent intent, int startId)
            {
                alarm.setAlarm(this);
            }

            @Override
            public IBinder onBind(Intent intent)
            {
                return null;
            }
        }
        public class Alarm extends BroadcastReceiver
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
                wl.acquire();
                Log.d("TAG", "onReceive: hi");

                // Put here YOUR code.
                Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example

                wl.release();
            }

            public void setAlarm(Context context)
            {
                AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                Intent i = new Intent(context, Alarm.class);
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
                am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60, pi); // Millisec * Second * Minute
            }

            public void cancelAlarm(Context context)
            {
                Intent intent = new Intent(context, Alarm.class);
                PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(sender);
            }
        }*/
   /* public void SetAlarm()
    {
       // final Button button = buttons[2]; // replace with a button from your own UI
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent )
            {

                Toast.makeText(context, "Received : "+intent.getStringExtra("pid"), Toast.LENGTH_SHORT).show();
                //button.setBackgroundColor( Color.RED );
                context.unregisterReceiver( this ); // this == BroadcastReceiver, not Activity
            }
        };

        this.registerReceiver( receiver, new IntentFilter("com.blah.blah.somemessage") );

        PendingIntent pintent = PendingIntent.getBroadcast( this, 0, new Intent("com.blah.blah.somemessage").putExtra("pid","Hello"), 0 );

        AlarmManager manager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));

        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000*10, pintent );
    }*/
}
