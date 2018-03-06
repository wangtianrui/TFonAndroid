package com.example.rui.mnist.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rui.mnist.R;
import com.example.rui.mnist.tensorflow.Stylizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;


    private Stylizer stylizer;
    private Bitmap bm = null;
    private String path;
    public static final int REQUESTCODE = 1056;
    private List<String> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stylize);
        ButterKnife.bind(this);
        stylizer = new Stylizer(getAssets());
        for (int i = 0; i < 25; i++) {
            mList.add("1");
        }
        StylesAdapter stylesAdapter = new StylesAdapter(mList, StylizeActivity.this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(stylesAdapter);
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
                imageOrigin.setImageBitmap(bm);
                //ThumbnailUtils.extractThumbnail(bm, 100, 100)
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

    /*
    -----------------------------Holder--------------------------------
     */

    public class StylesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.button)
        Button button;


        public StylesHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindView(int position) {
            button.setText(position + "");
        }

        @OnClick(R.id.button)
        public void onViewClicked() {
            imageResult.setImageBitmap(stylizer.stylizeImage(path, Integer.parseInt(button.getText().toString()) + 1));
        }
    }
    /*
    --------------------------------------------Adapter----------------
     */

    public class StylesAdapter extends RecyclerView.Adapter<StylesHolder> {
        private List<String> mList;
        private Context mContext;

        public StylesAdapter(List<String> mList, Context mContext) {
            this.mList = mList;
            this.mContext = mContext;
        }

        @NonNull
        @Override
        public StylesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.styles_item, parent, false);
            return new StylesHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull StylesHolder holder, int position) {
            holder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}
