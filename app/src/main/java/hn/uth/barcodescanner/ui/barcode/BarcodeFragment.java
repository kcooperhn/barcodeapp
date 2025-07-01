package hn.uth.barcodescanner.ui.barcode;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

import hn.uth.barcodescanner.R;
import hn.uth.barcodescanner.databinding.FragmentBarcodeBinding;

public class BarcodeFragment extends Fragment {

    private FragmentBarcodeBinding binding;
    private Bitmap imagenSeleccionada;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentBarcodeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        try{
            Bitmap fotoCamara = getArguments().getParcelable("camara");
            Bitmap fotoGaleria = getArguments().getParcelable("galeria");
            if(fotoCamara != null){
                Log.d("BARCODE", "Cargando imagen de camara en pantalla");
                binding.imgBarcode.setImageBitmap(fotoCamara);
                showToast(this.getContext().getString(R.string.lbl_imagen_cargada));
                imagenSeleccionada = fotoCamara;
                binding.btnEscanear.setVisibility(View.VISIBLE);
            }else if(fotoGaleria != null){
                Log.d("BARCODE", "Cargando imagen de camara en pantalla");
                binding.imgBarcode.setImageBitmap(fotoGaleria);
                showToast(this.getContext().getString(R.string.lbl_imagen_cargada));
                imagenSeleccionada = fotoGaleria;
                binding.btnEscanear.setVisibility(View.VISIBLE);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        binding.btnEscanear.setOnClickListener(view -> {
            showToast(this.getContext().getString(R.string.lbl_ejecutando_escaneo));
            ejecutarEscaneoCodigoBarras();
        });
    
        return root;
    }

    private void ejecutarEscaneoCodigoBarras() {
        Log.d("BARCODE", "Iniciando escaneo");
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .enableAllPotentialBarcodes() // Optional
                //.setZoomSuggestionOptions(
                      //  new ZoomSuggestionOptions.Builder(zoomCallback)
                            //    .setMaxSupportedZoomRatio(maxSupportedZoomRatio)
                .build();
        Log.d("BARCODE", "Opciones de escaneo configuradas");
        int rotationDegree = 90;
        InputImage image = InputImage.fromBitmap(imagenSeleccionada, rotationDegree);
        //binding.imgBarcode.setimage(fotoCamara);

        Log.d("BARCODE", "Input Image convertido");
        BarcodeScanner scanner = BarcodeScanning.getClient();
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    Log.d("BARCODE", "Comenzando procesamiento...");
                    if(barcodes.isEmpty()){
                        showToast("No existen códigos de barra identificables");
                        Log.d("BARCODE", "No existen códigos de barra identificables");
                    }else{
                        showToast("Códigos de barras identificados");
                    }

                    for (Barcode barcode: barcodes) {
                        Log.d("BARCODE", "Código encontrado");
                        Rect bounds = barcode.getBoundingBox();
                        Point[] corners = barcode.getCornerPoints();

                        String rawValue = barcode.getRawValue();
                        binding.txtBarcodeType.setText(""+barcode.getFormat());
                        int valueType = barcode.getValueType();
                        Log.d("BARCODE", "RAW data procesada");
                        // See API reference for complete list of supported types
                        switch (valueType) {
                            case Barcode.TYPE_WIFI:
                                Log.d("BARCODE", "Código de tipo WIFI");
                                String ssid = barcode.getWifi().getSsid();
                                String password = barcode.getWifi().getPassword();
                                int type = barcode.getWifi().getEncryptionType();
                                binding.txtScanInfo.setText(ssid+"\n"+password+"\n"+type);
                                binding.txtBarcodeInfo.setText("WIFI");
                                break;
                            case Barcode.TYPE_URL:
                                Log.d("BARCODE", "Código de tipo URL");
                                String title = barcode.getUrl().getTitle();
                                String url = barcode.getUrl().getUrl();
                                binding.txtScanInfo.setText(title+"\n"+url);
                                binding.txtBarcodeInfo.setText("URL");
                                break;
                            default:
                                Log.d("BARCODE", "Otro tipo de código procesado");
                                binding.txtScanInfo.setText(rawValue);
                                binding.txtBarcodeInfo.setText("unknown");
                                break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("BARCODE", "Error al procesar la imagen");
                    binding.txtScanInfo.setText(e.getLocalizedMessage());
                    binding.txtBarcodeInfo.setText("Error al obtener información");
                    binding.txtBarcodeType.setText("unknown");
                    showToast("Error al obtener el código de barras");
                });
        if(result.isCanceled()){
            Log.d("BARCODE", "Result cancelado");
        }else if (result.isComplete()){
            Log.d("BARCODE", "Result completado");
        }else if (result.isSuccessful()){
            Log.d("BARCODE", "Result exitoso");
        }
    }

    private void showToast(String mensaje) {
        Toast.makeText(this.getContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        imagenSeleccionada = null;
    }
}