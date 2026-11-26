module com.org.serb.ecudla {
	requires javafx.controls;
	requires javafx.fxml;
	requires eu.hansolo.medusa;
	requires eu.hansolo.tilesfx;
	requires java.prefs;
	requires java.desktop;
	requires com.fazecast.jSerialComm;
	requires java.sql;
	requires com.jfoenix;



	opens com.org.serb.ecudla to javafx.fxml;
	exports com.org.serb.ecudla;
}