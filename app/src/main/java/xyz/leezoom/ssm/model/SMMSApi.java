package xyz.leezoom.ssm.model;

import java.io.File;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import xyz.leezoom.ssm.model.gson.Upload;

/**
 * @Author lee
 * @Time 9/26/17.
 */

public interface SMMSApi {

    /**
     * <p>An sm.ms api to upload picture.There has three params,<code>smfile</code>is the image file will be uploaded;
     * <code>ssl</code> is forced https;<code>format</code>, json  is the default format.
     *
     * @param file
     * @return
     */

    @Multipart
    @POST("upload")
    Observable<Upload> uploadPicture(@Part MultipartBody.Part file);


    @GET("list")
    Observable<Upload> getList();

    @GET("clear")
    Observable<Upload> clearList();
}
