// ============================================================
//  CameraHandler.java  –  Manages Webcam via OpenCV
//
//  What this class does:
//  1. Opens / Closes the webcam using OpenCV VideoCapture
//  2. Grabs frames (images) from the webcam
//  3. Converts OpenCV Mat (matrix) → Java BufferedImage (for Swing)
//  4. Saves a frame as a JPEG file to disk
// ============================================================

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CameraHandler {

    // VideoCapture is the OpenCV class that controls the webcam
    private VideoCapture camera;

    // Tracks whether the camera is currently running
    private boolean isRunning = false;

    // -------------------------------------------------------
    // Constructor – initializes the VideoCapture object.
    // The actual camera is NOT opened here yet.
    // -------------------------------------------------------
    public CameraHandler() {
        camera = new VideoCapture();
    }

    // -------------------------------------------------------
    // Opens the webcam.
    // @param deviceIndex  0 = default/built-in webcam,
    //                     1 = external USB camera, etc.
    // @return true if camera opened successfully
    // -------------------------------------------------------
    public boolean startCamera(int deviceIndex) {
        // Try to open the camera at the given index
        camera.open(deviceIndex);

        // Set resolution (optional – comment out if issues occur)
        camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
        camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);

        // isOpened() returns true only if camera is accessible
        isRunning = camera.isOpened();
        return isRunning;
    }

    // -------------------------------------------------------
    // Closes the webcam and releases system resources.
    // ALWAYS call this when done — otherwise webcam stays locked!
    // -------------------------------------------------------
    public void stopCamera() {
        if (camera != null && camera.isOpened()) {
            camera.release(); // release hardware resource
        }
        isRunning = false;
    }

    // -------------------------------------------------------
    // Grabs a single frame (image) from the webcam.
    // @return Mat object (OpenCV image matrix), or empty Mat on failure
    // -------------------------------------------------------
    public Mat grabFrame() {
        Mat frame = new Mat(); // empty matrix to store the frame

        if (camera != null && camera.isOpened()) {
            // camera.read() fills 'frame' with the latest webcam image
            boolean success = camera.read(frame);
            if (!success) {
                System.out.println("[CameraHandler] Warning: Failed to grab frame.");
            }
        }
        return frame; // may be empty if camera is closed
    }

    // -------------------------------------------------------
    // Converts an OpenCV Mat (BGR format) → Java BufferedImage.
    //
    // Why is this needed?
    //   OpenCV uses its own image format (Mat).
    //   Java Swing can only display BufferedImage.
    //   So we encode Mat to JPEG bytes, then decode back to BufferedImage.
    //
    // @param mat  The OpenCV frame to convert
    // @return     BufferedImage ready for JLabel / JPanel display
    // -------------------------------------------------------
    public static BufferedImage matToBufferedImage(Mat mat) {
        if (mat == null || mat.empty()) {
            return null; // nothing to convert
        }

        // MatOfByte is a special Mat that holds raw byte data
        MatOfByte mob = new MatOfByte();

        // Encode the Mat as JPEG into the byte buffer
        Imgcodecs.imencode(".jpg", mat, mob);

        // Convert MatOfByte → regular byte array
        byte[] byteArray = mob.toArray();

        BufferedImage bufferedImage = null;
        try {
            // Use Java's ImageIO to decode the JPEG bytes into BufferedImage
            bufferedImage = ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            System.err.println("[CameraHandler] Error converting Mat to BufferedImage: " + e.getMessage());
        }

        return bufferedImage;
    }

    // -------------------------------------------------------
    // Saves a Mat frame as a JPEG image file at the given path.
    // @param frame     The OpenCV frame to save
    // @param filePath  Full file path (e.g., "captured_images/2024-01-01_12-00-00.jpg")
    // @return true if saved successfully
    // -------------------------------------------------------
    public static boolean saveFrame(Mat frame, String filePath) {
        if (frame == null || frame.empty()) {
            System.out.println("[CameraHandler] Cannot save – frame is empty.");
            return false;
        }
        // imwrite() writes the Mat to disk as an image file
        boolean saved = Imgcodecs.imwrite(filePath, frame);
        if (saved) {
            System.out.println("[CameraHandler] Image saved: " + filePath);
        } else {
            System.err.println("[CameraHandler] Failed to save image: " + filePath);
        }
        return saved;
    }

    // -------------------------------------------------------
    // Checks if the camera is currently running
    // -------------------------------------------------------
    public boolean isRunning() {
        return isRunning;
    }
}
