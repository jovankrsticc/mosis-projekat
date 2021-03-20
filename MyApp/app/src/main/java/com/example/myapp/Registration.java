package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class Registration extends AppCompatActivity {

    private EditText name,lastName,number,userName,email,password;
    private Button regBtn;
    private TextView log;
    private FirebaseAuth fAuth;
    private FirebaseDatabase database= FirebaseDatabase.getInstance();
    private DatabaseReference ref;
    private ImageView img;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private Bitmap photo;
    private StorageReference storageRef= FirebaseStorage.getInstance().getReference();
    String userId;
    private boolean  slika;
    private FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        name=(EditText) findViewById(R.id.name);
        lastName=(EditText) findViewById(R.id.lastName);
        number=(EditText) findViewById(R.id.number);
        userName=(EditText) findViewById(R.id.userName);
        email=(EditText) findViewById(R.id.email);
        password=(EditText) findViewById(R.id.password);
        regBtn=(Button) findViewById(R.id.regBtn);
        log=(TextView)findViewById(R.id.log);
        fAuth=FirebaseAuth.getInstance();
        ref=database.getReference("Users");
        img=(ImageView)findViewById(R.id.imageView_registracija);
        slika=false;


        if(fAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(getApplicationContext(),Home.class));
            finish();
        }

       img.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
               {
                   requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
               }
               else
               {
                   Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                   startActivityForResult(cameraIntent, CAMERA_REQUEST);
               }
           }
       });

       regBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String emailS = email.getText().toString();
                String passwordS = password.getText().toString();
                String nameS=name.getText().toString();
                String lastNameS=lastName.getText().toString();
                String userNameS =userName.getText().toString();
                String numberS=number.getText().toString();

                if (TextUtils.isEmpty(emailS))
                {
                    email.setError("Email is required.");
                    email.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(passwordS))
                {
                    password.setError("Password is required.");
                    password.requestFocus();
                    return;
                }

                fAuth.createUserWithEmailAndPassword(emailS,passwordS).addOnCompleteListener(Registration.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(Registration.this, "User created.",Toast.LENGTH_SHORT).show();
                            String id=fAuth.getCurrentUser().getUid();
                            ref.child(id).setValue(new User(nameS,lastNameS,userNameS,numberS, emailS,passwordS));

                            if(slika)
                            {

                                userId = id;
                                submit();
                            }

                            startActivity(new Intent(getApplicationContext(),Image.class));
                            finish();
                        }
                        else
                        {
                            Toast.makeText(Registration.this, "Error."+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            photo = (Bitmap) data.getExtras().get("data");
            img.setImageBitmap(photo);
            slika=true;
            //submit();
        }
    }

    public void submit(){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] b = stream.toByteArray();
        //StorageReference storageReference =FirebaseStorage.getInstance().getReference().child("documentImages").child("noplateImg");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);
        storageReference.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                    }
                });
                Toast.makeText(Registration.this, "uploaded", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Registration.this,"failed",Toast.LENGTH_LONG).show();

            }
        });

    }
}