package com.org.serb.ecudla;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class settingsController implements Initializable {
    @FXML
    Button btnSave,btnCancel;
    @FXML
    TextField txtInputFuel;
    @FXML
    Spinner<Double> spnbat1;
    @FXML
    Spinner<Double> spnbat2;
    @FXML
    Spinner<Double> spnbat3;
    @FXML
    Spinner<Double> spnbat4;
    @FXML
    Spinner<Double> spnbat5;
    @FXML
    RadioButton radioButtonAuro,radioButtonManual;
    ToggleGroup group;
    double initialValue=12.6;
    SpinnerValueFactory<Double> valueFactory1,valueFactory2,valueFactory3,valueFactory4,valueFactory5;
    @FXML
    public void btnSave(){
        try {


            //Params.getInstance().pref.putDouble(Params.getInstance().KEYBAT1, valueFactory1.getValue());
            //Params.getInstance().pref.putDouble(Params.getInstance().KEYBAT2, valueFactory2.getValue());
            //Params.getInstance().pref.putDouble(Params.getInstance().KEYBAT3, valueFactory3.getValue());
            //Params.getInstance().pref.putDouble(Params.getInstance().KEYBAT4, valueFactory4.getValue());
            //Params.getInstance().pref.putDouble(Params.getInstance().KEYBAT5, valueFactory5.getValue());
            //Params.getInstance().pref.putBoolean(Params.getInstance().isSesstingsChanged, true);
            Params.getInstance().pref.putInt(Params.getInstance().FuelTank, Integer.parseInt(txtInputFuel.getText().toString()));
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Done !"+Params.getInstance().pref.getInt(Params.getInstance().FuelTank, 0),ButtonType.FINISH);
            alert.show();
        }catch (Exception ex) {
            //ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING,"Invalid Fuel Capacity!"+"\n"+ex.getMessage(),ButtonType.CLOSE);
            alert.show();
        }
    }
    @FXML
    public void btnFlightModeSave() {
        RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
        String toogleGroupValue = selectedRadioButton.getText();
        if (toogleGroupValue.equals("Auto")){
            Params.getInstance().pref.put(Params.getInstance().FLIGHTMODE, "AUTO");
    }else{
            Params.getInstance().pref.put(Params.getInstance().FLIGHTMODE, "MANUAL");
    }
        System.out.println(Params.getInstance().pref.get(Params.getInstance().FLIGHTMODE,null));
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        group = new ToggleGroup();
        radioButtonAuro.setToggleGroup(group);
        radioButtonManual.setToggleGroup(group);
        try {
              Params.getInstance().pref.putBoolean(Params.getInstance().isSesstingsChanged,false);
         //   Params.getInstance().pref.put(Params.getInstance().FLIGHTMODE, "AUTO");
            valueFactory1 = //
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 40.0, initialValue, 0.1);
             valueFactory2 = //
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 40.0, initialValue, 0.1);
             valueFactory3 = //
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 40.0, initialValue, 0.1);
             valueFactory4 = //
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 40.0, initialValue, 0.1);
            valueFactory5 = //
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 40.0, initialValue, 0.1);
            spnbat1.setValueFactory(valueFactory1);
            spnbat2.setValueFactory(valueFactory2);
            spnbat3.setValueFactory(valueFactory3);
            spnbat4.setValueFactory(valueFactory4);
            spnbat5.setValueFactory(valueFactory5);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
