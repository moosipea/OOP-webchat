
import java.io.IOException;
import java.net.Socket;

public class ConnectionClientTest{
    public static void main(String[] args) throws IOException {
        Socket sock = new Socket("localhost", 6000);
        Connection conn = new Connection(sock);
        conn.createEndpoint("message");
        conn.send("message", "hello world!!!!!".getBytes());
    }
}