package com.org.serb.ecudla;
import com.jfoenix.controls.JFXButton;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.TickMarkType;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.colors.Bright;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Random;
public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	BorderPane borderPane;
	ToolBar toolbar;
	int fuelCounter = 0;

	double fuelConsumedValue = 0;
	Pane leftSpacer, rightSpacer, bottompane;
	VBox leftPane;
	VBox rightPane;
	HBox battryPane1;
	HBox battryPane2;
	HBox centerPane;
	ImageButton btnPlane;
	TextField fieldBattVol;
	Gauge levelGuageTank1, levelGuageTank2;
	Slider slider;
	private static final Random RND = new Random(100);
	private long lastTimerCall;
	private AnimationTimer timer;
	Stage stageSetting,stageAbout;
	Gauge tileStatus;
	FXMLLoader myLoader = null; Scene myScene = null; Stage prevStage = null;
	DecimalFormat decimalFormat = new DecimalFormat("#.###");
	@Override
	public void init() throws Exception {
		//  super.init();   Color.Monitor....readings are according to the law of modesity

		Color color = Color.rgb(2, 138, 204);
		leftSpacer = new Pane();
		HBox.setHgrow(leftSpacer,  Priority.SOMETIMES);
		rightSpacer = new Pane();
		HBox.setHgrow(rightSpacer, Priority.SOMETIMES);
		borderPane = new BorderPane();
		borderPane.setStyle("-fx-padding: 0;" + "-fx-border-style: solid inside;"
				+ "-fx-border-width: 1;" + "-fx-border-insets: 0;"
				+ "-fx-border-radius: 3;" + "-fx-border-color: gray;" + "-fx-background-color: #303030;");
		toolbar = new ToolBar();
		ImageView logoview=new ImageView();
		Image usbconnect = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\baseline_usb_black_24dp.png").toURI().toURL().toString());
		Image imageFlag = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\logo-black_200.png").toURI().toURL().toString());
		Image imghelp = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\baseline_help_black_24dp.png").toURI().toURL().toString());
		Image imgdevider = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\baseline_more_vert_black_24dp.png").toURI().toURL().toString());
		Image imgPause = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\plane-18.png").toURI().toURL().toString());
		Image imgLogData = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\baseline_save_alt_black_24dp.png").toURI().toURL().toString());
		Image imgsetting = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\baseline_settings_black_24dp.png").toURI().toURL().toString());
		ImageButton btnConnect = new ImageButton(usbconnect, "Connect", 20, 20);
		btnPlane = new ImageButton(imgPause, "Plane", 20, 20);
		ImageButton btnLogdata = new ImageButton(imgLogData, "Reset Max", 20, 20);
		ImageButton btnSettings = new ImageButton(imgsetting, "Settings", 20, 20);
		ImageButton btnhelp = new ImageButton(imghelp, "About", 20, 20);


		btnConnect.setOnAction(event -> {
			try {
				System.out.print("Connect Button");
				//Alert alert=new Alert(Alert.AlertType.CONFIRMATION,"Yes Going Okay");
				// alert.show();

				Params.getInstance().setMaxECT(1);
				Params.getInstance().setMaxFuelCons(1);
				Params.getInstance().setMaxIAT(1);
				Params.getInstance().setMaxPres(1);
				Params.getInstance().setMaxRPM(1);
				Params.getInstance().setMaxTPS(1);
				readSerial();
				// ArdiunoPort.setRunning(true);
			}catch (Exception e){
				System.out.println("There is wrong with serial port");
			}
		});
		btnPlane.setOnAction(event -> {
			//  System.out.print("Pause Button");
			//ArdiunoPort.setRunning(false);
			//timer.stop();

			selectionBox();
		});
		btnhelp.setOnAction(event -> {
			try {
				AnchorPane borderPane = new AnchorPane();
				borderPane.setStyle("-fx-padding: 5;" + "-fx-border-style: solid inside;"
						+ "-fx-border-width: 2;" + "-fx-border-insets: 2;"
						+ "-fx-border-radius: 5;" + "-fx-border-color: gray;" + "-fx-background-color: #ffffff;");
				ImageView imgview = new ImageView();
				imgview.setPreserveRatio(true);
				imgview.setSmooth(true);
				imgview.prefHeight(200);
				imgview.prefWidth(200);
				Image icon = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\smalllogo.png").toURI().toURL().toString());
				imgview.setImage(icon);
				imgview.setStyle("-fx-padding: 2;" + "-fx-border-style: solid inside;" + "-fx-border-insets: 5;");
				Label lbel = new Label("\t ECU Control (DLA 180) V 3.8.9 R \nCopyright 2025, SERB and/or its affiliates.\n\t\t  All rights reserved.");
				borderPane.getChildren().addAll(imgview, lbel);
				AnchorPane.setLeftAnchor(imgview, 60.0);
				AnchorPane.setTopAnchor(imgview, 10.0);
				AnchorPane.setTopAnchor(lbel, 138.0);
				AnchorPane.setLeftAnchor(lbel, 20.0);
				Scene scene = new Scene(borderPane, 300, 300);
				stageAbout = new Stage();
				stageAbout.setResizable(false);
				stageAbout.setTitle("About");
				Image iconhelp = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\baseline_help_black_24dp.png").toURI().toURL().toString());
				stageAbout.getIcons().add(iconhelp);                      //Very Good initiative
				stageAbout.setScene(scene);
				stageAbout.show();
			} catch (IOException e) {
				System.out.println(e.getStackTrace());
			}
		});
		btnSettings.setOnAction(event -> {

     // ArdiunoPort.initDLA();
			Params.getInstance().extractSample();
//			try {
//				// String path="C:\\Users\\Net_A\\Videos\\MavinUAV"+"\\src\\main\\resources\\org.serb.fx\\settings.fxml";
//				String path=System.getProperty("user.dir")+"\\src\\main\\resources\\com\\org\\serb\\ecudla\\settings.fxml";
//				stageSetting = new Stage();
//				stageSetting.initStyle(StageStyle.DECORATED);
//				stageSetting.setTitle("Settings");
//				stageSetting.setResizable(false);
//				Image iconhelp = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\baseline_settings_black_24dp.png").toURI().toURL().toString());
//				stageSetting.getIcons().add(iconhelp);
//				myLoader = new FXMLLoader(new File(path).toURI().toURL());
//				//     System.out.println(System.getProperty("user.dir")+"\\src\\main\\resources\\org.serb.fx\\settings.fxml");
//				AnchorPane root =  myLoader.load();
//				Scene scene = new Scene(root);
//				stageSetting.setScene(scene);
//				// setPrevStage(stageSetting);
//				stageSetting.show();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		});
		btnLogdata.setOnAction(event -> {
//            System.out.println(Params.getInstance().pref.getDouble(Params.getInstance().KEYBAT1,0.0));
//            //Params.getInstance().pref.putBoolean(Params.getInstance().isSesstingsChanged,true);
//            System.out.println(Params.getInstance().pref.getBoolean(Params.getInstance().isSesstingsChanged,false));
//            double val1=     Params.getInstance().pref.getDouble(Params.getInstance().KEYBAT1,12.0);
//            double val2=      Params.getInstance().pref.getDouble(Params.getInstance().KEYBAT2,12.0);
//            double val3=     Params.getInstance().pref.getDouble(Params.getInstance().KEYBAT4,12.6);
//            double val4=      Params.getInstance().pref.getDouble(Params.getInstance().KEYBAT5,12.0);
			//System.out.println(myDocumentPath);
			System.out.println("\007");
			byte[] buffer ={(byte)0x90,(byte)0x9f,(byte)0xfa,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x24,0x46,0x48,0x46,0x00,0x00,0x68,0x00, (byte) 0xc4, (byte) 0xbb};
			byte[] beffer ={3,9,12,13,8};
			int fromAir= (buffer[35]<<8 & 0xff00) + (buffer[36] & 0xff);
			System.out.println(calcCRC(beffer) + "From Air "+fromAir);       //Designer with Go Ahead & Go Ahead...... Volume Concetration...
			Params.getInstance().setMaxECT(1);
			Params.getInstance().setMaxFuelCons(1);                          //Designer Samples
			Params.getInstance().setMaxIAT(1);
			Params.getInstance().setMaxPres(1);
			Params.getInstance().setMaxRPM(1);
			Params.getInstance().setMaxTPS(1);
		});
		toolbar.getItems().add(btnConnect);
		toolbar.getItems().add(btnPlane);
		toolbar.getItems().add(btnLogdata);
		toolbar.getItems().add(btnSettings);
		toolbar.getItems().add(rightSpacer);
		toolbar.getItems().add(btnhelp);
		toolbar.getItems().add(new ImageView(imgdevider));
		ImageView companylogoView = new ImageView(imageFlag);
		companylogoView.setFitWidth(35); companylogoView.setFitHeight(30);
		//Button button = new Button(); button.setGraphic(companylogoView);
		//companylogoView.setPreserveRatio(true);
		//companylogoView.setSmooth(true);

		toolbar.getItems().add(companylogoView);
		// levelGuageTank1= GaugeBuilder.create().skinType(Gauge.SkinType.LEVEL).prefSize(200,100).title("Fule Tank 01").
	    // animated(true).barBorderColor(Color.RED).ledBlinking(true).alertMessage("Fueltank 01").build();
		// levelGuageTank2= GaugeBuilder.create().skinType(Gauge.SkinType.LEVEL).title("Fule Tank 02").prefSize(200,100).build();
		// FunLevelGauge   gaugeFuel1 = new FunLevelGauge();
		//gaugeFuel1.setPrefSize(200, 200);
		// FunLevelGauge    gaugeFuel2 = new FunLevelGauge();
		//  gaugeFuel2.setPrefSize(200, 200);
		//Gauge gaugeFuel1 = GaugeBuilder.create().skinType(Gauge.SkinType.LEVEL).prefSize(200, 220).barColor(Color.rgb(200, 0, 0)).foregroundBaseColor(color.deriveColor(0, 0.3, 1.5, 1)).needleBorderColor(Color.rgb(200, 0, 0)).oldValueVisible(false).maxValue(Params.getInstance().pref.getInt(Params.getInstance().FuelTank,21)).minValue(0).build();
		//Gauge gaugeFuel2 = GaugeBuilder.create().skinType(Gauge.SkinType.LEVEL).prefSize(200, 200).barColor(Color.rgb(200, 0, 0)).foregroundBaseColor(color.deriveColor(0, 0.3, 1.5, 1)).needleBorderColor(Color.rgb(200, 0, 0)).oldValueVisible(false).maxValue(21).minValue(0).build();
		Tile fuleConsumpsion = TileBuilder.create().skinType(Tile.SkinType.SPARK_LINE).prefSize(495, 130).title("Fuel Consumption").unit("L/H").middleText("fwf").strokeWithGradient(true).tickLabelDecimals(2).decimals(2).smoothing(true).gradientStops(new Stop(0, Color.web("#1CAF4D")),
				new Stop(0.0075,  Color.web("#1CAF4D")),
				new Stop(0.00751, Color.web("#91CA40")),
				new Stop(0.01166, Color.web("#91CA40")),
				new Stop(0.01167, Color.web("#F8C610")),
				new Stop(0.01666, Color.web("#F8C610")),
				new Stop(0.01667, Color.web("#F29222")),
				new Stop(0.025,   Color.web("#F29222")),
				new Stop(0.02501, Color.web("#EC1D24")),
				new Stop(1.0,     Color.web("#EC1D24"))).build();
//		Tile fuelTile1 = TileBuilder.create().skinType(Tile.SkinType.CUSTOM).prefSize(180, 130).text("").roundedCorners(true)
//				.textAlignment(TextAlignment.CENTER).textSize(Tile.TextSize.BIGGER).textColor(Color.rgb(0, 200, 0)).build();
//		Tile fuelTile2 = TileBuilder.create().skinType(Tile.SkinType.CUSTOM).prefSize(180, 130).text("").roundedCorners(true)
//				.textAlignment(TextAlignment.CENTER).textSize(Tile.TextSize.BIGGER).textColor(Color.rgb(0, 200, 0)).build();
//		fuelTile1.setGraphic(gaugeFuel1);
//		fuelTile2.setGraphic(gaugeFuel2);
		HBox fuelPanel = new HBox(fuleConsumpsion);
		fuelPanel.setStyle("-fx-padding: 2;" + "-fx-border-style: solid inside;"
				+ "-fx-border-width: .1;" + "-fx-border-insets: 0;"
				+ "-fx-border-radius: 2;" + "-fx-border-color: gray;");
		// fuelPanel.setStyle("-fx-background-color: #303030");
		fuelPanel.setSpacing(1);
		fuelPanel.setAlignment(Pos.CENTER);
		Gauge gaugeRPM = GaugeBuilder.create()
				.borderPaint(Color.WHITE)
				.foregroundBaseColor(Color.WHITE)
				.prefSize(160, 160)
				.startAngle(0)
				.angleRange(270)
				.minValue(0)
				.maxValue(7000)
				.valueVisible(true)
				.minorTickMarksVisible(true)
				.majorTickMarkType(TickMarkType.BOX)
				.mediumTickMarkType(TickMarkType.BOX)
				.title("RPM\nx100")
				.needleShape(Gauge.NeedleShape.ROUND)
				.needleSize(Gauge.NeedleSize.THICK)
				.needleColor(Color.rgb(234, 67, 38))
				.knobColor(Gauge.DARK_COLOR)
				.customTickLabelsEnabled(true)
				.customTickLabelFontSize(40)
				.customTickLabels("0", "10", "20", "30", "40", "50", "60","70")
				.animated(true)
				.build();


		Gauge gaugeTPS = GaugeBuilder.create()
				.skinType(Gauge.SkinType.GAUGE)
				.title("Throttle")
				.customTickLabelFontSize(40)
				.prefSize(180, 180)
				.foregroundBaseColor(Color.rgb(249, 249, 249))
				.knobColor(Color.DARKGRAY)
				.build();
		HBox ECUPanel = new HBox(gaugeRPM,gaugeTPS);                   //Install limitations
		ECUPanel.setPadding(new Insets(0, 5, 0, 5));
		// ECUPanel.setStyle("-fx-background-color: #303030");
		ECUPanel.setSpacing(2);
		ECUPanel.setAlignment(Pos.CENTER);
		Gauge gaugeTempCylinder1 =  GaugeBuilder.create().skinType(Gauge.SkinType.DIGITAL).prefSize(150, 150).title("Temp 1").decimals(2).barColor(Color.rgb(200, 0, 70)).foregroundBaseColor(color.deriveColor(0, 0.3, 1.5, 1)).needleBorderColor(Color.rgb(200, 0, 0)).build();
		Gauge gaugeTempCylinder2 =  GaugeBuilder.create().skinType(Gauge.SkinType.DIGITAL).prefSize(150, 150).title("Temp 2").decimals(2).barColor(Color.rgb(200, 0, 70)).foregroundBaseColor(color.deriveColor(0, 0.3, 1.5, 1)).needleBorderColor(Color.rgb(200, 0, 0)).build();
		Gauge gaugeTempAirIntake = GaugeBuilder.create().skinType(Gauge.SkinType.DIGITAL).prefSize(150, 150).title("Atmospheric").decimals(2).barColor(Color.rgb(200, 0, 70)).foregroundBaseColor(color.deriveColor(0, 0.3, 1.5, 1)).needleBorderColor(Color.rgb(200, 0, 0)).build();
		//Gauge gaugePressure =      GaugeBuilder.create().skinType(Gauge.SkinType.DIGITAL).prefSize(150, 150).title("Pressure").barColor(Color.rgb(2, 138, 204)).foregroundBaseColor(color.deriveColor(0, 0.3, 1.5, 1)).build();
		HBox TempPanel =           new HBox(gaugeTempCylinder1,gaugeTempCylinder2, gaugeTempAirIntake);
		//TempPanel.setPadding(new Insets(5, 5, 5, 5));
		// TempPanel.setStyle("-fx-background-color: #303030");
		TempPanel.setSpacing(2); //Temperament losing ... Environment Losing in this category
		TempPanel.setAlignment(Pos.CENTER); //Composit Materials ...
		Gauge battery1 = GaugeBuilder.create().skinType(Gauge.SkinType.BATTERY).prefSize(200, 150).title("Volt Src 01 ").barColor(Color.rgb(0, 200, 0)).areaTextVisible(true).barBorderColor(Color.rgb(200, 0, 0)).build();
		Gauge battery2 = GaugeBuilder.create().skinType(Gauge.SkinType.BATTERY).prefSize(200, 150).title("Volt Src 02 ").barColor(Color.rgb(0, 200, 0)).build();
		Gauge battery3 = GaugeBuilder.create().skinType(Gauge.SkinType.BATTERY).prefSize(200, 150).title("Volt Src 03 ").barColor(Color.rgb(0, 200, 0)).build();
		Gauge battery4 = GaugeBuilder.create().skinType(Gauge.SkinType.BATTERY).prefSize(200, 150).title("Volt Src 04 ").barColor(Color.rgb(0, 200, 0)).build();
		Gauge battery5 = GaugeBuilder.create().skinType(Gauge.SkinType.BATTERY).prefSize(200, 150).title("Volt Src 05 ").barColor(Color.rgb(0, 200, 0)).build();
		//  Gauge battery6 = GaugeBuilder.create().skinType(Gauge.SkinType.BATTERY).prefSize(200, 150).title("Volt Src 03 ").barColor(Color.rgb(0, 200, 0)).build();
//		Tile batteryTile1 = TileBuilder.create().skinType(Tile.SkinType.CUSTOM).prefSize(180, 130).title("Battery Autopilot").text("").roundedCorners(true)
//				.textAlignment(TextAlignment.CENTER).textSize(Tile.TextSize.BIGGER).textColor(Color.rgb(0, 200, 0)).build();
//		Tile batteryTile2 = TileBuilder.create().skinType(Tile.SkinType.CUSTOM).prefSize(180, 130).title("Telemetry").text("").roundedCorners(true)
//				.textAlignment(TextAlignment.CENTER).textSize(Tile.TextSize.BIGGER).textColor(Color.rgb(0, 200, 0)).build();
//		Tile batteryTile3 = TileBuilder.create().skinType(Tile.SkinType.CUSTOM).prefSize(180, 130).title("Payload").text("").roundedCorners(true)
//				.textAlignment(TextAlignment.CENTER).textSize(Tile.TextSize.BIGGER).textColor(Color.rgb(0, 200, 0)).build();
//		Tile batteryTile4 = TileBuilder.create().skinType(Tile.SkinType.CUSTOM).prefSize(180, 130).title("Alternator1").text("").roundedCorners(true)
//				.textAlignment(TextAlignment.CENTER).textSize(Tile.TextSize.BIGGER).textColor(Color.rgb(0, 200, 0)).build();
//		Tile batteryTile5 = TileBuilder.create().skinType(Tile.SkinType.CUSTOM).prefSize(180, 130).title("Alternator2").text("").roundedCorners(true)
//				.textAlignment(TextAlignment.CENTER).textSize(Tile.TextSize.BIGGER).textColor(Color.rgb(0, 200, 0)).build();
//		batteryTile1.setGraphic(battery1);
//		batteryTile2.setGraphic(battery2);
//		batteryTile3.setGraphic(battery3);
//		batteryTile4.setGraphic(battery4);
//		batteryTile5.setGraphic(battery5);
//		battery1.setMaxValue(100);       //Let me show you one thing..... droptown ALfaqra
//		battery2.setMaxValue(12.6);
//		battery3.setMaxValue(12.6);
//		battery4.setMaxValue(12.6);
//		battery5.setMaxValue(12.8);
		Text txt = new Text("Engine Status"); setTextStyle(txt);
		TextField fieldEngineStatus = new TextField(); setFieldStyle(fieldEngineStatus);
		fieldEngineStatus.setPrefSize(50,10);
		VBox box0 = new VBox(txt,fieldEngineStatus);
		HBox.setHgrow(box0,Priority.ALWAYS);
		box0.setPrefWidth(70);
		box0.setAlignment(Pos.CENTER_LEFT);

		Text txt1 = new Text("Consumption"); setTextStyle(txt1);
		TextField fieldFuelConsumption = new TextField(); setFieldStyle(fieldFuelConsumption);
		fieldFuelConsumption.setPrefSize(30,10);
		VBox box = new VBox(txt1,fieldFuelConsumption);
		HBox.setHgrow(box,Priority.ALWAYS);
		box.setPrefWidth(70);
		box.setAlignment(Pos.CENTER_LEFT);

//		Text txt2 = new Text("Fuel L/H"); setTextStyle(txt2);
//		TextField fieldFuelLH = new TextField(); setFieldStyle(fieldFuelLH);
//		fieldFuelLH.setPrefSize(30,10);
//		VBox box2 = new VBox(txt2,fieldFuelLH); box2.setPrefWidth(60); box2.setAlignment(Pos.CENTER);

		Text txt3 = new Text("Battery Voltage"); setTextStyle(txt3);
		fieldBattVol = new TextField(); setFieldStyle(fieldBattVol);
		fieldBattVol.setPrefSize(30,10);
		VBox box3 = new VBox(txt3,fieldBattVol);
		HBox.setHgrow(box3,Priority.ALWAYS);
		box3.setPrefWidth(70);
		box3.setAlignment(Pos.CENTER_LEFT);

		Text txt4 = new Text("Baro Pressure"); setTextStyle(txt4);
		TextField fieldBaro = new TextField(); setFieldStyle(fieldBaro);
		fieldBaro.setPrefSize(30,10);
		VBox box4 = new VBox(txt4,fieldBaro);
		HBox.setHgrow(box4,Priority.ALWAYS);
		box4.setPrefWidth(70);
		box4.setAlignment(Pos.CENTER_LEFT);

		Text txt5 = new Text(" Running Time "); setTextStyle(txt5);
		TextField fieldTime = new TextField(); setFieldStyle(fieldTime);
		fieldTime.setPrefSize(30,10);
		VBox box5 = new VBox(txt5,fieldTime); //box5.setAlignment(Pos.CENTER);




		battryPane1 = new HBox(box0,box,box3,box4,box5);

		battryPane1.setStyle("-fx-padding: 5; -fx-spacing: 5;" + "-fx-border-style: solid inside;"
				+ "-fx-border-width: 2;" + "-fx-border-insets: 2;"
				+ "-fx-border-radius: 5;" + "-fx-border-color: gray;");
		battryPane1.setSpacing(30);
		//battryPane1.setAlignment(Pos.CENTER);
		//battryPane1.
		//battryPane1.setAlignment(Pos.CENTER);
		//battryPane1.setSpacing(5);
		// battryPane2.setSpacing(1);
		// battryPane2.setAlignment(Pos.CENTER);
		rightPane = new VBox(ECUPanel, TempPanel,battryPane1, fuelPanel); //rightPane.setPrefSize(400,700);
		rightPane.setStyle("-fx-padding: 5;" + "-fx-border-style: solid inside;"
				+ "-fx-border-width: .5;" + "-fx-border-insets: 0;"
				+ "-fx-border-radius: 2;" + "-fx-border-color: gray;");
		rightPane.setAlignment(Pos.BOTTOM_CENTER);
//		VBox batteryPanel = new VBox(battryPane1);
//		batteryPanel.setAlignment(Pos.BOTTOM_CENTER);
//		batteryPanel.setSpacing(2);
		//rightPane.setMinSize(400,700);
		Gauge maxRPM = GaugeBuilder.create()
				.skinType(Gauge.SkinType.LCD)
				.prefSize(120, 40)
				.unit("     rpm")
				.decimals(2)
				.maxValue(6500).padding(new Insets(1)).
				title("MAX. RPM")
				.build();
		Gauge tilefuelConsumed = GaugeBuilder.create()
				.skinType(Gauge.SkinType.LCD)
				.prefSize(120, 40)
				.unit("(L)")
				.decimals(2)
				.padding(new Insets(1))
				.maxValue(300).
				title("ConsumedFuel")
				.build();
		Gauge maxTemp1 = GaugeBuilder.create()
				.skinType(Gauge.SkinType.LCD)
				.prefSize(120, 40)
				.unit("     deg")
				.maxValue(300).
				decimals(2)
				.title("MAX. Temp1 ").padding(new Insets(1))
				.build();
		Gauge maxTemp2 = GaugeBuilder.create()
				.skinType(Gauge.SkinType.LCD)
				.prefSize(120, 40)
				.unit("     deg")
				.decimals(2)
				.maxValue(300).
				title("MAX. Temp2").padding(new Insets(1))
				.build();
		Gauge avgFuelLitSec = GaugeBuilder.create()
				.skinType(Gauge.SkinType.LCD)
				.unit("L/H")
				.maxValue(300)
				.decimals(2)
				.prefSize(120, 40)
				.title("Avg. FuelCons").padding(new Insets(1))
				.build();
		Gauge maxFuelCons = GaugeBuilder.create()
				.skinType(Gauge.SkinType.LCD)
				.prefSize(120, 40)
				.unit("L/H")
				.decimals(2)
				.titleColor(Color.AQUA)
				.maxValue(100).
				title("MAX. FuelCons").padding(new Insets(1))
				.build();
		//maxRPM.setValue(4400);
		Image logoSerb = new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\ic_logo.png").toURI().toURL().toString(),150,150,true,true);
		ImageView uavLogo=new ImageView();//.barColor(Color.web("#500220"))
		uavLogo.setImage(logoSerb);
		//Gauge gaugeFuelPerHour=GaugeBuilder.create().skinType(Gauge.SkinType.).prefSize(130,130).maxValue(100).title("Fuel Milage").unit("v").needleColor(Color.rgb(0, 200, 0)).build();
		Tile gaugeFuelPerHour= TileBuilder.create().skinType(Tile.SkinType.BAR_GAUGE).maxValue(15).title("Lit/hr") //sectionsVisible(true)
				//.sections(new Section(0, 4.4, Color.BLUE),new Section(4.5,6,Color.YELLOW),new Section(6.5,16,Color.RED))
				.strokeWithGradient(true).gradientStops(new Stop(1.5, Bright.BLUE),
						new Stop(2.5, Bright.GREEN),
						new Stop(4.0, Bright.RED)).build();
		//Tile tileUAV=TileBuilder.create().skinType(Tile.SkinType.CUSTOM).prefSize(150,150).backgroundColor(Color.web("#303030")).borderColor(Color.GRAY).graphic(gaugeFuelPerHour).build();
		Tile logoTile=TileBuilder.create().skinType(Tile.SkinType.CUSTOM).graphic(uavLogo).prefSize(120,120).backgroundColor(Color.web("#303030")).description("SERB PVT LTD.").borderColor(Color.GRAY).build();
		//tileStatus=GaugeBuilder.create().skinType(Gauge.SkinType.CHARGE).prefSize(150,50).build();
		JFXButton btnStartheat= new JFXButton("Start heat");setBtnStyle(btnStartheat,1);
		JFXButton btnPreStart= new JFXButton("Pre Start");setBtnStyle(btnPreStart,1);
		JFXButton btnStartUp= new JFXButton("Start up");setBtnStyle(btnStartUp,2);
		JFXButton btnShutDownEng= new JFXButton("Flame out");setBtnStyle(btnShutDownEng,0);

		btnStartUp.setOnAction(event -> {
			Alert alertStart = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure to start the engine?");
			//((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(img);
			alertStart.getDialogPane().setMaxWidth(80);
			alertStart.getDialogPane().setMaxHeight(180);
			alertStart.setResultConverter(dialogBtn ->{if(dialogBtn == ButtonType.OK){ ArdiunoPort.getInstance().startDLA();}
				return null;});
			alertStart.show();
		});
		btnShutDownEng.setOnAction(event -> {
			Alert alertShutDown = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure to shutdown the engine?");
			//((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(img);
			alertShutDown.getDialogPane().setMaxWidth(80);
			alertShutDown.getDialogPane().setMaxHeight(180);
			alertShutDown.setResultConverter(dialogBtn ->{if(dialogBtn == ButtonType.OK){ ArdiunoPort.getInstance().shutDownDLA();}
				return null;});
			alertShutDown.show();
		});


		//Throttle Slider






		Label valueLabel = new Label("Throttle: 0");
		valueLabel.setStyle("-fx-foreground-color: #ffffff; -fx-fill: #B0C4DE; -fx-text-fill: white;");
		slider = new Slider(0, 100, 0); // Min = 1, Max = 100, Default = 50
		slider.setValue(0);
		//slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);

		//slider.setMajorTickUnit(10);
		//slider.setMinorTickCount(5);
		//slider.setSnapToTicks(true);
		// Button to decrease value
		Button decreaseButton = new Button("<");
		decreaseButton.setOnAction(e -> slider.setValue(slider.getValue() - .5));

		// Button to increase value
		Button increaseButton = new Button(">");
		increaseButton.setOnAction(e -> slider.setValue(slider.getValue() + .5));
		slider.valueProperty().addListener((obs, oldVal, newVal) -> {
					valueLabel.setText("Throttle: " + newVal.intValue());
					ArdiunoPort.getInstance().sendThrottle(newVal.intValue());
				}

		);

		HBox layout = new HBox(14, decreaseButton, valueLabel, increaseButton);
		layout.setAlignment(Pos.CENTER);
		VBox boxSlider = new VBox(2, slider,layout);
		boxSlider.setStyle("-fx-padding: 2; -fx-alignment: center; -fx-background-color: #303030; -fx-foreground-color: #ffffff; -fx-fill: #B0C4DE; -fx-text-fill: white;");
		VBox boxButtons = new VBox(btnStartheat,btnPreStart,btnStartUp,btnShutDownEng);




		//HBox boxButtonswSlider= new HBox(boxButtons,boxSlider);
		VBox vboxMaxPanel=new VBox(maxRPM,maxTemp1,maxTemp2,maxFuelCons,avgFuelLitSec,tilefuelConsumed,boxSlider,boxButtons); vboxMaxPanel.setSpacing(1); vboxMaxPanel.setPadding(new Insets(1,2,0,2));
		vboxMaxPanel.setPrefWidth(150);
		bottompane = new Pane();
		bottompane.setStyle("-fx-background-color: #508030");
		centerPane = new HBox();
		centerPane.setSpacing(5);
		borderPane.setTop(toolbar);
		//borderPane.setBottom(batteryPanel);
		borderPane.setCenter(rightPane);
		//borderPane.setLeft();
		borderPane.setRight(vboxMaxPanel);
		lastTimerCall = System.nanoTime();
		//gaugeFuel1.setValue(Params.getInstance().pref.getInt(Params.getInstance().FuelTank,0 ));
		timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (now > lastTimerCall + 10_000_000L) { //10_000_000_00L

					gaugeRPM.setValue(Params.getInstance().getRPM());
					gaugeTPS.setValue(Params.getInstance().getTPS());
					gaugeTempAirIntake.setValue(Params.getInstance().getIAT());
					// gaugeTempAirIntake.setValue(535325);
					gaugeTempCylinder1.setValue(Params.getInstance().getTempClndr1());
					gaugeTempCylinder2.setValue(Params.getInstance().getTempClndr2());

					maxRPM.setValue(Params.getInstance().getMaxRPM());
					//tilefuelConsumed.setValue(Params.getInstance().getFuelConsumed());
					maxTemp1.setValue(Params.getInstance().getMaxTemp1());
					maxTemp2.setValue(Params.getInstance().getMaxTemp2());
					maxFuelCons.setValue(Params.getInstance().getMaxFuelCons());
					tilefuelConsumed.setValue(Params.getInstance().getFuelConsumed());
					avgFuelLitSec.setValue(Params.getInstance().getFuelperHour());

					//if(Params.getInstance().getMaxRPM()>0) {


					fuleConsumpsion.setValue(Params.getInstance().getFuelperHour());   //Graph Tile L/H
					//fieldEngineStatus.setText(String.valueOf(Params.getInstance().getEngineStatusFlag()));
					String formattedFuel = String.format("%.2f", Params.getInstance().getFuelConsumed());
					fieldFuelConsumption.setText(formattedFuel);


					fieldBattVol.setText(String.valueOf(Params.getInstance().getVol1()));
					fieldBaro.setText(String.valueOf((Params.getInstance().getBAROPressure())));

					fieldTime.setText(Params.getInstance().getEngineRunTime());

					//maxFuelCons.setValue(Params.getInstance().getMaxFuelCons());
					fieldEngineStatus.setText(Params.getInstance().getEngineState());
					//fieldTime.setText(StopWatch.displayElapsedTime(now));
					lastTimerCall = now;

				}
			}
		};
	}

	private void readSerial(){
		String[] ports = ArdiunoPort.getInstance().getComPorts();
		ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("USB Connection",ports);
		choiceDialog.getDialogPane().setPrefSize(100,50);
		try {
			Image iconimage = new Image(new File(System.getProperty("user.dir") + "\\src\\main\\resources\\baseline_security_black_24dp.png").toURI().toURL().toString());
			((Stage)choiceDialog.getDialogPane().getScene().getWindow()).getIcons().add(iconimage);
			choiceDialog.showingProperty().addListener((ov, b, b1)->{
				if(b1) {
					Node comboBox = choiceDialog.getDialogPane().lookup(".combo-box");
					comboBox.requestFocus();
				}
			});
			choiceDialog.showAndWait();
			String portName = choiceDialog.getSelectedItem();
			System.out.println(choiceDialog.getSelectedItem());
			boolean status = ArdiunoPort.getInstance().connectPort(portName);
			if(!status){
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Error in Connection!");
				Image img = new Image(new File(System.getProperty("user.dir") + "\\src\\main\\resources\\baseline_security_black_24dp.png").toURI().toURL().toString());
				((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(img);
				alert.getDialogPane().setMaxWidth(230);
				alert.getDialogPane().setMaxHeight(70);
				alert.show();
			}else {
				//ArdiunoPort.getInstance().startPingThread();
				ArdiunoPort.getInstance().startFeedbackThreadDLA();
			}
		}catch (MalformedURLException e){e.printStackTrace();}
	}
	//Statements does not applied to rest of the code
	//Let me show some intresting environment
	//Print stack trace in front of liquidation outfits
	void  selectionBox(){
		try {
			String array[] = {"SMD","SML"};
			ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("SML",array);
			Image iconimage = new Image(new File(System.getProperty("user.dir") + "\\src\\main\\resources\\baseline_security_black_24dp.png").toURI().toURL().toString());
			((Stage)choiceDialog.getDialogPane().getScene().getWindow()).getIcons().add(iconimage);
			choiceDialog.showingProperty().addListener((ov, b, b1)->{
				if(b1) {
					Node comboBox = choiceDialog.getDialogPane().lookup(".combo-box");
					comboBox.requestFocus();
				}
			});

			choiceDialog.showAndWait();
			String planename = choiceDialog.getSelectedItem();
			System.out.println(choiceDialog.getSelectedItem());
			Params.getInstance().pref.put("plane",planename);
			btnPlane.setText(planename);
			Params.getInstance().setPlaneCaliFactor();

//           boolean status = ArdiunoPort.getInstance().connectPort(portName);
//           if(!status){
//               Alert alert = new Alert(Alert.AlertType.ERROR);
//               alert.setHeaderText("Error in Connection!");
//               Image img = new Image(new File(System.getProperty("user.dir") + "\\src\\main\\resources\\baseline_security_black_24dp.png").toURI().toURL().toString());
//               ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(img);
//               alert.getDialogPane().setMaxWidth(230);
//               alert.getDialogPane().setMaxHeight(70);
//               alert.show();
//           }else {
//              // ArdiunoPort.getInstance().startFeedbackThread();
//           }
		}catch (MalformedURLException e){e.printStackTrace();}
	}

	public void setPrevStage(Stage stage){
		this.prevStage = stage;
	}
	@Override
	public void stop() throws Exception {
		timer.stop();
		try{
			if(ArdiunoPort.getInstance().isRunning()){
				ArdiunoPort.getInstance().arduinoPort.closePort();
				ArdiunoPort.getInstance().setRunning(false);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		System.exit(0);
		super.stop();
	}
	@Override
	public void start(Stage primaryStage) throws MalformedURLException {
		Scene scene = new Scene(borderPane, 600, 530);
		primaryStage.setScene(scene); primaryStage.setMaxWidth(650);primaryStage.setMaxHeight(580);
		primaryStage.setTitle("SERB ECU 3.8.9 (DLA 180)" );
		//Image icon = new Image(new File("appicon.ico").toURI().toURL().toString());
		//primaryStage.getIcons().add(new Image("file:baseline_security_black_24dp.png"));
		primaryStage.getIcons().add(new Image(new File(System.getProperty("user.dir")+"\\src\\main\\resources\\baseline_security_black_24dp.png").toURI().toURL().toString()));
		primaryStage.show();
		primaryStage.setOnCloseRequest(event -> {
			if(stageSetting!=null && stageSetting.isShowing()){
				stageSetting.close();
			}
			if(stageAbout!=null && stageAbout.isShowing()){
				stageAbout.close();
			}
		});
		timer.start();
	}

	int calcCRC(byte[] buffer){
		int crc = 0;
		for (int i=0;i<5;i++){
			crc = crc ^ buffer[i];
		}
		return crc;
	}

	void setTextStyle(Text txt){
		txt.setStyle("-fx-fill: #F5F9F9;"+
				"-fx-font-weight: bold;"+
				"-fx-font-size: 10px;");
	}
	void setFieldStyle(TextField field){
		field.setStyle("-fx-background-color: #303030;" +" -fx-border-color: #303030; -fx-border-width: 1;"
				+ " -fx-background-radius: 1 1 1 1; -fx-fill: #B0C4DE;"
				+ "-fx-text-fill: white; -fx-font-size: 12px;"
				+ "-fx-border-radius: 0 0 0 0;");
		//field.setDisable(true);
		field.setText("23.55");

	}
	void setBtnStyle(JFXButton btn, int type){
		btn.setButtonType(JFXButton.ButtonType.RAISED);
		btn.setPrefHeight(32);
		//btn.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");
		btn.setPrefSize(145,32);
		String colorCode = "#e10000";
		switch (type) {
			case 1:
				colorCode = "#4B4B4B";
				break;                //Agent seems agent in the sense of military occupation...
			case 2:
				colorCode = "#4B5320";
				break;
			default:
				colorCode = "#e00000";
		}
		btn.setStyle("-fx-background-color: "+colorCode+";" +" -fx-border-color: #FFFFFF; -fx-border-width: 1;"
				+ " -fx-background-radius: 5 5 5 5;"
				+ "-fx-text-fill: white; -fx-font-size: 12px;"
				+ "-fx-border-radius: 5 5 5 5;");
		//Let me explain
		//
	}
}