package com.example.rui.mnist.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rui.mnist.R;
import com.example.rui.mnist.tensorflow.Stylizer;

import java.io.IOException;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    @BindView(R.id.main_long_distanc_button)
    Button mainLongDistancButton;
    @BindView(R.id.main_mnist_button)
    Button mainMnistButton;

    public static final int REQUESTCODE = 1056;
    @BindView(R.id.image1)
    ImageView image1;
    @BindView(R.id.image2)
    ImageView image2;
    @BindView(R.id.translate_button)
    Button translateButton;

    private FragmentManager fragmentManager;
    private Stylizer stylizer;
    private Bitmap bm = null;
    private String path;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int PERMISSIONS_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        stylizer = new Stylizer(getAssets());
        if (!hasPermission()) {
            requestPermission();
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) || shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
                Toast.makeText(MainActivity.this, "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
        }
    }


    @OnClick({R.id.main_long_distanc_button, R.id.main_mnist_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.main_long_distanc_button:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUESTCODE);
                break;
            case R.id.main_mnist_button:
                Intent i = new Intent(MainActivity.this, MnistActivity.class);
                startActivity(i);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap translate = null;
        Log.d(TAG, "onActivityResult: " + "stylizer1");
        ContentResolver resolver = getContentResolver();

        if (requestCode == REQUESTCODE) {
            try {
                Uri originalUri = data.getData(); // 获得图片的uri
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                image1.setImageBitmap(ThumbnailUtils.extractThumbnail(bm, 100, 100));  //使用系统的一个工具类，参数列表为 Bitmap Width,Height  这里使用压缩后显示，否则在华为手机上ImageView 没有显示
                // 显得到bitmap图片
                // imageView.setImageBitmap(bm);
                String[] proj = {MediaStore.Images.Media.DATA};

                // 好像是android多媒体数据库的封装接口，具体的看Android文档
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);

                // 按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                // 最后根据索引值获取图片路径
                path = cursor.getString(column_index);

            } catch (IOException e) {
                Log.e("TAG-->Error", e.toString());

            } finally {
                return;
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.translate_button)
    public void onViewClicked() {
        image2.setImageDrawable(getDrawable(R.drawable.test));
        if (bm == null) {
            Toast.makeText(MainActivity.this, "请先获取", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "onActivityResult: " + path);

            Bitmap translate = stylizer.stylizeImage(path,1);
            image2.setImageBitmap(ThumbnailUtils.extractThumbnail(translate, 100, 100));
        }
    }
}
