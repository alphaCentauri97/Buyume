package com.example.buyume;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1;

    private ImageView imageView;
    private EditText editText;
    private Button selectImageBtn;
    private Button addTextBtn;
    private Button shareImageBtn;

    private Uri selectedImageUri;
    private Bitmap selectedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        addTextBtn = findViewById(R.id.addTextBtn);
        shareImageBtn = findViewById(R.id.shareImageBtn);

        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        addTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTextToImage();
            }
        });

        shareImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Getting drawable value from image
                if (selectedBitmap != null) {
                    Bitmap editedBitmap = addTextToBitmap(selectedBitmap, editText.getText().toString());
                    shareImage(editedBitmap);
                } else {
                    Toast.makeText(MainActivity.this, "Select an image and add text first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imageView.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addTextToImage() {
        if (selectedBitmap != null) {
            String text = editText.getText().toString();
            Bitmap editedBitmap = addTextToBitmap(selectedBitmap, text);
            imageView.setImageBitmap(editedBitmap);
        } else {
            Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap addTextToBitmap(Bitmap bitmap, String text) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(50);

        // You can adjust the position of the text here
        canvas.drawText(text, 100, 100, paint);

        return mutableBitmap;
    }

    private void shareImage(Bitmap bitmap) {
        Uri uri = getImageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);

        // putting uri of image to be shared
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, "Sharing Image with Text");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");

        // setting type to image
        intent.setType("image/png");

        // calling startActivity() to share
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    // Retrieving the URL to share
    private Uri getImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(this, "com.example.buyume.provider", file);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return uri;
    }
}
