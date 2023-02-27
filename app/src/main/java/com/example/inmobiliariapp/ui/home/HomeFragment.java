package com.example.inmobiliariapp.ui.home;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.inmobiliariapp.R;
import com.example.inmobiliariapp.SharedViewModel;
import com.example.inmobiliariapp.Vivienda;
import com.example.inmobiliariapp.databinding.FragmentHomeBinding;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {


    String downloadUrl;
    //Galeria pruebas
    private static int RESULT_LOAD_IMG = 2;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Uri photoURI;
    private FirebaseUser authUser;
    StorageReference storageRef;
    StorageReference ref;

    //PHOTO IMPORTS
    ImageView foto;
    String imgDecodableString;
    String calle = "";
    String calleM = "";
    String mCurrentPhotoPath;
    String randomName;

    private boolean DARDEALTA = false;

    //PRUEBA LOCALIZACION
    private FragmentHomeBinding binding;
    private boolean manualOno = false;

    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnMakePhoto.setOnClickListener(button -> {
            loadImagefromGallery();
        });

        binding.Permisos.setOnClickListener(button -> {
            requestPermission();
        });

        binding.LonMan.setVisibility(View.INVISIBLE);
        binding.LatMan.setVisibility(View.INVISIBLE);
        binding.txtDireccioM.setVisibility(View.INVISIBLE);

        binding.manualOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    manualOno = true;
                    binding.LonMan.setVisibility(View.VISIBLE);
                    binding.LatMan.setVisibility(View.VISIBLE);
                    binding.txtDireccioM.setVisibility(View.VISIBLE);


                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }

                } else {
                    manualOno = false;
                    binding.LonMan.setVisibility(View.INVISIBLE);
                    binding.LatMan.setVisibility(View.INVISIBLE);
                    binding.txtDireccioM.setVisibility(View.INVISIBLE);
                }
            }
        });


        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


        sharedViewModel.getCurrentLatLng().observe(getViewLifecycleOwner(), latlng -> {
            binding.txtLatitud.setText(String.valueOf(latlng.latitude));
            binding.txtLongitud.setText(String.valueOf(latlng.longitude));

            binding.txtDireccio.setText(fetchAddress());
            binding.txtDireccioM.setText(fetchAddressM());
        });

        sharedViewModel.getProgressBar().observe(getViewLifecycleOwner(), visible -> {
            if (visible)
                binding.loading.setVisibility(ProgressBar.VISIBLE);
            else
                binding.loading.setVisibility(ProgressBar.INVISIBLE);
        });

        sharedViewModel.switchTrackingLocation();

        sharedViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            authUser = user;
        });

        binding.buttonNotificar.setOnClickListener(button -> {
            if(DARDEALTA == false) {
                Toast.makeText(getContext(), "Sube una nueva imagen", Toast.LENGTH_SHORT).show();
            }else {
                Vivienda vivienda = new Vivienda();
                if (!manualOno) {
                    vivienda.setLatitud(binding.txtLatitud.getText().toString());
                    vivienda.setLongitud(binding.txtLongitud.getText().toString());
                    vivienda.setDireccio(binding.txtDireccio.getText().toString());

                } else {
                    vivienda.setLatitud(binding.LatMan.getText().toString());
                    vivienda.setLongitud(binding.LonMan.getText().toString());
                    vivienda.setDireccio(binding.txtDireccioM.getText().toString());
                }

                vivienda.setInformación(binding.txtAdreca.getText().toString());
                vivienda.setUrl(randomName);
                Toast.makeText(getContext(), "Vivienda añadida", Toast.LENGTH_LONG).show();


                DatabaseReference base = FirebaseDatabase.getInstance(
                ).getReference();

                DatabaseReference users = base.child("users");
                DatabaseReference uid = users.child(authUser.getUid());
                DatabaseReference incidencies = uid.child("pisos");

                DatabaseReference reference = incidencies.push();
                reference.setValue(vivienda);
                uploadImage();
                DARDEALTA = false;
                foto.setImageDrawable(getResources().getDrawable(R.drawable.mas));
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String fetchAddress() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Geocoder geocoder = new Geocoder(requireContext(),
                Locale.getDefault());

        executor.execute(() -> {
            // Aquest codi s'executa en segon pla
            List<Address> addresses = null;
            String resultMessage = "";

            try {

                addresses = geocoder.getFromLocation(
                        Double.parseDouble(String.valueOf(binding.txtLatitud.getText())),
                        Double.parseDouble(String.valueOf(binding.txtLongitud.getText())),
                        1);

                if (addresses == null || addresses.size() == 0) {
                    if (resultMessage.isEmpty()) {
                        resultMessage = "No s'ha trobat cap adreça";
                        Log.e("INCIVISME", resultMessage);
                    }
                } else {


                    Address address = addresses.get(0);
                    ArrayList<String> addressParts = new ArrayList<>();

                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressParts.add(address.getAddressLine(i));
                    }
                    calle = addresses.get(0).getAddressLine(0);
                }

            } catch (IOException ioException) {
                resultMessage = "Servei no disponible";
                Log.e("INCIVISME", resultMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                resultMessage = "Coordenades no vàlides";
                Log.e("INCIVISME", resultMessage + ". " +
                        "Latitude = " + binding.txtLatitud.getText().toString() +
                        ", Longitude = " +
                        binding.txtLongitud.getText(), illegalArgumentException);
            }
        });
        return calle;
    }

    private String fetchAddressM() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Geocoder geocoder = new Geocoder(requireContext(),
                Locale.getDefault());

        executor.execute(() -> {
            // Aquest codi s'executa en segon pla
            List<Address> addresses1 = null;
            String resultMessage = "";

            if (String.valueOf(binding.LonMan.getText()) == null) {
                Log.e("Datos", "Se necesitan aun los datos");
            } else {
                try {

                    addresses1 = geocoder.getFromLocation(
                            Double.parseDouble(String.valueOf(binding.LonMan.getText())),
                            Double.parseDouble(String.valueOf(binding.LatMan.getText())),
                            1);

                    if (addresses1 == null || addresses1.size() == 0) {
                        if (resultMessage.isEmpty()) {
                            calleM = "No s'ha trobat cap adreça";
                            Log.e("INCIVISME", resultMessage);
                        }
                    } else {
                        Address address = addresses1.get(0);
                        ArrayList<String> addressParts = new ArrayList<>();

                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                            addressParts.add(address.getAddressLine(i));
                        }
                        calleM = addresses1.get(0).getAddressLine(0);
                        Log.e("Address 1:", calleM);

                        resultMessage = TextUtils.join("\n", addressParts);
                        String finalResultMessage = resultMessage;
                    }


                } catch (IOException ioException) {
                    resultMessage = "Servei no disponible";
                    Log.e("INCIVISME", resultMessage, ioException);
                } catch (IllegalArgumentException illegalArgumentException) {
                    resultMessage = "Coordenades no vàlides";
                    Log.e("Mensaje", "Faltan coordenadas manuales");
                }
            }
        });
        Log.e("CALLE MANUAL", calleM);

        return calleM;
    }


    //GALERIA
    public void loadImagefromGallery() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        DARDEALTA = true;
    }

    private void uploadImage(){

        ref.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Failed " + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                ProgressDialog progressDialog = new ProgressDialog(getContext());
                double progress
                        = (100.0
                        * snapshot.getBytesTransferred()
                        / snapshot.getTotalByteCount());
                progressDialog.setMessage(
                        "Uploaded "
                                + (int)progress + "%");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
            randomName = UUID.randomUUID().toString();
            ref = storageRef.child("images/"+randomName);

            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContext().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                imgDecodableString = cursor.getString(columnIndex);

                mCurrentPhotoPath = cursor.getString(columnIndex);

                File uriFile = new File(mCurrentPhotoPath);
                photoURI = Uri.fromFile(uriFile);
                storageRef.putFile(photoURI);

                Log.e("PATH" , cursor.getString(columnIndex));

                cursor.close();

                foto = getView().findViewById(R.id.imvPhoto);
                // Set the Image in ImageView after decoding the String
                foto.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG)
                    .show();
            Log.e("ERROR URI", e+"");
        }
    }
    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getActivity().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }
}