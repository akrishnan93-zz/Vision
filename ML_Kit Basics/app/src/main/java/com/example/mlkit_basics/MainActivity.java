package com.example.mlkit_basics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ObjectDetector objectDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()  // Optional
                        .build();
        objectDetector = ObjectDetection.getClient(options);

        AssetManager assetManager = this.getAssets();
        InputStream istr = null;

        try {
            istr = assetManager.open("example.jpeg");
        } catch (IOException e) {
            System.out.println("shits null0: " + e.toString());
        }

        if (istr != null) {
            Bitmap bitmap = BitmapFactory.decodeStream(istr);

            try {
                istr.close();
            } catch (IOException e) {
                System.out.println("shits null1");
            }

            InputImage image = InputImage.fromBitmap(bitmap, 0);

            objectDetector.process(image)
                    .addOnSuccessListener(
                            detectedObjects -> {
                                System.out.println("tits and sexy");
                            })
                    .addOnFailureListener(
                            e -> {
                                System.out.println("fucked boys");
                            });
        } else {
            System.out.println("shits null2");
        }

    }
}