package com.example.food_delivery.Api;

import com.example.food_delivery.Activity.Sign_up;
import com.example.food_delivery.Model.AcceptRejectOrderModel;
import com.example.food_delivery.Model.DocumentGetResponse;
import com.example.food_delivery.Model.DocumentResponse;
import com.example.food_delivery.Model.OrderHistoryResponse;
import com.example.food_delivery.Model.OrderModel;
import com.example.food_delivery.Model.GetProfileResponse;
import com.example.food_delivery.Model.OtpResponse;
import com.example.food_delivery.Model.OtpVerifyResponse;
import com.example.food_delivery.Model.ProfileModel;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

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

    @GET("delivery-partner/documents")
    Call<DocumentGetResponse> getdocuments();


    @Multipart
    @PUT("delivery-partner/documents")
    Call<DocumentResponse> uploadDocuments(
            @PartMap Map<String, RequestBody> formFields,
            @Part List<MultipartBody.Part> files
    );




    @GET("delivery-partner/orders/active")
    Call<OrderModel>getActivOrders(@Header("Authorization") String bearerToken);
    @GET("delivery-partner/profile")
    Call<GetProfileResponse> getProfile();

    @POST("delivery-partner/orders/accept")
    Call<AcceptRejectOrderModel> updateAcceptRejectOrder(
            @Header("Authorization") String token,
            @Body Map<String, String> body
    );

    @GET("delivery-partner/orders/history")
    Call<OrderHistoryResponse> getHistory(@Header("Authorization") String bearerToekn);


}
