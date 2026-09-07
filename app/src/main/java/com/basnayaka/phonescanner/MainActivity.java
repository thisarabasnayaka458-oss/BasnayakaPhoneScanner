package com.basnayaka.phonescanner;

import android.content.*; import android.os.*; import android.view.*; import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.*;
import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    EditText serverUrl, manualCode; TextView status; SharedPreferences prefs;
    @Override public void onCreate(Bundle b){ super.onCreate(b); setContentView(R.layout.activity_main);
        serverUrl=findViewById(R.id.serverUrl); manualCode=findViewById(R.id.manualCode); status=findViewById(R.id.status); prefs=getSharedPreferences("scanner",MODE_PRIVATE);
        serverUrl.setText(prefs.getString("url",""));
        findViewById(R.id.saveBtn).setOnClickListener(v->{prefs.edit().putString("url",serverUrl.getText().toString().trim()).apply(); status.setText("Status: PC address saved");});
        findViewById(R.id.sendBtn).setOnClickListener(v->send(manualCode.getText().toString().trim()));
        findViewById(R.id.scanBtn).setOnClickListener(v->new IntentIntegrator(this).setPrompt("Scan a barcode").setBeepEnabled(true).setOrientationLocked(true).initiateScan());
    }
    @Override protected void onActivityResult(int r,int c,Intent d){ IntentResult x=IntentIntegrator.parseActivityResult(r,c,d); if(x!=null){ if(x.getContents()!=null){ manualCode.setText(x.getContents()); send(x.getContents()); } else status.setText("Status: Scan cancelled"); } else super.onActivityResult(r,c,d); }
    void send(String code){ String url=serverUrl.getText().toString().trim(); if(code.isEmpty()){status.setText("Status: Enter or scan a barcode"); return;} if(url.isEmpty()){status.setText("Status: Enter PC URL first"); return;} status.setText("Status: Sending "+code+"..."); new Thread(()->{try{
        HttpURLConnection h=(HttpURLConnection)new URL(url).openConnection(); h.setConnectTimeout(5000); h.setReadTimeout(5000); h.setRequestMethod("POST"); h.setDoOutput(true); h.setRequestProperty("Content-Type","application/json");
        String body="{\"barcode\":\""+code.replace("\\","\\\\").replace("\"","\\\"")+"\"}"; try(OutputStream o=h.getOutputStream()){o.write(body.getBytes(StandardCharsets.UTF_8));}
        int rc=h.getResponseCode(); runOnUiThread(()->status.setText(rc>=200&&rc<300?"Status: Sent to POS ✓":"Status: POS error: HTTP "+rc));
    }catch(Exception e){runOnUiThread(()->status.setText("Status: Cannot connect to PC - "+e.getMessage()));}}).start(); }
}
