package server;

public interface ServerCommand {
    boolean run(String msg, ConnectionHandler conn);
}
