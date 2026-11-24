package com.org.serb.ecudla;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;

public class ImageButton extends Button {
    public ImageButton(Image originalImage, String name, double h, double w) {
        setText(name);
        setTextAlignment(TextAlignment.LEFT);
        ImageView image = new ImageView(originalImage);
        image.setFitHeight(h);
        image.setFitWidth(w);
        image.setPreserveRatio(true);
        setGraphic(image);
        getStyleClass().add("toolbar-button");
    }
}
