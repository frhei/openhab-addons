package org.openhab.binding.bluetooth.eqivablue.communication;

public class WaitingForDisconnectState extends OnlineState {

    public WaitingForDisconnectState(DeviceHandler theHandler) {
        super(theHandler);
    }

    @Override
    protected void onEntry() {
        deviceHandler.requestDisconnect();
    }

}
