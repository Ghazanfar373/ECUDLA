# DLA EFI Engine Control System

## Overview

This project is a Java-based system designed to interface with, monitor, and control a DLA (Direct Lean Actuation or similar) Electronic Fuel Injection (EFI) engine system. It features a JavaFX Graphical User Interface (GUI) for real-time data visualization and control. The system facilitates data acquisition from the Engine Control Unit (ECU), allows sending commands (like throttle adjustments, engine start/stop), parses various data packet formats, and logs critical engine parameters for analysis. Communication with the EFI unit is handled via a serial connection, potentially using an Arduino or a similar microcontroller as a bridge.

## Core Components

The system is built around several key Java classes:

*   **`Main.java`**: The main entry point of the application, providing a JavaFX-based Graphical User Interface (GUI). It is responsible for:
    *   Displaying real-time engine parameters using various gauges (RPM, TPS, CHT, IAT, Fuel Consumption, Battery Voltages, etc.) and text fields.
    *   Providing interactive controls (buttons, sliders) for:
        *   Connecting to the EFI system via a selected COM port.
        *   Selecting plane type for calibration.
        *   Resetting maximum recorded values.
        *   Accessing application settings (stubbed) and an "About" dialog.
        *   Sending engine commands (Start, Shutdown - via `ArdiunoPort`).
        *   Controlling throttle position via a slider.
    *   Using an `AnimationTimer` to periodically refresh UI elements with the latest data from `Params.getInstance()`.
    *   Structuring the UI using JavaFX layout panes (BorderPane, HBox, VBox) and a ToolBar.
    *   Loading image resources for icons and logos.

*   **`ArdiunoPort.java`**: Manages the serial communication (using the `jSerialComm` library) with the external EFI hardware. It handles:
    *   Establishing and managing the COM port connection.
    *   Sending command packets to the EFI (e.g., throttle control, engine start/stop, initialization).
    *   Receiving data packets from the EFI in various formats.
    *   Orchestrating data reading and command sending threads.
    *   Integrating with `CRC_Calculator` for command packet integrity.
    *   Triggering the `DataLogger` to record received data.

*   **`Params.java`**: Acts as a central singleton repository for all engine parameters. Its responsibilities include:
    *   Storing a wide range of real-time engine data (RPM, TPS, temperatures, pressures, fuel consumption, engine status, etc.).
    *   Parsing and interpreting incoming data packets from different protocols (DLA native, DLA via Arduino, SMD devices).
    *   Performing unit conversions and applying calibration factors to raw sensor data.
    *   Tracking maximum observed values for critical parameters.
    *   Managing persistent settings using `java.util.prefs.Preferences`.

*   **`DataLogger.java`**: Provides functionality to log real-time engine telemetry to CSV (Comma Separated Values) files.
    *   Creates timestamped log files in a dedicated directory (`Documents/SERB_ECU_LOGS`).
    *   Records parameters like RPM, Throttle, Cylinder Temperatures, IAT, Fuel Consumed, and Fuel per Hour.
    *   Can be started and stopped, and logs data when its `onEvent()` method is triggered.

*   **`CRC_Calculator.java`**: A utility class that implements CRC16_CCITT_FALSE (Cyclic Redundancy Check).
    *   Used by `ArdiunoPort` to calculate checksums for command packets sent to the EFI, ensuring data integrity during transmission.

## Key Features

*   **Graphical User Interface**: JavaFX-based UI for intuitive monitoring and control.
*   **Real-time Engine Monitoring**: Acquires and processes live data from the DLA EFI system, displayed dynamically on the GUI.
*   **Serial Communication**: Interfaces with the EFI hardware via a serial (COM) port, likely through an Arduino.
*   **Multi-Protocol Data Parsing**: Capable of understanding and extracting data from several distinct packet formats.
*   **Engine Control Commands**:
    *   Throttle control via a GUI slider.
    *   Engine start and shutdown commands initiated from GUI buttons.
    *   EFI initialization/handshake.
*   **Comprehensive Parameter Tracking**: Manages a wide array of engine metrics, visualized on the GUI and stored in `Params`.
*   **Data Calibration**: Applies necessary conversions and calibration factors to sensor readings.
*   **Data Logging**: Saves time-series engine data to CSV files for later analysis, triggered by data updates.
*   **Data Integrity**: Utilizes CRC checks for command transmission.
*   **Persistent Settings**: Remembers certain configurations (e.g., plane type for CHT calibration) using Java Preferences.

## How it Works (High-Level Flow)

1.  **Application Start**: The `Main.java` class launches the JavaFX application, initializing the GUI components.
2.  **Connection**:
    *   The user interacts with the GUI (e.g., "Connect" button in `Main.java`).
    *   A dialog prompts for COM port selection.
    *   `Main.java` calls `ArdiunoPort.getInstance().connectPort()` to establish the serial connection.
3.  **Initialization**: If connection is successful, `ArdiunoPort` may perform an initialization/handshake with the EFI unit (e.g., `initDLA()`). Data reading and command handling threads are started by `ArdiunoPort` (e.g., `startFeedbackThreadDLA()`).
4.  **Data Exchange & UI Update**:
    *   **Receiving Data**: `ArdiunoPort` continuously listens for incoming data packets.
    *   **Sending Commands**: The user can send commands via GUI elements in `Main.java` (e.g., throttle slider calls `ArdiunoPort.getInstance().sendThrottle()`, start/stop buttons call respective methods in `ArdiunoPort`). These commands often include a CRC.
    *   **Data Processing**: Received raw byte arrays are passed to `Params.getInstance()`. `Params` parses the data, updates its internal state.
    *   **UI Refresh**: The `AnimationTimer` in `Main.java` periodically calls getter methods on `Params.getInstance()` to retrieve the latest engine data and updates the corresponding gauges and text fields on the GUI.
5.  **Data Logging**: After new data is processed by `Params`, `DataLogger.getInstance().onEvent()` is called (e.g., from `ArdiunoPort` or `Params`'s parsing methods) to write the current state to a CSV file.
6.  **User Interaction**: The user can interact with other GUI elements, such as selecting a plane type (which updates `Params` preferences and calibration factors) or resetting max values.

## Setup & Prerequisites

*   **Java Development Kit (JDK)**: Version 8 or higher (due to JavaFX).
*   **jSerialComm Library**: For serial port communication.
*   **JavaFX SDK**: Ensure JavaFX modules are available to your JDK/JRE. If using JDK 11+, JavaFX is typically a separate SDK.
*   **Medusa & TilesFX Libraries**: Used for the gauges and tiles in the JavaFX UI.
    *   `eu.hansolo.medusa`
    *   `eu.hansolo.tilesfx`
*   **JFoenix Library**: Used for some UI components (e.g., `JFXButton`).
*   **Hardware**:
    *   A DLA EFI engine system.
    *   An Arduino or similar microcontroller board programmed to interface between the PC and the DLA EFI via serial.
    *   Appropriate serial drivers for the communication device.
*   **Configuration**: The correct COM port for the Arduino/EFI interface must be identified by the user and selected in the GUI.

## Building the Project

The presence of a `.gitignore` file configured for Gradle suggests this project is intended to be built using Gradle.
To build the project (assuming a standard Gradle setup):

1.  Ensure you have Gradle installed.
2.  Ensure your `build.gradle` file includes dependencies for `jSerialComm`, `JavaFX` (controls, graphics, fxml), `Medusa`, `TilesFX`, and `JFoenix`.
3.  Navigate to the project's root directory in your terminal.
4.  Run the command: `gradlew build` (or `gradle build`).

This will compile the source code and create necessary build artifacts. To run, you might use `gradlew run` if configured, or execute the main class from the built JAR, ensuring JavaFX modules are accessible.

## Running the Application

The main entry point is `com.org.serb.ecudla.Main`. Execute this class to launch the GUI.

## Code Structure

*   **Package**: `com.org.serb.ecudla`
*   **Main UI Class**:
    *   `Main.java`: JavaFX application, GUI layout, event handling, and data display.
*   **Core Logic Classes**:
    *   `ArdiunoPort.java`: Handles all serial communication logic.
    *   `Params.java`: Singleton for data storage, parsing, and calibration.
    *   `DataLogger.java`: Manages CSV file logging.
    *   `CRC_Calculator.java`: Provides CRC calculation utilities.

## Areas for Improvement / Future Enhancements

*   **Configuration Management**: Externalize settings like default COM port, baud rates, calibration factors, and logging parameters into a configuration file.
*   **Error Handling and Resilience**: Enhance error handling for communication failures, packet parsing errors, file I/O issues, and GUI updates. Display user-friendly error messages.
*   **Modularity**:
    *   The `Params` class is quite large; specific packet parsing logic could be refactored.
    *   `ArdiunoPort` handles many responsibilities; consider separating thread management or command construction.
    *   `Main.java` could benefit from FXML for UI definition to separate view from controller logic, though it's currently built programmatically.
*   **Logging Framework**: Replace `System.out.println` with a dedicated logging framework (e.g., SLF4j with Logback/Log4j2) for better log management.
*   **Unit and Integration Testing**: Develop comprehensive tests.
*   **Settings Window**: Fully implement the settings window functionality (currently stubbed in `Main.java`).
*   **Code Comments and Documentation**: Add more detailed Javadoc comments and inline comments, especially for complex UI interactions or data processing logic in `Main.java`.
*   **Resource Management**: Ensure all resources (files, serial ports) are properly closed, especially in `stop()` methods and error scenarios. The `Main.java` `stop()` method handles port closing.
*   **Thread Safety**: Review interactions between the JavaFX application thread and background threads (e.g., serial reader in `ArdiunoPort`) to ensure UI updates are always done on the JavaFX Application Thread (e.g., using `Platform.runLater()`). `AnimationTimer` handles this for its updates.
