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
import java.util.Set;

/**
 * A processor to run object detector.
 */
public class ObjectDetectorProcessor extends VisionProcessorBase<List<DetectedObject>> {

    private static final String TAG = "ObjectDetectorProcessor";

    private final ObjectDetector detector;

    private static Set<Integer> used = new HashSet<Integer>();
    TextToSpeech t1;

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
    protected void onSuccess(
            @NonNull List<DetectedObject> results, @NonNull GraphicOverlay graphicOverlay) {
        for (DetectedObject object : results) {
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
        if(!used.contains(object.getTrackingId()))
        {
            String toSpeak = "Warning";
            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            used.add(object.getTrackingId());
            String log = Arrays.toString(used.toArray());
            if (object.getLabels().size() != 0) {
                log += "   " + object.getLabels().get(0).getText();
                //Log.d("myTag",  "" + Arrays.toString((used.toArray())) + "   " + object.getLabels().get(0).getText());
            }
            Log.d("myTag", log);
        }
    }
}
