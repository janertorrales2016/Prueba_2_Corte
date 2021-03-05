package uteq.student.prueba_corte;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 100;
    private static final int REQUEST_IMAGE_GALLERY =101;
    private ImageView imagen;
    private String iso;
    public Vision vision;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imagen = findViewById(R.id.imageView2);
    }
    public void btEnvier(View view){
        //envio manual de iso2 de un pais para probar funcionamiento de
        //http://www.geognos.com/api/en/countries/info/ .json y
        //http://www.geognos.com/api/en/countries/flag/ .png
        Intent intent = new Intent(MainActivity.this, ViewDatos.class);
        Bundle b= new Bundle();
        b.putString("PAIS",iso);
        intent.putExtras(b);
        startActivity(intent);
    }
    //traer imagen de la galeria para enviar a object-detection
    public void abrirgaleria( View view){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                open();
            }else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            }
        }
    }
    //permisos para la galeria
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_CODE){
            if(permissions.length >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                
            }else {
                Toast.makeText(this,"Habilita los permisos", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
    }
    //obtener la imagen de la galeria
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==REQUEST_IMAGE_GALLERY){
            if(resultCode==RESULT_OK && data!= null){
                Uri foto= data.getData();
                imagen.setImageURI(foto);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();
                        bitmap = scaleBitmapDown(bitmap, 1200);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream(); //2da de la api
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        byte[] imageInByte = stream.toByteArray();
                        Image inputImage = new Image(); //googlevision
                        inputImage.encodeContent(imageInByte);

                        AnnotateImageRequest request = new AnnotateImageRequest();
                        request.setImage(inputImage);
                        BatchAnnotateImagesRequest batchRequest = new
                                BatchAnnotateImagesRequest();
                        batchRequest.setRequests(Arrays.asList(request));
                        BatchAnnotateImagesResponse batchResponse = null;
                        try {
                            Vision.Images.Annotate annotateRequest =
                                    vision.images().annotate(batchRequest);
                            annotateRequest.setDisableGZipContent(true);
                            batchResponse = annotateRequest.execute();
                        } catch (IOException ex) {
                        }
                        TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();
                        iso =  text.getText();
                        
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                });

            }
            else{
                Toast.makeText(this,"error al cargar la foto", Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void open(){
        Intent intent= new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

}

