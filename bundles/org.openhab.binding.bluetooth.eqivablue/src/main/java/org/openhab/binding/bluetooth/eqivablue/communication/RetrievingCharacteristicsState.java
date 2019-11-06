package org.openhab.binding.bluetooth.eqivablue.communication;

public class RetrievingCharacteristicsState extends OfflineState {

    public RetrievingCharacteristicsState(DeviceHandler theHandler) {
        super(theHandler);
    }

    @Override
    protected void onEntry() {
        if (deviceHandler.getCharacteristics()) {
            if (!deviceHandler.getCommandHandler().areCommandsPending()) {
                deviceHandler.setState(WaitingForDisconnectState.class);
            } else {
                deviceHandler.setState(TransmitCommandState.class);
            }
        } else {
            deviceHandler.setState(FailureState.class);
        }
    }

}
