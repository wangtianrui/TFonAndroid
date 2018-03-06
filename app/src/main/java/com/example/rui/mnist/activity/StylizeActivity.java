package com.example.rui.mnist.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
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

public class StylizeActivity extends AppCompatActivity {

    @BindView(R.id.image_origin)
    ImageView imageOrigin;
    @BindView(R.id.image_result)
    ImageView imageResult;
    @BindView(R.id.choose_button)
    Button chooseButton;
    @BindView(R.id.translate_button)
    Button translateButton;


    private Stylizer stylizer;
    private Bitmap bm = null;
    private String path;
    public static final int REQUESTCODE = 1056;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stylize);
        ButterKnife.bind(this);
        stylizer = new Stylizer(getAssets());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.choose_button, R.id.translate_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.choose_button:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUESTCODE);
                break;
            case R.id.translate_button:
                imageResult.setImageDrawable(getDrawable(R.drawable.test));
                if (bm == null) {
                    Toast.makeText(this, "请先获取", Toast.LENGTH_SHORT).show();
                } else {
                    Bitmap translate = stylizer.stylizeImage(path, 1);
                    imageResult.setImageBitmap(translate);
                    break;
                }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ContentResolver resolver = getContentResolver();

        if (requestCode == REQUESTCODE) {
            try {
                Uri originalUri = data.getData(); // 获得图片的uri
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                imageOrigin.setImageBitmap(ThumbnailUtils.extractThumbnail(bm, 100, 100));  //使用系统的一个工具类，参数列表为 Bitmap Width,Height  这里使用压缩后显示，否则在华为手机上ImageView 没有显示
                // 显得到bitmap图片
                // imageView.setImageBitmap(bm);
                String[] proj = {MediaStore.Images.Media.DATA};

                // 查找。返回一个cursor对象
                Cursor cursor = resolver.query(originalUri, proj, null, null, null);

                // 返回字节长度
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头
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
}
