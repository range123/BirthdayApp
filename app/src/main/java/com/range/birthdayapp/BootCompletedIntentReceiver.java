package com.range.birthdayapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    private String CHANNEL_ID = "123";
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent intent1=new Intent(context,first_Activity.class);
            intent1.putExtra("reboot",true);
            PendingIntent pwintent=PendingIntent.getActivity(context,0,intent1,0);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("RE-BOOT")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(intent.getStringExtra("Your Device Rebooted,Please click here to continue receiving notifications")))
                    .setSmallIcon(R.drawable.notificon)
                    .setContentIntent(pwintent)
                    .setAutoCancel(true)
                    .setOngoing(true);
            Toast.makeText(context, "Serivce started", Toast.LENGTH_SHORT).show();
            NotificationManagerCompat nm = NotificationManagerCompat.from(context);
            //Toast.makeText(SendDataService.this, "hello "+intent.getLongExtra("phn",0), Toast.LENGTH_SHORT).show();
            nm.notify(21, mBuilder.build());
        }
    }
}
