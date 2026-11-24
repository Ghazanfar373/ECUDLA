package com.org.serb.ecudla;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class DataDisplayControl extends HBox {

    private Label labelText;
    private Label labelValue;
    private Label unitLabel;

    public DataDisplayControl(String labelText, String initialValue) {
        this(labelText, initialValue, "");
    }

    public DataDisplayControl(String labelText, String initialValue, String unit) {
        super(8);

        this.labelText = new Label(labelText);
        this.labelValue = new Label(initialValue);
        this.unitLabel = new Label(unit);

        setupStyles();
        setupLayout();
    }

    private void setupStyles() {
        getStyleClass().add("data-row");
        labelText.getStyleClass().add("data-k");
        labelValue.getStyleClass().add("data-v");
        unitLabel.getStyleClass().add("data-k");
        setAlignment(Pos.CENTER_LEFT);
    }

    private void setupLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (unitLabel.getText().isEmpty()) {
            getChildren().addAll(labelText, spacer, labelValue);
        } else {
            getChildren().addAll(labelText, spacer, labelValue, unitLabel);
        }
    }

    public void setValue(String value) {
        labelValue.setText(value);
    }

    public void setValue(double value) {
        labelValue.setText(String.format("%.2f", value));
    }

    public void setValue(int value) {
        labelValue.setText(String.valueOf(value));
    }

    public void setLabelText(String text) {
        labelText.setText(text);
    }

    public void setUnit(String unit) {
        unitLabel.setText(unit);
    }

    public String getValue() {
        return labelValue.getText();
    }

    public void setValueColor(String color) {
        labelValue.setStyle("-fx-text-fill: " + color + ";");
    }

    public void setAlert(boolean alert) {
        getStyleClass().remove("alert");
        if (alert) {
            getStyleClass().add("alert");
            setValueColor("#CC0000");
        } else {
            labelValue.setStyle("");
        }
    }
}
