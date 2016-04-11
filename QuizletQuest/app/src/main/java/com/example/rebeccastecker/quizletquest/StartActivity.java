package com.example.rebeccastecker.quizletquest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.rebeccastecker.quizletquest.models.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class StartActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;
    private ZXingScannerView mScannerView;
    private TextView textView;
    public static final String TAG = "Rebecca";
    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);

        ImageView imageView = (ImageView) findViewById(R.id.qrCode);
        textView = (TextView) findViewById(R.id.header_txt);

        textView.setText("starting...");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                doThing();
            }
        });

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        mScannerView.setFlash(false);


        contentFrame.addView(mScannerView);
        try {
            Bitmap bitmap = encodeAsBitmap("sharks!");
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 300, 300, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE: {
                if (resultCode != RESULT_CANCELED) {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    String moar_data = scanResult.getContents();
                    Log.i(TAG, "I got stuff!! "+moar_data+ " : "+mScannerView.getFlash());
                    textView.setText(moar_data);
                    mScannerView.setFlash(false);
                    // use this data
                } else {
                    // error
                }
                break;
            }
        }
    }
    public void doThing() {

        IntentIntegrator integrator = new IntentIntegrator(StartActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        //in case you want to customize a bit.
        integrator.setPrompt("Scan a QR/Bar code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText() + " : "+ mScannerView.getFlash()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        textView.setText(rawResult.getText());
        mScannerView.setFlash(false);

        String text = rawResult.getText();

        if (text.startsWith("s")) {
            textView.setText("Loading set "+ rawResult.getText()+"...");
            loadSet(415);
        } else if (text.startsWith("t")) {
            textView.setText("You need to start a Quest before you can scan answers!");
        }

        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }

    private void processServerResults(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Set set = mapper.readValue(jsonString, Set.class);
            Log.w(TAG, "OMG! WINNING! "+set);
            Log.w(TAG, "I see this many terms : "+set.terms.size());
            Log.w(TAG, "I see this many terms : "+set.terms.get(0).term+" : "+set.terms.get(0).id);
        } catch (IOException e) {
            Log.w(TAG, "Totes error chewing on that string : "+e);
            return;
        }
    }

    private void loadSet(int setId) {
        String url = "https://api.quizlet.com/2.0/sets/" + setId+ "?client_id=BcpDSe7sYr";
        Log.w(TAG, "We're hitting : "+url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.w(TAG, "Response is: " + response.substring(0, 500));
                        processServerResults(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(TAG, "That didn't work! " + error);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
