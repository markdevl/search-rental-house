package com.example.houserentalmanagement.houseOwner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.houserentalmanagement.HouseModel;
import com.example.houserentalmanagement.R;
import com.example.houserentalmanagement.adapter.SeeHouseAdapterOwner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SeeHouse extends AppCompatActivity {

    private RecyclerView showAllHouse;
    private SeeHouseAdapterOwner adapter;
    private ArrayList<HouseModel> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_house);

        showAllHouse = findViewById(R.id.recyclerView);
        showAllHouse.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SeeHouse.this);
        showAllHouse.setLayoutManager(linearLayoutManager);
        getAllArticle();

    }

    private void getAllArticle() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(RegisterOwner.HOUSES).child(userId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        HouseModel article = dataSnapshot.getValue(HouseModel.class);
                        mList.add(article);
                    }
                    adapter = new SeeHouseAdapterOwner(SeeHouse.this, mList);
                    showAllHouse.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}