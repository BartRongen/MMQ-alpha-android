package com.example.bartrongen.mmqalpha;

import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Bart Rongen on 30-3-2016.
 */
public class API {

    public static final String BASE_URL = "http://mmq.audio/";
    public static final String CHANNELS = "channels";
    public static final String ADD_CHANNEL = "add";

    public interface MMQApi{
        @GET(CHANNELS)
        Call<JsonElement> getChannels();

        @POST(ADD_CHANNEL)
        Call<JsonElement> addNewChannel(@Body HashMap<String, String> body);
    }

    private static MMQApi MMQSERVICE;

    public static MMQApi getService() {

        if (MMQSERVICE == null) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();


            MMQSERVICE = retrofit.create(MMQApi.class);
        }
        return MMQSERVICE;
    }
}
