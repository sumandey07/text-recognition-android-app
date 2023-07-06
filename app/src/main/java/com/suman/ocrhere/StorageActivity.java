package com.suman.ocrhere;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.InputStream;

public class StorageActivity extends AppCompatActivity {
    private Button chooseBtn, detectBtn, copyBtn;
    private String printText1 = "";
    private ImageView image;
    private TextView result1;
    private Bitmap imageBitmap1;

    private static final int REQUEST_STORAGE_PERSMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private ClipboardManager clipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        result1 = findViewById(R.id.text6);
        image = findViewById(R.id.image2);
        copyBtn = findViewById(R.id.copy);
        chooseBtn = findViewById(R.id.choose);
        detectBtn = findViewById(R.id.detect);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        copyBtn.setOnClickListener(v -> {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Text", result1.getText().toString()));
            Toast.makeText(StorageActivity.this, "Text Copied", Toast.LENGTH_SHORT).show();
        });

        chooseBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StorageActivity.this, new String[]{READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERSMISSION);
            } else {
                selectImage();
            }
        });
        detectBtn.setOnClickListener(v -> {
            if (imageBitmap1 != null) {
                detectText();
            } else {
                Toast.makeText(StorageActivity.this, "Please Choose an Image From Gallery", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        try {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQUEST_CODE_SELECT_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to open gallery", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERSMISSION && grantResults.length > 0) {
            boolean storagePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (storagePermission) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        imageBitmap1 = BitmapFactory.decodeStream(inputStream);
                        image.setImageBitmap(imageBitmap1);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void detectText() {
        InputImage image1 = InputImage.fromBitmap(imageBitmap1, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> resultImg = recognizer.process(image1).addOnSuccessListener(text -> {
            StringBuilder resultText = new StringBuilder();
            for (Text.TextBlock block : text.getTextBlocks()) {
                String blockText1 = block.getText();
                printText1 = printText1 + blockText1;
                Point[] blockCornerPoint = block.getCornerPoints();
                Rect blockFrame = block.getBoundingBox();
                for (Text.Line line : block.getLines()) {
                    String lineText = line.getText();
                    Point[] lineCornerPoint = line.getCornerPoints();
                    Rect linRect = line.getBoundingBox();
                    for (Text.Element element : line.getElements()) {
                        String elementText = element.getText();
                        resultText.append(elementText);
                    }
                }
                result1.setText(printText1);
            }
            if (printText1.length() > 0) {
                printText1 = "";
            }
        }).addOnFailureListener(e -> Toast.makeText(StorageActivity.this, "Failed to detect text from image" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}