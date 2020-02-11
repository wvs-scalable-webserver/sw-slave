package de.wvs.sw.slave;

import de.progme.iris.IrisConfig;

/**
 * Created by Marvin Erkes on 05.02.20.
 */
public class SlaveFactory {

    public SlaveFactory() {}

    public static Slave create(IrisConfig config) {

        return new Slave(config);
    }
}
