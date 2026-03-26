// ============================================================
//  MainUI.java  –  Main Surveillance Window (Swing GUI)
//
//  This is the largest class. It ties everything together:
//  - Builds the entire GUI (buttons, panels, labels)
//  - Runs a background timer that continuously:
//      → Grabs frames from the webcam (CameraHandler)
//      → Checks for motion (MotionDetector)
//      → Saves images when motion is detected
//      → Plays sound (AlertSound)
//      → Logs events (MotionLogger)
//  - Updates the video panel with each new frame
// ============================================================

import org.opencv.core.Mat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainUI extends JFrame {

    // ── Core system objects ──────────────────────────────────────
    private CameraHandler  cameraHandler;   // Manages the webcam
    private MotionDetector motionDetector;  // Detects motion
    private MotionLogger   motionLogger;    // Writes to log file
    private AlertSound     alertSound;      // Plays beep

    // ── Swing Components ─────────────────────────────────────────
    private JLabel   videoLabel;        // Displays the live video feed
    private JLabel   statusLabel;       // Shows "No Motion" / "Motion Detected!"
    private JLabel   captureCountLabel; // Shows how many images have been saved
    private JButton  startBtn;
    private JButton  stopBtn;
    private JButton  captureBtn;
    private JCheckBox soundToggle;
    private JSlider  sensitivitySlider;

    // ── State variables ──────────────────────────────────────────
    private Timer  frameTimer;          // Swing Timer – fires every N ms to grab a frame
    private int    captureCount = 0;    // Number of images captured in this session
    private Mat    lastFrame    = null; // Last grabbed frame (used for manual capture)

    // Folder where captured images are saved
    private static final String CAPTURE_FOLDER = "captured_images";

    // Timestamp format for saved image filenames
    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // Timer interval = how often we grab a frame (milliseconds)
    // 33 ms ≈ 30 frames per second
    private static final int FRAME_INTERVAL_MS = 33;

    // Controls how often motion can trigger auto-capture (cooldown in ms)
    private long lastCapturetime = 0;
    private static final long CAPTURE_COOLDOWN_MS = 2000; // 2 seconds minimum between auto captures

    // -------------------------------------------------------
    // Constructor – sets up the entire GUI
    // -------------------------------------------------------
    public MainUI() {
        // Initialize system objects
        cameraHandler  = new CameraHandler();
        motionDetector = new MotionDetector();
        motionLogger   = new MotionLogger();
        alertSound     = new AlertSound();

        // Create capture folder if it doesn't exist
        new File(CAPTURE_FOLDER).mkdirs();

        // Build the UI
        buildUI();
        setupWindowClosing();

        motionLogger.logInfo("Application started. Login successful.");
    }

    // -------------------------------------------------------
    // Builds all Swing components and lays them out.
    // -------------------------------------------------------
    private void buildUI() {
        setTitle("🎥 Smart Surveillance System");
        setSize(900, 620);
        setMinimumSize(new Dimension(800, 550));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // handled in setupWindowClosing
        setLocationRelativeTo(null); // center on screen
        getContentPane().setBackground(new Color(20, 20, 35));

        // ── Main Layout ──────────────────────────────────────────
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // ── Left Panel: Video Feed ───────────────────────────────
        JPanel videoPanel = buildVideoPanel();
        add(videoPanel, BorderLayout.CENTER);

        // ── Right Panel: Controls ────────────────────────────────
        JPanel controlPanel = buildControlPanel();
        add(controlPanel, BorderLayout.EAST);

        // ── Bottom Panel: Status Bar ─────────────────────────────
        JPanel statusPanel = buildStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        // ── Top Panel: Title Bar ─────────────────────────────────
        JPanel titlePanel = buildTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
    }

    // ── Video Feed Panel (left side) ─────────────────────────────
    private JPanel buildVideoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 15, 30));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 255), 2),
                " Live Feed ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(0, 150, 255)));

        // videoLabel is where we will draw each camera frame
        videoLabel = new JLabel("Camera not started", SwingConstants.CENTER);
        videoLabel.setForeground(new Color(100, 100, 120));
        videoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        videoLabel.setPreferredSize(new Dimension(640, 480));
        videoLabel.setBackground(Color.BLACK);
        videoLabel.setOpaque(true);

        panel.add(videoLabel, BorderLayout.CENTER);
        return panel;
    }

    // ── Controls Panel (right side) ──────────────────────────────
    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 42));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(200, 0));

        // ── Camera Control Section ───────────────────────────────
        panel.add(createSectionLabel("📷 Camera Control"));
        panel.add(Box.createVerticalStrut(8));

        startBtn = createStyledButton("▶  Start Camera", new Color(0, 180, 80));
        stopBtn  = createStyledButton("⏹  Stop Camera",  new Color(200, 50, 50));
        stopBtn.setEnabled(false); // disabled until camera starts

        startBtn.addActionListener(e -> startCamera());
        stopBtn.addActionListener(e -> stopCamera());

        panel.add(startBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(stopBtn);
        panel.add(Box.createVerticalStrut(16));

        // ── Manual Capture Section ───────────────────────────────
        panel.add(createSectionLabel("📸 Manual Capture"));
        panel.add(Box.createVerticalStrut(8));

        captureBtn = createStyledButton("📷  Capture Image", new Color(0, 120, 200));
        captureBtn.setEnabled(false);
        captureBtn.addActionListener(e -> manualCapture());

        panel.add(captureBtn);
        panel.add(Box.createVerticalStrut(8));

        // Capture count display
        captureCountLabel = new JLabel("Images Saved: 0");
        captureCountLabel.setForeground(new Color(180, 180, 200));
        captureCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        captureCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(captureCountLabel);
        panel.add(Box.createVerticalStrut(16));

        // ── Sensitivity Section ──────────────────────────────────
        panel.add(createSectionLabel("🎚  Sensitivity"));
        panel.add(Box.createVerticalStrut(8));

        // Slider to control motion detection sensitivity
        // Lower value = more sensitive (triggers easier)
        sensitivitySlider = new JSlider(JSlider.HORIZONTAL, 1000, 20000, 5000);
        sensitivitySlider.setBackground(new Color(25, 25, 42));
        sensitivitySlider.setForeground(Color.WHITE);
        sensitivitySlider.addChangeListener(e -> {
            // Invert: slider right = less sensitive (higher threshold)
            motionDetector.setSensitivityThreshold(sensitivitySlider.getValue());
        });

        JLabel sliderLow  = new JLabel("High");
        JLabel sliderHigh = new JLabel("Low ");
        sliderLow.setForeground(new Color(150, 150, 170));
        sliderHigh.setForeground(new Color(150, 150, 170));
        sliderLow.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sliderHigh.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        JPanel sliderRow = new JPanel(new BorderLayout(3, 0));
        sliderRow.setBackground(new Color(25, 25, 42));
        sliderRow.add(sliderLow, BorderLayout.WEST);
        sliderRow.add(sensitivitySlider, BorderLayout.CENTER);
        sliderRow.add(sliderHigh, BorderLayout.EAST);

        JLabel sensitivityNote = new JLabel("← Sensitivity →");
        sensitivityNote.setForeground(new Color(120, 120, 140));
        sensitivityNote.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        sensitivityNote.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(sliderRow);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sensitivityNote);
        panel.add(Box.createVerticalStrut(16));

        // ── Sound Toggle ─────────────────────────────────────────
        panel.add(createSectionLabel("🔔 Alert Sound"));
        panel.add(Box.createVerticalStrut(8));

        soundToggle = new JCheckBox("Enable Alert Sound");
        soundToggle.setSelected(true); // sound ON by default
        soundToggle.setBackground(new Color(25, 25, 42));
        soundToggle.setForeground(new Color(180, 180, 200));
        soundToggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        soundToggle.addActionListener(e -> alertSound.setSoundEnabled(soundToggle.isSelected()));
        panel.add(soundToggle);

        panel.add(Box.createVerticalGlue()); // pushes everything up

        return panel;
    }

    // ── Title Panel (top) ─────────────────────────────────────────
    private JPanel buildTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 15, 30));
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel title = new JLabel("🔒 Smart Surveillance System  |  Powered by OpenCV");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(0, 180, 255));
        panel.add(title, BorderLayout.WEST);

        JLabel userLabel = new JLabel("👤 Admin  ");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(new Color(120, 180, 120));
        panel.add(userLabel, BorderLayout.EAST);

        return panel;
    }

    // ── Status Bar (bottom) ───────────────────────────────────────
    private JPanel buildStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(15, 15, 30));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 80)));

        statusLabel = new JLabel("⚪ Status: System Ready – Start the camera to begin.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(180, 180, 200));

        panel.add(statusLabel);
        return panel;
    }

    // ── Helper: Creates a bold section label ──────────────────────
    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(100, 180, 255));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    // ── Helper: Creates a styled rounded button ───────────────────
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    // ============================================================
    //  CAMERA CONTROL METHODS
    // ============================================================

    // ── Start Camera ─────────────────────────────────────────────
    private void startCamera() {
        setStatus("⏳ Starting camera...", Color.YELLOW);

        // Try to open camera index 0 (default webcam)
        boolean success = cameraHandler.startCamera(0);

        if (!success) {
            setStatus("❌ Camera not found! Check connection.", Color.RED);
            JOptionPane.showMessageDialog(this,
                    "Could not open webcam!\n\nPlease check:\n" +
                    "1. Webcam is connected\n" +
                    "2. No other app is using the camera\n" +
                    "3. Camera index is correct (default = 0)",
                    "Camera Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Reset motion detector (clears previous frame cache)
        motionDetector.reset();

        // Update UI buttons
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        captureBtn.setEnabled(true);
        setStatus("🟢 Camera running – Monitoring for motion...", new Color(0, 220, 100));

        motionLogger.logInfo("Camera started.");

        // ── Start the Frame Timer ─────────────────────────────────
        // This Swing Timer fires every FRAME_INTERVAL_MS milliseconds.
        // On each tick → grab frame → check motion → update display.
        frameTimer = new Timer(FRAME_INTERVAL_MS, e -> processFrame());
        frameTimer.start();
    }

    // ── Stop Camera ──────────────────────────────────────────────
    private void stopCamera() {
        if (frameTimer != null) {
            frameTimer.stop(); // stop the timer loop
        }
        cameraHandler.stopCamera(); // release webcam

        // Reset video display
        videoLabel.setIcon(null);
        videoLabel.setText("Camera stopped");
        videoLabel.setForeground(new Color(100, 100, 120));

        // Update UI
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        captureBtn.setEnabled(false);
        setStatus("⚪ Camera stopped.", new Color(180, 180, 200));

        motionLogger.logInfo("Camera stopped.");
        System.out.println("[MainUI] Camera stopped.");
    }

    // ============================================================
    //  CORE FRAME PROCESSING (called every ~33ms by the Timer)
    // ============================================================
    private void processFrame() {
        // 1. Grab a frame from the webcam
        Mat frame = cameraHandler.grabFrame();

        if (frame == null || frame.empty()) {
            return; // skip this tick if frame grab failed
        }

        lastFrame = frame; // store for manual capture

        // 2. Check for motion in the current frame
        boolean motionDetected = motionDetector.detect(frame);

        if (motionDetected) {
            // ── Motion Detected! ─────────────────────────────────
            setStatus("🔴 MOTION DETECTED!", Color.RED);

            // Apply cooldown: don't auto-capture faster than every 2 seconds
            long now = System.currentTimeMillis();
            if (now - lastCapturetime >= CAPTURE_COOLDOWN_MS) {
                lastCapturetime = now;

                // Auto-capture the image
                String savedPath = saveImage(frame);

                // Play alert sound in background
                alertSound.playAlert();

                // Log the motion event
                motionLogger.logMotion(savedPath);

                // Update capture count label
                captureCount++;
                captureCountLabel.setText("Images Saved: " + captureCount);
            }
        } else {
            // No motion – show calm status
            setStatus("🟢 No Motion Detected", new Color(0, 220, 100));
        }

        // 3. Convert frame to BufferedImage and display it in the label
        BufferedImage img = CameraHandler.matToBufferedImage(frame);
        if (img != null) {
            // Scale image to fit the video label size
            Image scaledImg = img.getScaledInstance(
                    videoLabel.getWidth(), videoLabel.getHeight(), Image.SCALE_FAST);
            videoLabel.setIcon(new ImageIcon(scaledImg));
            videoLabel.setText(""); // clear placeholder text
        }

        // Release the Mat to free native OpenCV memory
        frame.release();
    }

    // ============================================================
    //  IMAGE SAVING
    // ============================================================

    // ── Save frame to disk with timestamp filename ────────────────
    private String saveImage(Mat frame) {
        // Generate filename from current timestamp
        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);
        String filename  = timestamp + ".jpg";
        String fullPath  = CAPTURE_FOLDER + File.separator + filename;

        // Save using CameraHandler utility method
        boolean saved = CameraHandler.saveFrame(frame, fullPath);
        return saved ? fullPath : null;
    }

    // ── Manual Capture (user clicked the Capture button) ──────────
    private void manualCapture() {
        if (lastFrame == null || lastFrame.empty()) {
            JOptionPane.showMessageDialog(this,
                    "No frame available to capture.", "Capture Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String savedPath = saveImage(lastFrame);
        if (savedPath != null) {
            captureCount++;
            captureCountLabel.setText("Images Saved: " + captureCount);
            motionLogger.logInfo("Manual capture: " + savedPath);
            setStatus("📸 Image captured: " + savedPath, new Color(100, 200, 255));
        } else {
            setStatus("❌ Capture failed!", Color.RED);
        }
    }

    // ============================================================
    //  UI UTILITY METHODS
    // ============================================================

    // Updates the status bar label text and color
    private void setStatus(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(color);
        });
    }

    // ── Handle window close (X button) ───────────────────────────
    private void setupWindowClosing() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        MainUI.this,
                        "Are you sure you want to exit the surveillance system?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    // Clean shutdown sequence
                    if (frameTimer != null) frameTimer.stop();
                    cameraHandler.stopCamera();
                    motionLogger.close(); // flush and close log file
                    dispose();
                    System.exit(0);
                }
            }
        });
    }
}
