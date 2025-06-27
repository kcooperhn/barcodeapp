package hn.uth.barcodescanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.Manifest;
import android.view.Menu;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.navigation.NavigationView;

import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import hn.uth.barcodescanner.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 100;
    private ActivityMainBinding binding;
    private String directorioImagen;
    private Bitmap imagenSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        directorioImagen = "";
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view ->{
            if(checkAndRequestPermissions()){
                Log.d("IMAGEN_CAMARA", "Permisos aceptados");
                Intent tomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(tomarFoto.resolveActivity(getPackageManager()) != null){
                    Log.d("IMAGEN_CAMARA", "Intent camara resuelto");
                    File archivoImagen = null;
                    try{
                        archivoImagen = createImageFile();
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.d("IMAGEN_CAMARA", "Error al crear archivo");
                    }
                    if(archivoImagen != null){
                        Uri fotoUri = FileProvider.getUriForFile(this, "hn.uth.barcodescanner.fileprovider", archivoImagen);
                        tomarFoto.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                    }
                    startActivityForResult(tomarFoto, REQUEST_IMAGE_CAPTURE);
                    Log.d("IMAGEN_CAMARA", "Intent Camara Abierto");
                }else{
                    Log.d("IMAGEN_CAMARA", "No se encontro camara");
                }
            }else{
                Log.d("IMAGEN_CAMARA", "Permisos denegados");
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_barcode)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private File createImageFile() throws IOException {
        String fechaHoy = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreArchivo = "JPEG_" + fechaHoy + "_";
        File directorio = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreArchivo, ".jpg", directorio);
        directorioImagen = imagen.getAbsolutePath();

        return imagen;
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        Log.d("IMAGEN_CAMARA", "Evaluando permisos");
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                Log.d("IMAGEN_CAMARA", "Permiso Rechzado");
                return false;
            }
        }else{
            if(cameraPermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
                Log.d("IMAGEN_CAMARA", "Permiso rechazado");
                return false;
            }
        }
        Log.d("IMAGEN_CAMARA", "Permiso concedido");
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("IMAGEN_CAMARA", "Datos de camara");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("IMAGEN_CAMARA", "Data contiene imagen de camara");
            if(!"".equals(directorioImagen) && directorioImagen != null){
                File imagen = new File(directorioImagen);
                if(imagen.exists()){
                    Bitmap bitmap = BitmapFactory.decodeFile(imagen.getAbsolutePath());
                    int megapixeles = bitmap.getWidth() * bitmap.getHeight() / 1000000;//OBTENIENDO TAMAÑO EN MEGAPIXELES
                    int factor = megapixeles/2; //CALCULANDO FACTOR DE REDUCCIÓN EN BASE A MINIMO DE 2MP
                    Log.d("IMAGEN_CAMARA", "Imagen tomada es de tamaño: "+bitmap.getWidth()+"x"+bitmap.getHeight()+" = "+megapixeles+"MP");

                    //REDIMENSIONANDO IMAGEN A TAMAÑO ACEPTABLE DE 2MP
                    bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/factor, bitmap.getHeight()/factor, false);
                    imagenSeleccionada = bitmap;
                }else{
                    Bundle extras = data.getExtras();
                    if(extras != null){
                        imagenSeleccionada = (Bitmap) extras.get("data");
                    }
                }
            }else{
                Bundle extras = data.getExtras();
                if(extras != null){
                    imagenSeleccionada = (Bitmap) extras.get("data");
                }
            }
            Log.d("IMAGEN_CAMARA", "Mostrando imagen en pantalla");
            navegarProcesamiento("camara");
        }
        //TODO: COLOCAR LOGICA DE OBTENER FOTO DE GALERIA


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void navegarProcesamiento(String tipoFoto) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(tipoFoto, imagenSeleccionada);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_barcode, bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        imagenSeleccionada = null;
        super.onDestroy();
    }
}