package com.segwumbo.www.wumbo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AvailableServices extends AppCompatActivity {
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseServices;

    private ArrayList<Service> allServices, currentServices;
    private static ArrayList<Service> servicesToAdd;

    private RecyclerView rvServices;
    private UserAccount userAccount;
    private String userKey;

    @Override
    protected void onStart() {

        super.onStart();
        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot user : dataSnapshot.getChildren()){
                    if (user.getKey().equals(userKey)){
                        userAccount = user.getValue(UserAccount.class);
                        ArrayList<Service> tempService = userAccount.getProfile().getServicesOffered();
                        if(tempService != null && !tempService.isEmpty()) {
                            currentServices.addAll(tempService);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        databaseServices.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (allServices != null) {
                    boolean currentServiceBool;
                    // getting all user account data from firebase
                    for (DataSnapshot serviceSnapshot : dataSnapshot.getChildren()) {
                        Service service = serviceSnapshot.getValue(Service.class);
                        currentServiceBool = false;

                        if(!currentServices.isEmpty()) {
                            for (Service currentService : currentServices) {
                                if (currentService.getName().equals(service.getName())) {
                                    currentServiceBool = true;
                                    break;
                                }
                            }
                            if(!currentServiceBool){ allServices.add(service); }
                        }
                        else{ allServices.add(service); }
                    }
                    ServiceAdapter sAdapter = new ServiceAdapter(allServices, new ClickListener() {
                        @Override
                        public void onPositionClicked(int position) { }
                    }, 2);
                    rvServices.setAdapter(sAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        rvServices.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_services);

        // gets the reference of the database
        databaseServices = FirebaseDatabase.getInstance().getReference("services");
        databaseUsers = FirebaseDatabase.getInstance().getReference("users");
        rvServices = findViewById(R.id.availableServices);
        allServices = new ArrayList<>();
        currentServices = new ArrayList<>();
        servicesToAdd = new ArrayList<>();

        databaseUsers = FirebaseDatabase.getInstance().getReference("users");
        Bundle bundle = getIntent().getExtras();
        Bundle infoBundle = bundle.getBundle("bundle");
        userKey = infoBundle.getString("userKey");
    }

    static void addService(Service service){
        servicesToAdd.add(service);
    }

    public void onClickUpdate(View view){
        currentServices.addAll(servicesToAdd);
        databaseUsers.child(userKey).child("profile").child("servicesOffered").setValue(currentServices);
        Toast.makeText(this, "Services Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
