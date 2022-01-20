package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.AlarmUi.AlarmActivity;
import com.example.myapplication.Community_ui.AddPostActivity;
import com.example.myapplication.Community_ui.Post;
import com.example.myapplication.Community_ui.PostAdapter;
import com.example.myapplication.Community_ui.SetUpActivity;
import com.example.myapplication.Community_ui.SignInActivity;
import com.example.myapplication.Community_ui.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Community extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Toolbar mainToolbar;
    private FirebaseFirestore firestore;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;
    private PostAdapter adapter;
    private List<Post> list;
    private Query query;
    private ListenerRegistration listenerRegistration;
    private List<Users> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

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
                }
                return false;
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        //mainToolbar = findViewById(R.id.main_toolbar);

        mRecyclerView = findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(Community.this));
        list = new ArrayList<>();
        usersList = new ArrayList<>();
        adapter = new PostAdapter(Community.this , list, usersList);

        mRecyclerView.setAdapter(adapter);

        fab = findViewById(R.id.floatingActionButton);
        /*setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Community");*/

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Community.this , AddPostActivity.class));
            }
        });

        //플로팅 버튼
        FloatingActionButton fb_back2 = findViewById(R.id.fb_back2);
        fb_back2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Community.this, Community_button.class);
                startActivity(intent);
            }
        });


        if (firebaseAuth.getCurrentUser() != null){

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean isBottom = !mRecyclerView.canScrollVertically(1);
                    if (isBottom)
                        Toast.makeText(Community.this, "Reached Bottom", Toast.LENGTH_SHORT).show();
                }
            });
            query = firestore.collection("Posts").orderBy("time" , Query.Direction.DESCENDING);
            listenerRegistration = query.addSnapshotListener(Community.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    for (DocumentChange doc : value.getDocumentChanges()){
                        if (doc.getType() == DocumentChange.Type.ADDED){
                            String postId = doc.getDocument().getId();
                            Post post = doc.getDocument().toObject(Post.class).withId(postId);
                            String postUserId = doc.getDocument().getString("user");
                            firestore.collection("Users").document(postUserId).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()){
                                                Users users = task.getResult().toObject(Users.class);
                                                usersList.add(users);
                                                list.add(post);
                                                adapter.notifyDataSetChanged();
                                            }else{
                                                Toast.makeText(Community.this, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }else{
                            adapter.notifyDataSetChanged();
                        }
                    }
                    listenerRegistration.remove();
                }
            });
        }
    }
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null){
            startActivity(new Intent(Community.this , SignInActivity.class));
            finish();
        }else{
            String currentUserId = firebaseAuth.getCurrentUser().getUid();
            firestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (!task.getResult().exists()){
                            startActivity(new Intent(Community.this , SetUpActivity.class));
                            finish();
                        }
                    }
                }
            });
        }

    }

    //위에 점세개 눌렀을 때 나오는거 : 로그아웃, 프로필 수정 (프로필 수정을 info에 연결했음)
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile_menu){
            startActivity(new Intent(Community.this , SetUpActivity.class));
        }else if(item.getItemId() == R.id.sign_out_menu){
            firebaseAuth.signOut();
            startActivity(new Intent(Community.this , SignInActivity.class));
            finish();
        }
        return true;
    }*/
}