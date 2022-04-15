package com.hadjhadji.adminmasjidna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Button sadaka_btn, salat_btn, notif_btn;
    LinearLayout salatLL, sadakaLL, notifLL;

    //salat
    static String fajr_str, dohr_str, asr_str, maghreb_str, isha_str;
    EditText fajr_et,dohr_et,asr_et,maghreb_et,isha_et;
    TextView salat_date_tv;
    Button update_btn, previous_btn, next_btn;


    //sadaka
    static int sadaka_total_str, sadaka_lastmonth_str, sadaka_thismonth_str, sadaka_lastjumua_str;
    static String sadaka_date_str, sadaka_amount_str;
    TextView sadaka_total_tv, sadaka_lastmonth_tv, sadaka_lastjumua_tv, sadaka_thismonth_tv;
    EditText sadaka_date_et, sadaka_amount_et;
    Button sadaka_add_btn;

    //notif
    static String notif_title_str, notif_message_str;
    EditText notif_title_et, notif_message_et;
    Button notif_send_btn;

    //FirebaseDatabase database
    FirebaseDatabase database;

    //Calendar
    static Calendar calendar;

    static String euro = "€";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //defining views
        salatLL = findViewById(R.id.salatLL);
        sadakaLL = findViewById(R.id.sadakaLL);
        notifLL = findViewById(R.id.notifLL);
        salatLL.setVisibility(View.VISIBLE);
        sadakaLL.setVisibility(View.INVISIBLE);
        notifLL.setVisibility(View.INVISIBLE);

        sadaka_btn = findViewById(R.id.sadaka_btn);
        salat_btn = findViewById(R.id.salat_btn);
        notif_btn = findViewById(R.id.notif_btn);

        //Salat
        fajr_et = findViewById(R.id.fajt_et);
        dohr_et = findViewById(R.id.dohr_et);
        asr_et = findViewById(R.id.asr_et);
        maghreb_et = findViewById(R.id.maghreb_et);
        isha_et = findViewById(R.id.isha_et);
        salat_date_tv = findViewById(R.id.salat_date_tv);
        update_btn = findViewById(R.id.update_btn);
        previous_btn = findViewById(R.id.previous_btn);
        next_btn = findViewById(R.id.next_btn);

        //Sadaka
        sadaka_total_tv = findViewById(R.id.sadaka_total_tv);
        sadaka_lastmonth_tv = findViewById(R.id.sadaka_lastmonth_tv);
        sadaka_thismonth_tv = findViewById(R.id.sadaka_thismonth_tv);
        sadaka_lastjumua_tv = findViewById(R.id.sadaka_lastjumua_tv);
        sadaka_date_et = findViewById(R.id.sadaka_date_et);
        sadaka_amount_et = findViewById(R.id.sadaka_amount_et);
        sadaka_add_btn = findViewById(R.id.sadaka_add_btn);

        //notif
        notif_title_et = findViewById(R.id.notif_title_et);
        notif_message_et = findViewById(R.id.notif_message_et);
        notif_send_btn = findViewById(R.id.notif_send_btn);

        //Handling Layout Change Buttons
        salat_btn.setOnClickListener((view -> {
            salatLL.setVisibility(View.VISIBLE);
            sadakaLL.setVisibility(View.GONE);
            notifLL.setVisibility(View.GONE);
            refreshSalat(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
        }));
        sadaka_btn.setOnClickListener((view -> {
            salatLL.setVisibility(View.GONE);
            sadakaLL.setVisibility(View.VISIBLE);
            notifLL.setVisibility(View.GONE);
            refreshSadaka();
        }));
        notif_btn.setOnClickListener((view -> {
            salatLL.setVisibility(View.GONE);
            sadakaLL.setVisibility(View.GONE);
            notifLL.setVisibility(View.VISIBLE);
            refreshNotif();
        }));

        //refreshing views
        final String[] currentDate = {new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())};
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        refreshSalat(currentDate[0]);
        refreshSadaka();
        refreshNotif();


        database = FirebaseDatabase.getInstance();

        //Salat handling


        update_btn.setOnClickListener((view -> {
            try {
                DatabaseReference myRef = database.getReference("Salat").child(currentDate[0]);
                myRef.child("fajr").setValue(fajr_et.getText().toString());
                myRef.child("dohr").setValue(dohr_et.getText().toString());
                myRef.child("asr").setValue(asr_et.getText().toString());
                myRef.child("maghreb").setValue(maghreb_et.getText().toString());
                myRef.child("isha").setValue(isha_et.getText().toString());
            } catch (Exception e){
                e.printStackTrace();
            }
        }));
        previous_btn.setOnClickListener(view -> {
            calendar.add(Calendar.DAY_OF_MONTH,-1);
            currentDate[0] = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.getTime());
            refreshSalat(currentDate[0]);
            salat_date_tv.setText(currentDate[0]);
        });
        next_btn.setOnClickListener(view -> {
            calendar.add(Calendar.DAY_OF_MONTH,1);
            currentDate[0] = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.getTime());
            refreshSalat(currentDate[0]);
            salat_date_tv.setText(currentDate[0]);
        });


        //Sadaka handling
        sadaka_add_btn.setOnClickListener(view -> {
            refreshSadaka();
            sadaka_date_str = sadaka_date_et.getText().toString();
            sadaka_amount_str = sadaka_amount_et.getText().toString();
            sadaka_lastmonth_str = calculateLastMonth();
            sadaka_thismonth_str = calculateThisMonth();
            sadaka_total_str = calculateTotal();

            if (sadaka_date_str.length()>0 && sadaka_amount_str.length()>0 ){
                try {

                    int amount_to_add = Integer.parseInt(sadaka_amount_str);
                    DatabaseReference myRef = database.getReference("Jummuas");

                    myRef.child("history").child(sadaka_date_str).setValue(amount_to_add);
                    myRef.child("general").child("last_jum").setValue(amount_to_add);

                    sadaka_amount_et.setText("");
                    sadaka_date_et.setText("");

                    refreshSadaka();

                    Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error! "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please fill the date and amount!", Toast.LENGTH_SHORT).show();

            }

        });


        //Notif handling
        notif_send_btn.setOnClickListener(view -> {
            try {
                notif_title_str = notif_title_et.getText().toString();
                notif_message_str = notif_message_et.getText().toString();
                DatabaseReference myRef = database.getReference("Notifications");
                if (notif_title_str.length()>0 & notif_message_str.length()>0){

                    String id = generateRandomString();
                    myRef.child("Last").child("id").setValue(id);
                    myRef.child("Last").child("title").setValue(notif_title_str);
                    myRef.child("Last").child("message").setValue(notif_message_str);
                    myRef.child("Last").child("timestamp").setValue(System.currentTimeMillis());

                    //for the record
                    myRef.child("history").child(""+System.currentTimeMillis()).child("id").setValue(id);
                    myRef.child("history").child(""+System.currentTimeMillis()).child("title").setValue(notif_title_str);
                    myRef.child("history").child(""+System.currentTimeMillis()).child("message").setValue(notif_message_str);

                    Toast.makeText(getApplicationContext(), "Sent!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), "Please fill the title and message.", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e){
                Toast.makeText(getApplicationContext(), "Error! "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




    }


    // Methods


    int calculateTotal(){
        final int[] total = {0};
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Jummuas").child("history");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    int toAdd = Integer.parseInt("" + dataSnapshot.getValue());
                    total[0] += toAdd;
                    Log.e("Total", (total[0] - toAdd) + " + " + toAdd + " = " + total[0]);
                    /*if (!dataSnapshot.getKey().contains("t")) {
                        the code up
                    }*/
                }
                storeCalculations("total",total[0]);
                sadaka_total_tv.setText(euro + total[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return total[0];
    }

    int calculateThisMonth(){
        final int[] thisMonth = {0};
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Jummuas").child("history");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey().substring(3).equals(currentDate.substring(3))){
                        int toAdd = Integer.parseInt(""+dataSnapshot.getValue());
                        thisMonth[0] += toAdd;
                        Log.e("ThisMonth",(thisMonth[0]-toAdd) + " + " + toAdd + " = " + thisMonth[0]);
                    }
                }
                storeCalculations("this_month",thisMonth[0]);
                sadaka_thismonth_tv.setText(euro + thisMonth[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return thisMonth[0];
    }

    int calculateLastMonth(){
        final int[] lastMonth = {0};
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH,-1);
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.getTime());
        calendar.setTime(new Date());
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Jummuas").child("history");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey().substring(3).equals(currentDate.substring(3))){
                        int toAdd = Integer.parseInt(""+dataSnapshot.getValue());
                        lastMonth[0] += toAdd;
                        Log.e("LastMonth",(lastMonth[0]-toAdd) + " + " + toAdd + " = " + lastMonth[0]);

                    }
                }
                storeCalculations("last_month",lastMonth[0]);
                sadaka_lastmonth_tv.setText(euro + lastMonth[0]);
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return lastMonth[0];
    }




    void refreshSalat(String date){
        salat_date_tv.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Salat").child(date);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    fajr_str = dataSnapshot.child("fajr").getValue(String.class);
                    dohr_str = dataSnapshot.child("dohr").getValue(String.class);
                    asr_str = dataSnapshot.child("asr").getValue(String.class);
                    maghreb_str = dataSnapshot.child("maghreb").getValue(String.class);
                    isha_str = dataSnapshot.child("isha").getValue(String.class);

                    fajr_et.setText(fajr_str);
                    dohr_et.setText(dohr_str);
                    asr_et.setText(asr_str);
                    maghreb_et.setText(maghreb_str);
                    isha_et.setText(isha_str);
                    Log.e("Salat","i reeeeefreeshed !");
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("refreshSalat", "Failed to read value.", error.toException());
            }
        });
    }

    void refreshSadaka(){

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Jummuas").child("general");
        myRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                sadaka_lastjumua_tv.setText("€"+0);
                sadaka_total_str = calculateTotal();
                sadaka_lastmonth_str = calculateLastMonth();
                sadaka_thismonth_str = calculateThisMonth();
                //sadaka_total_tv.setText("€"+sadaka_total_str);
                //sadaka_lastmonth_tv.setText("€"+sadaka_lastmonth_str);
                //sadaka_thismonth_tv.setText("€"+sadaka_thismonth_str);
                sadaka_lastjumua_str = Integer.parseInt(Objects.requireNonNull(""+dataSnapshot.child("last_jum").getValue()));
                sadaka_lastjumua_tv.setText("€"+sadaka_lastjumua_str);

                sadaka_date_et.setText("");
                sadaka_amount_et.setText("");
                Log.e("refreshSadaka","i reeeefresheed!!");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("refresh Sadaka", "Failed to read value.", error.toException());
            }
        });
    }

    void refreshNotif(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Notifications");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*
                notif_title_et.setHint(""+dataSnapshot.child("Last").child("title").getValue());
                notif_message_et.setHint(""+dataSnapshot.child("Last").child("message").getValue());
                */
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("refreshNotif", "Failed to read value.", error.toException());
            }
        });
    }

    private void storeCalculations(String s, int i) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Jummuas").child("general");
        myRef.child(s).setValue(i);
    }


    String generateRandomString(){
        String toReturn ="";
        String range="AZERTYUIOPQSDFGHJKLMWXCVBNazertyuiopqsdfghjklmwxcvbn0123456789";
        for (int i = 0; i < 15; i++) {
            toReturn += range.charAt((int)(Math.random() * (62)));
        }
        return toReturn;
    }
}