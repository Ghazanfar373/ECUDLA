package com.org.serb.ecudla;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.prefs.Preferences;

/// /f11f0000007d0000229a60b5070000db08cb00014093930000bd03bd03da02f30532006f00930093000000640064006300640000006400670065001f001f000800000000000000000000000000000093010000130e3200000001004061734f6005000000008706000000000d00000000000000e509e509005c2e46ac3cf22
public class Params {
    private static final int MAX31865CalibFactor = 10;
    public static Params param;
    public final String isSesstingsChanged = "isSetting";
    public final String KEYBAT1 = "Bat1";
    public final String KEYBAT2 = "Bat2";
    public final String KEYBAT3 = "Bat3";
    public final String KEYBAT4 = "Bat4";
    public final String KEYBAT5 = "Bat5";
    public final String FLIGHTMODE = "Mode";
    public final String FuelTank = "FuelTank";
    private final byte[] sendingPack;
    private final byte[] receivingPackSMD;
    private final byte[] receivingPackDLA;
    private final byte[] receivingPackDLAArduino;
    private final int bufferSizeDLA = 127;
    private final int sendingPackSize = 1;
    private final byte header = 0x2d;
    private final byte handShakeByte = 0x61;
    private final byte footer = 0x6a;
    private final double factor100 = 17;
    private final double tempThreshold = 100;
    double valueX, valueY, valueZ;
    boolean btnfire, btnReset;
    int btnModeFire = 0, btnFire = 0;
    int btnSelection = 0;
    double[] fuelVolBuffer = {0.0, 0.0, 0.0, 0.0, 0.0};
    double[] fuelBuffer = {0.0, 0.0, 0.0, 0.0, 0.0};
    int fuelFreqCounter = 0;
    int fuelVolCounter = 0;
    double fuelVolConsumed = 0.0;
    Preferences pref;
    float fuelLevel1;
    float fuelLevel2;
    volatile double RPM;
    int TPS;
    double seconds;
    double pw1, pw2;
    double adv_deg;
    int squirt;
    String engineState;
    int afrtgt1, afrtgt2;
    double kFactorCalc;
    int wbo2_en1, wbo2_en2;
    double baro, advanceAngle,InjectorPulse;
    double map, mat, clt, tps, batt;
    volatile int ECT;
    volatile double CHTLeft, CHTRight;
    int IAT;
    double BAROPressure;
    double fuelConsumption;
    double fuelConsumed;
    double fuelperHour;
    byte engineStatusFlag;
    long runningTime;
    float tempClndr1, tempClndr2;
    float vol1, vol2, vol3, vol4, vol5;
    double maxRPM, maxTPS, maxECT, maxIAT, maxPres, maxFuelCons, maxCHTLeft, maxCHTRight;
    double maxTemp1, maxTemp2;
    String downlinkBuffer = "void";
    byte[] bufferSample = {0x43, 0x01, (byte) 0xEC, 0x1C, (byte) 0xB8, 0x0B, 0x65, 0x0F, 0x15, 0x01, 0x03, 0x04, (byte) 0x93, (byte) 0x93, 0x00, 0x00, 0x03, (byte) 0xAC, 0x03, 0x48, 0x03, 0x5F, 0x037, 0x3, 0x00, 0x6F, 0x00, (byte) 0x93
            , 0x00, (byte) 0x93, 0x00, 0x00, 0x00, 0x64, 0x00, 0x64, 0x00, 0x61, 0x00, (byte) 0xDF, 0x00, 0x00, 0x00, 0x64, 0x00, 0x67, 0x00, (byte) 0xDC, 0x00, 0x3D, 0x00, 0x3D, 0x00, 0x16, 0x00, 0x00
            , 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xEB, 0x01, 0x00, 0x00, 0x63, 0x0B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private boolean isRunning = false;
    private String engineRunTime;
    private byte[] receivingPack;
    private int crc = 0;
    private int MAX6675CalibFactor = 0;

    Params() {
        sendingPack = new byte[sendingPackSize];
        //receivingPack= new byte[receivingPacketSize];
        receivingPackSMD = new byte[10];
        receivingPackDLA = new byte[bufferSizeDLA];
        receivingPackDLAArduino = new byte[17];
        pref = Preferences.userRoot().node(getClass().getName());
    }

    public static Params getParam() {
        return param;
    }

    public static void setParam(Params param) {
        Params.param = param;
    }

    public static Params getInstance() {
        if (param == null) {
            param = new Params();
        }
        return param;
    }

    public String getDownlinkBuffer() {
        return downlinkBuffer;
    }

    public void setDownlinkBuffer(String downlinkBuffer) {
        this.downlinkBuffer = downlinkBuffer;
    }

    public double getFuelConsumed() {
        return fuelConsumed;
    }

    public void setFuelConsumed(double fuelConsumed) {


        this.fuelConsumed = fuelConsumed;
    }

    public double getInjectorPulse() {
        return InjectorPulse;
    }

    public void setInjectorPulse(double injectorPulse) {
        InjectorPulse = injectorPulse;
    }

    public double getMaxTemp1() {
        return maxTemp1;
    }

    public void setMaxTemp1(double maxTemp1) {
        this.maxTemp1 = maxTemp1;
    }

    public double getMaxTemp2() {
        return maxTemp2;
    }

    public void setMaxTemp2(double maxTemp2) {
        this.maxTemp2 = maxTemp2;
    }

    public String getEngineRunTime() {
        return engineRunTime;
    }

    public void setEngineRunTime(String engineRunTime) {
        this.engineRunTime = engineRunTime;
    }

    public double getMaxRPM() {
        return maxRPM;
    }

    public void setMaxRPM(double maxRPM) {
        this.maxRPM = maxRPM;
    }

    public double getMaxTPS() {
        return maxTPS;
    }

    public void setMaxTPS(double maxTPS) {
        this.maxTPS = maxTPS;
    }

    public double getMaxECT() {
        return maxECT;
    }

    public void setMaxECT(double maxECT) {
        this.maxECT = maxECT;
    }

    public double getSeconds() {
        return seconds;
    }

    public void setSeconds(double seconds) {
        this.seconds = seconds;
    }

    public double getPw1() {
        return pw1;
    }

    public void setPw1(double pw1) {
        this.pw1 = pw1;
    }

    public double getPw2() {
        return pw2;
    }

    public void setPw2(double pw2) {
        this.pw2 = pw2;
    }

    public double getAdv_deg() {
        return adv_deg;
    }

    public void setAdv_deg(double adv_deg) {
        this.adv_deg = adv_deg;
    }

    public int getSquirt() {
        return squirt;
    }

    public void setSquirt(int squirt) {
        this.squirt = squirt;
    }

    public String getEngineState() {
        return engineState;
    }

    public void setEngineState(String engineState) {
        this.engineState = engineState;
    }

    public double getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(int runningTime) {
        this.runningTime = runningTime;
    }

    public float getTempClndr1() {
        return tempClndr1;
    }

    public void setTempClndr1(float tempClndr1) {
        this.tempClndr1 = tempClndr1;
    }

    public float getTempClndr2() {
        return tempClndr2;
    }

    public void setTempClndr2(float tempClndr2) {
        this.tempClndr2 = tempClndr2;
    }

    public int getMAX6675CalibFactor() {
        return MAX6675CalibFactor;
    }

    public void setMAX6675CalibFactor(int MAX6675CalibFactor) {
        this.MAX6675CalibFactor = MAX6675CalibFactor;
    }
//public  void setFuelVolBuffer(double)

    void setPlaneCaliFactor() {
        String type = Params.getInstance().pref.get("plane", "");
        switch (type) {
            case "SMD":
                MAX6675CalibFactor = 10;
                break;
            case "SML":
                MAX6675CalibFactor = 11;
                break;
            default:
                MAX6675CalibFactor = 0;
        }
    }

    public double getCHTRight() {
        return CHTRight;
    }

    //Calib factor changes only for MAX6675
    public void setCHTRight(double CHTRight) {

        if (CHTRight > 500) CHTRight = 500;
        this.CHTRight = CHTRight - MAX6675CalibFactor;

        if (this.CHTRight > tempThreshold) {
            double fix100error = this.CHTRight - factor100;
            this.CHTRight = fix100error;
        }
    }

    public void setFuelBuffer(double fuelValue) {

        if (fuelFreqCounter < fuelBuffer.length) {
            fuelBuffer[fuelFreqCounter] = fuelValue;
            fuelFreqCounter++;
        } else fuelFreqCounter = 0;

        if (fuelVolCounter < 5) {
            fuelVolBuffer[fuelVolCounter] = fuelValue;
            fuelVolCounter++;
        } else fuelVolCounter = 0;
    }

    public double getFuelVolume() {
        double sum = 0.0;
        int count = 0;
        for (double v : fuelVolBuffer)
            if (v > 0.0) {
                sum += v;
                count++;
            }
        fuelVolConsumed = sum / count;
        return fuelVolConsumed;
    }

    public double getFuelBufferAvg() {
        double sum = 0.0;
        int count = 0;
        int length = fuelBuffer.length;
        if (length > 0) {
            for (double fuelVal : fuelBuffer)
                if (fuelVal > 0.0) {
                    sum += fuelVal;
                    count++;
                }
        }
        return sum / count;
    }

    public double getCHTLeft() {
        return CHTLeft;
    }

    public void setCHTLeft(double CHTLeft) {
        if (CHTLeft > 500) CHTLeft = 500;
        this.CHTLeft = CHTLeft - MAX6675CalibFactor;

        if (this.CHTLeft > tempThreshold) {
            double fix100error = this.CHTLeft - factor100;
            this.CHTLeft = fix100error;
        }
    }

    public double getMaxCHTLeft() {
        return maxCHTLeft;
    }

    public void setMaxCHTLeft(double maxCHTLeft) {
        this.maxCHTLeft = maxCHTLeft;
    }

    public double getMaxCHTRight() {
        return maxCHTRight;
    }

    public void setMaxCHTRight(double maxCHTRight) {
        this.maxCHTRight = maxCHTRight;
    }

    public double getMaxIAT() {
        return maxIAT;
    }

    public void setMaxIAT(double maxIAT) {
        this.maxIAT = maxIAT;
    }

    public double getMaxPres() {
        return maxPres;
    }

    public void setMaxPres(double maxPres) {
        this.maxPres = maxPres;
    }

    public double getMaxFuelCons() {
        return maxFuelCons;
    }

    public void setMaxFuelCons(double maxFuelCons) {
        this.maxFuelCons = maxFuelCons;
    }

    public boolean isBtnfire() {
        return btnfire;
    }

    public void setBtnfire(boolean btnfire) {
        this.btnfire = btnfire;
    }

    public boolean isBtnReset() {
        return btnReset;
    }

    public void setBtnReset(boolean btnReset) {
        this.btnReset = btnReset;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    //AC Laga huwa hai
    public int getBtnModeFire() {
        return btnModeFire;
    }

    public void setBtnModeFire(int btnModeFire) {
        this.btnModeFire = btnModeFire;
    }

    public int getBtnFire() {
        return btnFire;
    }

    public void setBtnFire(int btnFire) {
        this.btnFire = btnFire;
    }

    public int getBtnSelection() {
        return btnSelection;
    }

    public void setBtnSelection(int btnSelection) {
        this.btnSelection = btnSelection;
    }

    public double getValueX() {
        return valueX;
    }

    public void setValueX(double valueX) {
        this.valueX = valueX;
    }

    public double getValueY() {
        return valueY;
    }

    public void setValueY(double valueY) {
        this.valueY = valueY;
    }

    public double getValueZ() {
        return valueZ;
    }

    public void setValueZ(double valueZ) {
        this.valueZ = valueZ;
    }

    public synchronized byte[] getSendingPack() {
        updateSendingPack();
        return sendingPack;
    }

    public byte[] getReceivingPack() {
        return receivingPack;
    }

    public void setReceivingPack(byte[] receivingPack) {
        this.receivingPack = receivingPack;
    }

    public byte[] getReceivingPackSMD() {
        return receivingPackSMD;
    }

    public byte[] getReceivingPackDLA() {
        return receivingPackDLA;
    }

    public byte[] getReceivingPackDLAArduino() {
        return receivingPackDLAArduino;
    }

    public void updateSendingPack() {
        sendingPack[0] = Params.getInstance().handShakeByte;
    }

    // let me show you
    public float getFuelLevel1() {
        return fuelLevel1;
    }

    public void setFuelLevel1(float fuelLevel1) {
        if (fuelLevel1 > 100.0) this.fuelLevel1 = 100;
        else this.fuelLevel1 = fuelLevel1;
    }

    public float getFuelLevel2() {
        return fuelLevel2;
    }

    public void setFuelLevel2(float fuelLevel2) {
        if (fuelLevel2 > 100.0) this.fuelLevel2 = 100;
        else this.fuelLevel2 = fuelLevel2;
    }

    public double getRPM() {
        return this.RPM;
    }

    public void setRPM(double RPM) {
        this.RPM = RPM * 10.0;
        if (this.RPM < 250) this.RPM = RPM * 10.0;
        // System.out.println("RPM :"+RPM);
    }

    public double getAdvanceAngle() {
        return advanceAngle;
    }

    public void setAdvanceAngle(double advanceAngle) {
        this.advanceAngle = advanceAngle;
    }

    public int getTPS() {
        return TPS;
    }

    public void setTPS(int TPS) {
        this.TPS = TPS;
    }

    public int getECT() {
        return ECT;
    }

    public void setECT(int ECT) {
        this.ECT = ECT;
    }

    public double getIAT() {
        return IAT;
    }

    public void setIAT(int IAT) {
        this.IAT = IAT;
    }

    public double getBAROPressure() {
        return BAROPressure;
    }

    public void setBAROPressure(double BARO) {
        this.BAROPressure = BAROPressure;
    }

    public double getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
        //setFuelBuffer(fuelConsumption);

    }

    public double getFuelperHour() {
        return fuelperHour;
    }

    public void setFuelperHour(double fuelperHour) {
        this.fuelperHour = fuelperHour;
    }

    public double getAfrtgt1() {
        return afrtgt1;
    }

    public void setAfrtgt1(int afrtgt1) {
        this.afrtgt1 = afrtgt1;
    }

    public int getAfrtgt2() {
        return afrtgt2;
    }

    public void setAfrtgt2(int afrtgt2) {
        this.afrtgt2 = afrtgt2;
    }

    public int getWbo2_en1() {
        return wbo2_en1;
    }

    public void setWbo2_en1(int wbo2_en1) {
        this.wbo2_en1 = wbo2_en1;
    }

    public int getWbo2_en2() {
        return wbo2_en2;
    }

    public void setWbo2_en2(int wbo2_en2) {
        this.wbo2_en2 = wbo2_en2;
    }

    public double getBaro() {
        return baro;
    }

    public void setBaro(double baro) {
        this.baro = baro;
    }

    public double getMap() {
        return map;
    }

    public void setMap(double map) {
        this.map = map;
    }

    public double getMat() {
        return mat;
    }

    public void setMat(double mat) {
        this.mat = mat;
    }

    public double getClt() {
        return clt;
    }

    public void setClt(double clt) {
        this.clt = clt;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    public double getBatt() {
        return batt;
    }

    public void setBatt(double batt) {
        this.batt = batt;
    }

    public float getVol1() {
        return vol1;
    }

    public void setVol1(float vol1) {
        this.vol1 = vol1;
    }

    public float getVol2() {
        return vol2;
    }

    public void setVol2(float vol2) {
        this.vol2 = vol2;
    }

    public float getVol3() {
        return vol3;
    }

    public void setVol3(float vol3) {
        this.vol3 = vol3;
    }

    public float getVol4() {
        return vol4;
    }

    public void setVol4(float vol4) {
        this.vol4 = vol4;
    }

    public float getVol5() {
        return vol5;
    }

    public void setVol5(float vol5) {
        this.vol5 = vol5;
    }

    public byte getEngineStatusFlag() {
        return engineStatusFlag;
    }

    public void setEngineStatusFlag(byte engineStatusFlag) {
        this.engineStatusFlag = engineStatusFlag;
    }



//    void extractSample() {
//        extractDLAPack(bufferSample);
//        BinaryGates();
//    }

    void BinaryGates() {
        int p = 13, q = 60;
        int r = 0;
        System.out.println("OR Gate");
        r = p | q;
        printGates(r);
        System.out.println("AND Gate");
        r = p & q;
        printGates(r);
        System.out.println("Shift Operater");
        byte a = 5;
        byte g = (byte) (a >> 1);
        printGates(r);
    }

    void printGates(int r) {
        System.out.println("Decimal System:  " + Integer.toString(r, 10));
        System.out.println("Octal System:  " + Integer.toString(r, 8));
        System.out.println("HEX System:  " + Integer.toString(r, 16));
        System.out.println("Binary System:  " + Integer.toString(r, 2));
    }


    public void extractDLAPack2(byte[] buffer) {
        int crcValue = CRC_Calculator.crcCalByByte(buffer, buffer.length - 4);


        // Read CRC from bytes 124-125 (check which byte order: big-endian or little-endian)
        // Try little-endian first (most common):
        int receivedCRC = ((buffer[123] & 0xFF) << 8) | (buffer[124] & 0xFF);

        //System.out.printf("Computed CRC: 0x%04X%n", crcValue);
        ;
        //System.out.println("Received CRC: 0x" + Integer.toHexString(receivedCRC));
        if (crcValue == receivedCRC) {
            RPM = (buffer[16] << 8 & 0xff00) | (buffer[15] & 0x00ff);
            byte[] dataBuffer = {buffer[94], buffer[93], buffer[92], buffer[91]};
            fuelConsumed = ByteBuffer.wrap(dataBuffer).order(ByteOrder.BIG_ENDIAN).getFloat();
            InjectorPulse = (buffer[12] << 8 & 0xff00) | (buffer[11] & 0x00ff);
            advanceAngle = (buffer[18] << 8 & 0xff00) | (buffer[17] & 0x00ff);
            fuelperHour = (buffer[108] << 8 & 0xff00) | (buffer[107] & 0x00ff);                         //Real time fuel Consumption
            engineStatusFlag = buffer[89];
            //
            // Combine into an integer (Big-endian)
            byte[] dataBufferTime = {buffer[122], buffer[121], buffer[120], buffer[119]};
            double runningTimeFloat = ByteBuffer.wrap(dataBufferTime).order(ByteOrder.BIG_ENDIAN).getFloat();

            Params.getInstance().setEngineRunTime(extractRnningTime(runningTimeFloat));
            //runningTime = (byte4) | (byte3) | (byte2) | (byte1);
            tempClndr1 = (buffer[96] << 8 & 0xff00) | (buffer[95] & 0x00ff);
            tempClndr2 = (buffer[102] << 8 & 0xff00) | (buffer[101] & 0x00ff);
            IAT = (buffer[30] << 8 & 0xff00) | (buffer[29] & 0x00ff);
            BAROPressure = (buffer[26] << 8 & 0xff00) | (buffer[25] & 0x00ff);
            vol1 = (buffer[36] << 8 & 0xff00) | (buffer[35] & 0x00ff);
            TPS = (buffer[34] << 8 & 0xff00) | (buffer[33] & 0x00ff);

            tempClndr1 = (tempClndr1 / 10);
            tempClndr2 = (tempClndr2 / 10);
            //BARO = BARO /10;
            Params.getInstance().setEngineState(getEngineState(engineStatusFlag));
            //float floatFuel= Float.intBitsToFloat(fuelConsumed);
            Params.getInstance().setInjectorPulse(InjectorPulse / 1000);
            Params.getInstance().setAdvanceAngle(advanceAngle / 10);
            Params.getInstance().setFuelConsumed((fuelConsumed) / 1E9);
            Params.getInstance().setFuelperHour(fuelperHour / 10);           //if value is 39 it means 3.9L/H
            if ((Params.getInstance().getMaxFuelCons()) < (Params.getInstance().getFuelperHour())) {
                Params.getInstance().setMaxFuelCons(Params.getInstance().getFuelperHour());
            }
            Params.getInstance().setEngineStatusFlag(engineStatusFlag);
            Params.getInstance().setTempClndr1(((tempClndr1) - 32) * 5 / 9);
            if (Params.getInstance().getMaxTemp1() < Params.getInstance().getTempClndr1()) {
                Params.getInstance().setMaxTemp1(Params.getInstance().getTempClndr1());
            }
            Params.getInstance().setTempClndr2(((tempClndr2) - 32) * 5 / 9);
            if (Params.getInstance().getMaxTemp2() < Params.getInstance().getTempClndr2()) {
                Params.getInstance().setMaxTemp2(Params.getInstance().getTempClndr2());
            }
            //System.out.println("Running Time: " + extractRnningTime(runningTime) +" raw seconds: "+runningTime + "Engin Flag "+engineStatusFlag);// " RPM-" + RPM + "-" + buffer[7] + "TPS- " + TPS + "Tempclndr1- " + tempClndr1 + "Tempclndr2- " + tempClndr2 + "IAT- " + IAT + "BARO- " + BAROPressure + "FuelConsm- " + fuelConsumption +" L/H:  " + fuelperHour+ " Battery- " + vol1 );

            //System.out.printf("Engine Byte: 0x%02x  (%d) \n",buffer[20],buffer[20]);
            Params.getInstance().setRPM(RPM / 10);
            if ((Params.getInstance().getMaxRPM()) < (Params.getInstance().getRPM())) {
                Params.getInstance().setMaxRPM(Params.getInstance().getRPM());
            }
            Params.getInstance().setTPS(TPS / 10);
            if (Params.getInstance().getMaxTPS() < Params.getInstance().getTPS()) {
                Params.getInstance().setMaxTPS(Params.getInstance().getTPS());
            }
            Params.getInstance().setECT(((ECT / 10) - 32) * 5 / 9);
            if (Params.getInstance().getMaxECT() < Params.getInstance().getECT()) {
                Params.getInstance().setMaxECT(Params.getInstance().getECT());
            }
            Params.getInstance().setIAT(((IAT / 10) - 32) * 5 / 9);
            if (Params.getInstance().getMaxIAT() < Params.getInstance().getIAT()) {
                Params.getInstance().setMaxIAT(Params.getInstance().getIAT());
            }
            Params.getInstance().setBAROPressure((BAROPressure - 32) * 5 / 9);
            Params.getInstance().setVol1(vol1 / 10);


            ArdiunoPort.loogger.onEvent();
        }
    }
    String extractRnningTime(double runningTimeMs) {
// Convert milliseconds to seconds
        long totalSeconds = (long) runningTimeMs;
        long hours = (totalSeconds / 3600);
        long remainingSeconds = (totalSeconds % 3600);
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        // if (hours>10) hours=0
        //System.out.println("Running Seconds: "+totalSeconds);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);

    }
    String getEngineState(byte flag) {
        String state = null;
        switch (flag) {
            case 0x01:
                state = "Running";
                break;
            case 0x02:
                state = "Drive Statrtup";
                break;
            case 0x04:
                state = "Enable Compensation";
                break;
            case 0x08:
                state = "Warm Up";
                break;
            case 0x10:
                state = "Acceleration";
                break;
            case 0x20:
                state = "Deceleration";
                break;
            case 0x40:
                state = "Oil Pump Working";
                break;
            case (byte) 0x80:
                state = "Engine Flameout";
                break;
            default:
                state = "Cold State";

        }

        return state;
    }




    void extractDLAviaArduino(byte[] buffer) {
        seconds = (buffer[2] << 8 & 0xff00) | (buffer[1] & 0x00ff);
        RPM = (buffer[4] << 8 & 0xff00) | (buffer[3] & 0x00ff);
        // BARO = (buffer[6] << 8 & 0xff00) | (buffer[5] & 0x00ff);
        IAT = (buffer[8] << 8 & 0xff00) | (buffer[7] & 0x00ff);
        ECT = (buffer[10] << 8 & 0xff00) | (buffer[9] & 0x00ff);
        TPS = (buffer[12] << 8 & 0xff00) | (buffer[11] & 0x00ff);
        batt = (buffer[14] << 8 & 0xff00) | (buffer[13] & 0x00ff);
        //engineState = (byte) (buffer[15] & 0xff);
        kFactorCalc = (buffer[16] & 0xff) * 0.0559;

        //System.out.println("RPM = "+RPM*10);
        //System.out.println("FuelCons = "+fuelConsumption);
        //fuelConsumption = fuelConsumption/100;
        fuelConsumption = (float) kFactorCalc;
        Params.getInstance().setFuelConsumption(fuelConsumption);
        Params.getInstance().setFuelperHour(getFuelBufferAvg());
        //if(fuelConsumption > 0.0) {

        if ((Params.getInstance().getMaxFuelCons()) < (Params.getInstance().getFuelConsumption())) {
            Params.getInstance().setMaxFuelCons(Params.getInstance().getFuelConsumption());
        }
        //
        // Params.getInstance().setRPM(runningTime);
        System.out.println("TimeStamp:" + seconds + " RPM-" + RPM + "-" + " TPS- " + TPS + " ECT- " + ECT + " IAT- " + IAT + " BARO- " + BAROPressure + " FuelConsm- " + fuelConsumption);// + "Battery- " + vol3 + "Fuel1- " + fuelLevel1 + "Fuel2- " + fuelLevel2 + "Consm:  " + fuelConsumption);
        Params.getInstance().setRPM(RPM / 10);
        if ((Params.getInstance().getMaxRPM()) < (Params.getInstance().getRPM())) {
            Params.getInstance().setMaxRPM(Params.getInstance().getRPM());
        }
        Params.getInstance().setTPS(TPS / 10);
        if (Params.getInstance().getMaxTPS() < Params.getInstance().getTPS()) {
            Params.getInstance().setMaxTPS(Params.getInstance().getTPS());
        }
        Params.getInstance().setECT(((ECT / 10) - 32) * 5 / 9);
        if (Params.getInstance().getMaxECT() < Params.getInstance().getECT()) {
            Params.getInstance().setMaxECT(Params.getInstance().getECT());
        }
        Params.getInstance().setIAT(((IAT / 10) - 32) * 5 / 9);
        if (Params.getInstance().getMaxIAT() < Params.getInstance().getIAT()) {
            Params.getInstance().setMaxIAT(Params.getInstance().getIAT());
        }
        //Params.getInstance().setBARO(BARO/10);
        Params.getInstance().setVol1((float) batt / 10);

    }



    float getfloat(byte[] bytes) {
        float f = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        return f;
    }

    int calcCRC(byte[] buffer) {
        int crc = 0;
        for (int i = 3; i <= 34; i++) {
            crc = crc ^ buffer[i];
        }
        return crc;
    }

    int calcXOR(byte[] buffer) {
        int crc = buffer[3] ^ buffer[4] ^ buffer[5] ^ buffer[6] ^ buffer[7] ^ buffer[8] ^ buffer[9] ^ buffer[10] ^ buffer[11] ^ buffer[12]
                ^ buffer[13] ^ buffer[14] ^ buffer[15] ^ buffer[16] ^ buffer[17] ^ buffer[18] ^ buffer[19] ^ buffer[20] ^ buffer[21] ^ buffer[22]
                ^ buffer[23] ^ buffer[24] ^ buffer[25] ^ buffer[26] ^ buffer[27] ^ buffer[28] ^ buffer[29] ^ buffer[30] ^ buffer[31] ^ buffer[32]
                ^ buffer[33] ^ buffer[34] ^ buffer[35];
        return crc;
    }

}
