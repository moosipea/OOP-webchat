package server.commands;

import server.ConnectionHandler;
import server.ServerCommand;

public class MotdCommand implements ServerCommand {
    @Override
    public boolean run(String msg, ConnectionHandler conn) {
        if (!msg.startsWith("/motd")) {
            return false;
        }
        System.out.println("motd!");
        return true;
    }
}
