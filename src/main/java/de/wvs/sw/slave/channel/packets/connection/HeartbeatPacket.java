package de.wvs.sw.slave.channel.packets.connection;

import com.google.gson.Gson;
import de.wvs.sw.shared.application.SWSlave;
import de.wvs.sw.slave.channel.Packet;

/**
 * Created by Marvin Erkes on 17.02.20.
 */
public class HeartbeatPacket extends Packet {

    private static final Gson gson = new Gson();

    public HeartbeatPacket(SWSlave slave) {
        super("slave");

        this.data.put("connection", "heartbeat");
        this.data.put("slave", gson.toJson(slave));
    }
}
