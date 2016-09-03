package com.gowhich.androidimageview2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button1;
    private Button button2;
    private ImageView imageView;

    //声明两个静态变量，主要用于意图的返回标志
    private static final int IMAGE_SELECT = 1;
    private static final int IMAGE_CUT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = (Button) this.findViewById(R.id.button1);
        button2 = (Button) this.findViewById(R.id.button2);
        imageView = (ImageView) this.findViewById(R.id.imageView);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_SELECT);
                break;
            case R.id.button2:
                Intent intent2 = getImageClipIntent();
                startActivityForResult(intent2, IMAGE_CUT);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            //处理图片按照手机的屏幕大小显示
            if (requestCode == IMAGE_SELECT) {
                Uri uri = data.getData();
                int dw = getWindowManager().getDefaultDisplay().getWidth() / 2;
                int dh = getWindowManager().getDefaultDisplay().getHeight();

                try {
                    //实现对图片的裁剪的类，是一个匿名内部类
                    BitmapFactory.Options factory = new BitmapFactory.Options();
                    factory.inJustDecodeBounds = true;//如果设置为true，允许查询图片
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, factory);

                    //对图片的宽度和高度对应手机的屏幕进行匹配
                    int hRatio = (int) Math.ceil(factory.outHeight/(float)dh);
                    //如果大于1 表示图片的高度大于手机屏幕的高度
                    int wRatio = (int) Math.ceil(factory.outWidth/(float)dw);
                    //如果大于1 表示图片的宽度大于手机屏幕的宽度

                    //缩放到1/ratio 的尺寸和1/ratio^2像素
                    if(hRatio > 1 || wRatio > 1){
                        if(hRatio > wRatio){
                            factory.inSampleSize = hRatio;
                        } else {
                            factory.inSampleSize = wRatio;
                        }
                    }
                    factory.inJustDecodeBounds = false;
                    //使用BitmapFactory对图片进行适屏
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, factory);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception err) {

                }
            } else if (requestCode == IMAGE_CUT) {
                Log.i("ImageCut", String.valueOf(IMAGE_CUT));
                Bitmap bitmap = data.getParcelableExtra("data");
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private Intent getImageClipIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        //实现对图片的裁剪，必须要设置图片的属性和大小
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 80);
        intent.putExtra("outputY", 80);
        intent.putExtra("return-data", true);

        return intent;
    }
}
