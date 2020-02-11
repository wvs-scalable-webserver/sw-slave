package de.wvs.sw.slave.rest;

import de.progme.hermes.server.impl.HermesConfig;
import de.wvs.sw.slave.rest.resource.SlaveResource;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
public class SlaveRestConfig extends HermesConfig {

    public SlaveRestConfig(String host, int port) {

        host(host);
        port(port);
        corePoolSize(2);
        maxPoolSize(4);
        backLog(50);
        register(SlaveResource.class);
    }
}
