package sample.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import sample.app.EdgeType;
import sample.app.GrainGrid;
import sample.app.NeighboursType;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;

public class MainController {

    public static Boolean stop = false;

    @FXML
    private ComboBox neighbourhoodType, edgeType;

    @FXML
    private TextField canvasXTF, canvasYTF, grainSizeTF, grainAmountTF, radiusTF, mcIterationsTF;

    @FXML
    private Canvas canvas;

    @FXML
    private Button continueButton;

    private GraphicsContext gc;
    private GrainGrid grainGrid;

    @FXML
    private void initialize() {
        continueButton.setDisable(true);
        gc = canvas.getGraphicsContext2D();
        fillCanvasStartPosition(canvas.getWidth(), canvas.getHeight());
        for (NeighboursType type : NeighboursType.values()) {
            neighbourhoodType.getItems().add(type.getNeighbourName());
        }
        neighbourhoodType.setValue(neighbourhoodType.getItems().get(0));

        for (EdgeType type : EdgeType.values()) {
            edgeType.getItems().add(type.getName());
        }
        edgeType.setValue(edgeType.getItems().get(0));

        canvasXTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[0-9]*$")) {
                canvasXTF.textProperty().setValue(oldValue);
                return;
            }
            if (newValue.equals("")) {
                return;
            }
            if (Double.parseDouble(newValue) > 1500 || Double.parseDouble(newValue) < 0) {
                canvasXTF.textProperty().setValue(oldValue);
                showMessageDialog("Warning, canvas size is incorrect (0-1500), retrieve last data.");
                return;
            }
            canvas.setWidth(Double.parseDouble(newValue));
            fillCanvasStartPosition(Double.parseDouble(newValue), canvas.getHeight());
        });

        canvasYTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[0-9]*$")) {
                canvasYTF.textProperty().setValue(oldValue);
                return;
            }
            if (newValue.equals("")) {
                return;
            }
            if (Double.parseDouble(newValue) > 1500 || Double.parseDouble(newValue) < 0) {
                canvasYTF.textProperty().setValue(oldValue);
                showMessageDialog("Warning, canvas size is incorrect (0-1500), retrieve last data.");
                return;
            }
            canvas.setHeight(Double.parseDouble(newValue));
            fillCanvasStartPosition(canvas.getWidth(), Double.parseDouble(newValue));
        });
    }

    private void fillCanvasStartPosition(double width, double height) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
    }

    private void showMessageDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    @FXML
    private void handleStart() throws InterruptedException {
        stop = false;
        fillCanvasStartPosition(canvas.getWidth(), canvas.getHeight());
        grainGrid = new GrainGrid(canvas.getWidth(), canvas.getHeight(), canvas, Integer.valueOf(grainSizeTF.getText()), Integer.valueOf(grainAmountTF.getText()),
                neighbourhoodType.getSelectionModel().getSelectedItem().toString(), edgeType.getSelectionModel().getSelectedItem().toString(), Integer.valueOf(radiusTF
                .getText()));
        grainGrid.simulateGrowth();
    }

    @FXML
    private void stop() {
        stop = true;
        continueButton.setDisable(false);
    }

    @FXML
    private void runMonteCarlo() throws InterruptedException {
        grainGrid.setMonteCarloIterations(Integer.valueOf(mcIterationsTF.getText()));
        grainGrid.runMonteCarlo();
    }

    @FXML
    private void saveCanvas() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("PNG Files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName("grain_growth");
        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (Exception ex) {
                showMessageDialog("Can't save this file");
            }
        }
    }

    @FXML
    private void handleContinueSimulation() throws InterruptedException {
        stop = false;
        grainGrid.simulateGrowth();
    }

}
