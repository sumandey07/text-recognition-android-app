package com.suman.ocrhere;

import static android.Manifest.permission.CAMERA;

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
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
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

public class CameraActivity extends AppCompatActivity {
    private Button snapBtn, detectBtn, copyBtn;
    private String printText = "";
    private ImageView image;
    private TextView result;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap;

    private ClipboardManager clipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        result = findViewById(R.id.text5);
        copyBtn = findViewById(R.id.clipboard);
        image = findViewById(R.id.image1);
        snapBtn = findViewById(R.id.snap);
        detectBtn = findViewById(R.id.detect);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        copyBtn.setOnClickListener(v -> {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Text", result.getText().toString()));
            Toast.makeText(CameraActivity.this, "Text Copied", Toast.LENGTH_SHORT).show();
        });

        snapBtn.setOnClickListener(v -> {
            if (checkPermissions()) {
                captureImage();
            } else {
                requestPermission();
            }
        });
        detectBtn.setOnClickListener(v -> {
            if (imageBitmap != null) {
                detectText();
            } else {
                Toast.makeText(CameraActivity.this, "Please Capture an Image First", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }

    private void captureImage() {
        try {
            Intent takePicture = new Intent();
            takePicture.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraPermission) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                captureImage();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, 350, 350, true);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            image.setImageBitmap(rotatedBitmap);
        }
    }

    private void detectText() {
        InputImage image1 = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> resultImg = recognizer.process(image1).addOnSuccessListener(text -> {
            StringBuilder resultText = new StringBuilder();
            for (Text.TextBlock block : text.getTextBlocks()) {
                String blockText = block.getText();
                printText = printText + blockText;
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
                result.setText(printText);
            }
            if (printText.length() > 0) {
                printText = "";
            }
        }).addOnFailureListener(e -> Toast.makeText(CameraActivity.this, "Failed to detect text from image" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}