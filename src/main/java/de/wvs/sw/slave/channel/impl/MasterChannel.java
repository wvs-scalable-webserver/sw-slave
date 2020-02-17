package de.wvs.sw.slave.channel.impl;

import de.progme.thor.client.sub.impl.handler.annotation.Channel;
import de.progme.thor.client.sub.impl.handler.annotation.Key;
import de.progme.thor.client.sub.impl.handler.annotation.Value;
import de.wvs.sw.shared.application.SWSlave;
import de.wvs.sw.slave.Slave;
import de.wvs.sw.slave.channel.packets.connection.ConnectPacket;
import de.wvs.sw.slave.channel.packets.connection.DisconnectPacket;
import org.json.JSONObject;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
@Channel("master")
public class MasterChannel {

    @Key("connection")
    @Value("reconnect")
    public void onConnectionReconnect(JSONObject data) {

        SWSlave slave = Slave.getInstance().getSlave();

        Slave.getInstance().getChannelManager().send(new DisconnectPacket(slave));
        Slave.getInstance().getChannelManager().send(new ConnectPacket(slave));
    }
}
