package com.macbook.hurmat.bitcoin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity_conversion extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FirebaseAuth firebaseAuth;

    String url = "http://staging.techeasesol.com/coinrates/api.php";

    TextView tvBtcUsd, tvBtcMxn, tvBtcMxntoUsd;
    TextView tvEthUsd, tvEthMxn, tvEthMxntoUsd;
    TextView tvEthToday, tvBtcToday;
    ImageView alertOne, alertTwo;
    TextView dateOne, dateTwo;
    String Date;
    Double compareValue = 18.11;
    Double BTCPriceInUSD,BTCPriceInMXN, ETHPriceInUSD,ETHPriceInMXN,USDtoMXNRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversion_bitcoin);

       // logout = (TextView) findViewById(R.id.logout);
        firebaseAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("Reg", 0);
        editor = sharedPreferences.edit();


        TabHost tabHost =(TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        //tabOne
        TabHost.TabSpec specOne = tabHost.newTabSpec("Bitcoin");
        specOne.setContent(R.id.tabBitcoin);
        specOne.setIndicator("Bitcoin");
        specOne.setIndicator(new Tab(getApplicationContext(), R.drawable.bitcoin_b,"Bitcoin"));
        tabHost.addTab(specOne);


        //tabTwo
        TabHost.TabSpec specTwo = tabHost.newTabSpec("Ethereum");
        specTwo.setContent(R.id.tabEthereum);
        specTwo.setIndicator("Ethereum");
        specTwo.setIndicator(new Tab(getApplicationContext(), R.drawable.ethereum_b,"Ethereum"));
        tabHost.addTab(specTwo);


        tvBtcUsd = (TextView)findViewById(R.id.tvBtc_usd);
        tvBtcMxn = (TextView)findViewById(R.id.tvBtc_mxn);
        tvBtcMxntoUsd = (TextView)findViewById(R.id.tvBtc_MxnUsd);
        tvEthUsd = (TextView)findViewById(R.id.tvEth_Usd);
        tvEthMxn = (TextView)findViewById(R.id.tvEth_mxn);
        tvEthMxntoUsd =(TextView)findViewById(R.id.tvEth_MxnUsd);
        tvBtcToday =(TextView)findViewById(R.id.tvBtcToday);
        tvEthToday =(TextView) findViewById(R.id.tvEthToday);
        alertOne = (ImageView) findViewById(R.id.imgAlert); //bitcoin
        alertTwo =(ImageView) findViewById(R.id.imgAlertTwo); //Ethereum
        dateOne = (TextView) findViewById(R.id.tvDate1);
        dateTwo = (TextView) findViewById(R.id.tvDate2);

        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        Date = sdf.format(date);

        dateOne.setText(Date);
        dateTwo.setText(Date);


        alertOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showValueBox();
            }
        });

        alertTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showValueBox();
            }
        });


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1 * 60 * 1000); // every 1 minutes
                coinRates();
            }
        }, 0);

    }

    public void coinRates(){

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            BTCPriceInUSD = jsonObject.getDouble("BTCPriceInUSD");
                            tvBtcUsd.setText(String.valueOf(BTCPriceInUSD));
                            tvBtcToday.setText(String.valueOf(BTCPriceInUSD));

                            BTCPriceInMXN = jsonObject.getDouble("BTCPriceInMXN");
                            tvBtcMxn.setText(String.valueOf(BTCPriceInMXN));

                            ETHPriceInUSD = jsonObject.getDouble("ETHPriceInUSD");
                            tvEthUsd.setText(String.valueOf(ETHPriceInUSD));
                            tvEthToday.setText(String.valueOf(ETHPriceInUSD));

                            ETHPriceInMXN = jsonObject.getDouble("ETHPriceInMXN");
                            tvEthMxn.setText(String.valueOf(ETHPriceInMXN));
                            USDtoMXNRate =  jsonObject.getDouble("USDtoMXNRate");
                            tvEthMxntoUsd.setText(String.valueOf(USDtoMXNRate));
                            tvBtcMxntoUsd.setText(String.valueOf(USDtoMXNRate));


                            if(USDtoMXNRate>=compareValue){

                                showNotification();
                                showAlert();
                                compareValue= compareValue+1.00;

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity_conversion.this, error.toString(), Toast.LENGTH_SHORT).show();


            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    public void showValueBox(){
        
        final View mView = getLayoutInflater().inflate(R.layout.value_box, null);
        final EditText value = (EditText)mView.findViewById(R.id.etValue);
        Button save =(Button)mView.findViewById(R.id.btnSave);

        value.setText(compareValue.toString());
        final AlertDialog valueBox = new AlertDialog.Builder(MainActivity_conversion.this).create();
        valueBox.setView(mView);
        valueBox.show();
        valueBox.setCancelable(true);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                compareValue = Double.parseDouble(value.getText().toString());
                valueBox.dismiss();


            }
        });
        

    }

    public void showNotification(){
        Intent intent = new Intent(this, MainActivity_conversion.class);

        int pendingIntentId = 0;
        PendingIntent pendindIntent = PendingIntent.getActivity(this,pendingIntentId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentIntent(pendindIntent)
                .setContentTitle("Bitcoin")
                .setContentText("The rate of ETH crossed "+ compareValue)
                .setSmallIcon(R.drawable.notification_bitcoin)
                .setWhen(Calendar.getInstance().getTimeInMillis())
                .setAutoCancel(true);

        Notification notification = builder.build();
        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         int notificationId = 0;
        notificationManager.notify(notificationId,notification);


    }

    public void showAlert(){

        final View mView = getLayoutInflater().inflate(R.layout.alert_box, null);
        TextView tvValue = (TextView) mView.findViewById(R.id.tvValue);
        Button oky = (Button) mView.findViewById(R.id.btnOky);
        TextView alertDate =(TextView) mView.findViewById(R.id.tvAlertDate);

        alertDate.setText(Date);

        tvValue.setText(compareValue.toString());
        final AlertDialog alertBox = new AlertDialog.Builder(MainActivity_conversion.this).create();
        alertBox.setView(mView);
        alertBox.show();
        alertBox.setCancelable(true);

        oky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertBox.cancel();
            }
        });

    }

}


