package com.example.smarthomeautomation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

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

    CountDownTimer countDownTimer;

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

        countDownTimer=new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                Snackbar.make(linearLayout, "Error : Unstable Internet Connection!!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDHT();
                    }
                })
                        .setActionTextColor(getResources().getColor(R.color.colorylo))
                        .show();
                progressBar.setVisibility(View.GONE);
            }
        };

        initialize();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel("CHANNEL", "Priority Channel2", NotificationManager.IMPORTANCE_DEFAULT);
        }


        database=FirebaseDatabase.getInstance();
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(MainActivity.this, "CHANNEL");


        final DatabaseReference water=database.getReference("water");
        water.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notification.setSmallIcon(R.drawable.alert);
                notification.setContentTitle("Alert!");
                notification.setContentText("Water Tank Is Filled!");
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if(!dataSnapshot.getValue().toString().trim().equals("0"))
                    notificationManager.notify(1, notification.build());
                else
                    notificationManager.cancel(1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference smoke=database.getReference("smoke");
        smoke.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notification.setSmallIcon(R.drawable.critacal);
                notification.setContentTitle("Alert!");
                notification.setContentText("Smoke Detected In Your House!");
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if(!dataSnapshot.getValue().toString().trim().equals("0"))
                    notificationManager.notify(2, notification.build());
                else
                    notificationManager.cancel(2);
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
                updateDHT();
            }
        });

    }

    private void updateDHT() {
        countDownTimer.start();
        progressBar.setVisibility(View.VISIBLE);
        final RequestQueue getQueue = Volley.newRequestQueue(this);
        String url = "https://erratic-porcupine-6148.dataplicity.io/cgi-bin/get_temp.py";
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        countDownTimer.cancel();
                        try {
                            progressBar.setVisibility(View.GONE);
                            String[] status = response.trim().split("/");
                            float tmp = Float.parseFloat(status[0]);
                            float humid = Float.parseFloat(status[1]);
                            temptv.setText(String.valueOf(tmp) + "\u00B0C~" + humid + "%\nLast Updated\n" + status[2]);
                        }
                        catch (Exception e)
                        {
                            Snackbar.make(linearLayout, "Error : Server Might Be Down!!", Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateDHT();
                                }
                            }).setActionTextColor(getResources().getColor(R.color.colorylo)).show();
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
        getRequest.setTag("requests");
        getQueue.add(getRequest);
        new CountDownTimer(10000, 1000)
        {

            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                getQueue.cancelAll("requests");
            }
        }.start();
    }

    private void initialize() {
        final CountDownTimer timer=new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                Snackbar.make(linearLayout, "Error : Unstable Internet Connection!!", Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initialize();
                    }
                }).setActionTextColor(getResources().getColor(R.color.colorylo)).show();
            }
        }.start();
        final RequestQueue getQueue = Volley.newRequestQueue(this);
        String url = "https://erratic-porcupine-6148.dataplicity.io/cgi-bin/get_data.py";
        final StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        timer.cancel();
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
                            Snackbar.make(linearLayout, "Error : Server Might Be Down!!", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Retry", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            initialize();
                                        }
                                    })
                                    .setActionTextColor(getResources().getColor(R.color.colorylo))
                                    .show();
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
        getRequest.setTag("requests");
        getQueue.add(getRequest);

        new CountDownTimer(10000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                getQueue.cancelAll("requests");
            }
        }.start();
    }

    public void fanUpdate(View v)
    {
        final CountDownTimer timer=new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                Snackbar.make(linearLayout, "Error : Unstable Internet Connection!!", Snackbar.LENGTH_LONG)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fanUpdate(linearLayout);
                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.colorylo))
                        .show();
            }
        }.start();
        final RequestQueue getQueue = Volley.newRequestQueue(this);
        String url = "https://erratic-porcupine-6148.dataplicity.io/cgi-bin/fan_handler.py";
        StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        timer.cancel();
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
                         Snackbar.make(linearLayout, "Error : Server Might Be Down!!", Snackbar.LENGTH_LONG)
                                .setAction("Retry", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fanUpdate(linearLayout);
                                    }
                                })
                                .setActionTextColor(getResources().getColor(R.color.colorylo))
                                .show();
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
        getRequest.setTag("requests");
        getQueue.add(getRequest);
        new CountDownTimer(10000,1000)
        {

            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                getQueue.cancelAll("requests");
            }
        }.start();
    }

    public void bulbUpdate(View v)
    {
        final CountDownTimer timer=new CountDownTimer(10000 ,1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                Snackbar.make(linearLayout, "Error : Unstable Internet Connection!!", Snackbar.LENGTH_LONG)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bulbUpdate(linearLayout);
                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.colorylo))
                        .show();
            }
        }.start();
        final RequestQueue getQueue = Volley.newRequestQueue(this);
        String url = "https://erratic-porcupine-6148.dataplicity.io/cgi-bin/bulb_handler.py";
        StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        timer.cancel();

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
                            Snackbar.make(linearLayout, "Error : Server Might Be Down!!", Snackbar.LENGTH_LONG)
                                    .setAction("Retry", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            bulbUpdate(linearLayout);
                                        }
                                    })
                                    .setActionTextColor(getResources().getColor(R.color.colorylo))
                                    .show();
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
        getRequest.setTag("requests");
        getQueue.add(getRequest);
        new CountDownTimer(10000, 1000)
        {

            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                getQueue.cancelAll("requests");
            }
        }.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void makeNotificationChannel(String id, String name, int importance)
    {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
}
