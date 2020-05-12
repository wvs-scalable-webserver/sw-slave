package de.wvs.sw.slave;

import ch.qos.logback.classic.Level;
import com.google.gson.Gson;
import de.progme.hermes.client.HermesClient;
import de.progme.hermes.client.HermesClientFactory;
import de.progme.iris.IrisConfig;
import de.progme.iris.config.Header;
import de.progme.iris.config.Key;
import de.progme.thor.client.pub.Publisher;
import de.progme.thor.client.pub.PublisherFactory;
import de.progme.thor.client.sub.Subscriber;
import de.progme.thor.client.sub.SubscriberFactory;
import de.wvs.sw.shared.application.SWSlave;
import de.wvs.sw.slave.application.ApplicationManager;
import de.wvs.sw.slave.channel.ChannelManager;
import de.wvs.sw.slave.channel.packets.connection.ConnectPacket;
import de.wvs.sw.slave.channel.packets.connection.DisconnectPacket;
import de.wvs.sw.slave.command.Command;
import de.wvs.sw.slave.command.CommandManager;
import de.wvs.sw.slave.command.impl.DebugCommand;
import de.wvs.sw.slave.command.impl.EndCommand;
import de.wvs.sw.slave.command.impl.HelpCommand;
import de.wvs.sw.slave.command.impl.StatsCommand;
import de.wvs.sw.slave.rest.RestServer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

/**
 * Created by Marvin Erkes on 05.02.20.
 */
public class Slave {

    @Getter
    public static Slave instance;

    private static final String SLAVE_PACKAGE_NAME = "de.wvs.sw.slave";
    private static final Pattern ARGS_PATTERN = Pattern.compile(" ");
    private static Logger logger = LoggerFactory.getLogger(Slave.class);
    private static ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SLAVE_PACKAGE_NAME);

    @Getter
    private IrisConfig config;

    @Getter
    private ScheduledExecutorService scheduledExecutorService;

    @Getter
    private SWSlave slave;

    @Getter
    private CommandManager commandManager;

    private Scanner scanner;

    @Getter
    private HermesClient restClient;

    @Getter
    private Publisher publisher;
    @Getter
    private Subscriber subscriber;
    @Getter
    private ChannelManager channelManager;

    private RestServer restServer;

    @Getter
    private ApplicationManager applicationManager;

    public Slave(IrisConfig config) {

        Slave.instance = this;

        this.config = config;
    }

    public void start() {

        this.scheduledExecutorService = Executors.newScheduledThreadPool(1000);

        String host = this.config.getHeader("networking").getKey("host").getValue(0).asString();
        Key portRange = this.config.getHeader("networking").getKey("portRange");
        this.slave = new SWSlave(host, portRange.getValue(0).asInt(), portRange.getValue(1).asInt());

        this.commandManager = new CommandManager();
        commandManager.addCommand(new HelpCommand("help", "List of available commands", "h"));
        commandManager.addCommand(new EndCommand("end", "Stops the load balancer", "stop", "exit"));
        commandManager.addCommand(new DebugCommand("debug", "Turns the debug mode on/off", "d"));
        commandManager.addCommand(new StatsCommand("stats", "Shows live stats", "s"));

        this.initializeRestClient();

        this.startThor();
        this.startCommunication();

        this.startRestServer();

        this.channelManager.send(new ConnectPacket(this.slave));
        this.channelManager.initializeHeartbeat();

        this.applicationManager = new ApplicationManager();
    }

    public void stop() {

        logger.info("Slave is going to be stopped");

        // Close the scanner
        scanner.close();

        this.applicationManager.stop();

        this.channelManager.send(new DisconnectPacket(this.slave));

        this.stopRestServer();

        this.stopThor();

        this.scheduledExecutorService.shutdown();

        logger.info("Slave has been stopped");
    }

    private void initializeRestClient() {
        this.restClient = HermesClientFactory.create(this.config.getHeader("general").getKey("master").getValue(0).asString());
    }

    private void startRestServer() {
        restServer = new RestServer(this.config);
        restServer.start();
    }

    private void stopRestServer() {
        try {
            this.restServer.stop();
        } catch (Exception e) {
            logger.debug("RESTful API server already stopped");
        }
    }

    private void startThor() {
        Header config = this.config.getHeader("thor");
        Key hostKey = config.getKey("host");
        String host = hostKey.getValue(0).asString();
        int port = hostKey.getValue(1).asInt();
        this.publisher = PublisherFactory.create(host, port);
        this.subscriber = SubscriberFactory.create(host, port, "slave-" + this.slave.getUuid().toString());

        logger.debug("Thor started");
    }

    private void startCommunication() {
        this.channelManager = new ChannelManager();
        this.channelManager.subscribe();
    }

    private void stopThor() {

        this.publisher.disconnect(true);
        this.subscriber.disconnect(true);
    }

    public void console() {

        scanner = new Scanner(System.in);

        try {
            String line;
            while ((line = scanner.nextLine()) != null) {
                if (!line.isEmpty()) {
                    String[] split = ARGS_PATTERN.split(line);

                    if (split.length == 0) {
                        continue;
                    }

                    // Get the command name
                    String commandName = split[0].toLowerCase();

                    // Try to get the command with the name
                    Command command = commandManager.findCommand(commandName);

                    if (command != null) {
                        logger.info("Executing command: {}", line);

                        String[] cmdArgs = Arrays.copyOfRange(split, 1, split.length);
                        command.execute(cmdArgs);
                    } else {
                        logger.info("Command not found!");
                    }
                }
            }
        } catch (IllegalStateException ignore) {}
    }

    public void changeDebug(Level level) {

        // Set the log level to debug or info based on the config value
        rootLogger.setLevel(level);

        logger.info("Logger level is now {}", rootLogger.getLevel());
    }

    public void changeDebug() {

        // Change the log level based on the current level
        changeDebug((rootLogger.getLevel() == Level.INFO) ? Level.DEBUG : Level.INFO);
    }
}
