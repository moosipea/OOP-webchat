package common.networking.packets;

import java.util.Arrays;
import java.util.List;

/**
 * Seda packetit ei tohiks üle võrgu saata, see lihtsalt mähib mingi
 * listi packeteid, mis peab kindlasti koos saatma.
 */
public class PackagedPacket extends AbstractPacket {
    private final List<? extends AbstractPacket> packets;

    public PackagedPacket(List<? extends AbstractPacket> packets) {
        this.packets = packets;
    }

    public List<? extends AbstractPacket> getPackets() {
        return packets;
    }
}
