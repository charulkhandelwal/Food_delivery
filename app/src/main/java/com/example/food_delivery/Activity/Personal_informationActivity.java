package com.example.food_delivery.Activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.etDob.setOnClickListener(v -> showDatePicker());
        binding.btnUpload.setOnClickListener(v -> showImagePicker());
        binding.btnSubmit.setOnClickListener(v -> submitProfileMultipart());

    }

    private void populateProfileData(ProfileModel profile) {
        if (profile != null && profile.results != null) {
            binding.etFirstName.setText(profile.results.firstName);
            binding.etLastName.setText(profile.results.lastName);
            binding.etDob.setText(profile.results.dob);
            binding.etPrimaryMobile.setText(profile.results.mobile);
            binding.etBloodGroup.setText(profile.results.bloodGroup);
            binding.etCity.setText(profile.results.city);
            binding.etAddress.setText(profile.results.address);
            binding.etFatherName.setText(profile.results.fatherName);
            binding.etSecondaryMobile.setText(profile.results.secondaryMobile);
        }
    }
    private void submitProfileMultipart() {
        // Collect form data
        String firstNameStr = binding.etFirstName.getText().toString().trim();
        String lastNameStr = binding.etLastName.getText().toString().trim();
        String fatherNameStr = binding.etFatherName.getText().toString().trim();
        String dobStr = binding.etDob.getText().toString().trim();
        String primaryMobileStr = binding.etPrimaryMobile.getText().toString().trim();
        String secondaryMobileStr = binding.etSecondaryMobile.getText().toString().trim();
        String bloodGroupStr = binding.etBloodGroup.getText().toString().trim();
        String cityStr = binding.etCity.getText().toString().trim();
        String addressStr = binding.etAddress.getText().toString().trim();
        String languagesStr = "hindi,english"; // static for now


        if (firstNameStr.isEmpty()) {
            binding.etFirstName.setError("First name required");
            binding.etFirstName.requestFocus();
            return;
        }
        if (lastNameStr.isEmpty()) {
            binding.etLastName.setError("Last name required");
            binding.etLastName.requestFocus();
            return;
        }
        if (fatherNameStr.isEmpty()) {
            binding.etFatherName.setError("Father name required");
            binding.etFatherName.requestFocus();
            return;
        }
        if (dobStr.isEmpty()) {
            binding.etDob.setError("DOB required");
            binding.etDob.requestFocus();
            return;
        }
        if (primaryMobileStr.isEmpty()) {
            binding.etPrimaryMobile.setError("Primary mobile required");
            binding.etPrimaryMobile.requestFocus();
            return;
        }
        if (bloodGroupStr.isEmpty()) {
            binding.etBloodGroup.setError("Blood group required");
            binding.etBloodGroup.requestFocus();
            return;
        }
        if (cityStr.isEmpty()) {
            binding.etCity.setError("City required");
            binding.etCity.requestFocus();
            return;
        }
        if (addressStr.isEmpty()) {
            binding.etAddress.setError("Address required");
            binding.etAddress.requestFocus();
            return;
        }

        if (selectedProfileImage == null) {
            Toast.makeText(this, "Profile image required", Toast.LENGTH_SHORT).show();
            return;
        }


        Log.d("PROFILE_DEBUG", "firstName: " + firstNameStr);
        Log.d("PROFILE_DEBUG", "lastName: " + lastNameStr);
        Log.d("PROFILE_DEBUG", "fatherName: " + fatherNameStr);
        Log.d("PROFILE_DEBUG", "dob: " + dobStr);
        Log.d("PROFILE_DEBUG", "primaryMobile: " + primaryMobileStr);
        Log.d("PROFILE_DEBUG", "secondaryMobile: " + secondaryMobileStr);
        Log.d("PROFILE_DEBUG", "bloodGroup: " + bloodGroupStr);
        Log.d("PROFILE_DEBUG", "city: " + cityStr);
        Log.d("PROFILE_DEBUG", "address: " + addressStr);
        Log.d("PROFILE_DEBUG", "languages: " + languagesStr);


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

            Log.d("PROFILE_DEBUG", "Profile image attached, size: " + imageBytes.length + " bytes");
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
                    Log.d("PROFILE_DEBUG", "API Success: " + new Gson().toJson(response.body()));
                    Toast.makeText(Personal_informationActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    // ✅ Next Screen
                    Intent intent = new Intent(Personal_informationActivity.this, Document_Activity.class);
                    startActivity(intent);
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



    /*  private void submitProfile() {
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String dob = binding.etDob.getText().toString().trim();
        String mobile = binding.etPrimaryMobile.getText().toString().trim();
        String bloodGroup = binding.etBloodGroup.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String fatherName = binding.etFatherName.getText().toString().trim();
        String secondaryMobile = binding.etSecondaryMobile.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("dob", dob);
        body.put("mobile", mobile);
        body.put("bloodGroup", bloodGroup);
        body.put("city", city);
        body.put("address", address);
        body.put("fatherName", fatherName);
        body.put("secondaryMobile", secondaryMobile);


        if (selectedProfileImage != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedProfileImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            body.put("profile", encodedImage); // backend field = "profile"
        }

        Log.d("API_CALL", "Request Body: " + new Gson().toJson(body));

        OtpApi api = ApiClient.getClient().create(OtpApi.class);
        Call<ProfileModel> call = api.profile(body);

        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_CALL", "Success: " + new Gson().toJson(response.body()));
                    Toast.makeText(Personal_informationActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    populateProfileData(response.body());
                } else {
                    Log.e("API_CALL", "Error code: " + response.code() + " Error body: " + response.errorBody());
                    Toast.makeText(Personal_informationActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                Log.e("API_CALL", "API Failed: " + t.getMessage(), t);
                Toast.makeText(Personal_informationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
*/
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(
                this,
                (DatePicker view, int year1, int month1, int dayOfMonth) ->
                        binding.etDob.setText(dayOfMonth + "-" + (month1 + 1) + "-" + year1),
                year, month, day
        ).show();
    }

    private void showImagePicker() {
        String[] options = {"Camera", "Gallery"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Camera
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
        startActivityForResult(new android.content.Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
    }

    private void openGallery() {
        startActivityForResult(new android.content.Intent(android.content.Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
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
