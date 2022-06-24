package pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.profiles;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pt.ulisboa.tecnico.cmov.conversational_ist.BuildConfig;
import pt.ulisboa.tecnico.cmov.conversational_ist.R;
import pt.ulisboa.tecnico.cmov.conversational_ist.firebase.FirebaseHandler;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.User;
import pt.ulisboa.tecnico.cmov.conversational_ist.satic.StaticData;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.RegisterActivity;

public class MyProfileActivity extends AppCompatActivity {

    private int PERMISSION_CAMERA = 123;
    private int PERMISSION_STORAGE = 124;
    private int REQUEST_TAKE_PHOTO = 125;
    private String TAG = "MyProfile";

    private SwitchCompat switchBtn;
    private ImageView modeIcon;
    private TextView modeTextContent;

    private LinearLayout policyBtn;

    private DatabaseReference db;
    private StorageReference dbStorageRef;
    private ImageView imageView;
    private FloatingActionButton uploadBtn;
    private FloatingActionButton cameraBtn;
    private ProgressBar progressBar;
    private Uri imageUri;

    private CircularImageView profileImage;
    private TextView userName;
    private TextView userName2;
    private EditText bio;
    private FloatingActionButton bioUploadBtn;

    private String userId;
    private SharedPreferences sharedPref;
    private SharedPreferences sharedPrefMode;

    private boolean isDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyLightDarkMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        initLightDarkMode();

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        initUser();
        initProfile();
        uploadBio();

        //To take a picture
        initCamera();

        //To Upload a picture
        uploadPhoto();

        initPolicy();
    }

    private void initUser() {
        SharedPreferences sh = getApplicationContext().getSharedPreferences("MyPrefs",MODE_PRIVATE);
        userId = sh.getString("saved_username","");
        Log.d("UserId: ", userId);
    }

    private void initProfile() {
        userName = findViewById(R.id.username);
        userName2 = findViewById(R.id.tv_username);
        profileImage = findViewById(R.id.image_profile);
        bio = findViewById(R.id.tv_bio);

        FirebaseHandler.getCurrentProfileInfo(userId, userName, userName2, profileImage, bio);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void uploadBio() {
        bioUploadBtn = findViewById(R.id.btn_bio_upload);
        bioUploadBtn.setOnClickListener(view ->{
            String bioText = getBio();
            if (bioText == null) {
                Toast.makeText(MyProfileActivity.this, "Bio cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseHandler.uploadUserBio(userId, bioText);
            }
        });
    }

    private String getBio() {
        String bioText = bio.getText().toString();
        if (bioText.trim().equals("")){
            return null;
        }
        else {
            return bioText;
        }
    }

    private void initCamera() {
        imageView = findViewById(R.id.image_profile);
        cameraBtn = findViewById(R.id.fab_camera);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCameraAndStorage();
            }
        });
    }

    private void verifyLightDarkMode() {
        sharedPrefMode = getSharedPreferences("mode", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPrefMode.getBoolean("mode_status", false);
        System.out.println("isDarkMode: " + isDarkMode);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void initLightDarkMode() {
        switchBtn = findViewById(R.id.switch_btn);
        modeIcon = findViewById(R.id.mode_icon);
        modeTextContent = findViewById(R.id.mode_text);

        sharedPrefMode = getSharedPreferences("mode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefMode.edit();

        isDarkMode = sharedPrefMode.getBoolean("mode_status", false); // False for light mode

        if (isDarkMode) {
            modeTextContent.setText("Light Mode");
            modeIcon.setImageResource(R.drawable.ic_baseline_wb_sunny_24);
        }
        else {
            modeTextContent.setText("Dark Mode");
            modeIcon.setImageResource(R.drawable.ic_baseline_dark_mode_24);
        }

        switchBtn.setChecked(isDarkMode);

        System.out.println("isDarkMode: " + isDarkMode);

        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    modeTextContent.setText("Light Mode");
                    modeIcon.setImageResource(R.drawable.ic_baseline_wb_sunny_24);

                    editor.putBoolean("mode_status", true);
                    editor.apply();
                    switchBtn.setChecked(true);

                    System.out.println("Current mode: Dark Mode");
                    System.out.println("Change to: Light Mode");
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    modeTextContent.setText("Dark Mode");
                    modeIcon.setImageResource(R.drawable.ic_baseline_dark_mode_24);

                    editor.putBoolean("mode_status", false);
                    editor.apply();
                    switchBtn.setChecked(false);

                    System.out.println("Current mode: Light Mode");
                    System.out.println("Change to: Dark Mode");
                }
            }
        });
    }

    private void initPolicy() {
        policyBtn = findViewById(R.id.ll_policy);

        policyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/AndreProenza/ConversationalIST");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void checkPermissionCameraAndStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA);
        } else if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_STORAGE);
        }
        else {
            dispatchTakenPictureIntent();
        }
    }

    private void dispatchTakenPictureIntent() {
        Intent takenPictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takenPictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Continue only if the File was sucessfully created
            if (photoFile != null) {
                StaticData.uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                takenPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, StaticData.uri);
                startActivityForResult(takenPictureIntent, REQUEST_TAKE_PHOTO);
            }
            else {
                Log.d(TAG, "dispatchTakenPictureIntent: photoFile null");
            }
        }
        else {
            Log.d(TAG, "dispatchTakenPictureIntent: null");
        }
    }

    private File createImageFile() throws IOException {
        @SuppressWarnings("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }




    // UPLOAD PHOTO USAGE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // UPLOAD PHOTO USAGE
        if (requestCode == 2 && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
        // CAMERA USAGE
        if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
            if (StaticData.uri != null) {
                imageView.setImageURI(StaticData.uri);
            }
        }
    }

    private void uploadPhoto() {
        imageView = findViewById(R.id.image_profile);
        uploadBtn = findViewById(R.id.btn_upload);
        progressBar = findViewById(R.id.profile_progress_bar);

        progressBar.setVisibility(View.INVISIBLE);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent , 2);
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    uploadToFirebase(imageUri);
                } else{
                    Toast.makeText(MyProfileActivity.this, "Please Select Image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadToFirebase(Uri uri){

        db = FirebaseDatabase.getInstance().getReference("users");
        dbStorageRef = FirebaseStorage.getInstance().getReference();

        final StorageReference fileRef = dbStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        User user = new User();
                        db.child(userId).child("photo").setValue(uri.toString());

                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(MyProfileActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        imageView.setImageResource(R.drawable.conversationalist_icon);
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MyProfileActivity.this, "Uploading Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }
}