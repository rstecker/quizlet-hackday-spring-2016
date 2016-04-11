package com.example.rebeccastecker.quizletquest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Pair;
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
import com.example.rebeccastecker.quizletquest.models.Img;
import com.example.rebeccastecker.quizletquest.models.Set;
import com.example.rebeccastecker.quizletquest.models.Term;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Date;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class StartActivity extends Activity implements ZXingScannerView.ResultHandler {

    private long lastScannedTermId = 0;
    private long currentSetId = 0;
    private GameMaster currentGame;

    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;
    private ZXingScannerView mScannerView;
    private TextView textView;
    public static final String TAG = "Rebecca";
    private RequestQueue queue;
    private ImageView goalImageView;
    private TextView goalTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_start);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.scan_window);

        ImageView imageView = (ImageView) findViewById(R.id.qrCode);
        goalImageView = (ImageView) findViewById(R.id.current_goal_img);
        goalTextView = (TextView) findViewById(R.id.current_goal_txt);
        textView = (TextView) findViewById(R.id.title_txt);

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
//        try {
//            Bitmap bitmap = encodeAsBitmap("sharks!");
//            imageView.setImageBitmap(bitmap);
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
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
                    Log.w(TAG, "I got stuff!! "+moar_data+ " : "+mScannerView.getFlash());
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
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText() + " : "+ mScannerView.getFlash()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        textView.setText(rawResult.getText());
        mScannerView.setFlash(false);

        String text = rawResult.getText();
        Long scannedNumber = null;
        try {
             scannedNumber = Long.parseLong(text.substring(1));
        } catch (NumberFormatException e) {}

        if (text.startsWith("s") && scannedNumber != null && shouldLoadSet(scannedNumber)) {
            textView.setText("Loading set "+ rawResult.getText()+"...");
            requestSet(scannedNumber);
        } else if (text.startsWith("t") && currentGame == null) {
            textView.setText("You need to start a Quest before you can scan answers!");
        } else if (text.startsWith("t") && currentGame != null && currentGame.isOver()) {
            textView.setText("You need to re-start the Quest before you can scan answers!");
        } else if (text.startsWith("t") && scannedNumber != null && currentGame != null && !currentGame.isOver()) {
            processAnswerSubmitted(scannedNumber);
        }

        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }

    private void processServerResults(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Set set = mapper.readValue(jsonString, Set.class);
            currentGame = new GameMaster(set, GameMaster.Mode.DEFINITIONS);
            showQuestStep(currentGame.getCurrentQuestionText(), currentGame.getCurrentQuestionImage());
        } catch (IOException e) {
            Log.w(TAG, "Totes error chewing on that string : "+e);
            return;
        }
    }

    private void processAnswerSubmitted(long termId) {
        if (!shouldConsiderTermId(termId)) {
            // FIXME : this is... likely? a double scan, ignore for now (should be handled better)
            Log.w(TAG, "  > ignoring a double scan... "+termId);
            return;
        }
        Pair<Term, Term> problem = currentGame.submitAnswer(termId);
        if (problem == null) {
            // success!
            if (currentGame.nextQuestion()) {
                textView.setText("Correct!");
                Log.w(TAG, "they totes got it right! "+termId+" was right");
                showQuestStep(currentGame.getCurrentQuestionText(), currentGame.getCurrentQuestionImage());
            } else {
                // game over!
                textView.setText("Game over, man! Game over!");
                Log.w(TAG, "Game over!");
            }
        } else {
            // they done did wrong
            if (problem.second == null) {
                textView.setText("Oops, wrong answer! ... (1)");
                Log.w(TAG, "Fail! "+problem.first.id+" was what we were looking for and you submitted... ??");
            } else {
                textView.setText("Oops, wrong answer! ... (2)");
                Log.w(TAG, "Fail! "+problem.first.id+" was what we were looking for and you submitted "+problem.second.id);
            }
            currentGame.restartQuest();
            textView.setText("Restarting game....");
            showQuestStep(currentGame.getCurrentQuestionText(), currentGame.getCurrentQuestionImage());
        }
    }

    public void showQuestStep(String text, Img img) {
        if (text != null) {
            goalTextView.setText(text);
        }
        if (img != null) {
            Picasso.with(this).load(img.url).into(goalImageView);
        }
    }

    private void requestSet(long setId) {
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

    private synchronized boolean shouldLoadSet(long setId) {
        lastScannedTermId = 0;
        if (setId == currentSetId) {
            return false;
        }
        currentSetId = setId;
        return true;
    }

    private synchronized boolean shouldConsiderTermId(long termId) {
        currentSetId = 0;
        if (lastScannedTermId == termId) {
            return false;
        }
        lastScannedTermId = termId;
        return true;
    }
}
