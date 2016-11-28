package com.networks.networksproject;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

import static android.R.layout.simple_spinner_item;

public class MainActivity extends AppCompatActivity {
    int requests = 0, lines = 0;
    String WebServer,OS,wordList;
    Integer responseCode;
    RequestQueue queue;

    protected void onCreate(Bundle savedInstanceState) {
        queue = Volley.newRequestQueue(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner wordlistSpinner = (Spinner) findViewById(R.id.wordlists);
        ArrayAdapter<CharSequence> directoryAdapter = ArrayAdapter.createFromResource(this, R.array.directory_array, simple_spinner_item);
        directoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wordlistSpinner.setAdapter(directoryAdapter);
        wordlistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wordList = parent.getItemAtPosition(position).toString();
                lines = getLineCount();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    public int getLineCount(){
        int x = 0;
        String line;
        try{
            Context context = getApplicationContext();
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(wordList);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.contains("#") && !line.equals("")){
                    x++;
                }
            }
            bufferedReader.close();
        } catch (Exception e){
            Log.d("ERROR", e.toString());
        }
        return x;
    }

    public void attackOnClick(View view){
        requests = 0;
        Context context = getApplicationContext();
        final Toast toast = Toast.makeText(context, "Starting", Toast.LENGTH_SHORT);
        final EditText editTextURL = (EditText) findViewById(R.id.editTextURL);
        final TextView OSlabel = (TextView) findViewById(R.id.textViewOSVersionLabel);
        final TextView WebServerLabel = (TextView) findViewById(R.id.textViewWebServerVersionLabel);
        final Button attackButton = (Button) findViewById(R.id.attackButton);
        attackButton.setEnabled(false);
        ArrayList<String> listItems=new ArrayList<>();
        ListView listView = (ListView)findViewById(R.id.foundDirectoriesListView);
        final ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(listViewAdapter);
        String line;
        try {
            toast.show();
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(wordList);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while ((line = bufferedReader.readLine()) != null) {
                final String url = editTextURL.getText().toString()+line;
                if (!line.contains("#") && !line.equals("")) {
                    StringRequest stringRequest = new StringRequest(Request.Method.GET,
                            url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (requests == lines){
                                        attackButton.setEnabled(true);
                                        toast.setText("Done");
                                        toast.show();
                                    }
                                    if (responseCode == 200 ){
                                        listViewAdapter.add(url);
                                        responseCode = 0;
                                    }
                                    if (WebServer != null){
                                        WebServerLabel.setText(WebServer);
                                    }
                                    if (OS != null) {
                                        OSlabel.setText(OS);
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    requests++;
                                    if (requests == lines){
                                        attackButton.setEnabled(true);
                                        toast.setText("Done");
                                        toast.show();
                                    }
                                }
                            }) {
                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            requests++;
                            responseCode = response.statusCode;
                            if (response.headers.containsKey("Server")) {
                                String tmpHeader = response.headers.get("Server");
                                if(tmpHeader.contains("/")){
                                    String[] tmp = tmpHeader.split(" ");
                                    WebServer = tmp[0].replace("/", " ");
                                    OS = tmp[1].replace("(","").replace(")","");
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
            toast.setText("Error: "+e.toString());
            toast.show();
            Log.d("DEBUG",e.toString());
        }
    }
}