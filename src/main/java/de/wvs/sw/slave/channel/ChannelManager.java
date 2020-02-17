package de.wvs.sw.slave.channel;

import de.progme.thor.client.pub.Publisher;
import de.progme.thor.client.sub.Subscriber;
import de.wvs.sw.slave.Slave;
import de.wvs.sw.slave.channel.impl.MasterChannel;
import de.wvs.sw.slave.channel.packets.connection.HeartbeatPacket;

import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
public class ChannelManager {

    private Subscriber subscriber;
    private Publisher publisher;

    public ChannelManager() {

        Slave slave = Slave.getInstance();
        this.subscriber = slave.getSubscriber();
        this.publisher = slave.getPublisher();
    }

    public void subscribe() {

        this.subscriber.subscribeMulti(MasterChannel.class);
    }

    public void send(Packet packet) {

        this.publisher.publish(packet.getChannel(), packet.getData());
    }

    public void initializeHeartbeat() {
        Slave.getInstance().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            this.send(new HeartbeatPacket(Slave.getInstance().getSlave()));
        }, 0, 1, TimeUnit.SECONDS);
    }
}
