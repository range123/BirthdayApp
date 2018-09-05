package com.range.birthdayapp;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.TimePicker;
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
    private String CHANNEL_ID = "123";
    SharedPreferences pref = null;

    void delpref() {
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
        Intent i =getIntent();
        if(i.getBooleanExtra("reboot",false))
        {
            delpref();
            restartApp();

        }

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
    final ArrayList<String> uids = new ArrayList<>();
    CustomAdapter customAdapter = null;


    int insert(birthdaypost b) throws java.text.ParseException, IndexOutOfBoundsException {
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
        uids.add(i, b.getUid());
        pids.add(i, b.getPid());
        urls.add(i, b.getPhotourl());
        names.add(i, b.getName());
        customAdapter.notifyDataSetChanged();
        return i;


    }
    void restartApp()
    {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
    int getMin()
    {
        SharedPreferences timepref=getSharedPreferences("timing",MODE_PRIVATE);
        return timepref.getInt("min",0);
    }
    int getHour()
    {
        SharedPreferences timepref=getSharedPreferences("timing",MODE_PRIVATE);
        return timepref.getInt("hour",21);
    }

    void startServ(birthdaypost b, int i) throws java.text.ParseException {
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        Random r = new Random();
        Intent intent = new Intent(getApplicationContext(), SendDataService.class);
        intent.putExtra("name", b.getName())
                .putExtra("pid", b.getPid())
                .putExtra("dob", b.getDob())
                .putExtra("photo", b.getPhotourl())
                .putExtra("phn", Long.parseLong(b.getPhone_number()));

        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), ((int) Long.parseLong(b.getPhone_number())), intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(b.getDob()).getTime());
        c.set(Calendar.YEAR, c.get(Calendar.YEAR));
        c.set(Calendar.MONTH, c.get(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) - 1);
        c.set(Calendar.HOUR_OF_DAY, getHour());
        c.set(Calendar.MINUTE, getMin());
        c.set(Calendar.SECOND, r.nextInt(61));
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pintent);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(b.getPid(), true);
        editor.putString(b.getName(), b.getPhone_number());
        editor.apply();

    }

    public String GetClass() {
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String uname[] = mail.split("@");
        if (!uname[1].equals("cse.ssn.edu.in"))
            return null;
        else {
            String data = uname[0].replaceAll("[^\\d.]", "");
            if (data.charAt(2) == '0' && data.charAt(3) <= '6')
                return "csea";
        }

        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_);
        //Shared preferences for deletion
        pref = getSharedPreferences("myprefs", MODE_PRIVATE);
        //delpref();//remove this later
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        final ListView list_View = (ListView) findViewById(R.id.lv);
        dl = findViewById(R.id.drawer_layout);
        NavigationView nv = findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                dl.closeDrawers();
                if (item.getTitle().equals("Add Birthday")) {
                    Intent i = new Intent(getApplicationContext(), addactivity.class);
                    i.putExtra("class", "default");
                    startActivity(i);
                }
                if (item.getTitle().equals("Users")) {
                    Intent intent = new Intent(getApplicationContext(), Useractivity.class);
                    startActivity(intent);


                }
                if (item.getTitle().equals("Add To Class")) {
                    if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("jayaraman17064@cse.ssn.edu.in")) {
                        AlertDialog.Builder b = new AlertDialog.Builder(first_Activity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                        b.setCancelable(true);
                        b.setTitle("Permission Denied").setMessage("You Dont Have The Permissions To Alter The Class's Data").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });
                        b.show();
                    } else {

                        Intent i = new Intent(getApplicationContext(), addactivity.class);
                        if (GetClass() == null)
                            Toast.makeText(first_Activity.this, "Class Not Yet Added", Toast.LENGTH_SHORT).show();
                        else {
                            i.putExtra("class", GetClass());
                            startActivity(i);
                        }
                    }


                }
                if (item.getTitle().equals("Change Time")) {
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(first_Activity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            SharedPreferences timepref=getSharedPreferences("timing",MODE_PRIVATE);
                            SharedPreferences.Editor edit=timepref.edit();
                            edit.putInt("hour",timePicker.getHour());
                            edit.putInt("min",timePicker.getMinute());
                            edit.apply();
                            Toast.makeText(first_Activity.this, "Notification time changed", Toast.LENGTH_SHORT).show();
                            delpref();
                            restartApp();
                        }
                    }, getHour(), getMin(), true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                }

                if (item.getTitle().equals("Clear Clutter")) {
                    delpref();
                    restartApp();

                }


                return true;
            }
        });


        customAdapter = new CustomAdapter(getApplicationContext(), names, dobs, urls);
        list_View.setAdapter(customAdapter);
        FirebaseDatabase.getInstance().getReference("Birthdays").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                birthdaypost b = dataSnapshot.getValue(birthdaypost.class); //b.getUid().equals(GetClass()))
                if ((b.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) || b.getUid().equals(GetClass()))) {//change the limit?
                    try {
                        int i = insert(b);
                        if (!pref.getBoolean(b.getPid(), false)) {
                            startServ(b, i);

                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                birthdaypost b = dataSnapshot.getValue(birthdaypost.class);
                names.remove(b.getName());
                dobs.remove(b.getDob());
                urls.remove(b.getPhotourl());
                pids.remove(b.getPid());
                uids.remove(b.getUid());
                customAdapter.notifyDataSetChanged();
                String def = "https://firebasestorage.googleapis.com/v0/b/birthdayapp-34806.appspot.com/o/default%2Fdefaultpic.png?alt=media&token=cf644d60-48e8-43bb-b90a-e79cd2021499";
                if (!(b.getPhotourl().equals(def))) {
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
                }
                SharedPreferences.Editor edit = pref.edit();
                edit.remove(b.getPid());
                edit.remove(b.getName());
                edit.apply();
                Intent intent = new Intent(getApplicationContext(), SendDataService.class);
                intent.putExtra("name", b.getName())
                        .putExtra("pid", b.getPid())
                        .putExtra("dob", b.getDob())
                        .putExtra("photo", b.getPhotourl())
                        .putExtra("phn", Long.parseLong(b.getPhone_number()));
                PendingIntent pintent = PendingIntent.getService(getApplicationContext(), (int) Long.parseLong(b.getPhone_number()), intent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(pintent);


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
                if ((uids.get(i).equals("csea") && FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("jayaraman17064@cse.ssn.edu.in")) || uids.get(i).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
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
                } else {
                    AlertDialog.Builder b = new AlertDialog.Builder(first_Activity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                    b.setCancelable(true);
                    b.setTitle("Permission Denied").setMessage("You Dont Have The Permissions To Alter The Class's Data").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.cancel();
                        }
                    });
                    b.show();
                }
                return true;
            }
        });


    }

    public String convert(String d, String curr) {
        String s[] = new String[3];
        String c[] = new String[3];
        s = d.split("/");
        c = curr.split("/");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        StringBuilder end = new StringBuilder();
        if (s[1].compareTo(c[1]) < 0) {
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
            end.append(s[0] + "/" + s[1] + "/" + cal.get(Calendar.YEAR));

        } else if (s[1].equals(c[1]) && s[0].compareTo(c[0]) < 0) {
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
            end.append(s[0] + "/" + s[1] + "/" + cal.get(Calendar.YEAR));

        } else {
            end.append(s[0] + "/" + s[1] + "/" + cal.get(Calendar.YEAR));
        }
        return end.toString();


    }

    public String converttocurrent(String d) throws java.text.ParseException {
        String curr = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());
        Date current = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(curr);
        Date da = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(d);
        String dafinal = convert(d, curr);
        Log.d("TAG", "dafinal = " + dafinal);
        return dafinal;


    }




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
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            TextView name = (TextView) view.findViewById(R.id.name);
            TextView dob = (TextView) view.findViewById(R.id.birthday);
            name.setText(names.get(i));
            dob.setText(dobs.get(i));
            Glide.with(getApplicationContext()).load(Uri.parse(urls.get(i))).into(imageView);
            return view;
        }
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



