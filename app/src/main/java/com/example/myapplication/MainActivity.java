package com.example.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import org.apache.commons.io.FileUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 101;
    private static final int VIDEO_CAPTURE = 102;

    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"};

    final ArrayList<TextView> tvArray = new ArrayList<TextView>(4);
    final ArrayList<String> predictionsArray = new ArrayList<String>(4);
    TextView indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        populatePredictionsArray();
        indicator = findViewById(R.id.tvStatusIndicator);
        indicator.setText("HAZIR");
        tvArray.add((TextView) findViewById(R.id.tv_latest_0));
        tvArray.add((TextView) findViewById(R.id.tv_latest_1));
        tvArray.add((TextView) findViewById(R.id.tv_latest_2));
        tvArray.add((TextView) findViewById(R.id.tv_latest_3));
        updatePredictionsOnView();
        FloatingActionButton fab = findViewById(R.id.fab);

    }

    private void uploadFile(Uri fileUri) {

        // create upload service client
        FileUploadService service =
                ServiceGenerator.createService(FileUploadService.class);

        File file = new File(FileChooser.getPath(getBaseContext(),fileUri));
        Log.v("File From URI", "success");
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );
        Log.v("Request File Creation", "success");
        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        Log.v("Multipart Body", "success");
        // add another part within the multipart request
        String descriptionString = "Video file to predict from";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);
        Log.v("Request", "executing");
        indicator.setText("GONDERILIYOR");
        // finally, execute the request
        Call<ResponseBody> call = service.upload(description, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                JsonObject a = null;
                try {
                    a = new JsonParser().parse(response.body().string()).getAsJsonObject();
                    Toast.makeText(MainActivity.this, a.get("label").toString(), Toast.LENGTH_SHORT).show();
                    setPrediction(a.get("label").toString().replace("\"", ""));
                    Log.v("response as string", a.get("label").toString());
                    Log.v("Upload", "success");
                    indicator.setText("HAZIR (OK)");
                } catch (IOException e) {
                    e.printStackTrace();
                    indicator.setText("HAZIR (ERR)");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                setPrediction("Hata :(");
                indicator.setText("HAZIR (ERR)");
            }
        });
    }

    public void handleVideoButtonClick(View view) {


        if(allPermissionsGranted()){
            //Snackbar.make(view, "Start Camera here", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show();
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
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
                    uploadFile(videoUri);
                    indicator.setText("CEVAP BEKLENIYOR");
                }

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
