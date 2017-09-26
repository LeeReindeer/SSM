package xyz.leezoom.ssm;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import xyz.leezoom.ssm.model.SMUploadApi;
import xyz.leezoom.ssm.model.UploadHelper;
import xyz.leezoom.ssm.model.gson.Upload;

public class MainActivity extends AppCompatActivity {


    private final static int REQUEST_PICK_PICTURE = 1;
    private File file;
    private boolean isHttps = true;

    @BindView(R.id.upload_bt)
    Button uploadButton;
    @BindView(R.id.image_view)
    ImageView imageView;
    @BindView(R.id.url_text)
    TextView urlText;
    @BindView(R.id.https_switch)
    Switch aSwitch;

    private Upload mUpload;
    private SMUploadApi uploadApi;
    private Disposable disposable;
    private Observable<Upload> observable;
    private Observer<Upload> observer = new Observer<Upload>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable = d;
        }

        @Override
        public void onNext(Upload upload) {
            mUpload = upload;
            test();

        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "error");
        }

        @Override
        public void onComplete() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        aSwitch.setChecked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @OnCheckedChanged(R.id.https_switch)
    void OnSwitchChecked(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            isHttps = true;
            aSwitch.setText("Turn off Https");
        } else {
            isHttps = false;
            aSwitch.setText(getString(R.string.turn_on_https));
        }
        Log.d("Switch", isHttps + "");
    }

    @OnClick(R.id.image_view)
    void pickImage() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            Log.d("image", "pick");
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_PICTURE);
        }
        urlText.setVisibility(View.GONE);
        uploadButton.setVisibility(View.VISIBLE);
    }

    @OnLongClick(R.id.url_text)
    boolean copyUrl(){
        ClipboardManager cmb = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        cmb.setText(urlText.getText().toString().trim());
        return false;
    }

    @OnClick(R.id.upload_bt)
    void uploadAction() {
        Log.d("button","hi");
        uploadInAction();
    }

    private void uploadInAction() {
        uploadApi = UploadHelper.init();
        if (file != null) {
            //build http request header
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("smfile", file.getName(), requestFile);
            uploadApi.uploadPicture(body, isHttps)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }

    void test() {
        if (mUpload.getStatus().equals("success")){
            String u = mUpload.getData().getPicUrl();
            urlText.setText(mUpload.getData().getPicUrl());
        } else if (mUpload.getStatus().equals("error")) {
            urlText.setText(mUpload.getMsg());
        }
        uploadButton.setVisibility(View.GONE);
        urlText.setVisibility(View.VISIBLE);
    }

    private void showPicture(Bitmap bitmap) {
        //Glide.with(MainActivity.this)
         //       .load(bitmap)
         //       .into(imageView);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                pickImage();
            }else {
                Log.d("mainActivity","Permissions deny");
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_PICTURE:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri uri = data.getData();
                        file = new File(PathUtil.getPath(MainActivity.this, uri));
                        showPicture(BitmapFactory.decodeStream(getContentResolver().openInputStream(uri)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT);
                    }
                }
                break;
            default:
                break;
        }
    }
}
