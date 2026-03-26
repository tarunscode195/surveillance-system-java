// ============================================================
//  Main.java  –  Entry point of the Smart Surveillance System
//  This is the FIRST class that runs when you start the app.
// ============================================================

import org.opencv.core.Core; // OpenCV library loader

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        // -------------------------------------------------------
        // Step 1: Load the OpenCV native library (.dll / .so / .dylib)
        // Without this line, ALL OpenCV methods will crash.
        // -------------------------------------------------------
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // -------------------------------------------------------
        // Step 2: Run the GUI on the Event Dispatch Thread (EDT).
        // Swing is NOT thread-safe, so we always use SwingUtilities
        // to create/update the UI. This is the correct Java way.
        // -------------------------------------------------------
        SwingUtilities.invokeLater(() -> {

            // Step 3: Show Login Dialog first
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true); // blocks until dialog is closed

            // Step 4: If the user logged in successfully, open the main window
            if (login.isLoginSuccessful()) {
                MainUI mainUI = new MainUI();
                mainUI.setVisible(true);
            } else {
                // Wrong credentials – exit the application
                JOptionPane.showMessageDialog(null,
                        "Login failed. Exiting application.",
                        "Access Denied",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        });
    }
}
