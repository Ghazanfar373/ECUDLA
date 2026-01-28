package com.org.serb.ecudla;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class DataDisplayControl extends HBox {

    private Label labelText;
    private Label labelValue;
    private Label unitLabel;

    // Constructor with label and initial value
    public DataDisplayControl(String labelText, String initialValue) {
        this(labelText, initialValue, "");
    }

    // Constructor with label, initial value, and unit
    public DataDisplayControl(String labelText, String initialValue, String unit) {
        super(5); // spacing between elements

        this.labelText = new Label(labelText);
        this.labelValue = new Label(initialValue);
        this.unitLabel = new Label(unit);

        setupStyles();
        setupLayout();
    }

    private void setupStyles() {
        // Label text style
        labelText.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-font-weight: normal;" +
                        "-fx-text-fill: #333333;"
        );

        // Value style
        labelValue.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #232a34;" +
                        "-fx-min-width: 40px;" +
                        "-fx-alignment: center-right;"
        );

        // Unit style
        unitLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: normal;" +
                        "-fx-text-fill: #666666;"
        );

        // Container style
        this.setStyle(
                "-fx-background-color: #9da796;" +
                        "-fx-background-radius: 1px;" +
                        "-fx-padding: 6px 6px;" +
                        "-fx-border-color: #DDDDDD;" +
                        "-fx-border-radius: 3px;" +
                        "-fx-border-width: 1px;"
        );

        this.setAlignment(Pos.CENTER_LEFT);
    }

    private void setupLayout() {
        // Add spacer for better layout
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (unitLabel.getText().isEmpty()) {
            this.getChildren().addAll(labelText, spacer, labelValue);
        } else {
            this.getChildren().addAll(labelText, spacer, labelValue, unitLabel);
        }
    }

    // Update the value
    public void setValue(String value) {
        labelValue.setText(value);
    }

    // Update the value with numeric formatting
    public void setValue(double value) {
        labelValue.setText(String.format("%.2f", value));
    }

    public void setValue(int value) {
        labelValue.setText(String.valueOf(value));
    }

    // Update the label text
    public void setLabelText(String text) {
        labelText.setText(text);
    }

    // Update the unit
    public void setUnit(String unit) {
        unitLabel.setText(unit);
    }

    // Get current value
    public String getValue() {
        return labelValue.getText();
    }

    // Set value color (e.g., for warnings)
    public void setValueColor(String color) {
        String currentStyle = labelValue.getStyle();
        labelValue.setStyle(currentStyle.replaceAll("-fx-text-fill: [^;]+;", "-fx-text-fill: " + color + ";"));
    }

    // Highlight for alerts
    public void setAlert(boolean alert) {
        if (alert) {
            this.setStyle(
                    "-fx-background-color: #FFE6E6;" +
                            "-fx-background-radius: 5px;" +
                            "-fx-padding: 8px 12px;" +
                            "-fx-border-color: #FF6666;" +
                            "-fx-border-radius: 5px;" +
                            "-fx-border-width: 2px;"
            );
            setValueColor("#CC0000");
        } else {
            setupStyles();
        }
    }
}