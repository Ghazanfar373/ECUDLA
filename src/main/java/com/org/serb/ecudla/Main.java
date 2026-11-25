package com.org.serb.ecudla;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.TickMarkType;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.prefs.Preferences;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static final String THEME_PREF = "ui.theme";
    private static final String UDP_MASK = "___.___.___.___:_____";
    private static final int[][] UDP_SLOTS = {
            {0, 1, 2},
            {4, 5, 6},
            {8, 9, 10},
            {12, 13, 14},
            {16, 17, 18, 19, 20}
    };

    BorderPane borderPane;
    Button btnPlane;
    Slider slider;
    private AnimationTimer timer;
    private long lastTimerCall;
    Stage stageSetting, stageAbout;

    private Gauge gaugeRPM;
    private Gauge gaugeTPS;
    private Gauge gaugeTempCylinder1;
    private Gauge gaugeTempCylinder2;
    private Gauge gaugeTempAirIntake;
    private Label peakRpm;
    private Label peakTemp1;
    private Label peakTemp2;
    private Label peakFuel;
    private Label fuelUsed;
    private Tile fuelSpark;

    private Label fieldEngineStatus;
    private Label fieldFuelConsumption;
    private Label fieldBattVol;
    private Label fieldBaro;
    private Label fieldTime;
    private Label linkChip;
    private ComboBox<String> linkTypeBox;
    private ComboBox<String> portBox;
    private TextField udpField;

    private String dashboardCss;
    private DashboardTheme activeTheme = DashboardTheme.NIGHT;
    private final Preferences uiPrefs = Preferences.userNodeForPackage(Main.class);

    @Override
    public void init() throws Exception {
        URL css = getClass().getResource("/com/org/serb/ecudla/dashboard.css");
        dashboardCss = css == null ? null : css.toExternalForm();
        activeTheme = DashboardTheme.byId(uiPrefs.get(THEME_PREF, DashboardTheme.NIGHT.id));

        borderPane = new BorderPane();
        borderPane.getStyleClass().add(activeTheme.styleClass);

        Image imageFlag = fileImage("logo-black_200.png");
        btnPlane = toolbarButton("Plane");
        Button btnConnect = toolbarButton("Connect");
        Button btnLogdata = toolbarButton("Reset Max");
        Button btnSettings = toolbarButton("Settings");
        Button btnhelp = toolbarButton("About");

        btnConnect.setOnAction(event -> connectFromBar());
        btnPlane.setOnAction(event -> selectionBox());
        btnhelp.setOnAction(event -> showAbout());
        btnSettings.setOnAction(event -> openSettings());
        btnLogdata.setOnAction(event -> {
            System.out.println("\007");
            byte[] buffer = {(byte) 0x90, (byte) 0x9f, (byte) 0xfa, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x24, 0x46, 0x48, 0x46, 0x00, 0x00, 0x68, 0x00, (byte) 0xc4, (byte) 0xbb};
            byte[] beffer = {3, 9, 12, 13, 8};
            int fromAir = (buffer[35] << 8 & 0xff00) + (buffer[36] & 0xff);
            System.out.println(calcCRC(beffer) + "From Air " + fromAir);
            Params.getInstance().setMaxECT(1);
            Params.getInstance().setMaxFuelCons(1);
            Params.getInstance().setMaxIAT(1);
            Params.getInstance().setMaxPres(1);
            Params.getInstance().setMaxRPM(1);
            Params.getInstance().setMaxTPS(1);
        });

        linkChip = new Label("OFFLINE");
        linkChip.getStyleClass().addAll("status-chip", "offline");

        ComboBox<DashboardTheme> themeBox = new ComboBox<>();
        themeBox.getItems().addAll(DashboardTheme.ALL);
        themeBox.setValue(activeTheme);
        themeBox.setOnAction(event -> {
            DashboardTheme chosen = themeBox.getValue();
            if (chosen != null) {
                applyTheme(chosen);
            }
        });

        ImageView companylogoView = new ImageView(imageFlag);
        companylogoView.setFitWidth(28);
        companylogoView.setFitHeight(28);
        StackPane logoBadge = new StackPane(companylogoView);
        logoBadge.getStyleClass().add("logo-badge");

        Label brandTitle = new Label("SERB ECU");
        brandTitle.getStyleClass().add("brand-title");
        Label brandSub = new Label("DLA 180");
        brandSub.getStyleClass().add("brand-sub");
        VBox brandText = new VBox(0, brandTitle, brandSub);
        HBox brand = new HBox(10, logoBadge, brandText);
        brand.setAlignment(Pos.CENTER_LEFT);

        Label themeCaption = new Label("Theme");
        themeCaption.getStyleClass().add("theme-label");
        HBox themePicker = new HBox(6, themeCaption, themeBox);
        themePicker.setAlignment(Pos.CENTER_LEFT);

        linkTypeBox = new ComboBox<>();
        linkTypeBox.getItems().addAll("Serial", "UDP");
        linkTypeBox.setValue("Serial");
        portBox = new ComboBox<>();
        portBox.setPrefWidth(110);
        portBox.setOnShowing(event -> refreshPorts());
        udpField = new TextField();
        udpField.setPrefWidth(206);
        udpField.getStyleClass().addAll("link-field", "link-mask");
        udpField.setTextFormatter(new TextFormatter<>(this::maskUdpChange));
        udpField.textProperty().addListener((obs, old, value) -> markUdpField(value));
        udpField.setText("192.168.1.2:9100");
        markUdpField(udpField.getText());
        linkTypeBox.setOnAction(event -> showLinkFields());
        refreshPorts();
        showLinkFields();

        Label linkCaption = new Label("Link");
        linkCaption.getStyleClass().add("theme-label");
        HBox linkBar = new HBox(8, linkCaption, linkTypeBox, portBox, udpField, btnConnect);
        linkBar.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(8, btnPlane, btnLogdata, btnSettings, btnhelp);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(12, brand, linkBar, spacer, linkChip, themePicker, actions);
        header.getStyleClass().add("top-bar");
        header.setAlignment(Pos.CENTER_LEFT);

        Color accent = Color.rgb(2, 138, 204);
        fuelSpark = TileBuilder.create().skinType(Tile.SkinType.SPARK_LINE).prefSize(640, 150)
                .title("Fuel Consumption").unit("L/H").strokeWithGradient(true).tickLabelDecimals(2)
                .decimals(2).smoothing(true)
                .gradientStops(
                        new Stop(0, Color.web("#1CAF4D")),
                        new Stop(0.0075, Color.web("#1CAF4D")),
                        new Stop(0.00751, Color.web("#91CA40")),
                        new Stop(0.01166, Color.web("#91CA40")),
                        new Stop(0.01167, Color.web("#F8C610")),
                        new Stop(0.01666, Color.web("#F8C610")),
                        new Stop(0.01667, Color.web("#F29222")),
                        new Stop(0.025, Color.web("#F29222")),
                        new Stop(0.02501, Color.web("#EC1D24")),
                        new Stop(1.0, Color.web("#EC1D24")))
                .build();
        fuelSpark.setMinHeight(150);
        fuelSpark.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        gaugeRPM = GaugeBuilder.create()
                .borderPaint(Color.TRANSPARENT)
                .foregroundBaseColor(Color.WHITE)
                .prefSize(230, 230)
                .startAngle(290)
                .angleRange(220)
                .minValue(0)
                .maxValue(7000)
                .valueVisible(true)
                .minorTickMarksVisible(true)
                .majorTickMarkType(TickMarkType.BOX)
                .mediumTickMarkType(TickMarkType.BOX)
                .title("RPM")
                .unit("x100")
                .needleShape(Gauge.NeedleShape.ROUND)
                .needleSize(Gauge.NeedleSize.THICK)
                .needleColor(Color.rgb(234, 67, 38))
                .knobColor(Gauge.DARK_COLOR)
                .customTickLabelsEnabled(true)
                .customTickLabelFontSize(18)
                .customTickLabels("0", "10", "20", "30", "40", "50", "60", "70")
                .animated(true)
                .build();

        gaugeTPS = GaugeBuilder.create()
                .skinType(Gauge.SkinType.GAUGE)
                .title("Throttle")
                .unit("%")
                .minValue(0)
                .maxValue(100)
                .customTickLabelFontSize(16)
                .prefSize(210, 210)
                .foregroundBaseColor(Color.rgb(249, 249, 249))
                .knobColor(Color.DARKGRAY)
                .animated(true)
                .build();

        gaugeTempCylinder1 = digitalGauge("Temp 1", accent);
        gaugeTempCylinder2 = digitalGauge("Temp 2", accent);
        gaugeTempAirIntake = digitalGauge("Atmospheric", accent);

        HBox powerRow = new HBox(8, gaugeRPM, gaugeTPS);
        powerRow.setAlignment(Pos.CENTER);
        HBox tempRow = new HBox(8, gaugeTempCylinder1, gaugeTempCylinder2, gaugeTempAirIntake);
        tempRow.setAlignment(Pos.CENTER);

        fieldEngineStatus = readout();
        fieldFuelConsumption = readout();
        fieldBattVol = readout();
        fieldBaro = readout();
        fieldTime = readout();

        HBox statusRow = new HBox(8,
                metric("Engine Status", fieldEngineStatus),
                metric("Consumption", fieldFuelConsumption),
                metric("Battery Voltage", fieldBattVol),
                metric("Baro Pressure", fieldBaro),
                metric("Running Time", fieldTime));
        statusRow.setFillHeight(true);

        VBox instruments = card("Engine", powerRow, tempRow);
        VBox live = card("Live readings", statusRow);
        VBox fuelCard = card("Fuel flow", fuelSpark);
        VBox.setVgrow(fuelSpark, Priority.ALWAYS);
        VBox.setVgrow(fuelCard, Priority.ALWAYS);
        fuelCard.setMaxHeight(Double.MAX_VALUE);

        VBox mainCol = new VBox(12, instruments, live, fuelCard);
        mainCol.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(mainCol, Priority.ALWAYS);

        peakRpm = readout();
        peakTemp1 = readout();
        peakTemp2 = readout();
        peakFuel = readout();
        fuelUsed = readout();

        DataDisplayControl ctrlAdvancedAngle = new DataDisplayControl("Advanced Angle", "0.00");
        DataDisplayControl ctrlInjectorPulse = new DataDisplayControl("Injector Pulse", "0.00");

        Label throttleReadout = new Label("Thr: 0.0");
        throttleReadout.getStyleClass().add("throttle-readout");
        slider = new Slider(0, 100, 0);
        slider.setValue(0);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(25);
        slider.setMinorTickCount(4);
        slider.setMaxWidth(Double.MAX_VALUE);
        Button decreaseButton = new Button("−");
        decreaseButton.getStyleClass().add("step-button");
        decreaseButton.setOnAction(e -> slider.setValue(slider.getValue() - .5));
        Button increaseButton = new Button("+");
        increaseButton.getStyleClass().add("step-button");
        increaseButton.setOnAction(e -> slider.setValue(slider.getValue() + .5));
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            throttleReadout.setText(String.format("Thr:  %.1f", newVal.doubleValue()));
            ArdiunoPort.getInstance().updateThrottleBuffer(newVal.intValue());
        });
        HBox throttleSteps = new HBox(8, decreaseButton, throttleReadout, increaseButton);
        throttleSteps.setAlignment(Pos.CENTER);
        VBox throttleBox = new VBox(8, slider, throttleSteps);

        Button btnStartheat = commandButton("Start heat", "cmd-idle");
        Button btnPreStart = commandButton("Pre Start", "cmd-idle");
        Button btnStartUp = commandButton("Start up", "cmd-go");
        Button btnShutDownEng = commandButton("Flame out", "cmd-stop");

        btnStartUp.setOnAction(event -> {
            if (confirmEngine("Start engine", "Are you sure to start the engine?", "Start up", "cmd-go")) {
                ArdiunoPort.getInstance().startDLA();
            }
        });
        btnShutDownEng.setOnAction(event -> {
            if (confirmEngine("Flame out", "Are you sure to shutdown the engine?", "Flame out", "cmd-stop")) {
                ArdiunoPort.getInstance().shutDownDLA();
                ArdiunoPort.getInstance().shutDownDLA();
            }
        });

        GridPane commandGrid = new GridPane();
        commandGrid.setHgap(8);
        commandGrid.setVgap(8);
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        half.setHgrow(Priority.ALWAYS);
        ColumnConstraints half2 = new ColumnConstraints();
        half2.setPercentWidth(50);
        half2.setHgrow(Priority.ALWAYS);
        commandGrid.getColumnConstraints().addAll(half, half2);
        commandGrid.add(btnStartheat, 0, 0);
        commandGrid.add(btnPreStart, 1, 0);
        commandGrid.add(btnStartUp, 0, 1);
        commandGrid.add(btnShutDownEng, 1, 1);

        HBox peakRpmRow = peakRow("MAX. RPM", "rpm", peakRpm);
        HBox peakTemp1Row = peakRow("MAX. Temp1", "deg", peakTemp1);
        HBox peakTemp2Row = peakRow("MAX. Temp2", "deg", peakTemp2);
        HBox peakFuelRow = peakRow("MAX. FuelCons", "L/H", peakFuel);
        HBox fuelUsedRow = peakRow("ConsumedFuel", "L", fuelUsed);
        for (HBox row : new HBox[]{peakRpmRow, peakTemp1Row, peakTemp2Row, peakFuelRow, fuelUsedRow}) {
            fillVertical(row);
        }
        VBox peaks = card("Peaks", peakRpmRow, peakTemp1Row, peakTemp2Row, peakFuelRow, fuelUsedRow);

        fillVertical(ctrlAdvancedAngle);
        fillVertical(ctrlInjectorPulse);
        VBox injection = card("Injection", ctrlAdvancedAngle, ctrlInjectorPulse);

        throttleBox.setAlignment(Pos.CENTER);
        fillVertical(throttleBox);
        VBox throttleCard = card("Throttle", throttleBox);

        RowConstraints commandRow = new RowConstraints();
        commandRow.setVgrow(Priority.ALWAYS);
        commandRow.setFillHeight(true);
        RowConstraints commandRow2 = new RowConstraints();
        commandRow2.setVgrow(Priority.ALWAYS);
        commandRow2.setFillHeight(true);
        commandGrid.getRowConstraints().addAll(commandRow, commandRow2);
        commandGrid.setMaxHeight(Double.MAX_VALUE);
        for (Node command : commandGrid.getChildren()) {
            GridPane.setVgrow(command, Priority.ALWAYS);
            GridPane.setFillHeight(command, true);
            ((Region) command).setMaxHeight(Double.MAX_VALUE);
        }
        fillVertical(commandGrid);
        VBox commands = card("Engine commands", commandGrid);

        for (VBox panel : new VBox[]{peaks, injection, throttleCard, commands}) {
            fillVertical(panel);
        }
        VBox sideCol = new VBox(12, peaks, injection, throttleCard, commands);
        sideCol.setMinWidth(280);
        sideCol.setPrefWidth(320);
        sideCol.setMaxWidth(380);
        sideCol.setMaxHeight(Double.MAX_VALUE);

        HBox board = new HBox(12, mainCol, sideCol);
        board.getStyleClass().add("board");
        board.setFillHeight(true);
        board.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ScrollPane scroll = new ScrollPane(board);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("dashboard-scroll");

        borderPane.setTop(header);
        borderPane.setCenter(scroll);
        applyTheme(activeTheme);

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now > lastTimerCall + 10_000_000L) {
                    gaugeRPM.setValue(Params.getInstance().getRPM());
                    gaugeTPS.setValue(Params.getInstance().getTPS());
                    gaugeTempAirIntake.setValue(Params.getInstance().getIAT());
                    gaugeTempCylinder1.setValue(Params.getInstance().getTempClndr1());
                    gaugeTempCylinder2.setValue(Params.getInstance().getTempClndr2());

                    peakRpm.setText(formatReading(Params.getInstance().getMaxRPM()));
                    peakTemp1.setText(formatReading(Params.getInstance().getMaxTemp1()));
                    peakTemp2.setText(formatReading(Params.getInstance().getMaxTemp2()));
                    peakFuel.setText(formatReading(Params.getInstance().getMaxFuelCons()));
                    fuelUsed.setText(formatReading(Params.getInstance().getFuelConsumed()));

                    fuelSpark.setValue(Params.getInstance().getFuelperHour());
                    fieldFuelConsumption.setText(String.format("%.2f", Params.getInstance().getFuelConsumed()));
                    ctrlAdvancedAngle.setValue(String.valueOf(Params.getInstance().getAdvanceAngle()));
                    ctrlInjectorPulse.setValue(String.valueOf(Params.getInstance().getInjectorPulse()));

                    fieldBattVol.setText(String.valueOf(Params.getInstance().getVol1()));
                    fieldBaro.setText(String.valueOf(Params.getInstance().getBAROPressure()));
                    fieldTime.setText(Params.getInstance().getEngineRunTime());
                    fieldEngineStatus.setText(Params.getInstance().getEngineState());
                    lastTimerCall = now;
                }
            }
        };
    }

    private void showAbout() {
        try {
            if (stageAbout != null && stageAbout.isShowing()) {
                stageAbout.toFront();
                return;
            }
            ImageView imgview = new ImageView(fileImage("smalllogo.png"));
            imgview.setFitWidth(72);
            imgview.setPreserveRatio(true);
            StackPane badge = new StackPane(imgview);
            badge.getStyleClass().add("logo-badge");
            Label copy = new Label("ECU Control (DLA 180) V 3.8.9 R\nCopyright 2025, SERB and/or its affiliates.\nAll rights reserved.");
            copy.getStyleClass().add("about-copy");
            copy.setWrapText(true);
            copy.setAlignment(Pos.CENTER);
            VBox root = new VBox(14, badge, copy);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(22));
            root.getStyleClass().add(activeTheme.styleClass);
            Scene scene = new Scene(root, 340, 240);
            if (dashboardCss != null) {
                scene.getStylesheets().add(dashboardCss);
            }
            stageAbout = new Stage();
            stageAbout.setResizable(false);
            stageAbout.setTitle("About");
            stageAbout.getIcons().add(fileImage("baseline_help_black_24dp.png"));
            stageAbout.setScene(scene);
            stageAbout.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void openSettings() {
        try {
            if (stageSetting != null && stageSetting.isShowing()) {
                stageSetting.toFront();
                return;
            }
            URL fxml = getClass().getResource("/com/org/serb/ecudla/settings.fxml");
            if (fxml == null) {
                showAlert(Alert.AlertType.ERROR, "Settings", "Settings layout was not found.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            root.getStyleClass().add(activeTheme.styleClass);
            Scene scene = new Scene(root);
            if (dashboardCss != null) {
                scene.getStylesheets().add(dashboardCss);
            }
            stageSetting = new Stage();
            stageSetting.setTitle("Settings");
            stageSetting.setResizable(false);
            stageSetting.getIcons().add(fileImage("baseline_settings_black_24dp.png"));
            stageSetting.setScene(scene);
            stageSetting.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Settings", "Could not open settings.");
        }
    }

    private void applyTheme(DashboardTheme theme) {
        activeTheme = theme;
        borderPane.getStyleClass().removeAll("theme-night", "theme-day", "theme-amber");
        borderPane.getStyleClass().add(theme.styleClass);
        if (stageAbout != null && stageAbout.getScene() != null) {
            restyle(stageAbout.getScene().getRoot(), theme);
        }
        if (stageSetting != null && stageSetting.getScene() != null) {
            restyle(stageSetting.getScene().getRoot(), theme);
        }
        paintAnalog(gaugeRPM, theme);
        gaugeRPM.setNeedleColor(Color.web("#ff5a3c"));
        paintAnalog(gaugeTPS, theme);
        gaugeTPS.setNeedleColor(theme.accent);
        paintDigital(gaugeTempCylinder1, theme);
        paintDigital(gaugeTempCylinder2, theme);
        paintDigital(gaugeTempAirIntake, theme);
        gaugeTempCylinder1.setBarColor(Color.web("#ff5a3c"));
        gaugeTempCylinder2.setBarColor(Color.web("#ff8a3c"));
        gaugeTempAirIntake.setBarColor(theme.accent);
        fuelSpark.setBackgroundColor(theme.field);
        fuelSpark.setBorderColor(theme.line);
        fuelSpark.setTitleColor(theme.muted);
        fuelSpark.setUnitColor(theme.muted);
        fuelSpark.setValueColor(theme.accent);
        fuelSpark.setTextColor(theme.ink);
        uiPrefs.put(THEME_PREF, theme.id);
    }

    private void restyle(Node root, DashboardTheme theme) {
        root.getStyleClass().removeAll("theme-night", "theme-day", "theme-amber");
        root.getStyleClass().add(theme.styleClass);
    }

    private void paintAnalog(Gauge gauge, DashboardTheme theme) {
        gauge.setBackgroundPaint(theme.surface);
        gauge.setBorderPaint(Color.TRANSPARENT);
        gauge.setForegroundBaseColor(theme.ink);
        gauge.setTitleColor(theme.muted);
        gauge.setUnitColor(theme.muted);
        gauge.setValueColor(theme.accent);
        gauge.setTickLabelColor(theme.ink);
        gauge.setTickMarkColor(theme.muted);
        gauge.setMajorTickMarkColor(theme.ink);
        gauge.setMediumTickMarkColor(theme.muted);
        gauge.setMinorTickMarkColor(theme.muted);
        gauge.setKnobColor(theme.field);
    }

    private void paintDigital(Gauge gauge, DashboardTheme theme) {
        gauge.setBackgroundPaint(theme.field);
        gauge.setBorderPaint(Color.TRANSPARENT);
        gauge.setForegroundBaseColor(theme.accent);
        gauge.setTitleColor(theme.muted);
        gauge.setValueColor(theme.accent);
    }

    private Gauge digitalGauge(String title, Color bar) {
        return GaugeBuilder.create()
                .skinType(Gauge.SkinType.DIGITAL)
                .prefSize(180, 150)
                .title(title)
                .decimals(2)
                .barColor(bar)
                .foregroundBaseColor(bar)
                .needleBorderColor(Color.rgb(200, 0, 0))
                .build();
    }

    private void fillVertical(Region node) {
        VBox.setVgrow(node, Priority.ALWAYS);
        node.setMaxHeight(Double.MAX_VALUE);
    }

    private HBox peakRow(String name, String unit, Label value) {
        Label caption = new Label(name);
        caption.getStyleClass().add("data-k");
        Label unitLabel = new Label(unit);
        unitLabel.getStyleClass().add("data-k");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, caption, spacer, value, unitLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("data-row");
        return row;
    }

    private String formatReading(double value) {
        return String.format("%.2f", value);
    }

    private VBox card(String title, Node... body) {
        Label heading = new Label(title.toUpperCase());
        heading.getStyleClass().add("section-title");
        VBox box = new VBox(10);
        box.getStyleClass().add("card");
        box.getChildren().add(heading);
        box.getChildren().addAll(body);
        return box;
    }

    private VBox metric(String caption, Label value) {
        Label cap = new Label(caption.toUpperCase());
        cap.getStyleClass().add("metric-caption");
        VBox tile = new VBox(2, cap, value);
        tile.getStyleClass().add("metric-tile");
        HBox.setHgrow(tile, Priority.ALWAYS);
        tile.setMaxWidth(Double.MAX_VALUE);
        return tile;
    }

    private Label readout() {
        Label label = new Label("—");
        label.getStyleClass().add("metric-value");
        return label;
    }

    private Button toolbarButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("toolbar-button");
        return button;
    }

    private Button commandButton(String text, String tone) {
        Button button = new Button(text);
        button.getStyleClass().addAll("cmd", tone);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return button;
    }

    private void setLinkState(boolean online, String detail) {
        linkChip.setText(online ? "LIVE  " + detail : "OFFLINE");
        linkChip.getStyleClass().removeAll("online", "offline");
        linkChip.getStyleClass().add(online ? "online" : "offline");
    }

    private boolean confirmEngine(String title, String message, String confirmText, String tone) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        if (borderPane.getScene() != null) {
            dialog.initOwner(borderPane.getScene().getWindow());
        }
        DialogPane pane = dialog.getDialogPane();
        pane.setHeaderText(null);
        pane.setGraphic(null);
        pane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        pane.setContent(noticeBody(title, message));
        pane.getStyleClass().add("confirm-pane");
        pane.setPrefWidth(440);
        paintDialog(dialog);

        Button confirm = (Button) pane.lookupButton(ButtonType.OK);
        confirm.setText(confirmText);
        confirm.getStyleClass().addAll("cmd", tone);
        Button cancel = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancel.setText("Cancel");
        cancel.getStyleClass().add("toolbar-button");

        return dialog.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private VBox noticeBody(String title, String message) {
        Label heading = new Label(title);
        heading.getStyleClass().add("confirm-title");
        Label body = new Label(message);
        body.setWrapText(true);
        body.getStyleClass().add("confirm-body");
        VBox box = new VBox(8, heading, body);
        box.setPadding(new Insets(8, 4, 10, 4));
        box.setPrefWidth(400);
        return box;
    }

    private void paintDialog(Dialog<?> dialog) {
        if (dashboardCss == null) {
            return;
        }
        DialogPane pane = dialog.getDialogPane();
        if (!pane.getStylesheets().contains(dashboardCss)) {
            pane.getStylesheets().add(dashboardCss);
        }
        pane.getStyleClass().removeAll("theme-night", "theme-day", "theme-amber");
        pane.getStyleClass().add(activeTheme.styleClass);
    }

    private Image fileImage(String name) throws MalformedURLException {
        File file = new File(System.getProperty("user.dir"), "src/main/resources/" + name);
        return new Image(file.toURI().toURL().toString());
    }

    private TextFormatter.Change maskUdpChange(TextFormatter.Change change) {
        String current = change.getControlText();
        if (current == null || current.length() != UDP_MASK.length()) {
            current = UDP_MASK;
        }
        String inserted = change.getText() == null ? "" : change.getText();
        int start = change.getRangeStart();
        int end = change.getRangeEnd();
        String[] groups = readUdpGroups(current);
        int caret;
        if (inserted.isEmpty()) {
            caret = deleteUdpRange(groups, start, end);
        } else {
            if (end > start) {
                deleteUdpRange(groups, start, end);
            }
            caret = Math.max(0, Math.min(start, UDP_MASK.length()));
            for (int i = 0; i < inserted.length(); i++) {
                caret = typeUdpChar(groups, caret, inserted.charAt(i));
            }
        }
        String rendered = writeUdpMask(groups);
        int oldLength = change.getControlText() == null ? 0 : change.getControlText().length();
        change.setRange(0, oldLength);
        change.setText(rendered);
        int bounded = Math.max(0, Math.min(caret, rendered.length()));
        change.setCaretPosition(bounded);
        change.setAnchor(bounded);
        return change;
    }

    private int typeUdpChar(String[] groups, int caret, char ch) {
        int group = udpGroupForCaret(caret);
        if (ch == '.' || ch == ':') {
            if (group >= 4) {
                return udpCaret(4, groups[4].length());
            }
            if (groups[group].isEmpty()) {
                return udpCaret(group, 0);
            }
            if (ch == ':' && group < 3) {
                return caret;
            }
            return udpCaret(group + 1, 0);
        }
        if (ch < '0' || ch > '9') {
            return caret;
        }
        return typeUdpDigit(groups, group, ch, udpIndexInGroup(group, caret));
    }

    private int typeUdpDigit(String[] groups, int group, char ch, int index) {
        int maxLen = group < 4 ? 3 : 5;
        int maxVal = group < 4 ? 255 : 65535;
        String g = groups[group];
        if (index > g.length()) {
            index = g.length();
        }
        if (index >= maxLen) {
            if (group >= 4) {
                return udpCaret(4, g.length());
            }
            return typeUdpDigit(groups, group + 1, ch, 0);
        }
        String trial = g.substring(0, index) + ch + g.substring(index);
        int trialValue;
        try {
            trialValue = Integer.parseInt(trial);
        } catch (NumberFormatException ex) {
            return udpCaret(group, index);
        }
        if (trial.length() > maxLen || trialValue > maxVal) {
            if (group < 4 && index == g.length() && !g.isEmpty()) {
                return typeUdpDigit(groups, group + 1, ch, 0);
            }
            return udpCaret(group, index);
        }
        groups[group] = trial;
        int placed = index + 1;
        if (group < 4 && placed >= maxLen) {
            return UDP_SLOTS[group + 1][0];
        }
        return udpCaret(group, placed);
    }

    private int deleteUdpRange(String[] groups, int start, int end) {
        if (end <= start) {
            return Math.max(0, start);
        }
        int caret = start;
        boolean removed = false;
        for (int pos = end - 1; pos >= start; pos--) {
            int[] loc = locateUdpSlot(pos);
            if (loc == null || loc[1] >= groups[loc[0]].length()) {
                continue;
            }
            int g = loc[0];
            int index = loc[1];
            groups[g] = groups[g].substring(0, index) + groups[g].substring(index + 1);
            caret = UDP_SLOTS[g][index];
            removed = true;
            if (end - start == 1) {
                return caret;
            }
        }
        if (removed) {
            return caret;
        }
        for (int pos = start; pos >= 0; pos--) {
            int[] loc = locateUdpSlot(pos);
            if (loc != null && loc[1] < groups[loc[0]].length()) {
                int g = loc[0];
                int index = loc[1];
                groups[g] = groups[g].substring(0, index) + groups[g].substring(index + 1);
                return UDP_SLOTS[g][index];
            }
        }
        return 0;
    }

    private String[] readUdpGroups(String text) {
        String[] groups = {"", "", "", "", ""};
        if (text == null || text.length() != UDP_MASK.length()) {
            return groups;
        }
        for (int g = 0; g < UDP_SLOTS.length; g++) {
            StringBuilder digits = new StringBuilder();
            for (int slot : UDP_SLOTS[g]) {
                char ch = text.charAt(slot);
                if (ch < '0' || ch > '9') {
                    break;
                }
                digits.append(ch);
            }
            groups[g] = digits.toString();
        }
        return groups;
    }

    private String writeUdpMask(String[] groups) {
        char[] chars = UDP_MASK.toCharArray();
        for (int g = 0; g < UDP_SLOTS.length; g++) {
            String digits = groups[g] == null ? "" : groups[g];
            for (int i = 0; i < digits.length() && i < UDP_SLOTS[g].length; i++) {
                chars[UDP_SLOTS[g][i]] = digits.charAt(i);
            }
        }
        return new String(chars);
    }

    private int udpGroupForCaret(int caret) {
        if (caret >= UDP_MASK.length()) {
            return 4;
        }
        for (int g = 0; g < UDP_SLOTS.length; g++) {
            int first = UDP_SLOTS[g][0];
            int last = UDP_SLOTS[g][UDP_SLOTS[g].length - 1];
            if (caret >= first && caret <= last) {
                return g;
            }
            if (g > 0 && caret == first - 1) {
                return g;
            }
        }
        return 0;
    }

    private int udpIndexInGroup(int group, int caret) {
        int[] slots = UDP_SLOTS[group];
        for (int i = 0; i < slots.length; i++) {
            if (caret <= slots[i]) {
                return i;
            }
        }
        return slots.length;
    }

    private int udpCaret(int group, int index) {
        int[] slots = UDP_SLOTS[group];
        if (index >= slots.length) {
            return group < 4 ? UDP_SLOTS[group + 1][0] : UDP_MASK.length();
        }
        return slots[Math.max(0, index)];
    }

    private int[] locateUdpSlot(int pos) {
        for (int g = 0; g < UDP_SLOTS.length; g++) {
            for (int i = 0; i < UDP_SLOTS[g].length; i++) {
                if (UDP_SLOTS[g][i] == pos) {
                    return new int[]{g, i};
                }
            }
        }
        return null;
    }

    private boolean isCompleteUdpMask(String text) {
        String[] groups = readUdpGroups(text);
        for (int g = 0; g < groups.length; g++) {
            if (groups[g].isEmpty()) {
                return false;
            }
            int value = Integer.parseInt(groups[g]);
            int max = g < 4 ? 255 : 65535;
            if (value > max) {
                return false;
            }
        }
        return true;
    }

    private String udpAddressText(String masked) {
        if (!isCompleteUdpMask(masked)) {
            return "";
        }
        String[] groups = readUdpGroups(masked);
        return Integer.parseInt(groups[0]) + "."
                + Integer.parseInt(groups[1]) + "."
                + Integer.parseInt(groups[2]) + "."
                + Integer.parseInt(groups[3]) + ":"
                + Integer.parseInt(groups[4]);
    }

    private void markUdpField(String value) {
        boolean valid = isCompleteUdpMask(value);
        if (valid) {
            udpField.getStyleClass().remove("invalid");
        } else if (!udpField.getStyleClass().contains("invalid")) {
            udpField.getStyleClass().add("invalid");
        }
    }

    private void showLinkFields() {
        boolean udp = "UDP".equals(linkTypeBox.getValue());
        portBox.setVisible(!udp);
        portBox.setManaged(!udp);
        udpField.setVisible(udp);
        udpField.setManaged(udp);
        if (!udp) {
            refreshPorts();
        }
    }

    private void refreshPorts() {
        String previous = portBox.getValue();
        String[] ports = new SerialConnectionHandler().getComPorts();
        portBox.getItems().setAll(ports);
        if (ports.length == 0) {
            portBox.setValue(null);
            portBox.setPromptText("No ports");
            return;
        }
        portBox.setPromptText(null);
        if (previous != null && portBox.getItems().contains(previous)) {
            portBox.setValue(previous);
        } else {
            portBox.setValue(ports[0]);
        }
    }

    private void connectFromBar() {
        System.out.println("Connect Button clicked.");
        try {
            Params.getInstance().setMaxECT(1);
            Params.getInstance().setMaxFuelCons(1);
            Params.getInstance().setMaxIAT(1);
            Params.getInstance().setMaxPres(1);
            Params.getInstance().setMaxRPM(1);
            Params.getInstance().setMaxTPS(1);

            ConnectionHandler previous = ArdiunoPort.getInstance().getConnectionHandler();
            if (previous != null && previous.isConnected()) {
                previous.disconnect();
            }

            ConnectionHandler handler;
            String connectionIdentifier;
            if ("UDP".equals(linkTypeBox.getValue())) {
                handler = new UdpConnectionHandler();
                connectionIdentifier = udpAddressText(udpField.getText());
                if (connectionIdentifier.isEmpty()) {
                    setLinkState(false, "");
                    linkChip.setText("NEED IP:PORT");
                    markUdpField(udpField.getText());
                    System.out.println("UDP address must look like ___.___.___.___:_____.");
                    return;
                }
                System.out.println("UdpConnectionHandler set.");
            } else {
                refreshPorts();
                handler = new SerialConnectionHandler();
                connectionIdentifier = portBox.getValue();
                if (connectionIdentifier == null || connectionIdentifier.isBlank()) {
                    setLinkState(false, "");
                    linkChip.setText("NO PORTS");
                    System.out.println("No serial ports found.");
                    return;
                }
                System.out.println("SerialConnectionHandler set.");
            }

            ArdiunoPort.getInstance().setConnectionHandler(handler);
            System.out.println("Attempting to connect with identifier: " + connectionIdentifier);
            boolean status = ArdiunoPort.getInstance().connectPort(connectionIdentifier);
            if (!status) {
                setLinkState(false, "");
                linkChip.setText("FAILED");
                System.out.println("Connection failed for: " + connectionIdentifier);
            } else {
                setLinkState(true, connectionIdentifier);
                System.out.println("Connection successful for: " + connectionIdentifier);
                ArdiunoPort.getInstance().startFeedbackThreadDLA();
            }
        } catch (Exception e) {
            setLinkState(false, "");
            linkChip.setText("FAILED");
            System.err.println("Exception in connectFromBar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        if (borderPane.getScene() != null) {
            alert.initOwner(borderPane.getScene().getWindow());
        }
        DialogPane pane = alert.getDialogPane();
        pane.setContent(noticeBody(title, message));
        pane.getStyleClass().add("confirm-pane");
        pane.setPrefWidth(440);
        paintDialog(alert);
        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        if (ok != null) {
            ok.setText("OK");
            String tone = type == Alert.AlertType.ERROR || type == Alert.AlertType.WARNING ? "cmd-stop" : "cmd-go";
            ok.getStyleClass().addAll("cmd", tone);
        }
        alert.showAndWait();
    }

    void selectionBox() {
        try {
            String[] array = {"SMD", "SML"};
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("SML", array);
            paintDialog(choiceDialog);
            Image iconimage = fileImage("baseline_security_black_24dp.png");
            ((Stage) choiceDialog.getDialogPane().getScene().getWindow()).getIcons().add(iconimage);
            choiceDialog.showingProperty().addListener((ov, b, b1) -> {
                if (b1) {
                    Node comboBox = choiceDialog.getDialogPane().lookup(".combo-box");
                    if (comboBox != null) {
                        comboBox.requestFocus();
                    }
                }
            });

            choiceDialog.showAndWait();
            String planename = choiceDialog.getSelectedItem();
            System.out.println(choiceDialog.getSelectedItem());
            Params.getInstance().pref.put("plane", planename);
            btnPlane.setText(planename);
            Params.getInstance().setPlaneCaliFactor();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        timer.stop();
        System.out.println("Application stop() method called.");
        try {
            ArdiunoPort.setRunning(false);
            ConnectionHandler handler = ArdiunoPort.getInstance().getConnectionHandler();
            if (handler != null && handler.isConnected()) {
                System.out.println("Disconnecting active connection handler.");
                handler.disconnect();
            } else {
                System.out.println("No active connection to disconnect or handler is null.");
            }
        } catch (Exception e) {
            System.err.println("Error during application shutdown: " + e.getMessage());
            e.printStackTrace();
        }
        super.stop();
    }

    @Override
    public void start(Stage primaryStage) throws MalformedURLException {
        Scene scene = new Scene(borderPane);
        if (dashboardCss != null) {
            scene.getStylesheets().add(dashboardCss);
        }
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1024);
        primaryStage.setHeight(895);
        primaryStage.setTitle("SERB ECU 4.5.0 (DLA 180)");
        primaryStage.getIcons().add(fileImage("baseline_security_black_24dp.png"));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            if (stageSetting != null && stageSetting.isShowing()) {
                stageSetting.close();
            }
            if (stageAbout != null && stageAbout.isShowing()) {
                stageAbout.close();
            }
        });
        timer.start();
    }

    int calcCRC(byte[] buffer) {
        int crc = 0;
        for (int i = 0; i < 5; i++) {
            crc = crc ^ buffer[i];
        }
        return crc;
    }
}
