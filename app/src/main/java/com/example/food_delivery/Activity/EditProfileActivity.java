package com.example.food_delivery.Activity;
import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.Glide;
import com.example.food_delivery.Api.ApiClient;
import com.example.food_delivery.Api.OtpApi;
import com.example.food_delivery.Model.GetProfileResponse;
import com.example.food_delivery.Model.ProfileModel;
import com.example.food_delivery.R;
import com.example.food_delivery.SharedPrefrences.DocumentPrefs;
import com.example.food_delivery.databinding.ActivityEditProfileBinding;
import com.example.food_delivery.utils.FileUtils;
import com.facebook.shimmer.ShimmerFrameLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private Uri imageUri;
    private Bitmap selectedBitmap;
    private OtpApi apiService;

    private static final String TAG = "EditProfileActivity";


    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
                if (isSuccess && imageUri != null) {
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        binding.imgProfile.setImageBitmap(selectedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        binding.imgProfile.setImageBitmap(selectedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);

        apiService = ApiClient.getClientWithToken(DocumentPrefs.getToken(this)).create(OtpApi.class);


        showShimmer();

        getProfileData();
        setupUI();
    }

    private void setupUI() {
        binding.tvChangePhoto.setOnClickListener(v -> showImageSourceDialog());
        binding.etDob.setOnClickListener(v -> showDatePicker());
        binding.btnSave.setOnClickListener(v -> {
            showShimmer(); // Show shimmer before API call
            updateProfile();
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image From");
        builder.setItems(options, (DialogInterface dialog, int which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                imageUri = Uri.fromFile(FileUtils.createImageFile(this));
                takePhotoLauncher.launch(imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    private void openGallery() {
        pickImageLauncher.launch("image/*");
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


    private void getProfileData() {
        String token = DocumentPrefs.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            hideShimmer();
            return;
        }

        showShimmer();

        Call<GetProfileResponse> call = apiService.getProfile();
        call.enqueue(new Callback<GetProfileResponse>() {
            @Override
            public void onResponse(Call<GetProfileResponse> call, Response<GetProfileResponse> response) {
                hideShimmer();

                if (response.isSuccessful() && response.body() != null && response.body().results != null) {
                    fillProfileData(response.body().results);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to fetch profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetProfileResponse> call, Throwable t) {
                hideShimmer();
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillProfileData(GetProfileResponse.Results results) {
        String fullName = ((results.firstName != null ? results.firstName : "") + " " +
                (results.lastName != null ? results.lastName : "")).trim();

        binding.etName.setText(fullName);
        binding.etMobile.setText(results.mobile != null ? results.mobile : "");
        binding.etAddress.setText(results.address != null ? results.address : "");
        binding.etCity.setText(results.city != null ? results.city : "");
        binding.etBloodGroup.setText(results.bloodGroup != null ? results.bloodGroup : "");
        binding.etDob.setText(results.dob != null ? results.dob : "");

        if (results.profile != null && !results.profile.isEmpty()) {
            String imageUrl = "http://164.52.197.192:5678/uploads/" + results.profile;
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.log)
                    .into(binding.imgProfile);
        } else {
            binding.imgProfile.setImageResource(R.drawable.log);
        }
    }


    private void updateProfile() {
        String fullName = binding.etName.getText().toString().trim();
        String mobile = binding.etMobile.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();
        String bloodGroup = binding.etBloodGroup.getText().toString().trim().toUpperCase();
        String dob = binding.etDob.getText().toString().trim();

        if (fullName.isEmpty()) { binding.etName.setError("Full name required"); hideShimmer(); return; }
        if (mobile.isEmpty()) { binding.etMobile.setError("Mobile required"); hideShimmer(); return; }
        if (!mobile.matches("^[6-9]\\d{9}$")) { binding.etMobile.setError("Enter valid mobile number"); hideShimmer(); return; }
        if (address.isEmpty()) { binding.etAddress.setError("Address required"); hideShimmer(); return; }
        if (city.isEmpty()) { binding.etCity.setError("City required"); hideShimmer(); return; }
        if (bloodGroup.isEmpty()) { binding.etBloodGroup.setError("Blood Group required"); hideShimmer(); return; }
        if (dob.isEmpty()) { binding.etDob.setError("DOB required"); hideShimmer(); return; }

        String[] nameParts = fullName.split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        RequestBody firstNameBody = RequestBody.create(okhttp3.MediaType.parse("text/plain"), firstName);
        RequestBody lastNameBody = RequestBody.create(okhttp3.MediaType.parse("text/plain"), lastName);
        RequestBody fatherName = RequestBody.create(okhttp3.MediaType.parse("text/plain"), "");
        RequestBody dobBody = RequestBody.create(okhttp3.MediaType.parse("text/plain"), dob);
        RequestBody primaryMobile = RequestBody.create(okhttp3.MediaType.parse("text/plain"), mobile);
        RequestBody secondaryMobile = RequestBody.create(okhttp3.MediaType.parse("text/plain"), "");
        RequestBody bloodGroupBody = RequestBody.create(okhttp3.MediaType.parse("text/plain"), bloodGroup);
        RequestBody cityBody = RequestBody.create(okhttp3.MediaType.parse("text/plain"), city);
        RequestBody addressBody = RequestBody.create(okhttp3.MediaType.parse("text/plain"), address);
        RequestBody languages = RequestBody.create(okhttp3.MediaType.parse("text/plain"), "hindi,english");

        MultipartBody.Part profilePart = null;
        if (selectedBitmap != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                RequestBody requestFile = RequestBody.create(okhttp3.MediaType.parse("image/*"), imageBytes);
                profilePart = MultipartBody.Part.createFormData("profile", "profile.jpg", requestFile);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error preparing image", Toast.LENGTH_SHORT).show();
                hideShimmer();
                return;
            }
        }

        Call<ProfileModel> call = apiService.updateProfile(
                firstNameBody, lastNameBody, fatherName, dobBody, primaryMobile,
                secondaryMobile, bloodGroupBody, cityBody, addressBody, languages, profilePart
        );

        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                hideShimmer();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                hideShimmer();
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void showShimmer() {
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.shimmerLayout.startShimmer();
        binding.contentScrollView.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);
        binding.contentScrollView.setVisibility(View.VISIBLE);
    }
}
