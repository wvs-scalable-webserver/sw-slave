package de.wvs.sw.slave.command.impl;

import de.wvs.sw.slave.Slave;
import de.wvs.sw.slave.command.Command;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
public class EndCommand extends Command {

    public EndCommand(String name, String description, String... aliases) {

        super(name, description, aliases);
    }

    @Override
    public boolean execute(String[] args) {

        Slave.getInstance().stop();

        return true;
    }
}
