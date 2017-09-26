package xyz.leezoom.ssm.model;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @Author lee
 * @Time 9/26/17.
 */

public class UploadHelper {

    private static SMMSApi uploadApi;

    public static SMMSApi getUploadApi() {
        if (uploadApi == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://sm.ms/api/")
                    .client(new OkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            uploadApi = retrofit.create(SMMSApi.class);
        }
        return uploadApi;
    }

}
