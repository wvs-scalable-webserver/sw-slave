package de.wvs.sw.slave.application;

import de.progme.hermes.shared.http.Headers;
import de.progme.iris.Iris;
import de.progme.iris.IrisConfig;
import de.progme.iris.config.Header;
import de.progme.iris.config.Key;
import de.progme.iris.config.Value;
import de.progme.iris.exception.IrisException;
import de.wvs.sw.shared.application.Application;
import de.wvs.sw.shared.application.Deployment;
import de.wvs.sw.slave.Slave;
import de.wvs.sw.slave.channel.packets.application.StatusPacket;
import net.lingala.zip4j.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Marvin Erkes on 20.04.20.
 */
public class ApplicationManager {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationManager.class);

    private List<Deployment> deployments;

    public ApplicationManager() {
        this.deployments = new ArrayList<>();

        deleteDirectory(new File("./deployments"));
    }

    public void launchApplication(Deployment deployment) {
        this.deployments.add(deployment);

        try {
            Application application = deployment.getApplicationRef();
            logger.info("Deploying application {}.", application.getName());
            File applicationDirectoryFile = new File("./deployments/" + application.getId() + "/" + deployment.getUuid());

            if (applicationDirectoryFile.exists()) {
                applicationDirectoryFile.delete();
            }

            applicationDirectoryFile.mkdirs();
            applicationDirectoryFile.deleteOnExit();

            File applicationConfigFile = new File(applicationDirectoryFile, "application-config.iris");
            applicationConfigFile.createNewFile();
            Key thorHostKey = Slave.getInstance().getConfig().getHeader("thor").getKey("host");
            IrisConfig applicationConfig = Iris.from(applicationConfigFile)
                    .def(new Header("general"), new Key("deploymentUuid"), new Value(deployment.getUuid()))
                    .def(new Header("networking"), new Key("host"), new Value(deployment.getHost()))
                    .def(new Header("networking"), new Key("port"), new Value(String.valueOf(deployment.getPort())))
                    .def(new Header("thor"), new Key("host"), thorHostKey.getValue(0), thorHostKey.getValue(1))
                    .build();
            applicationConfig.save();

            Slave.getInstance().getRestClient().download("/application/" + application.getId() + "/bundles/" + application.getBundle(), Headers.empty(), applicationDirectoryFile.getAbsolutePath());
            ZipFile bundle = new ZipFile(applicationDirectoryFile + "/" + application.getBundle() + ".zip");
            bundle.extractAll(applicationDirectoryFile.getAbsolutePath());
            bundle.getFile().delete();

            File indexHtmlFile = new File(applicationDirectoryFile, "www/index.html");
            FileWriter indexHtmlWriter = new FileWriter(indexHtmlFile);
            indexHtmlWriter.write("<script>console.log(\"" + deployment.getHost() + ":" + deployment.getPort() +"\");</script>");
            indexHtmlWriter.close();

            Process execution = new ProcessBuilder()
                    .command(application.getCommand().split(" "))
                    .directory(applicationDirectoryFile)
                    .start();
            deployment.setProcess(execution);

            this.listenForDeploymentExit(deployment);
        } catch(Exception error) {
            logger.error("Error while launching deployment!", error);
            this.onDeploymentExit(deployment);
        }
    }

    private void listenForDeploymentExit(Deployment deployment) {
        try {
            deployment.getProcess().exitValue();
            this.onDeploymentExit(deployment);
        } catch (IllegalThreadStateException error) {
            Slave.getInstance().getScheduledExecutorService().execute(() -> {
                try {
                    deployment.getProcess().waitFor();
                    this.onDeploymentExit(deployment);
                } catch (InterruptedException e) {
                    deployment.getProcess().destroy();
                    this.onDeploymentExit(deployment);
                }
            });
        }
    }

    private void onDeploymentExit(Deployment deployment) {
        deployment.setStatus(Deployment.Status.TERMINATED);
        Slave.getInstance().getChannelManager().send(new StatusPacket(deployment.getUuid(), deployment.getStatus()));

        this.deployments = this.deployments
                .stream()
                .filter(d -> !d.getUuid().equals(deployment.getUuid()))
                .collect(Collectors.toList());
    }

    public void stop() {

        deleteDirectory(new File("./deployments"));

        for (Deployment deployment : this.deployments) {
            deployment.getProcess().destroy();
        }
    }

    private boolean deleteDirectory(File deleteDirectory) {
        File[] allContents = deleteDirectory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return deleteDirectory.delete();
    }
}
