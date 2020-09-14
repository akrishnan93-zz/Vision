package com.google.mlkit.vision.demo.objectdetector;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.speech.tts.TextToSpeech;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.VisionProcessorBase;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.lang.Comparable;
import java.util.Set;

public class ObjectTester {
    public static void main (String[] args) {

        List<DetectedObject> detectedObject = new ArrayList<DetectedObject>();
        ObjectDetectorProcessor ObjectProcessor = new ObjectDetectorProcessor();
        
        Rect rect1 = new Rect(10, 20, 20, 50);
        List<DetectedObject.Label> listLabel1 = new ArrayList<DetectedObject.Label>();
        listLabel1.add(new DetectedObject.Label("test1", .9f, 0));
        detectedObject.add(new DetectedObject(rect1, 0, listLabel1));

        Rect rect2 = new Rect(90, 90, 105, 100);
        List<DetectedObject.Label> listLabel2 = new ArrayList<DetectedObject.Label>();
        listLabel2.add(new DetectedObject.Label("test2", .9f, 0));
        detectedObject.add(new DetectedObject(rect2, 0, listLabel2));

        ObjectProcessor.onSuccess(detectedObject, null);

    }
}