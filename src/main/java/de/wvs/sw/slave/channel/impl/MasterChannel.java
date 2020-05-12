package de.wvs.sw.slave.channel.impl;

import com.google.gson.Gson;
import de.progme.iris.exception.IrisException;
import de.progme.thor.client.sub.impl.handler.annotation.Channel;
import de.progme.thor.client.sub.impl.handler.annotation.Key;
import de.progme.thor.client.sub.impl.handler.annotation.Value;
import de.wvs.sw.shared.application.Deployment;
import de.wvs.sw.shared.application.SWSlave;
import de.wvs.sw.slave.Slave;
import de.wvs.sw.slave.channel.packets.connection.ConnectPacket;
import de.wvs.sw.slave.channel.packets.connection.DisconnectPacket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
@Channel("master")
public class MasterChannel {

    private static Logger logger = LoggerFactory.getLogger(MasterChannel.class);

    private static Gson gson = new Gson();

    @Key("connection")
    @Value("reconnect")
    public void onConnectionReconnect(JSONObject data) {

        SWSlave slave = Slave.getInstance().getSlave();

        Slave.getInstance().getChannelManager().send(new ConnectPacket(slave));
    }

    @Key("application")
    @Value("deploy")
    public void onApplicationDeploy(JSONObject data) {

        Deployment deployment = gson.fromJson(data.getString("deployment"), Deployment.class);
        Slave.getInstance().getApplicationManager().launchApplication(deployment);
    }
}
