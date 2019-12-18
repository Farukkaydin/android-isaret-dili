package com.example.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 101;
    private static final int VIDEO_CAPTURE = 102;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    final ArrayList<TextView> tvArray = new ArrayList<TextView>(4);
    final ArrayList<String> predictionsArray = new ArrayList<String>(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        populatePredictionsArray();
        tvArray.add((TextView) findViewById(R.id.tv_latest_0));
        tvArray.add((TextView) findViewById(R.id.tv_latest_1));
        tvArray.add((TextView) findViewById(R.id.tv_latest_2));
        tvArray.add((TextView) findViewById(R.id.tv_latest_3));
        updatePredictionsOnView();
        FloatingActionButton fab = findViewById(R.id.fab);

    }

    public void handleVideoButtonClick(View view) {


        if(allPermissionsGranted()){
            //Snackbar.make(view, "Start Camera here", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show();
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
            startActivityForResult(intent, VIDEO_CAPTURE);

            //startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri videoUri = data.getData();

        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                if (videoUri != null) {
                    String[] uriArr = videoUri.toString().split("/");
                    setPrediction(uriArr[uriArr.length - 1].toString() + "pred");
                }
                Toast.makeText(this, "Video bu yola kaydedildi:\n" +
                        videoUri, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Kayıt iptal edildi.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Bir Hata Oluştu :(",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onHelpAction(MenuItem mi) {
        // handle click here
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Nasıl Kullanılır ?");
        alertDialog.setMessage("\nVideo butonuna tıklayarak videonuzu kaydedebilirsiniz.\n\nArdından kaydettiğiniz video işlenecek.\n\nSon görüntülere ait tahmin sonuçları ve tahmin oranları ekranınıza yansımaya başlayacaktır.\n\nVideo uzunluğu 5 saniyeyle sınırlandırılmıştır.\n\nÇektiğiniz videolar internet üzerinde işlenerek size cevabı iletilecektir.\n");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Anladım", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                //startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void populatePredictionsArray() {
        for(int i = 0; i < predictionsArray.size(); i++) {
            predictionsArray.add("");
        }
    }

    private void setPrediction(String prediction) {
        //predictionsArray.remove(predictionsArray.size() - 1);
        predictionsArray.add(0, prediction);
        updatePredictionsOnView();
    }

    private void updatePredictionsOnView() {
        for(int i = 0; i < predictionsArray.size(); i++) {
            tvArray.get(i).setText(predictionsArray.get(i));
        }
    }


    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

}
