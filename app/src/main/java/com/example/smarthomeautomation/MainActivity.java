package com.example.smarthomeautomation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Button update;
    Switch fansw;
    Switch bulbsw;
    TextView temptv;
    TextView fantv;
    TextView bulbtv;

    ImageView faniv;
    ImageView bulbiv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        update=findViewById(R.id.btntmp);
        fansw=findViewById(R.id.btnfan);
        bulbsw=findViewById(R.id.btnbulb);
        temptv =findViewById(R.id.tvtemp);
        fantv=findViewById(R.id.tvfan);
        bulbtv=findViewById(R.id.tvbulb);
        faniv=findViewById(R.id.ifan);
        bulbiv=findViewById(R.id.ibulb);

        doGet();

        faniv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fansw.isChecked())
                    fansw.setChecked(false);
                else
                    fansw.setChecked(true);
                doPost(v);
            }
        });

        bulbiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bulbsw.isChecked())
                    bulbsw.setChecked(false);
                else
                    bulbsw.setChecked(true);
                doPost(v);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGet();
            }
        });

    }

    public void doGet()
    {
        RequestQueue getQueue = Volley.newRequestQueue(this);
        String url = "http://kloud.cf/getStatus.php";
        StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] status = response.split("/");
                        float tmp = Float.parseFloat(status[2]);
                        temptv.setText(String.valueOf(tmp) + " C\nLast Updated\n" + status[3]);

                        if (Float.parseFloat(status[0]) == 1) {
                            fantv.setText("Fan Is On!");
                            fantv.setTextColor(getResources().getColor(R.color.colorskyblue));
                            fansw.setChecked(true);
                        } else {
                            fantv.setText("Fan Is Off!");
                            fantv.setTextColor(getResources().getColor(R.color.colorred));
                            fansw.setChecked(false);
                        }
                        if (Float.parseFloat(status[1]) == 1) {
                            bulbtv.setText("Bulb Is On!");
                            bulbtv.setTextColor(getResources().getColor(R.color.colorskyblue));
                            bulbsw.setChecked(true);
                        } else {
                            bulbtv.setText("Bulb Is Off!");
                            bulbtv.setTextColor(getResources().getColor(R.color.colorred));
                            bulbsw.setChecked(false);
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


    public void doPost(View view) {
        RequestQueue postQueue = Volley.newRequestQueue(this);
        String url = "http://kloud.cf/postStatus.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.d("Error.Response", response);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                if (fansw.isChecked())
                    params.put("fan", "1");
                else
                    params.put("fan", "0");
                if (bulbsw.isChecked())
                    params.put("bulb", "1");
                else
                    params.put("bulb", "0");

                return params;
            }
        };
        postQueue.add(postRequest);
        postQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<StringRequest>() {
            @Override
            public void onRequestFinished(Request<StringRequest> request) {
                doGet();
            }
        });

    }
}
