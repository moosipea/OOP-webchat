import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PirukaServer {
    // Logimine lähtudes sellest vahvast juhendist: https://www.baeldung.com/java-logging-intro
    private static final Logger logger = LogManager.getLogger(PirukaServer.class);

    public static void main(String[] args) {
        logger.info("Test message!");
    }
}
