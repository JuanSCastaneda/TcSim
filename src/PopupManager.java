

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.concurrent.CountDownLatch;

public class PopupManager {

    private static boolean initialized = false;
    private static boolean stopped = false;

    public static synchronized void initializeJavaFX() {
        if (stopped) {
            throw new IllegalStateException("JavaFX has already been stopped.");
        }

        if (!initialized) {
            Platform.startup(() -> {
            });
            Platform.setImplicitExit(false);
            initialized = true;
        }
    }

    public static synchronized void endJavaFX() {
        if (initialized && !stopped) {
            stopped = true;
            Platform.exit();
        }
    }

    public static void showPopUp(
            String title,
            String message,
            Alert.AlertType type) {

        initializeJavaFX();

        if (Platform.isFxApplicationThread()) {
            displayAlert(title, message, type);
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                displayAlert(title, message, type);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void displayAlert(
            String title,
            String message,
            Alert.AlertType type) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        showPopUp(title, message, Alert.AlertType.ERROR);
    }

    public static void showInfo(String title, String message) {
        showPopUp(title, message, Alert.AlertType.INFORMATION);
    }

    public static void showConfirmation(String title, String message) {
        showPopUp(title, message, Alert.AlertType.CONFIRMATION);
    }

    public static void showWarning(String title, String message) {
        showPopUp(title, message, Alert.AlertType.WARNING);
    }
}