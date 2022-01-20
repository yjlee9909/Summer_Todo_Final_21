package com.example.myapplication.Diary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.AlarmUi.AlarmActivity;
import com.example.myapplication.Community_ui.SetUpActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

import com.example.myapplication.ToDoList;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

//추가화면
public class Diary_main extends AppCompatActivity {
    EditText edtName, edtPrice;
    Button btnChoose, btnAdd, btnList;
    ImageView imageView;

    final int REQUEST_CODE_GALLERY = 999;

    public static com.example.myapplication.Diary.SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_main);

        //Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);


        bottomNavigationView.setItemIconTintList(null);
        //Set home selected
        bottomNavigationView.setSelectedItemId(R.id.community);

        //perform itemselectedlistener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.todolist:
                        startActivity(new Intent(getApplicationContext()
                                , ToDoList.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.community:

                        return true;
                    case R.id.calendar:
                        startActivity(new Intent(getApplicationContext()
                                , MainActivity.class));
                        overridePendingTransition(0,0);

                        return true;
                    case R.id.alarm:
                        startActivity(new Intent(getApplicationContext()
                                , AlarmActivity.class));
                        overridePendingTransition(0,0);

                        return true;
                    case R.id.info:
                        startActivity(new Intent(getApplicationContext()
                                , SetUpActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        init();

        sqLiteHelper = new com.example.myapplication.Diary.SQLiteHelper(this, "FoodDB.sqlite", null, 1);

        sqLiteHelper.queryData("CREATE TABLE IF NOT EXISTS FOOD(Id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, price VARCHAR, image BLOB)");

        //갤러리에서 사진 가져오기
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(
                        Diary_main.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });

        //추가버튼 눌렀을 때
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    sqLiteHelper.insertData(
                            edtName.getText().toString().trim(),
                            edtPrice.getText().toString().trim(),
                            imageViewToByte(imageView)
                    );
                    Intent intent = new Intent(Diary_main.this, DiaryList.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "추가되었습니다.", Toast.LENGTH_SHORT).show();

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        //리스트 보기
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Diary_main.this, DiaryList.class);
                startActivity(intent);
            }
        });
    }

    //이미지->바이트 함수
    public static byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    //바이트->이미지 함수
    public static Bitmap getImageFromByte(byte[] image){
        Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length);
        return bitmap;
    }

    //갤러리 허가 여부
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                launcher.launch(intent);
            }
            else {
                Toast.makeText(getApplicationContext(), "파일 접근에 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //갤러리에서 사진 가져오기
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>()
    {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK){
                //Uri uri = getIntent().getData();
                try {
                    Log.e(TAG, "result : " + result);
                    Intent intent = result.getData();
                    Log.e(TAG, "intent : " + intent);
                    Uri uri = intent.getData();
                    Log.e(TAG, "uri : " + uri);
                    InputStream inputStream = getContentResolver().openInputStream(uri);

                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            //super.onActivityResult(requestCode, resultCode, data);
        }
    });


    private void init(){
        edtName = (EditText) findViewById(R.id.edtName);
        edtPrice = (EditText) findViewById(R.id.edtPrice);
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnList = (Button) findViewById(R.id.btnList);
        imageView = (ImageView) findViewById(R.id.imageView);
    }
}