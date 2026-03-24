
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionServerTest{
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket sock = new ServerSocket(6000);
        Socket connSock = sock.accept();
        Connection conn = new Connection(connSock);
        conn.subscribe("message", m -> {System.out.println(new String(m));});
        
        conn.run();
        //Thread serverThread = new Thread(conn);
        //serverThread.start();
        //serverThread.join();
    }
}