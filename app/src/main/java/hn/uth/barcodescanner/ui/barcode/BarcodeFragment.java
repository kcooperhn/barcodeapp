package hn.uth.barcodescanner.ui.barcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.barcode.BarcodeScannerOptions;

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

            if(fotoCamara != null){
                Log.d("BARCODE", "Cargando imagen de camara en pantalla");
                binding.imgBarcode.setImageBitmap(fotoCamara);
                showToast(this.getContext().getString(R.string.lbl_imagen_cargada));
                imagenSeleccionada = fotoCamara;
                binding.btnEscanear.setVisibility(View.VISIBLE);
            }
            //TODO: COLOCAR LOGICA DE OBTENER FOTO DE GALERIA

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
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .enableAllPotentialBarcodes() // Optional
                //.setZoomSuggestionOptions(
                      //  new ZoomSuggestionOptions.Builder(zoomCallback)
                            //    .setMaxSupportedZoomRatio(maxSupportedZoomRatio)
                .build();
    }

    private void showToast(String mensaje) {
        Toast.makeText(this.getContext(), mensaje, Toast.LENGTH_LONG).show();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}