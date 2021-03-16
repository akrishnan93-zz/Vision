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
import android.graphics.Rect;
import android.media.Image;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.speech.tts.TextToSpeech;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.ArImage;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.LivePreviewActivity;
import com.google.mlkit.vision.demo.VisionProcessorBase;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
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

        double[][] line = whiskering(results);
        Rect lineRec = new Rect((int)line[0][0], (int)line[1][1], (int)line[1][0], (int)line[0][1]);
        List<DetectedObject.Label> linelabels = new ArrayList<>();
        DetectedObject lineObject = new DetectedObject(lineRec, 1, linelabels);
        graphicOverlay.add(new ObjectGraphic(graphicOverlay, lineObject));

        Log.d(TAG, "" + LivePreviewActivity.session);
        onDrawFrame();

        for (DetectedObject object : results) {
            setX(object);
            if (object.getLabels().size() != 0) {
                if (!object.getLabels().get(0).getText().equals("N/A")) { //Only overlay if we are identifying a proper bird
                    graphicOverlay.add(new ObjectGraphic(graphicOverlay, object));
                }
            }
        }

        Log.d(TAG, "line start: " + Arrays.toString(line[0]) + ", line end: " + Arrays.toString(line[1]));

    }

    public void onDrawFrame() {
        if (LivePreviewActivity.session == null) {
            return;
        }

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        LivePreviewActivity.session.setDisplayGeometry(0, 500, 1000);

        // Obtain the current frame from ARSession. When the configuration is set to
        // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
        // camera framerate.
        Frame frame = null;
        try {
            frame = LivePreviewActivity.session.update();
        } catch (CameraNotAvailableException e) {
            Log.d(TAG, "Frame is fucked");
            e.printStackTrace();
        }

        if (frame != null) {
            try (Image depthImage = frame.acquireDepthImage()) {
                int d = getMillimetersDepth(depthImage, 0, 0);
                StringBuilder dep = new StringBuilder();
                //w=1440 h=3120
                // for (int j = 0; j < 1920; j += 100) {
                //     for (int i = 0; i < 1080; i += 100) {
                //         int ij = getMillimetersDepth(depthImage, i, j);
                //         dep.append(String.format("%5d ", ij));
                //     }
                //     dep.append("\n");
                // }
                // System.out.println(dep.toString() + "\n\n");
//                System.out.printf("Depth is %d\n", d);
                Log.d(TAG, "Depth is " + d);
            } catch (NotYetAvailableException e) {
                // This normally means that depth data is not available yet. This is normal so we will not
                // spam the logcat with this.
                Log.d(TAG, "depth image fail");
            }
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
            //String log = Arrays.toString(used.toArray());
            String log = "sd";
            if (object.getLabels().size() != 0) {
                //log += "   " + object.getLabels().get(0).getText();
                //log += "   " + object.getLabels().get(0).getConfidence();
                //Log.d("myTag",  "" + Arrays.toString((used.toArray())) + "   " + object.getLabels().get(0).getText());
            }
            Log.d("myTag", log);
        }
    }

    public double[][] whiskering(List<DetectedObject> results) {
        int finalWidth = 720;
        int finalHeight = 1280;
        int screenMidWidth = finalWidth / 2;

        double threshConf = 0.1;
        ArrayList<double[]> output = new ArrayList<>();

        for (DetectedObject object : results) {
//            setX(object);
//            if (object.getLabels().size() != 0) {
//                if (!object.getLabels().get(0).getText().equals("N/A")) { //Only overlay if we are identifying a proper bird
//                    graphicOverlay.add(new ObjectGraphic(graphicOverlay, object));
//                }
//            }

            if (object.getLabels().size() == 0) {
                continue;
            }

            if (object.getLabels().get(0).getConfidence() >= threshConf) {
                int midX = object.getBoundingBox().centerX();
                int midY = object.getBoundingBox().centerY();

                double red_dist = midX - screenMidWidth; // how far away horizontally the object is from the mid point
                double blue_dist = Math.sqrt((Math.pow(midX - screenMidWidth, 2) + Math.pow(midY - finalHeight, 2))); // Euc distance from bottom mid of the screen to the object

                double yx_angle = Math.asin(red_dist / blue_dist) * (180 / Math.PI);
                double height = object.getBoundingBox().height();

                double distance = ((165 * 615) / height) / 30.48; // Distance from camera based on triangle similarity. Divide by 30.48 to get feet

                if (distance < 10) {
                    double[] current = {midX - screenMidWidth, midY -  height, distance, yx_angle};
                    output.add(current);
                }
            }

        }

        double sumXArr = 0;
        double sumYArr = 0;

        for (double[] current : output) {
            sumXArr += current[0] / current[2];
            sumYArr += current[1] / current[2];
        }

        double avgX = 0;
        double avgY = 0;

        if (output.size() > 0) {
            avgX = -1 * sumXArr / output.size();
            avgY =  -1 * sumYArr / output.size();
        }

        double[] lineStart = {avgX + screenMidWidth, avgY};
        double[] lineEnd = {screenMidWidth, finalHeight};

        double[][] line = {lineStart, lineEnd};

        return line;
    }

    public int getMillimetersDepth(Image depthImage, int x, int y) {
        Image.Plane plane = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            plane = depthImage.getPlanes()[0];
            int byteIndex = x * plane.getPixelStride() + y * plane.getRowStride();
            ByteBuffer buffer = plane.getBuffer().order(ByteOrder.nativeOrder());
            short depthSample = buffer.getShort(byteIndex);
            return depthSample;
        }
        return 0;
    }
}
