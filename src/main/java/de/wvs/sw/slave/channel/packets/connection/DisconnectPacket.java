package de.wvs.sw.slave.channel.packets.connection;

import com.google.gson.Gson;
import de.wvs.sw.shared.application.SWSlave;
import de.wvs.sw.slave.channel.Packet;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
public class DisconnectPacket extends Packet {

    private static final Gson gson = new Gson();

    public DisconnectPacket(SWSlave slave) {
        super("slave");

        this.data.put("connection", "disconnect");
        this.data.put("slave", gson.toJson(slave));
    }
}
