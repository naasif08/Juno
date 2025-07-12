package juno.device;

import juno.serial.JunoSerialThreaded;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents a JUNO-managed ESP32 device.
 * Handles connection (serial/OTA), firmware updates, and command sending.
 */
public class JunoDevice {

    private final DeviceInfo deviceInfo;

    private JunoSerialThreaded serialConnection;
    private OTAUploader otaUploader;

    public JunoDevice(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void connectSerial() throws IOException {
        if (serialConnection != null && serialConnection.isConnected()) {
            return;
        }
        serialConnection = new JunoSerialThreaded(deviceInfo.getSerialPort());
        serialConnection.open();
    }

    public void disconnectSerial() {
        if (serialConnection != null) {
            serialConnection.close();
            serialConnection = null;
        }
    }

    public Optional<String> sendSerialCommand(String command) throws IOException {
        if (serialConnection == null || !serialConnection.isConnected()) {
            throw new IOException("Serial connection is not open");
        }
        serialConnection.write(command);
        return serialConnection.readResponse(); // depends on your JunoSerialThreaded design
    }

    public void uploadFirmwareOTA(Path firmwareBinPath) throws IOException {
        if (otaUploader == null) {
            otaUploader = new OTAUploader(deviceInfo);
        }
        otaUploader.upload(firmwareBinPath);
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
}
