package com.example.mimovil;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity2 extends AppCompatActivity {
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 400;
    public static final int IMAGE_PICK_GALLERY_CODE = 1000;
    public static final int IMAGE_PICK_CAMERA_CODE = 200;
    private static final String ARG_PARAM1 = "param1";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private Boolean cameraAuthorization = false;
    private Boolean storageAuthorization = false;
    private Boolean boolCamera = false;
    private static final int SELECT_FILE = 2;
    private static final int SELECT_CAMERA = 1;
    private WebView wv;
    //Definir campos
    private static final int FILECHOOSER_RESULCODE =1;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessages;
    private Uri mCapturedImageUri = null;
    //Fin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportActionBar().hide();// Oculta la cabecera.
        wv = (WebView) findViewById(R.id.wv);
        wv.loadUrl("https://crm.altanredes.com/devl/app_cambaceo/registro.php");
        wv.setWebViewClient(new MyClient());
        wv.setWebChromeClient(new GoogleClient());
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.getSettings().setAllowFileAccess(true);
        wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv.clearCache(false);

        wv.setWebChromeClient(new WebChromeClient() {
            //Implementacion de Canvas de archivos
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String accepType) {
                mUploadMessage = uploadMsg;
                showImageImportDialog();
            }

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
    private void showImageImportDialog() {
        checkExternalStoragePermission();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ELIGE UNA OPCIÓN");
        builder.setPositiveButton("SELECCIONAR FOTO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                checkExternalStoragePermission();
                if(cameraAuthorization && storageAuthorization)
                    abrirGaleria();
            }
        });
        builder.setNegativeButton("TOMAR FOTO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                checkExternalStoragePermission();
                if(cameraAuthorization && storageAuthorization)
                    dispatchTakePictureIntent();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    private Boolean checkExternalStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                storageAuthorization = true;
                cameraAuthorization= true;
                return true;
            }else{
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},REQUEST_CODE_ASK_PERMISSIONS);

            }
        }
        return  false;
    }
    public void abrirGaleria(){
        boolCamera = false;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        this.startActivityForResult(
                Intent.createChooser(intent, "Seleccione una imagen"),SELECT_FILE);
    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(intent, SELECT_CAMERA);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".png",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();

        return image;
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

}