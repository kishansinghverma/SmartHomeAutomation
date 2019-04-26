package com.example.smarthomeautomation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Switch fansw;
    Switch bulbsw;
    TextView temptv;
    TextView fantv;
    TextView bulbtv;

    ImageView faniv;
    ImageView bulbiv;

    ProgressBar progressBar;
    LinearLayout linearLayout;

    FirebaseDatabase database;
    Button update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        update=findViewById(R.id.update);
        fansw=findViewById(R.id.btnfan);
        bulbsw=findViewById(R.id.btnbulb);
        temptv =findViewById(R.id.tvtemp);
        fantv=findViewById(R.id.tvfan);
        bulbtv=findViewById(R.id.tvbulb);
        faniv=findViewById(R.id.ifan);
        bulbiv=findViewById(R.id.ibulb);
        progressBar=findViewById(R.id.progressBar);
        linearLayout=findViewById(R.id.linearLayout);

        initialize();

        database=FirebaseDatabase.getInstance();
        DatabaseReference smoke=database.getReference("smoke");
        smoke.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    makeNotificationChannel("CHANNEL", "Priority Channel", NotificationManager.IMPORTANCE_DEFAULT);
                }

                NotificationCompat.Builder notification = new NotificationCompat.Builder(MainActivity.this, "CHANNEL_1");
                notification.setSmallIcon(R.mipmap.ic_launcher);
                notification.setContentTitle("Alert!");
                notification.setContentText("Smoke Detected In Your House!");

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification.build());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        faniv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fansw.isChecked())
                    fansw.setChecked(false);
                else
                    fansw.setChecked(true);
                fanUpdate(v);
            }
        });

        bulbiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bulbsw.isChecked())
                    bulbsw.setChecked(false);
                else
                    bulbsw.setChecked(true);
                bulbUpdate(v);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                updateDHT();
            }
        });

    }

    private void updateDHT() {
        RequestQueue getQueue = Volley.newRequestQueue(this);
        String url = "https://erratic-porcupine-6148.dataplicity.io/cgi-bin/get_temp.py";
        StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            progressBar.setVisibility(View.GONE);
                            String[] status = response.trim().split("/");
                            float tmp = Float.parseFloat(status[0]);
                            float humid = Float.parseFloat(status[1]);
                            temptv.setText(String.valueOf(tmp) + "\u00B0C~" + humid + "%\nLast Updated\n" + status[2]);
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(MainActivity.this, "Connection Failed!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.d("Error.Response", response);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                return params;
            }
        };
        getQueue.add(getRequest);
    }

    private void initialize() {
        RequestQueue getQueue = Volley.newRequestQueue(this);
        String url = "https://erratic-porcupine-6148.dataplicity.io/cgi-bin/get_data.py";
        StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String[] status = response.trim().split("/");
                            float tmp = Float.parseFloat(status[2]);
                            float humid = Float.parseFloat(status[3]);

                            temptv.setText(String.valueOf(tmp) + "\u00B0C~" + humid + "%\nLast Updated\n" + status[4]);

                            if (Float.parseFloat(status[0]) == 1) {
                                fantv.setText("Fan Is On!");
                                fantv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorskyblue));
                                fansw.setChecked(true);
                                faniv.setVisibility(View.GONE);
                                (findViewById(R.id.gif)).setVisibility(View.VISIBLE);
                            } else {
                                fantv.setText("Fan Is Off!");
                                fantv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorred));
                                fansw.setChecked(false);
                                (findViewById(R.id.gif)).setVisibility(View.GONE);
                                faniv.setVisibility(View.VISIBLE);

                            }
                            if (Float.parseFloat(status[1]) == 1) {
                                bulbtv.setText("Bulb Is On!");
                                bulbtv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorskyblue));
                                bulbsw.setChecked(true);
                                bulbiv.setImageResource(R.drawable.bon);

                            } else {
                                bulbtv.setText("Bulb Is Off!");
                                bulbtv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorred));
                                bulbsw.setChecked(false);
                                bulbiv.setImageResource(R.drawable.boff);
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Connection Failed!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.d("Error.Response", response);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                return params;
            }
        };
        getQueue.add(getRequest);
    }

    public void fanUpdate(View v)
    {
        RequestQueue getQueue = Volley.newRequestQueue(this);
        String url = "https://erratic-porcupine-6148.dataplicity.io/cgi-bin/fan_handler.py";
        StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                     if(response.trim().equals("0"))
                     {
                         fantv.setText("Fan Is Off!");
                         fantv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorred));
                         fansw.setChecked(false);
                         (findViewById(R.id.gif)).setVisibility(View.GONE);
                         faniv.setVisibility(View.VISIBLE);
                     }
                     else if (response.trim().equals("1"))
                     {
                         fantv.setText("Fan Is On!");
                         fantv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorskyblue));
                         fansw.setChecked(true);
                         faniv.setVisibility(View.GONE);
                         (findViewById(R.id.gif)).setVisibility(View.VISIBLE);
                     }
                     else
                         Toast.makeText(MainActivity.this, "Connection Failed!!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String>  params = new HashMap<String, String>();
                if(fansw.isChecked())
                    params.put("fan", "1");
                else
                    params.put("fan", "0");
                return params;
            }
        };
        getQueue.add(getRequest);

    }

    public void bulbUpdate(View v)
    {
        RequestQueue getQueue = Volley.newRequestQueue(this);
        String url = "https://erratic-porcupine-6148.dataplicity.io/cgi-bin/bulb_handler.py";
        StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.trim().equals("0"))
                        {
                            bulbtv.setText("Bulb Is Off!");
                            bulbtv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorred));
                            bulbsw.setChecked(false);
                            bulbiv.setImageResource(R.drawable.boff);
                        }
                        else if (response.trim().equals("1"))
                        {
                            bulbtv.setText("Bulb Is On!");
                            bulbtv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorskyblue));
                            bulbsw.setChecked(true);
                            bulbiv.setImageResource(R.drawable.bon);
                        }
                        else
                            Toast.makeText(MainActivity.this, "Connection Failed!!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String>  params = new HashMap<String, String>();
                if(bulbsw.isChecked())
                    params.put("bulb", "1");
                else
                    params.put("bulb", "0");
                return params;
            }
        };
        getQueue.add(getRequest);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void makeNotificationChannel(String id, String name, int importance)
    {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
}
