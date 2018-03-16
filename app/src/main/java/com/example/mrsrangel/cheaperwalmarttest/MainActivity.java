package com.example.mrsrangel.cheaperwalmarttest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import com.google.zxing.Result;
import com.squareup.picasso.Picasso;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView itemNameTextView;
    TextView itemPriceTextView;
    ImageView itemImageView;
    String imageUrl;
    Bitmap bitmap;
    Button scanButton;


    ZXingScannerView scannerView;

    String walmartApiKey = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemNameTextView =  findViewById(R.id.itemNameTextView);
        itemPriceTextView = findViewById(R.id.itemPriceTextView);
        itemImageView = findViewById(R.id.itemImageView);

        scanButton = findViewById(R.id.scanButton);
        scannerView = new ZXingScannerView(MainActivity.this);


    }

    //used for testing in catch blocks
    public void testToast(){
        Toast.makeText(MainActivity.this,"Error opening camera! Note 8 sucks!!!!",Toast.LENGTH_LONG).show();
    }

    public void scanCode(View view){

        //Request required permissions
        //check for camera permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //permission not granted
            //ask for permission
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
        }
        else{

            //try to start the camera
            try {
                //setContentView(scannerView);

                /*
                scannerView.startcamera() is causing errors starting camera in note 8
                 */
                scannerView.startCamera();

                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                //upc A is for 11 digit barcodes upc e is for 6 digits
                intent.putExtra("SCAN_MODE", "UPC_E");
                //dont save barcode history
                intent.putExtra("SAVE_HISTORY", false);
                startActivityForResult(intent, 0);
            }catch(Exception e){
                e.printStackTrace();
                testToast();

            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                final String contents = intent.getStringExtra("SCAN_RESULT");

                //String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
                Log.i("Test", "scan successful" + contents);
                scannerView.stopCamera();

                //download the api to check the price
                DownloadTask task = new DownloadTask();
                task.execute("https://api.walmartlabs.com/v1/items?apiKey=" + walmartApiKey +"&upc="+ contents);
                //https://api.walmartlabs.com/v1/items?apiKey=qhbe9gcmjzykbxq7wfryu4xe&upc=015000048341


            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel

                Log.i("Test", "scan NOT successful" + requestCode);

            }
        }
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {

            //variable to store the result recieved
            String result = "";
            //Url used to find the result
            URL url;
            //the connection
            HttpURLConnection urlConnection;

            try {
                url = new URL(strings[0]);

                Log.i("Location: string value", String.valueOf(url));

                urlConnection = (HttpURLConnection)url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i("Location: Error:", String.valueOf(e));
            } catch (Exception e){
                Log.i("Location: Error:", String.valueOf(e));
                e.printStackTrace();
            }

            //if the url fails
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            try {

                Log.i("Location: ", "Post execute..");

                JSONObject jsonObject = new JSONObject(result);



                //displays all the items of the jsonObject recieved
                Log.i("Json Object: ", String.valueOf(jsonObject));
                String walmartItem = jsonObject.getString("items");
                Log.i("Item Name:", walmartItem);

                JSONArray walmartArr = new JSONArray(walmartItem);

                Log.i("Location walmartArr", String.valueOf(walmartArr));

                if(walmartArr.length() > 0){

                    for(int i = 0; i<walmartArr.length(); i++){

                        JSONObject walmartParts = walmartArr.getJSONObject(i);

                        Log.i("Location parts", String.valueOf(walmartParts));

                        itemNameTextView.setText(walmartParts.getString("name"));
                        itemPriceTextView.setText( "Price: " + walmartParts.getString("salePrice"));
                        imageUrl = walmartParts.getString("mediumImage");
                        //sets the image using picasso
                        Picasso.with(MainActivity.this).load(imageUrl).into(itemImageView);




//                        Log.i("Location parts", name);
//                        Log.i("Location parts", walmartParts.getString("name"));
//                        Log.i("Location parts", String.valueOf(price));
//                        Log.i("Location parts", Integer.toString(name.length()));



                    }


//                    if(name.length() > 0 || price.length() > 0){
//
//                        itemNameTextView.setText(name);
//                        itemPriceTextView.setText(price);
//
//
//
//                        Log.i("Location", "updated TextViews");
//                        Log.i("Location", itemNameTextView.getText().toString());
//                        Log.i("Location", itemPriceTextView.getText().toString());
//                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    //this is onPause for the app not for the camera (prebuilt with Android)
    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }


}


//LEFT THIS HERE TO REMIND MYSELF WHAT i DID UP ABOVE
//class ZxingScannerView implements ZXingScannerView.ResultHandler {
//
//    @Override
//    public void handleResult(Result result) {
//
//
//
//        Log.i("Location: ", String.valueOf(result));
//
//
//    }
//}

