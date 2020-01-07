package com.xterraengine.tycho;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.xterraengine.tycho.utils.Constantes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TychoActivity extends AppCompatActivity {

    private ImageView ivExit;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Intent intent;
    private DatabaseReference mDatabase;// ...
    private FloatingActionButton floatingActionButton;
    private EditText etMensaje;
    private ListView listview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tycho);
        session();
        updateInformationFirebae();

        ivExit = findViewById(R.id.main_exit);
        floatingActionButton = findViewById(R.id.fab_save);
        etMensaje = findViewById(R.id.et_mensaje);
        listview = findViewById(R.id.listview);

        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();

                mGoogleSignInClient.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(TychoActivity.this, TychoIn.class));
                        finish();
                    }
                });


            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (etMensaje.getText().toString().length() > 2 || !etMensaje.getText().toString().isEmpty()) {

                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference myRef = mDatabase.child(Constantes.USER_NODE)
                            .child(Objects.requireNonNull(mAuth.getUid()))
                            .child(Constantes.LISTA);

                    Map<String, Object> hasmapRealtime = new HashMap<>();
                    hasmapRealtime.put("mensaje", etMensaje.getText().toString());
                    myRef.push().setValue(hasmapRealtime);

                    etMensaje.setText("");

                } else {
                    Toast.makeText(TychoActivity.this, "Ingrese un mensaje.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        readRealtime();
    }

    private void readRealtime() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(Constantes.USER_NODE)
                .child(Objects.requireNonNull(mAuth.getUid()))
                .child(Constantes.LISTA);


        final List<String> arr = new ArrayList<>();
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {




                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d("FIREBASEAZAZEL", "onChildAdded: "+ ds.getValue());
                    arr.add(Objects.requireNonNull(ds.getValue()).toString());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(TychoActivity.this, android.R.layout.simple_expandable_list_item_1, arr);
                listview.setAdapter(adapter);




            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("FIREBASEAZAZEL", "onChildChanged: " + dataSnapshot);

                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        };
        query.addChildEventListener(childEventListener);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(TychoActivity.this, ""+ arr.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInformationFirebae() {

        if (mAuth != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child(Constantes.USER_NODE).child(Objects.requireNonNull(mAuth.getUid())).child(Constantes.ACTIVO).setValue(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());
        }


    }

    private void session() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
    }


}
