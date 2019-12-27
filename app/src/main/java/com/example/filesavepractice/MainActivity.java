package com.example.filesavepractice;

//AndroidX
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v4.app.ActivityCompat;
//import android.support.annotation.NonNull;

import android.os.Bundle;
import android.os.Bundle;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private ImageView imageView;
    private ImageView imageView2;

    private final int REQUEST_PERMISSION = 1000;

    // asset の画像ファイル名
    private String fileName = "sample_image1.jpg";
    private String fileName2 = "sample_image2.jpg";
    private File file;
    private File file2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 画像を置く外部ストレージ先
        ///パスを保存, getExternalFilesDirで獲得できる
        ///Environment.DIRECTORY_PICTURES ユーザが写真をおけるディレクトリへのパス
        ///https://developer.android.com/reference/android/os/Environment.html#DIRECTORY_PICTURES
        ///public static String DIRECTORY_PICTURES
        ////他にも, Environment.でいろんなパスを獲得できる
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //File型のpathとfileの名前を引数にする
        file = new File(path, fileName);
        file2 = new File(path,fileName2);


        //textViewにtext_viewをセット
        textView = findViewById(R.id.text_view);


        //出力のための文字列かな?
        String str = "image file1: "+fileName;

        //textViewに文字列をセットする
        //textView.setText([String])で文字列がテキストにセットできるっぽい
        textView.setText(str);

        //imageViewにimage_viewをセットする
        imageView = findViewById(R.id.image_view);
        imageView2 = findViewById(R.id.image2);


        // Android 6, API 23以上でパーミッシンの確認
        if(Build.VERSION.SDK_INT >= 23){
            checkPermission();
        }
        else{
            setUpWriteExternalStorage();
        }
    }


    private void setUpWriteExternalStorage(){
        //buttonSaveにbutton_saveを割り当てる
        Button buttonSave = findViewById(R.id.button_save);
        Button buttonSave2 = findViewById(R.id.button_save2);
        // 外部ストレージに画像を保存する

        ///ButtonクラスのsetOnClickListenerでボタンを押した場合の対処をする
        buttonSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {

                try(// assetsから画像ファイルを取り出し
                    //assetsにアクセスするときは, getResource().getAssets().open([string])で良さそう
                    InputStream inputStream =
                            getResources().getAssets().open(fileName);
                    // 外部ストレージに画像を保存
                    //アウトプットストリーム??
                    FileOutputStream output =
                            new FileOutputStream(file)) {

                    // バッファーを使って画像を書き出す
                    int DEFAULT_BUFFER_SIZE = 10240 * 4;
                    byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
                    int len;

                    //inputStream.read(byte[] b)
                    //実際に読み込まれたバイト数は整数として返される
                    //入力ストリームから数バイト読み込み, それをバッファー配列bに格納する
                    //ストリームがファイルの終わりに達したために読み込むバイトがない場合は-1を返す
                    //https://docs.oracle.com/javase/jp/6/api/java/io/InputStream.html#read()
                    while((len=inputStream.read(buf))!=-1){

                        //指定されたバイト配列のオフセット位置offから始まるlenバイトをこのファイル出力ストリームに書き込む
                        //outputはファイル出力ストリーム
                        //bufがbyte配列
                        output.write(buf,0,len);
                    }

                    //このメソッドはよくわからない
                    https://docs.oracle.com/javase/jp/8/docs/api/java/io/OutputStream.html#flush--
                    output.flush();

                    //R.stringを利用すると, res>values>string.xmlの情報にアクセスできる
                    textView.setText(R.string.saved);

                    // 保存した画像をアンドロイドのデータベースへ登録
                    registerDatabase(file);
                    //例外処理
                } catch (IOException e) {
                    e.printStackTrace();
                    textView.setText(R.string.error);
                }
            }
        });


        //Button2を押した場合の処理
        buttonSave2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){

                try(
                        InputStream inputStream = getResources().getAssets().open(fileName2);

                        FileOutputStream output2 = new FileOutputStream(file2)) {
                    int DEFAULT_BUFFER_SIZE = 10240 * 4;
                    byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
                    int len;

                    while ((len = inputStream.read(buf)) != -1) {
                        output2.write(buf, 0, len);
                    }
                    output2.flush();
                    textView.setText(R.string.saved2);

                    registerDatabase(file2);
                }catch(IOException e){
                    e.printStackTrace();
                    textView.setText(R.string.error);
                }
            }

        });



        Button buttonRead = findViewById(R.id.button_read);
        Button buttonRead2 = findViewById(R.id.button_read2);
        //ボタンを押した場合の処理
        buttonRead.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                //インプットストリームを作成
                //ファイルストリームとは, 連続したデータを「流れるもの」として捉え, そのデータの入出力あるいは送受信を
                //扱うことであり, またその操作のための抽象データがたを指す.
                try(InputStream inputStream0 =
                            new FileInputStream(file) ) {
                    //Bitmap・・・画像のデータ形式のひとつ
                    //https://wa3.i-3-i.info/word1652.html
                    //BitmapFactory 様々なソース(file, stream, byte-array)からBitmapオブジェクトを生成するmethod
                    //.decodeStreamはおそらくストリームからbitmapを生成している
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream0);
                    //imageViewにBitmapをセットしている
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonRead2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                try(InputStream inputStream0 =
                            new FileInputStream(file2)) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream0);
                    imageView2.setImageBitmap(bitmap);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    // アンドロイドのデータベースへ登録する
    private void registerDatabase(File file) {
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put("_data", file.getAbsolutePath());
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues);
    }

    // permissionの確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            setUpWriteExternalStorage();
        }
        // 拒否していた場合
        else{
            requestLocationPermission();
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            Toast toast = Toast.makeText(this, "許可してください", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);
        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpWriteExternalStorage();
            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "何もできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
