package com.example.instagram_clone_codepath;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 232;
    private EditText etDescription;
    private Button btnCaptureImage;
    private ImageView ivPostImage;
    private Button btnSubmit;
    private File photoFile;
    private String photoFileName = "photo.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDescription = findViewById(R.id.etDescription);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnSubmit = findViewById(R.id.btnSubmit);

        // queryPosts();
        btnSubmit.setOnClickListener(view -> {
            String description = etDescription.getText().toString();
            if(description.isEmpty()) {
                Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if(photoFile == null || ivPostImage.getDrawable() == null) {
                Toast.makeText(this, "Image is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            ParseUser user = ParseUser.getCurrentUser();
            savePost(description, user, photoFile);
        });

        btnCaptureImage.setOnClickListener(view -> {
            launchCamera();
        });
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(MainActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivPostImage.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout) {
            ParseUser.logOutInBackground(e -> {
                if(e != null) {
                    Log.e(TAG, "Issue with logout", e);
                    return;
                }
                Toast.makeText(MainActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
                goLoginActivity();
            });
        }
        return true;
    }

    private void goLoginActivity() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void savePost(String description, ParseUser user, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(user);
        post.saveInBackground(e -> {
            if(e != null) {
                Log.e(TAG, "error in saving post", e);
                Toast.makeText(this, "error posting!", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, "posted!", Toast.LENGTH_SHORT).show();
            etDescription.setText("");
            ivPostImage.setImageResource(0);
        });
    }

    public void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.findInBackground((posts, e) -> {
            if(e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
            for(Post post: posts) {
                Log.i(TAG, "desc: " + post.getDescription());
            }
        });
    }
}