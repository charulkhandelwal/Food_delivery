package com.example.food_delivery.Activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.ProfileModel;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityPersonalInformationBinding;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Personal_informationActivity extends AppCompatActivity {

    private ActivityPersonalInformationBinding binding;
    private static final int CAMERA_REQUEST = 1001;
    private static final int GALLERY_REQUEST = 1002;

    private Bitmap selectedProfileImage = null;
    private JSONArray citiesArray;

    // ✅ Valid blood group list
    private final List<String> validBloodGroups = Arrays.asList(
            "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ If profile already saved, skip this screen
        if (DocumentPrefs.getProfile(this) != null) {
            startActivity(new Intent(this, Document_Activity.class));
            finish();
            return;
        }

        binding = ActivityPersonalInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.etDob.setOnClickListener(v -> showDatePicker());
        binding.btnUpload.setOnClickListener(v -> showImagePicker());
        binding.btnSubmit.setOnClickListener(v -> submitProfileMultipart());

        loadCitiesData(); // Load cities from JSON
    }

    private void loadCitiesData() {
        try {
            InputStream is = getAssets().open("cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            citiesArray = new JSONArray(json);

            ArrayList<String> cityList = new ArrayList<>();
            for (int i = 0; i < citiesArray.length(); i++) {
                cityList.add(citiesArray.getString(i));
            }

            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, cityList);
            binding.spinnerCity.setAdapter(cityAdapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load cities", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitProfileMultipart() {
        String firstNameStr = binding.etFirstName.getText().toString().trim();
        String lastNameStr = binding.etLastName.getText().toString().trim();
        String fatherNameStr = binding.etFatherName.getText().toString().trim();
        String dobStr = binding.etDob.getText().toString().trim();
        String primaryMobileStr = binding.etPrimaryMobile.getText().toString().trim();
        String secondaryMobileStr = binding.etSecondaryMobile.getText().toString().trim();
        String bloodGroupStr = binding.etBloodGroup.getText().toString().trim().toUpperCase();
        String cityStr = binding.spinnerCity.getSelectedItem().toString().trim();
        String addressStr = binding.etAddress.getText().toString().trim();
        String languagesStr = "hindi,english";

        if (firstNameStr.isEmpty()) { binding.etFirstName.setError("First name required"); return; }
        if (lastNameStr.isEmpty()) { binding.etLastName.setError("Last name required"); return; }
        if (fatherNameStr.isEmpty()) { binding.etFatherName.setError("Father name required"); return; }
        if (dobStr.isEmpty()) { binding.etDob.setError("DOB required"); return; }
        if (primaryMobileStr.isEmpty()) { binding.etPrimaryMobile.setError("Primary mobile required"); return; }

        // ✅ Blood group validation
        if (bloodGroupStr.isEmpty()) {
            binding.etBloodGroup.setError("Blood group required");
            return;
        } else if (!validBloodGroups.contains(bloodGroupStr)) {
            binding.etBloodGroup.setError("Enter valid blood group (e.g. A+, B-, O+)");
            return;
        }

        if (cityStr.isEmpty()) { Toast.makeText(this, "Please select a city", Toast.LENGTH_SHORT).show(); return; }
        if (addressStr.isEmpty()) { binding.etAddress.setError("Address required"); return; }
        if (selectedProfileImage == null) { Toast.makeText(this, "Profile image required", Toast.LENGTH_SHORT).show(); return; }

        Log.d("PROFILE_DEBUG", "firstName: " + firstNameStr);
        Log.d("PROFILE_DEBUG", "bloodGroup: " + bloodGroupStr);

        // Prepare request bodies
        RequestBody firstName = RequestBody.create(okhttp3.MediaType.parse("text/plain"), firstNameStr);
        RequestBody lastName = RequestBody.create(okhttp3.MediaType.parse("text/plain"), lastNameStr);
        RequestBody fatherName = RequestBody.create(okhttp3.MediaType.parse("text/plain"), fatherNameStr);
        RequestBody dob = RequestBody.create(okhttp3.MediaType.parse("text/plain"), dobStr);
        RequestBody primaryMobile = RequestBody.create(okhttp3.MediaType.parse("text/plain"), primaryMobileStr);
        RequestBody secondaryMobile = RequestBody.create(okhttp3.MediaType.parse("text/plain"), secondaryMobileStr);
        RequestBody bloodGroup = RequestBody.create(okhttp3.MediaType.parse("text/plain"), bloodGroupStr);
        RequestBody city = RequestBody.create(okhttp3.MediaType.parse("text/plain"), cityStr);
        RequestBody address = RequestBody.create(okhttp3.MediaType.parse("text/plain"), addressStr);
        RequestBody languages = RequestBody.create(okhttp3.MediaType.parse("text/plain"), languagesStr);

        MultipartBody.Part profile = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedProfileImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            RequestBody requestFile = RequestBody.create(okhttp3.MediaType.parse("image/*"), imageBytes);
            profile = MultipartBody.Part.createFormData("profile", "profile.jpg", requestFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to prepare image", Toast.LENGTH_SHORT).show();
            return;
        }

        OtpApi api = ApiClient.getClientWithToken(DocumentPrefs.getToken(this)).create(OtpApi.class);
        Call<ProfileModel> call = api.updateProfile(
                firstName, lastName, fatherName, dob, primaryMobile, secondaryMobile,
                bloodGroup, city, address, languages, profile
        );

        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Save profile locally to skip next time
                    String profileJson = new Gson().toJson(response.body());
                    DocumentPrefs.saveProfile(Personal_informationActivity.this, profileJson);

                    Toast.makeText(Personal_informationActivity.this,
                            "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    // Move to Document upload
                    startActivity(new Intent(Personal_informationActivity.this, Document_Activity.class));
                    finish();
                } else {
                    Log.e("PROFILE_DEBUG", "API Error: Code " + response.code());
                    Toast.makeText(Personal_informationActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                Log.e("PROFILE_DEBUG", "API Failure: " + t.getMessage(), t);
                Toast.makeText(Personal_informationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) ->
                        binding.etDob.setText(dayOfMonth + "-" + (month + 1) + "-" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showImagePicker() {
        String[] options = {"Camera", "Gallery"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                        } else {
                            openCamera();
                        }
                    } else {
                        openGallery();
                    }
                }).show();
    }

    private void openCamera() {
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
    }

    private void openGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CAMERA_REQUEST) {
                selectedProfileImage = (Bitmap) data.getExtras().get("data");
                binding.ivProfile.setImageBitmap(selectedProfileImage);
            } else if (requestCode == GALLERY_REQUEST) {
                Uri selectedImageUri = data.getData();
                try {
                    selectedProfileImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    binding.ivProfile.setImageBitmap(selectedProfileImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }
}
