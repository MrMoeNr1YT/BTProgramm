package application;
	
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.io.OutputStream;

public class BluetoothConnection extends Application {

    private StreamConnection bluetoothConnection = null;
    private OutputStream outputStream = null;

    @Override
    public void start(Stage stage) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        Button connectButton = new Button("Connect Bluetooth");
        {
            TextField dataTextField = new TextField();
            GridPane.setHalignment(connectButton, HPos.RIGHT);
            gridPane.add(dataTextField, 3, 3);
            gridPane.add(connectButton, 3, 4);

            connectButton.setOnAction(actionEvent -> {
                String connectionName = dataTextField.getText();
                if (connectionName != null && connectionName.length() > 0) {
                    try {
                        LocalDevice device = null;
                        device = LocalDevice.getLocalDevice();
                        RemoteDevice[] remoteDevices = device.getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);

                        for (RemoteDevice remoteDevice : remoteDevices) {
                            String name = remoteDevice.getFriendlyName(true);
                            String address = remoteDevice.getBluetoothAddress();
                            if (name.equals(connectionName)) {
                                System.out.println("Device Name: " + name);
                                System.out.println("Bluetooth Address: " + address);
                                int channel = 1;
                                StreamConnection streamConnection = openConnection(remoteDevice, channel);
                                if (streamConnection != null) {
                                    System.out.println("Verbindung mit dem Gerät " + name + " hergestellt");
                                    connectButton.setStyle("-fx-background-color: green");
                                    bluetoothConnection = streamConnection;
                                } else {
                                    System.out.println("Verbindung mit dem Gerät " + name + " konnte nicht hergestellt werden");
                                    connectButton.setStyle("-fx-background-color: red");
                                }
                            }
                        }
                    } catch (BluetoothStateException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        {
            Button sendButton = new Button("SendData");
            TextField dataTextField = new TextField();
            GridPane.setHalignment(sendButton, HPos.RIGHT);
            gridPane.add(dataTextField, 3, 5);
            gridPane.add(sendButton, 3, 6);
            sendButton.setOnAction(event -> {
                String data = dataTextField.getText();
                if (data != null && data.length() > 0) {
                    if (bluetoothConnection != null) {
                        sendData(data);
                        System.out.println("Die Daten wurden erfolgreich gesendet");
                        System.out.println("Gesendete Daten: " + data);
                        sendButton.setStyle("-fx-background-color: green");
                        dataTextField.setText("");
                    } else {
                        System.out.println("Keine Bluetooth Verbindung gefunden!");
                        sendButton.setStyle("-fx-background-color: red");
                    }
                } else {
                    System.out.println("Überarbeite die Daten, die gesendet werden sollen");
                    sendButton.setStyle("-fx-background-color: red");
                }
            });
        }

        {
            Button closeConnectionButton = new Button("Close Connection");
            gridPane.add(closeConnectionButton, 3, 8);
            GridPane.setHalignment(closeConnectionButton, HPos.RIGHT);
            closeConnectionButton.setOnAction(event -> {
                if (this.bluetoothConnection != null) {
                    closeConnection();
                    connectButton.setStyle("-fx-background-color: gray");
                    System.out.println("Die Verbindung mit dem Bluetooth wurde unterbrochen");
                } else {
                    System.out.println("Es wurde keine Bluetooth Verbindung gefunden!");
                }
            });
        }
        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.setWidth(500);
        stage.setHeight(500);
        stage.show();
    }

    private void sendData(String data) {
        try {
            OutputStream outputStream = (this.outputStream == null ? this.bluetoothConnection.openOutputStream() : this.outputStream);
            this.outputStream = outputStream;
            byte[] dataBytes = data.getBytes();
            outputStream.write(dataBytes);
            outputStream.flush();
            System.out.println("Daten erfolgreich gesendet: " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (this.bluetoothConnection != null) {
            try {
                this.bluetoothConnection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.outputStream = null;
        this.bluetoothConnection = null;
    }


    private StreamConnection openConnection(RemoteDevice remoteDevice, int channel) {
        StreamConnection connection = null;
        try {
            String connectionString = "btspp://" + remoteDevice.getBluetoothAddress() + ":" + channel;
            connection = (StreamConnection) Connector.open(connectionString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }


    public static void main(String[] args) {
        launch();
    }
}