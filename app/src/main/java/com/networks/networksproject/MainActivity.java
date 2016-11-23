package com.networks.networksproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    String WebServer,OS,wordList;
    Integer responseCode;
    RequestQueue queue;
    Boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        queue = Volley.newRequestQueue(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner wordlistSpinner = (Spinner) findViewById(R.id.wordlists);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.directory_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wordlistSpinner.setAdapter(adapter);
        wordlistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wordList = parent.getItemAtPosition(position).toString();
                Log.d("SelectedWordList", wordList);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public String addressValidation(String url){
        if (!url.startsWith("http://")){
            if (!url.startsWith("https://"))
            {
                url = "http://" + url;
            }
        }
        return url;
    }

    public void attackOnClick(View view){
        if (!clicked){
            final EditText editTextURL = (EditText) findViewById(R.id.editTextURL);
            final TextView OSlabel = (TextView) findViewById(R.id.textViewOSVersionLabel);
            final TextView WebServerLabel = (TextView) findViewById(R.id.textViewWebServerVersionLabel);
            clicked = !clicked;
            String line;
            try {
                Log.d("DEBUG", "Starting To Read File");
                Context context = getApplicationContext();
                AssetManager assetManager = context.getAssets();
                InputStream inputStream = assetManager.open(wordList);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("#")){
                        Log.d("LINE","Comment line");
                    }else if (line.equals("")){
                        Log.d("LINE","Blank line");
                    }else {
                        Log.d("LINE",editTextURL.getText().toString()+line);
                        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                                editTextURL.getText().toString()+line,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        WebServerLabel.setText(WebServer);
                                        OSlabel.setText(OS);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                    }
                                }) {

                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                responseCode = response.statusCode;
                                Log.d("ResponseCode",responseCode.toString());
                                if (response.headers.containsKey("Server")) {
                                    String tmpHeader = response.headers.get("Server");
                                    if(tmpHeader.contains("/")){
                                        String[] tmp = tmpHeader.split(" ");
                                        WebServer = tmp[0].replace("/", " ");
                                        OS = tmp[1].replace("(","").replace(")","");
                                        Log.d("WebServer",WebServer);
                                        Log.d("OperatingSystem",OS);
                                    }
                                }
                                return super.parseNetworkResponse(response);
                            }
                        };
                        queue.add(stringRequest);
                    }
                }
                bufferedReader.close();
            } catch (IOException e){
                Log.d("DEBUG",e.toString());
            }
            Log.d("OnClick","Click");
        }
    }
}