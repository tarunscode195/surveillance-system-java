// ============================================================
//  MotionLogger.java  –  Logs Motion Events to a Text File
//
//  Every time motion is detected, this class writes a timestamped
//  line to a log file inside the "logs/" folder.
//  Example log line:
//  [2024-06-15 14:32:07] MOTION DETECTED – Image saved: captured_images/2024-06-15_14-32-07.jpg
// ============================================================

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MotionLogger {

    // -------------------------------------------------------
    // LOG_FOLDER: Directory where log files are stored.
    // LOG_FILE:   Name of the text log file.
    // -------------------------------------------------------
    private static final String LOG_FOLDER = "logs";
    private static final String LOG_FILE   = LOG_FOLDER + File.separator + "motion_log.txt";

    // Formatter for human-readable timestamps in log entries
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // PrintWriter is used to write text to the log file
    private PrintWriter writer;

    // -------------------------------------------------------
    // Constructor – creates the log folder and opens the file.
    // 'true' in FileWriter = APPEND mode (don't overwrite old logs)
    // -------------------------------------------------------
    public MotionLogger() {
        try {
            // Create the logs/ directory if it doesn't already exist
            File folder = new File(LOG_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs(); // mkdirs creates parent dirs too
                System.out.println("[MotionLogger] Created log folder: " + LOG_FOLDER);
            }

            // Open the log file in APPEND mode (true = append)
            // AutoFlush = true means every write() instantly saves to disk
            writer = new PrintWriter(new FileWriter(LOG_FILE, true), true);
            System.out.println("[MotionLogger] Log file ready: " + LOG_FILE);

        } catch (IOException e) {
            System.err.println("[MotionLogger] ERROR: Could not open log file: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Logs a motion detection event with timestamp.
    // @param savedImagePath  Path of the captured image (can be null)
    // -------------------------------------------------------
    public void logMotion(String savedImagePath) {
        if (writer == null) return; // safety check

        // Get the current date and time
        String timestamp = LocalDateTime.now().format(LOG_TIMESTAMP_FORMAT);

        // Build the log message
        String message;
        if (savedImagePath != null && !savedImagePath.isEmpty()) {
            message = "[" + timestamp + "] MOTION DETECTED – Image saved: " + savedImagePath;
        } else {
            message = "[" + timestamp + "] MOTION DETECTED – (no image saved)";
        }

        // Write the message to the log file
        writer.println(message);
        System.out.println("[MotionLogger] " + message);
    }

    // -------------------------------------------------------
    // Logs a general informational message (e.g., "Camera started").
    // -------------------------------------------------------
    public void logInfo(String message) {
        if (writer == null) return;

        String timestamp = LocalDateTime.now().format(LOG_TIMESTAMP_FORMAT);
        String logLine = "[" + timestamp + "] INFO: " + message;

        writer.println(logLine);
        System.out.println("[MotionLogger] " + logLine);
    }

    // -------------------------------------------------------
    // Closes the log file properly.
    // Call this when the application exits.
    // -------------------------------------------------------
    public void close() {
        if (writer != null) {
            logInfo("Surveillance system stopped.");
            writer.flush();
            writer.close();
            System.out.println("[MotionLogger] Log file closed.");
        }
    }
}
