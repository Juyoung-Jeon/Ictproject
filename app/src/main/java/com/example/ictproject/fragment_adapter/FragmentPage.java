package com.example.ictproject.fragment_adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.ictproject.R;
import com.example.ictproject.activity.LoginActivity;
import com.example.ictproject.activity.Resume_revise;
import com.example.ictproject.upload.CompanyUpload;
import com.example.ictproject.upload.Upload;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class FragmentPage extends Fragment {

    private LinearLayout logout, reviseResume, withdraw;
    private ImageView imageView;
    private TextView name;
    private String uid;
    private DatabaseReference dataRef;
    private StorageReference mStorageRef;

    public FragmentPage() {
        // Required empty public constructor
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_page, container, false);

        logout = view.findViewById(R.id.logOut);
        reviseResume = view.findViewById(R.id.reviseResume);
        withdraw = view.findViewById(R.id.withdraw);
        imageView = view.findViewById(R.id.myPageImage);
        name = view.findViewById(R.id.myPageName);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dataRef = FirebaseDatabase.getInstance().getReference("user");
        mStorageRef = FirebaseStorage.getInstance().getReference("user");

        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (uid != null){
                    if (dataSnapshot.child("employee").hasChild(uid)) {
                        Upload upload = dataSnapshot.child("employee").child(uid).getValue(Upload.class);
                        Glide.with(getActivity())
                                .load(upload.getImageUrl())
                                .fitCenter()
                                .into(imageView);
                        name.setText(upload.getName());
                    } else if (dataSnapshot.child("company").hasChild(uid)){
                        CompanyUpload upload = dataSnapshot.child("company").child(uid).getValue(CompanyUpload.class);
                        imageView.setImageResource(R.drawable.ic_person);
                        name.setText(upload.getCompanyName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        reviseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Resume_revise.class);
                startActivity(intent);
            }
        });

        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getActivity(), "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            getActivity().finish();
                            startActivity(new Intent(getActivity(), LoginActivity.class));

                            dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("employee").hasChild(uid)){
                                        dataRef.child("employee").child(uid).removeValue();
                                        mStorageRef.child(uid).delete();
                                    } else {
                                        dataRef.child("company").child(uid).removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                        }

                    }
                });
            }
        });

        return view;
    }
}
