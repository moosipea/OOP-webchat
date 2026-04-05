package server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Haldab ühe kliendi ühendust serveriga.
 */
public class ConnectionHandler implements Runnable {
    // Sõnumite järjekord.
    private final LinkedBlockingQueue<String> localMessageEvents = new LinkedBlockingQueue<>();

    // Viit kõikide aktiivsete ühenduste hulgale (vajame seda registreerimiseks).
    private final CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers;

    // Socket, mille kaudu suhtlus kliendiga käib.
    private final Socket clientSocket;

    public ConnectionHandler(CopyOnWriteArraySet<ConnectionHandler> allConnectionHandlers, Socket clientSocket) {
        this.allConnectionHandlers = allConnectionHandlers;
        this.clientSocket = clientSocket;
    }

    /**
     * Käivitab ühenduse kliendiga.
     */
    @Override
    public void run() {
        // Registreerime oma ühenduse.
        register();

        // TODO: kogu selles asjas on vaja tagada, et see thread viisakalt
        //  ennast ära tapab siis, kui klient ühenduse katkestab.
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            // Sisse tulevate sõnumite kuulamine eraldi lõimes.
            Thread receiver = Thread.ofVirtual().start(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String line = in.readLine(); // Blokib
                        if (line != null) {
                            // Edastame kõigile, sh endale
                            broadcastMessage(line, false);
                        }
                    } catch (IOException ignored) {
                        break;
                    }
                }
            });

            // Siia võib panna mingi muu welcome sõnumi või mis iganes peab
            // toimuma siis, kui klient ühendab.
            queueClientMessage("Welcome to the test server!");

            // Sõnumeid *saadetakse* selles lõimes.
            while (!Thread.currentThread().isInterrupted()) {
                String message = localMessageEvents.take(); // Blokib
                out.println(message);
                out.flush(); // TODO: tegelt pole hea iga kord flushida, aga see tekitas varem mingeid probleeme
            }

            receiver.interrupt();
        } catch (IOException ignored) {
            // TODO: log exception, but don't crash!
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            unregister();
        }
    }

    /**
     * Lisab selle ühenduse aktiivsete ühenduste hulka.
     */
    private void register() {
        allConnectionHandlers.add(this);
    }

    /**
     * Eemaldab selle ühenduse aktiivsete ühenduste hulgast.
     */
    private void unregister() {
        allConnectionHandlers.remove(this);
    }

    /**
     * Lisab sõnumi selle ühenduse sõnumite järjekorda.
     *
     * @param message sõnum sõnena
     */
    // Varem oli see synchronized, aga nüüd vist pole tarvis?
    private void queueClientMessage(String message) {
        localMessageEvents.add(message);
    }

    /**
     * Edastab antud sõnumi kõigile teistele ühendatud klientidele.
     *
     * @param message    sõnum sõnena
     * @param ignoreSelf kas on tarvis ka saatjale tagasi saata?
     */
    private void broadcastMessage(String message, boolean ignoreSelf) {
        for (ConnectionHandler conn : allConnectionHandlers) {
            if (ignoreSelf && conn == this) {
                continue;
            }
            conn.queueClientMessage(message);
        }
    }
}
