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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import xyz.leezoom.ssm.model.SMMSApi;
import xyz.leezoom.ssm.model.UploadHelper;
import xyz.leezoom.ssm.model.gson.Upload;

public class MainActivity extends AppCompatActivity {


    private final static int REQUEST_PICK_PICTURE = 1;
    private final static int SHOW_PROCESS = 11;
    private final static int SHOW_URL_TEXT = 12;
    private final static int INIT_VIEW = 13;
    private File file;
    //private boolean isHttps = true;
    private boolean isMarkDown = false;

    @BindView(R.id.show_url_view)
    LinearLayout urlLayout;
    @BindView(R.id.upload_bt)
    Button uploadButton;
    @BindView(R.id.image_view)
    ImageView imageView;
    @BindView(R.id.url_text)
    TextView urlText;
    @BindView(R.id.https_switch)
    Switch aSwitch;
    @BindView(R.id.process_bar)
    ProgressBar progressBar;

    private Upload mUpload;
    private SMMSApi uploadApi;
    private Disposable disposable;
    private Observer<Upload> observer = new Observer<Upload>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable = d;
        }

        @Override
        public void onNext(Upload upload) {
            mUpload = upload;
            if (mUpload.getStatus().equals("success")){
                urlText.setText(mUpload.getData().getPicUrl());
            } else if (mUpload.getStatus().equals("error")) {
                urlText.setText(mUpload.getMsg());
            }
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "error");
        }

        @Override
        public void onComplete() {
            showView(SHOW_URL_TEXT);
            ClipboardManager cmb = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cmb.setText(urlText.getText().toString().trim());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        Glide.with(this)
                .load(getString(R.string.click_me_pic))
                .into(imageView);
        aSwitch.setChecked(false);
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
            isMarkDown = true;
            //markdown
            urlText.setText("![" + file.getName() + "]("
                    + mUpload.getData().getPicUrl() + ")");
        } else {
            //url
            isMarkDown = false;
            urlText.setText(mUpload.getData().getPicUrl());
        }
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
        showView(INIT_VIEW);
    }

    @OnLongClick(R.id.url_text)
    boolean copyUrl(){
        ClipboardManager cmb = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        cmb.setText(urlText.getText().toString().trim());
        return false;
    }

    @OnClick(R.id.upload_bt)
    void uploadAction() {
        Log.d("button","start upload");
        if (file != null) {
            showView(SHOW_PROCESS);
            uploadInAction();
        } else {
            Toast.makeText(MainActivity.this, "Please select picture", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadInAction() {
        uploadApi = UploadHelper.getUploadApi();
        //build http request header
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData(getString(R.string.post_file_name), file.getName(), requestFile);
        uploadApi.uploadPicture(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void showPicture(Bitmap bitmap) {
        //Glide.with(MainActivity.this)
         //       .load(bitmap)
         //       .into(imageView);
        imageView.setImageBitmap(bitmap);
    }

    private void showView(int status) {
        switch (status){
            case INIT_VIEW:
                uploadButton.setVisibility(View.VISIBLE);
                urlLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                break;
            case SHOW_PROCESS:
                uploadButton.setVisibility(View.GONE);
                urlLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case SHOW_URL_TEXT:
                uploadButton.setVisibility(View.GONE);
                urlLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }

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
                } else if (resultCode == RESULT_CANCELED && mUpload != null) {
                    showView(SHOW_URL_TEXT);
                }
                break;
            default:
                break;
        }
    }
}
