/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo.objectdetector;

import android.content.Context;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.lang.Comparable;
import java.util.Set;

/**
 * A processor to run object detector.
 */
public class ObjectDetectorProcessor extends VisionProcessorBase<List<DetectedObject>> {

    private static final String TAG = "ObjectDetectorProcessor";

    private final ObjectDetector detector;

    private static Set<Integer> used = new HashSet<Integer>();
    TextToSpeech t1;

    private static PriorityQueue<DetectedObjectProxy> objectPriority = new PriorityQueue<>();
 
    public ObjectDetectorProcessor(Context context, ObjectDetectorOptionsBase options) {
        super(context);
        detector = ObjectDetection.getClient(options);

        t1 = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
    }

    @Override
    public void stop() {
        super.stop();
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close object detector!", e);
        }
    }

    @Override
    protected Task<List<DetectedObject>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    @Override
    protected void onSuccess(@NonNull List<DetectedObject> results, @NonNull GraphicOverlay graphicOverlay) {

       for (DetectedObject object : results) {
           DetectedObjectProxy objectProxy = new DetectedObjectProxy(object.getBoundingBox(), object.getTrackingId(), object.getLabels());
           objectPriority.add(objectProxy);
        }

       while (!objectPriority.isEmpty()) {
           DetectedObjectProxy objectProxy = objectPriority.poll();
           DetectedObject object = new DetectedObject(objectProxy.getBoundingBox(), objectProxy.getTrackingId(), objectProxy.getLabels());
           setX(object);
           graphicOverlay.add(new ObjectGraphic(graphicOverlay, object));
       }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Object detection failed!", e);
    }

    public void setX(DetectedObject object)
    {
        String log = "";
        if(!used.contains(object.getTrackingId()))
        {
            String toSpeak = "" + object.getTrackingId();
            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            used.add(object.getTrackingId());
            if (object.getLabels().size() != 0) {
                int averageHeight = (object.getBoundingBox().top + object.getBoundingBox().bottom) / 2;
                int averageWidth = (object.getBoundingBox().left + object.getBoundingBox().right) / 2;
                log = "   " + object.getTrackingId() + " " + averageHeight + " " + averageWidth;
            }
            Log.d("myTag", log);
        }
    }
}