package example.kevin.mycamerav3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private String CARPETA_RAIZ="misImagenesPrueba/";
    private String RUTA_IMAGEN= CARPETA_RAIZ+"misPacks";
    private String path;
    private final static int COD_SELECT = 10;
    private final static int COD_CAMERA = 20;
    private static int COD_CAMERA_SD_PER = 100;

    ImageView ivImagen;
    Button btnCaptura;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        if (validarPermisos()){
            btnCaptura.setEnabled(true);
        }else {
            btnCaptura.setEnabled(false);
        }
        btnCaptura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarImagen();
            }
        });

    }

    private boolean validarPermisos() {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }
        if ((checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED)
                ||(checkSelfPermission(WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)){
            return true;
        }
        if ((shouldShowRequestPermissionRationale(CAMERA))||
                (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))){
            mostrarDialogPermission();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},COD_CAMERA_SD_PER);

        }

        return false;
    }

    private void mostrarDialogPermission() {
        AlertDialog.Builder dialog= new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Permisos Desactivados");
        dialog.setMessage("Debe Aceptar los permisos para poder utilizar la APP");

        dialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},COD_CAMERA_SD_PER);
                }
            }
        });

        dialog.show();

    }

    private void cargarImagen(){
        final CharSequence[] opciones ={"Tomar Foto","Cargar Imagen", "Cancelar"};
        AlertDialog.Builder dialog= new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Seleccione una opción");
        dialog.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (opciones[i].equals("Tomar Foto")){
                    takePhoto();
                    //Toast.makeText(MainActivity.this, "Tomar Foto", Toast.LENGTH_SHORT).show();
                } if (opciones[i].equals("Cargar Imagen")){
                    Intent intent= new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Selecciona APP"),COD_SELECT);


                }
                if (opciones[i].equals("Cancelar")){
                    dialog.dismiss();

                }

            }
        });

        dialog.show();
    }

    private void takePhoto() {
        File file= new File(Environment.getExternalStorageDirectory(),RUTA_IMAGEN);
        String nombre = "";
        boolean exist= file.exists();
        if (exist==false){
            exist = file.mkdirs();
        }
        if (exist){
            nombre=(System.currentTimeMillis()/100)+".jpg";
        }
        path= Environment.getExternalStorageDirectory()+File.separator+RUTA_IMAGEN+File.separator+nombre;

        File image= new File(path);
        Intent intent= null;
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            String authorities = getApplicationContext().getPackageName()+".provider";
            Uri imageUri= FileProvider.getUriForFile(this,authorities,image);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        }else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
        }
        startActivityForResult(intent,COD_CAMERA);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==COD_CAMERA_SD_PER){
            if (grantResults.length == 2 && grantResults[0]==PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                btnCaptura.setEnabled(true);
            }else
            {
                solicitarPermisosManual();
            }
        }

    }

    private void solicitarPermisosManual() {
     final CharSequence[] opcines ={"Si","No"};

        AlertDialog.Builder dialog= new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Configurar los permisos de forma manual");
        dialog.setItems(opcines, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (opcines[i].equals("Si")){
                    Intent intent= new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //tarea *revisar
                    Uri uri= Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }else {
                    Toast.makeText(getApplicationContext(), "Los Permisos no fueron aceptados por el usuario. ", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });

        dialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},COD_CAMERA_SD_PER);
                }
            }
        });

        dialog.show();


    }


    private void initView() {
        ivImagen = findViewById(R.id.ivPhoto);
        btnCaptura = findViewById(R.id.btnTakePhoto);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            switch (requestCode){
                case COD_SELECT:
                    Uri imagePath= data.getData();
                    ivImagen.setImageURI(imagePath);
                    break;
                case COD_CAMERA:
                    MediaScannerConnection.scanFile(this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Ruta Almacenamiento","Path: "+path);
                                }
                            });
                    Bitmap bitmap= BitmapFactory.decodeFile(path);
                    ivImagen.setImageBitmap(bitmap);

                    break;
            }
        }
    }
}
