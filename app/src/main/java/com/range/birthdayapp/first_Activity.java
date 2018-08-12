package com.range.birthdayapp;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class first_Activity extends AppCompatActivity {
    private String CHANNEL_ID="123";
     SharedPreferences pref=null;
     void delpref()
     {
         pref.edit().clear().apply();
     }

    DrawerLayout dl;

    /**
     * Sets up the options menu.
     *
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_out_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        createNotificationChannel();

    }

    /**
     * Handles a click on the menu option to get a place.
     *
     * @param item The menu item to handle.
     * @return Boolean.
     */


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Sign Out")) {
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            AuthUI.getInstance().signOut(getApplicationContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(getApplicationContext(), "SIGNOUT SUCCESSFULL", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference("Users").child(uid).removeValue();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "SIGNOUT failed", Toast.LENGTH_SHORT).show();

                }
            });
        } else if (item.getTitle().equals("Menu")) {
            if (dl.isDrawerOpen(GravityCompat.START))
                dl.closeDrawers();
            else
                dl.openDrawer(GravityCompat.START);
        }

        return true;
    }

    final ArrayList<String> names = new ArrayList<>();
    final ArrayList<String> dobs = new ArrayList<>();
    final ArrayList<String> urls = new ArrayList<>();
    final ArrayList<String> pids = new ArrayList<>();
    CustomAdapter customAdapter=null;



    int  insert(birthdaypost b) throws java.text.ParseException, IndexOutOfBoundsException {
        int i = 0, j;
        ArrayList<Date> dateobj = new ArrayList<>();
        for (i = 0; i < dobs.size(); i++)
            dateobj.add(i, new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(dobs.get(i)));

        String t;
        b.setDob(converttocurrent(b.getDob()));
        Date dob = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(b.getDob());
        for (i = 0; !dateobj.isEmpty() && i < dateobj.size() && dob.after(dateobj.get(i)); i++) {

        }
        dobs.add(i, b.getDob());
        pids.add(i, b.getPid());
        urls.add(i, b.getPhotourl());
        names.add(i, b.getName());
        customAdapter.notifyDataSetChanged();
        return i;
        //SetAlarm(i,dob);



    }

    void startServ(birthdaypost b,int i) throws java.text.ParseException
    {
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        Random r=new Random();
        Intent intent = new Intent(getApplicationContext(), SendDataService.class);
        intent.putExtra("name",b.getName())
                .putExtra("pid",b.getPid())
                .putExtra("dob",b.getDob())
                .putExtra("photo",b.getPhotourl())
                .putExtra("phn",Long.parseLong(b.getPhone_number()));

        //Log.d("TAG"," "+((int) Long.parseLong(b.getPhone_number())));
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), ((int) Long.parseLong(b.getPhone_number())), intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH).parse(b.getDob()).getTime());
        c.set(Calendar.YEAR,c.get(Calendar.YEAR));
        c.set(Calendar.MONTH,c.get(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH,c.get(Calendar.DAY_OF_MONTH)-1);
        c.set(Calendar.HOUR_OF_DAY,21);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,r.nextInt(61));
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pintent);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(b.getPid(),true);
        editor.putString(b.getName(),b.getPhone_number());
        editor.apply();

        //Toast.makeText(first_Activity.this, "Service Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_);
        //Shared preferences for deletion
        pref = getSharedPreferences("myprefs", MODE_PRIVATE);
        //delpref();//remove this later
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        final ListView list_View = (ListView) findViewById(R.id.lv);
        dl = findViewById(R.id.drawer_layout);
        NavigationView nv = findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                dl.closeDrawers();
                if (item.getTitle().equals("Add Birthday")) {
                    startActivity(new Intent(getApplicationContext(), addactivity.class));
                }
                if (item.getTitle().equals("Start Service")) {

                    /*Intent sendIntent = new Intent("android.intent.action.MAIN");
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setPackage("com.whatsapp");
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra("jid", "919789068365" + "@s.whatsapp.net");// here 91 is country code
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Demo test message");
                    startActivity(sendIntent);*/

                    try {
                        String text = "This is a test";// Replace with your message.

                        String toNumber = "919445299298"; // Replace with mobile phone number without +Sign or leading zeros.


                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
                        startActivity(intent);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }


                }

                if (item.getTitle().equals("Clear Clutter")) {
                    delpref();
                    finish();

                }


                return true;
            }
        });


        customAdapter = new CustomAdapter(getApplicationContext(), names, dobs, urls);
        list_View.setAdapter(customAdapter);
        FirebaseDatabase.getInstance().getReference("Birthdays").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                birthdaypost b = dataSnapshot.getValue(birthdaypost.class);
                if (b.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && dobs.size() < 10) {
                    try {
                        int i=insert(b);
                        if(!pref.getBoolean(b.getPid(),false))
                        {
                            startServ(b,i);

                        }
                        //customAdapter.notifyDataSetChanged();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                   /* names.add(b.getName());
                    dobs.add(b.getDob());
                    urls.add(b.getPhotourl());
                    pids.add(b.getPid());

                    // if(!(names.size()<=1)) {

                    //}
                    customAdapter.notifyDataSetChanged();*/

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                birthdaypost b = dataSnapshot.getValue(birthdaypost.class);
                //String name=b.getName();
                names.remove(b.getName());
                dobs.remove(b.getDob());
                urls.remove(b.getPhotourl());
                pids.remove(b.getPid());
                customAdapter.notifyDataSetChanged();
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://birthdayapp-34806.appspot.com");
                StorageReference ref = storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "/" + b.getName() + ".jpeg");
                ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(first_Activity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(first_Activity.this, "Delete Failed", Toast.LENGTH_SHORT).show();
                    }
                });
                SharedPreferences.Editor edit=pref.edit();
                edit.remove(b.getPid());
                edit.remove(b.getName());
                edit.apply();
                Intent intent = new Intent(getApplicationContext(), SendDataService.class);
                intent.putExtra("name",b.getName())
                        .putExtra("pid",b.getPid())
                        .putExtra("dob",b.getDob())
                        .putExtra("photo",b.getPhotourl())
                        .putExtra("phn",Long.parseLong(b.getPhone_number()));
                PendingIntent pintent=PendingIntent.getService(getApplicationContext(), (int) Long.parseLong(b.getPhone_number()), intent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(pintent);
                /*names.remove(b.getName());
                dobs.remove(b.getDob());
                urls.remove(b.getPhotourl());
                pids.remove(b.getPid());
                customAdapter.notifyDataSetChanged();*/


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //List view listener
        list_View.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int pos = i;
                AlertDialog.Builder builder = new AlertDialog.Builder(first_Activity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                builder.setCancelable(true);
                builder.setTitle("DELETE BIRTHDAY").setMessage("Are You Sure You Want To Delete This Birthday?").setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String pid = pids.get(pos);
                        FirebaseDatabase.getInstance().getReference("Birthdays").child(pid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(first_Activity.this, "Delete Complete", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialogInterface.cancel();


                    }
                })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(first_Activity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                                dialogInterface.cancel();


                            }
                        });
                AlertDialog a = builder.create();
                a.show();
                return true;
            }
        });


    }

    public String convert(String d, String curr) {
        String s[] = new String[3];
        String c[] = new String[3];
        s = d.split("/");
        c = curr.split("/");
        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        StringBuilder end = new StringBuilder();
        if (s[1].compareTo(c[1]) < 0) {
            //end.append(s[0] + "/" + s[1] + "/" + "2019");
            cal.set(Calendar.YEAR,cal.get(Calendar.YEAR)+1);
            end.append(s[0] + "/" + s[1] + "/" + cal.get(Calendar.YEAR));

        } else if (s[1].equals(c[1]) && s[0].compareTo(c[0]) < 0) {
            //end.append(s[0] + "/" + s[1] + "/" + "2019");
            cal.set(Calendar.YEAR,cal.get(Calendar.YEAR)+1);
            end.append(s[0] + "/" + s[1] + "/" + cal.get(Calendar.YEAR));

        } else {
            //end.append(s[0] + "/" + s[1] + "/" + "2018");
            end.append(s[0] + "/" + s[1] + "/" + cal.get(Calendar.YEAR));
        }
        return end.toString();


    }

    public String converttocurrent(String d) throws java.text.ParseException {
        String curr = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());
        Date current = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(curr);
        Date da = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(d);
        //String s[]=new String[3];

        String dafinal = convert(d, curr);
        //else
        //dafinal=da.toString()+"/2018";
        Log.d("TAG", "dafinal = " + dafinal);
        return dafinal;


    }

   /* public void sort(ArrayList<String> names, ArrayList<String> dobs, ArrayList<String> urls, ArrayList<String> pids) throws Exception {
        int i = 0, j = 0;
        String t1, t2, t3, t4;
        ArrayList<Date> dateobj = new ArrayList<Date>();
        for (i = 0; i < dobs.size(); i++)
            dateobj.set(i, new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(dobs.get(i)));
        for (i = 0; i < dateobj.size(); i++) {
            for (j = 0; j < dateobj.size() - i - 1; j++) {
                if (dateobj.get(j + 1).before(dateobj.get(j))) {
                    t1 = dobs.get(j);
                    dobs.set(j, dobs.get(j + 1));
                    dobs.set(j + 1, t1);

                    t2 = names.get(j);
                    names.set(j, names.get(j + 1));
                    names.set(j + 1, t2);

                    t3 = urls.get(j);
                    urls.set(j, urls.get(j + 1));
                    urls.set(j + 1, t3);

                    t4 = pids.get(j);
                    pids.set(j, pids.get(j + 1));
                    pids.set(j + 1, t4);
                }
            }
        }
    }*/

  /*public void sort(ArrayList<String> names,ArrayList<String> dobs,ArrayList<String> urls)
    {
        int i=0,j=0;
        String temp,temp1,temp2;
        for(i=0;i<names.size();i++)
        {
            for(j=0;j<names.size()-i-1;j++)
            {
                String jth=dobs.get(j);
                String j1th=dobs.get(j+1);
                String check[]=new String[3];
                check=dobs.get(j).split("/");
                String s[]=new String[3];
                s[0]=check[0];
                s[1]=check[1];
                if(isBeforecurrent(jth)) {
                    s[2] = "2019";
                }
                else
                    s[2]="2018";
                String nj= new String(s[0].toString()+"/"+s[1].toString()+"/"+s[2].toString());
                String check1[]=new String[3];
                check1=dobs.get(j+1).split("/");
                String s1[]=new String[3];
                s1[0]=check1[0];
                s1[1]=check1[1];
                if(isBeforecurrent(j1th)) {
                    s1[2] = "2019";
                }
                else
                    s1[2]="2018";
                String nj1= new String(s[0].toString()+"/"+s[1].toString()+"/"+s[2].toString());
                Date d1= null;
                try {
                    d1 = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(nj);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date d2= null;
                try {
                    d2 = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH).parse(nj1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String tempn,tempd,tempu;
                Log.d("TAG", "d1 = " +d1+" d2 = "+d2);
                if(d2.before(d1))
                {
                    //Log.d("TAG", "d1 = " +d1+" d2 = "+d2);
                    tempn=names.get(j);
                    names.set(j,names.get(j+1));
                    names.set(j+1,tempn);
                    tempd=dobs.get(j);
                    dobs.set(j,dobs.get(j+1));
                    dobs.set(j+1,tempd);

                    tempu=urls.get(j);
                    urls.set(j,urls.get(j+1));
                    urls.set(j+1,tempu);

                }
            }
        }
    }*/


    class CustomAdapter extends BaseAdapter {
        ArrayList<String> names;
        ArrayList<String> dobs;
        ArrayList<String> urls;
        private Context context;

        CustomAdapter(Context context, ArrayList<String> names, ArrayList<String> dobs, ArrayList<String> urls) {
            this.names = names;
            this.dobs = dobs;
            this.urls = urls;
            this.context = context;
        }

        @Override
        public int getCount() {
            return names.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.list_layout, null);
            //sort(names,dobs,urls);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            TextView name = (TextView) view.findViewById(R.id.name);
            TextView dob = (TextView) view.findViewById(R.id.birthday);
            //imageView.setImageResource(image[i]);
            name.setText(names.get(i));
            dob.setText(dobs.get(i));
            Glide.with(getApplicationContext()).load(Uri.parse(urls.get(i))).into(imageView);
            //dob.setText(dob[i]);
            return view;
        }
    }
    int search(String pid)
    {
        for(int i=0;i<pids.size();i++)
        {
            if(pids.get(i).equals(pid))
                return i;

        }
        return -1;
    }
    //Alarm setter
    public void SetAlarm(int index, Date dob) throws IndexOutOfBoundsException
    {
        // final Button button = buttons[2]; // replace with a button from your own UI
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent )
            {
                String pid=intent.getStringExtra("pid");
                int i=search(pid);

                Toast.makeText(context, "Received : "+names.get(i), Toast.LENGTH_SHORT).show();
                //button.setBackgroundColor( Color.RED );
                context.unregisterReceiver( this ); // this == BroadcastReceiver, not Activity
            }
        };

        this.registerReceiver( receiver, new IntentFilter("com.blah.blah.somemessage") );

        PendingIntent pintent = PendingIntent.getBroadcast( this, 0, new Intent("com.blah.blah.somemessage").putExtra("pid",pids.get(index)), 0 );

        AlarmManager manager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));

        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + dob.getTime()-System.currentTimeMillis()-1000*60*60*2+1000*60*10, pintent );
    }
    //notif channel
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}



