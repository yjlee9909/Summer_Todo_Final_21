package com.example.myapplication.Community_ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.AlarmUi.AlarmActivity;
import com.example.myapplication.Community_button;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ToDoList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

//info 클릭시 실행되는 창
public class SetUpActivity2 extends AppCompatActivity {
    private CircleImageView circleImageView;
    private EditText mProfileName;
    private Button mSaveBtn;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private String Uid;
    private Uri mImageUri = null;
    private ProgressBar progressBar;
    private boolean isPhotoSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up2);

        Toolbar setUpToolbar = findViewById(R.id.setup_toolbar);
        setSupportActionBar(setUpToolbar);
//        getSupportActionBar().setTitle("Profile");

        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        Uid = auth.getCurrentUser().getUid();

        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);
        circleImageView = findViewById(R.id.circleImageView2);
        mProfileName = findViewById(R.id.profile_name2);
        mSaveBtn = findViewById(R.id.save_btn2);

        auth = FirebaseAuth.getInstance();



        //시작
        firestore.collection("Users").document(Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String imageUrl = task.getResult().getString("image");
                        mProfileName.setText(name);
                        mImageUri = Uri.parse(imageUrl);

                        Glide.with(SetUpActivity2.this).load(imageUrl).into(circleImageView);
                    }
                }
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String name = mProfileName.getText().toString();
                StorageReference imageRef = storageReference.child("Profile_pics").child(Uid + ".jpg");
                if (isPhotoSelected) {
                    if (!name.isEmpty() && mImageUri != null) {
                        imageRef.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            saveToFireStore(task, name, uri);
                                        }
                                    });

                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SetUpActivity2.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SetUpActivity2.this, "Please Select picture and write your name", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    saveToFireStore(null , name , mImageUri);
                }
            }
        });
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(SetUpActivity2.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SetUpActivity2.this , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} , 1);
                    }else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetUpActivity2.this);
                    }
                }
            }
        });
    }

    private void saveToFireStore(Task<UploadTask.TaskSnapshot> task, String name, Uri downloadUri) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("image", downloadUri.toString());
        firestore.collection("Users").document(Uid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity2.this, "저장되었습니다!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetUpActivity2.this, MainActivity.class));
                    finish();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity2.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                mImageUri = result.getUri();
                circleImageView.setImageURI(mImageUri);

                isPhotoSelected = true;
            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null){
            startActivity(new Intent(SetUpActivity2.this , SignInActivity.class));
            finish();
        }else{
            String currentUserId = auth.getCurrentUser().getUid();
            firestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (!task.getResult().exists()){
                            startActivity(new Intent(SetUpActivity2.this , SetUpActivity.class));
                            finish();
                        }
                    }
                }
            });
        }

    }*/

    //위에 점세개 눌렀을 때 나오는거 : 로그아웃, 프로필 수정 (프로필 수정을 info에 연결했음)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /*if (item.getItemId() == R.id.profile_menu){
            startActivity(new Intent(SetUpActivity.this , SetUpActivity.class));
        }else if(item.getItemId() == R.id.sign_out_menu){
            auth.signOut();
            startActivity(new Intent(SetUpActivity.this , SignInActivity.class));
            finish();
        }*/
        if(item.getItemId() == R.id.sign_out_menu){
            auth.signOut();
            startActivity(new Intent(SetUpActivity2.this , SignInActivity.class));
            finish();
        }

        return true;
    }
}