package com.example.newvisionapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.core.Frame;

import java.nio.ByteBuffer;
import java.sql.SQLOutput;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
    private Session session;
    private boolean installRequested;
    private ImageView surfaceView;
    private Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enable AR-related functionality on ARCore supported devices only.
        //maybeEnableArButton();
        installRequested = false;
        surfaceView = findViewById(R.id.surfaceview);
    }

    //    void maybeEnableArButton() {
//      ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
//      if (availability.isTransient()) {
//        // Continue to query availability at 5Hz while compatibility is checked in the background.
//        new Handler().postDelayed(new Runnable() {
//          @Override
//          public void run() {
//            maybeEnableArButton();
//          }
//        }, 200);
//      }
//      if (availability.isSupported()) {
//        mArButton.setVisibility(View.VISIBLE);
//        mArButton.setEnabled(true);
//      } else { // The device is unsupported or unknown.
//        mArButton.setVisibility(View.INVISIBLE);
//        mArButton.setEnabled(false);
//      }
//    }

//    private boolean isARCoreSupportedAndUpToDate() {
//      ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
//      switch (availability) {
//        case SUPPORTED_INSTALLED:
//          return true;
//
//        case SUPPORTED_APK_TOO_OLD:
//        case SUPPORTED_NOT_INSTALLED:
//          try {
//            // Request ARCore installation or update if needed.
//            ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(this, true);
//            switch (installStatus) {
//              case INSTALL_REQUESTED:
//                //Log.i(TAG, "ARCore installation requested.");
//                return false;
//              case INSTALLED:
//                return true;
//            }
//          } catch (UnavailableException e) {
//            //Log.e(TAG, "ARCore not installed", e);
//          }
//          return false;
//
//        case UNSUPPORTED_DEVICE_NOT_CAPABLE:
//          // This device is not supported for AR.
//          return false;
//
//        case UNKNOWN_CHECKING:
//          // ARCore is checking the availability with a remote query.
//          // This function should be called again after waiting 200 ms to determine the query result.
//        case UNKNOWN_ERROR:
//        case UNKNOWN_TIMED_OUT:
//          // There was an error checking for AR availability. This may be due to the device being offline.
//          // Handle the error appropriately.
//      }
//    }

//    public void createSession() {
//      // Create a new ARCore session.
//      session = new Session(this);
//
//      // Create a session config.
//      Config config = new Config(session);
//
//      // Do feature-specific operations here, such as enabling depth or turning on
//      // support for Augmented Faces.
//
//      // Configure the session.
//      session.configure(config);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("We are doing the onResume method");
        System.out.println("-----");
        System.out.print("Session's value ");
        System.out.print((session == null) + "\n");
        System.out.println("-----");
        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                System.out.println("At line 135");
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        System.out.println("Returning on line 137");
                        return;
                    case INSTALLED:
                        System.out.println("Breaking on line 143");
                        break;
                    default:
                        System.out.println("What is the value of this shit??");
                        System.out.println("Value of the getInstance stuff");
                        System.out.print(ArCoreApk.getInstance().requestInstall(this, !installRequested));
                        System.out.println("Value of Install Requested");
                        System.out.print(installRequested);
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    System.out.println("Asking for camera perms");
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                // Creates the ARCore session.
                session = new Session(/* context= */ this);

            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                System.out.println("The name of this error is " + e.getClass().getName());
                System.out.println("__________________________");
                e.printStackTrace();
                System.out.println("_________________________");
                exception = e;
            }
            System.out.println(message);
            if (message != null) {
                //messageSnackbarHelper.showError(this, message);
                //Log.e(TAG, "Exception creating session", exception);
                //System.out.println("The name of this error is " + e.getClass().getName());
                return;
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            System.out.println("Resumeing Session");
            session.resume();
        } catch (CameraNotAvailableException e) {
            System.out.println("Going to Resumeing Session Error");
            //messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            session = null;
            return;
        }
        try {
            System.out.println("Updating Session");
            Frame frame = session.update();
            Image realFrame = frame.acquireCameraImage();
            // ^ realFrame
            // Need help need to somehow get realFrame to go onto surfaceview so we can display it
            //imageView.setImageResource();

            // Make Image into bitmap
            ByteBuffer buffer = realFrame.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            System.out.println("Printing bytes" + Arrays.toString(bytes) + "\n");
            Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
            System.out.println("Printing bitmap " + bitmapImage + "\n");
            //canvas.drawBitmap(bitmapImage, /*Matrix */, null);
            System.out.println("Displaying Image");
            ImageView tv1;
            surfaceView.setImageBitmap(bitmapImage);

            realFrame.close();

        } catch (Exception e) {
            System.out.println("ERRORRRRRRRRRRRR\n");
            System.out.println(e);
        }

        //surfaceView.onResume();
        //displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            //displayRotationHelper.onPause();
            //surfaceView.onPause();
            session.pause();
        }
    }
}