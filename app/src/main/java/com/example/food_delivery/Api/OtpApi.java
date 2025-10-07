package com.example.food_delivery.Api;

import com.example.food_delivery.Activity.Sign_up;
import com.example.food_delivery.Model.DocumentResponse;
import com.example.food_delivery.Model.OtpResponse;
import com.example.food_delivery.Model.OtpVerifyResponse;
import com.example.food_delivery.Model.ProfileModel;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface OtpApi {
    @Headers("Content-Type: application/json")
    @POST("delivery-partner/request-otp")
    Call<OtpResponse> sendOtp(@Body Map<String, String> body);


    @Headers("Content-Type: application/json")
    @POST("delivery-partner/verify-otp")
    Call<OtpVerifyResponse> verifyOtp(@Body Map<String, String> body);

    @Multipart
    @PUT("delivery-partner/profile")
    Call<ProfileModel> updateProfile(
            @Part("firstName") RequestBody firstName,
            @Part("lastName") RequestBody lastName,
            @Part("fatherName") RequestBody fatherName,
            @Part("dob") RequestBody dob,
            @Part("primaryMobile") RequestBody primaryMobile,
            @Part("secondaryMobile") RequestBody secondaryMobile,
            @Part("bloodGroup") RequestBody bloodGroup,
            @Part("city") RequestBody city,
            @Part("address") RequestBody address,
            @Part("languages") RequestBody languages,
            @Part MultipartBody.Part profile  // the profile image
    );

  /*  @Headers("Content-Type: application/json")
    @Multipart("Documents")
    Call<DocumentResponse>documents(@Body Map<String, String> body);*/

    @Multipart
    @POST("delivery-partner/documents")
    Call<DocumentResponse> uploadDocuments(
            // Aadhaar
            @Part MultipartBody.Part aadhaarFront,
            @Part MultipartBody.Part aadhaarBack,

            // PAN
            @Part MultipartBody.Part panFront,
            @Part MultipartBody.Part panBack,

            // Driving Licence
            @Part MultipartBody.Part drivingLicenseFront,
            @Part MultipartBody.Part drivingLicenseBack,

            // RC (Vehicle)
            @Part MultipartBody.Part rcFront,
            @Part MultipartBody.Part rcBack,

            // Bank Details (text fields)
            @Part("accountNumber") RequestBody accountNumber,
            @Part("ifscCode") RequestBody ifscCode,
            @Part("name") RequestBody name

            // UserId (if needed for backend mapping)
           // @Part("userId") RequestBody userId
    );
}
