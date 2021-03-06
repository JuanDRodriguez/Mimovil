package com.example.mimovil;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private Boolean storageAuthorization = false;
    private Boolean cameraAuthorization = false;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private Uri photoURI;
    private WebView wv;
    //Definir campos
    private static final int FILECHOOSER_RESULCODE =1;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessages;
    private Uri mCapturedImageUri = null;
    private String mCurrentPhotoPath;
    //Fin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportActionBar().hide();// Oculta la cabecera.
        wv = (WebView) findViewById(R.id.wv);
        Intent intent = new Intent(this,MainActivity2.class);
        //startActivity(intent);
        wv.loadUrl("http://crm.altanredes.com/devl/app_cambaceo/login.php");
        wv.setWebViewClient(new MyClient());
        wv.setWebChromeClient(new GoogleClient());
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.getSettings().setAllowFileAccess(true);
        wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv.clearCache(false);
        checkExternalStoragePermission();
        wv.setWebChromeClient(new WebChromeClient() {
            //Implementacion de Canvas de archivos
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String accepType) {
                mUploadMessage = uploadMsg;
               // openImageChooser();
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams filechooserParams) {
                mUploadMessages = filePathCallback;
                showImageImportDialog();
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }
            //Fin
        });
        //Metodo para descargar pdf
    }
    //metodo para abrir google drive
    private void openImageChooser(){


    }
    private File createImageFile() throws IOException{
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

                handleUploadMessages(requestCode, resultCode, intent);


    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showImageImportDialog() {
        checkExternalStoragePermission();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ELIGE UNA OPCIÓN");
        builder.setPositiveButton("SELECCIONAR FOTO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                    abrirGaleria();
            }
        });
        builder.setNegativeButton("TOMAR FOTO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                checkExternalStoragePermission();

                    dispatchTakePictureIntent();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    public void abrirGaleria(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Seleccione una imagen"),
                2);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "MyPicture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());
              // Uri photoURI = getContentResolver().insert(
                //        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                photoURI = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    private void handleUploadMessage(int requestCode, int resultCode, Intent intent) {
        Uri result = null;
        try {
            if (resultCode != RESULT_OK){
                result = null;
            } else {
                result = intent == null ? mCapturedImageUri : intent.getData();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        mUploadMessage.onReceiveValue(result);
        mUploadMessage = null;
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleUploadMessages(int requestCode, int resultCode, Intent intent) {
        Uri[] results = null;
        try {
            if (requestCode == 1){

                results = new Uri[]{photoURI};
                mUploadMessages.onReceiveValue(results);

            } else {
                if (intent != null){
                    String dataString = intent.getDataString();
                    ClipData clipData = intent.getClipData();
                    if (clipData != null){
                        results = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++){
                            ClipData.Item item = clipData.getItemAt(i);
                            results[i] = item.getUri();
                        }
                    }
                    if (dataString != null){
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                } else {

                }
                mUploadMessages.onReceiveValue(results);
            }
        } catch (Exception e){
            e.printStackTrace();

        }


    }
    class MyClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            super.onPageStarted(view,url,favicon);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view,String Url)
        {
            view.loadUrl(Url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view,String url)
        {
            super.onPageFinished(view,url);
        }
    }
    class GoogleClient extends WebChromeClient
    {
        @Override
        public void onProgressChanged(WebView view,int newProgress)
        {
            super.onProgressChanged(view,newProgress);
        }
    }

    @Override
    public void onBackPressed() {
        if (wv.canGoBack())
            wv.goBack();
        else
            super.onBackPressed();

    }
    private Boolean checkExternalStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                storageAuthorization = true;
                cameraAuthorization = true;
                return true;
            }else{
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},REQUEST_CODE_ASK_PERMISSIONS);

            }
        }
        return  false;
    }
}