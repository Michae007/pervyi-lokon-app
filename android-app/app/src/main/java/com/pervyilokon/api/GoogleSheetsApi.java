package com.pervyilokon.api;

import com.pervyilokon.models.ApiResponse;
import com.pervyilokon.models.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GoogleSheetsApi {
    @GET("exec")
    Call<ApiResponse> getAppointments(@Query("action") String action, @Query("date") String date);
    
    @POST("exec")
    Call<ApiResponse> updateAppointmentStatus(@Body RequestBody request);
    
    @POST("exec")
    Call<ApiResponse> updateAppointmentMaster(@Body RequestBody request);
}
