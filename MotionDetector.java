// ============================================================
//  MotionDetector.java  –  Core Motion Detection Logic
//
//  HOW MOTION DETECTION WORKS (simple explanation):
//  ─────────────────────────────────────────────────
//  Think of it like a "spot the difference" game between two photos.
//  If many pixels are different between two consecutive camera frames,
//  something has MOVED in the scene → Motion Detected!
//
//  Algorithm Steps:
//  1. Convert frame to Grayscale (simpler to compare, no color bias)
//  2. Apply Gaussian Blur (removes tiny noise / grain)
//  3. Compute Absolute Difference between current & previous frame
//  4. Apply Threshold (pixels brighter than cutoff = changed pixels)
//  5. Count the "white" (changed) pixels
//  6. If count > sensitivity threshold → MOTION DETECTED
// ============================================================

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class MotionDetector {

    // -------------------------------------------------------
    // previousFrame stores the LAST processed grayscale frame.
    // We compare every new frame against this stored frame.
    // -------------------------------------------------------
    private Mat previousFrame = null;

    // -------------------------------------------------------
    // SENSITIVITY THRESHOLD:
    // The minimum number of "changed" pixels needed to trigger motion.
    // Lower value = detects even tiny movements (may give false alarms).
    // Higher value = only triggers for large movements.
    // -------------------------------------------------------
    private int sensitivityThreshold = 5000;

    // -------------------------------------------------------
    // PIXEL THRESHOLD:
    // During binarization (step 4 above), pixels with a difference
    // value > this number are marked as "changed" (white = 255).
    // Range: 0–255. 25 is a good default balance.
    // -------------------------------------------------------
    private double pixelThreshold = 25.0;

    // Constructor with default settings
    public MotionDetector() { }

    // Constructor with custom sensitivity
    public MotionDetector(int sensitivityThreshold, double pixelThreshold) {
        this.sensitivityThreshold = sensitivityThreshold;
        this.pixelThreshold = pixelThreshold;
    }

    // -------------------------------------------------------
    // Main method: Detects if there is motion between
    // the given frame and the previously stored frame.
    //
    // @param currentFrame  Latest frame from the webcam (BGR color Mat)
    // @return true if motion is detected, false otherwise
    // -------------------------------------------------------
    public boolean detect(Mat currentFrame) {

        // Safety check – if frame is null or empty, skip detection
        if (currentFrame == null || currentFrame.empty()) {
            return false;
        }

        // ── Step 1: Convert to Grayscale ──────────────────────────
        // We don't need color information for motion detection.
        // Grayscale has only 1 channel (instead of 3 for BGR),
        // making comparisons faster and simpler.
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(currentFrame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        // ── Step 2: Apply Gaussian Blur ───────────────────────────
        // Blur smooths out tiny pixel-level noise (camera sensor noise,
        // minor lighting flicker). Without blur, even dust on the lens
        // might trigger false "motion" alerts.
        // Kernel size (21, 21) = how large the blur area is (must be odd numbers).
        Mat blurredFrame = new Mat();
        Imgproc.GaussianBlur(grayFrame, blurredFrame, new Size(21, 21), 0);

        // ── First Frame Handling ───────────────────────────────────
        // On the very first call, there is no "previous frame" yet.
        // We just store the current frame and return false (no motion).
        if (previousFrame == null) {
            previousFrame = blurredFrame.clone(); // clone = deep copy
            grayFrame.release(); // free memory
            return false;
        }

        // ── Step 3: Compute Absolute Difference ───────────────────
        // absdiff() computes pixel-by-pixel absolute difference.
        // Where frames are similar → dark pixels (small values).
        // Where frames differ (something moved) → bright pixels.
        Mat diffFrame = new Mat();
        Core.absdiff(previousFrame, blurredFrame, diffFrame);

        // ── Step 4: Apply Threshold (Binarization) ────────────────
        // Pixels with difference > pixelThreshold become 255 (white).
        // Pixels with difference <= pixelThreshold become 0 (black).
        // Result: black background, white "blobs" where motion occurred.
        Mat thresholdFrame = new Mat();
        Imgproc.threshold(diffFrame, thresholdFrame, pixelThreshold, 255, Imgproc.THRESH_BINARY);

        // Optional: Dilate to fill small holes in motion blobs
        // This makes detection more robust for partial movements.
        Mat dilatedFrame = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.dilate(thresholdFrame, dilatedFrame, kernel);

        // ── Step 5: Count Changed Pixels ──────────────────────────
        // countNonZero() counts all white (255) pixels = changed pixels.
        int changedPixels = Core.countNonZero(dilatedFrame);

        // ── Step 6: Update Previous Frame ─────────────────────────
        // Replace previous frame with current frame for NEXT comparison.
        previousFrame.release(); // free old frame memory
        previousFrame = blurredFrame.clone();

        // ── Clean up all temporary Mat objects (free RAM) ─────────
        grayFrame.release();
        diffFrame.release();
        thresholdFrame.release();
        dilatedFrame.release();
        kernel.release();

        // ── Step 7: Decide – Is it Motion? ────────────────────────
        // If changed pixels exceed our threshold → motion detected!
        boolean motionDetected = changedPixels > sensitivityThreshold;

        if (motionDetected) {
            System.out.println("[MotionDetector] Motion detected! Changed pixels: " + changedPixels);
        }

        return motionDetected;
    }

    // -------------------------------------------------------
    // Resets the detector – clears the stored previous frame.
    // Call this when restarting the camera.
    // -------------------------------------------------------
    public void reset() {
        if (previousFrame != null) {
            previousFrame.release();
            previousFrame = null;
        }
        System.out.println("[MotionDetector] Detector reset.");
    }

    // Getter/Setter for sensitivity (can be connected to a slider in UI)
    public int getSensitivityThreshold() { return sensitivityThreshold; }
    public void setSensitivityThreshold(int value) { this.sensitivityThreshold = value; }

    public double getPixelThreshold() { return pixelThreshold; }
    public void setPixelThreshold(double value) { this.pixelThreshold = value; }
}
