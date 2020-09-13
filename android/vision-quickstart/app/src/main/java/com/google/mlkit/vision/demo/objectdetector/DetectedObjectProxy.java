//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.google.mlkit.vision.demo.objectdetector;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.android.gms.common.internal.Objects;
import com.google.mlkit.vision.objects.DetectedObject;

import java.util.List;
import java.util.PriorityQueue;

public class DetectedObjectProxy implements Comparable<DetectedObjectProxy>{
    private final Rect zza;
    @Nullable
    private final Integer zzb;
    private final List<DetectedObject.Label> zzc;

    @KeepForSdk
    public DetectedObjectProxy(@NonNull Rect var1, @Nullable Integer var2, @NonNull List<DetectedObject.Label> var3) {
        this.zza = var1;
        this.zzb = var2;
        this.zzc = var3;
    }

    @NonNull
    public Rect getBoundingBox() {
        return this.zza;
    }

    @Nullable
    public Integer getTrackingId() {
        return this.zzb;
    }

    @NonNull
    public List<DetectedObject.Label> getLabels() {
        return this.zzc;
    }

    public boolean equals(Object var1) {
        if (var1 == this) {
            return true;
        } else if (!(var1 instanceof DetectedObject)) {
            return false;
        } else {
            DetectedObject var2 = (DetectedObject)var1;
            return Objects.equal(this.zza, var2.getBoundingBox()) && Objects.equal(this.zzb, var2.getTrackingId()) && Objects.equal(this.zzc, var2.getLabels());
        }
    }

    public int hashCode() {
        return Objects.hashCode(new Object[]{this.zza, this.zzb, this.zzc});
    }

    private static double distanceFromMiddle(double centerLB_mid, double centerTB_mid, double objLR_mid, double objTB_mid) {
        double dX = Math.pow(objLR_mid - centerLB_mid, 2);
        double dY = Math.pow(objTB_mid - centerTB_mid, 2);
        double dist = Math.sqrt(dX + dY);
        return dist;
    }

    public int compareTo(DetectedObjectProxy other) {
        double c1LR_mid = (this.getBoundingBox().left + this.getBoundingBox().right) / 2.0;
        double c1TB_mid = (this.getBoundingBox().top + this.getBoundingBox().bottom) / 2.0;    
        
        double c2LR_mid = (other.getBoundingBox().left + other.getBoundingBox().right) / 2.0;
        double c2TB_mid = (other.getBoundingBox().top + other.getBoundingBox().bottom) / 2.0;

        double centerLR_mid = 100;//imageView.getX() + imageView.getWidth()  / 2;
        double centerTB_mid = 100;//

        double c1Dist = distanceFromMiddle(centerLR_mid, centerTB_mid, c1LR_mid, c1TB_mid);
        double c2Dist = distanceFromMiddle(centerLR_mid, centerTB_mid, c2LR_mid, c2TB_mid);
        return (int) (c1Dist - c2Dist);
    }

    public static class Label {
        @NonNull
        private final String zza;
        private final float zzb;
        private final int zzc;

        @KeepForSdk
        public Label(@NonNull String var1, float var2, int var3) {
            this.zza = var1;
            this.zzb = var2;
            this.zzc = var3;
        }

        @NonNull
        public String getText() {
            return this.zza;
        }

        public float getConfidence() {
            return this.zzb;
        }

        public int getIndex() {
            return this.zzc;
        }

        public boolean equals(Object var1) {
            if (var1 == this) {
                return true;
            } else if (!(var1 instanceof DetectedObject.Label)) {
                return false;
            } else {
                DetectedObject.Label var2 = (DetectedObject.Label)var1;
                return Objects.equal(this.zza, var2.getText()) && Float.compare(this.zzb, var2.getConfidence()) == 0 && this.zzc == var2.getIndex();
            }
        }

        public int hashCode() {
            return Objects.hashCode(new Object[]{this.zza, this.zzb, this.zzc});
        }
    }
}
