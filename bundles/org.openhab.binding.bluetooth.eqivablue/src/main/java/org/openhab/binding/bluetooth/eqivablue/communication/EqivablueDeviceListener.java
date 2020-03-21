package org.openhab.binding.bluetooth.eqivablue.communication;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.bluetooth.eqivablue.internal.EncodedReceiveMessage;

public interface EqivablueDeviceListener {

    void notifyReceivedSignalStrength(int rssi);

    void notifyConnectionEstablished();

    void notifyConnectionClosed();

    void notifyCharacteristicWritten();

    void notifyCharacteristicUpdate(@NonNull EncodedReceiveMessage message);

}