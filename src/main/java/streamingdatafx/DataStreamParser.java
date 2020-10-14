package streamingdatafx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataStreamParser {

    public static final int DEFAULT_PORT = 5432;

    private int port = DEFAULT_PORT;

    private final ExecutorService exec;
    private final Logger logger;
    private ServerSocket serverSocket;
    private final DoubleProperty speed = new SimpleDoubleProperty(this, "speed", 0);
    private final StringProperty message = new SimpleStringProperty(this, "message");

    public final DoubleProperty speedProperty() {
        return this.speed;
    }

    public final StringProperty messageProperty() {
        return this.message;
    }

    public final double getSpeed() {
        return this.speedProperty().get();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public final void setSpeed(final double speed) {
        this.speedProperty().set(speed);
    }

    public DataStreamParser(int port) throws IOException {
        this.port = port;

        this.exec = Executors.newCachedThreadPool(runnable -> {
            // run thread as daemon:
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });

        this.logger = Logger.getLogger("DataStreamParser");

        try {
            startListening();
        } catch (IOException exc) {
            exc.printStackTrace();
            throw exc;
        }
    }

    public DataStreamParser() throws IOException {
        this(DEFAULT_PORT);
    }

    public void startListening() throws IOException {
        Callable<Void> connectionListener = () -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                setServerSocket(serverSocket);
                logger.info(
                        "Server listening on " + serverSocket.getInetAddress()
                                + ":" + serverSocket.getLocalPort());
                message.set("Server listening on " + serverSocket.getInetAddress()
                        + ":" + serverSocket.getLocalPort());
                while (true) {
                    logger.info("Waiting for connection:");
                    Socket socket = serverSocket.accept();
                    logger.info("Connection accepted from " + socket.getInetAddress());
                    message.set("Incoming connection from " + socket.getInetAddress());
                    handleConnection(socket);
                }
            } catch (BindException e) {
                message.set("Fatal error: port in use");
            } catch (SocketException e) {
                message.set("Server stopped on port " + port);
            } catch (Exception exc) {
                logger.log(Level.SEVERE, "Exception in connection handler", exc);
                message.set("Fatal error: " + exc.getMessage());
            }

            return null;
        };
        exec.submit(connectionListener);
    }

    public void shutdown() {
        exec.shutdownNow();
    }

    private void handleConnection(Socket socket) {
        Callable<Void> connectionHandler = () -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    logger.info("Received: " + line);
                    processLine(line);
                }
                System.out.println("Connection closed from " + socket.getInetAddress());
                message.set("Connection closed from " + socket.getInetAddress());
            }
            return null;
        };
        exec.submit(connectionHandler);
    }

    private void processLine(String line) {
        message.set(line);
        String[] tokens = line.split("=");
        if ("speed".equals(tokens[0]) && tokens.length == 2) {
            try {
                speed.set(Double.parseDouble(tokens[1]));
            } catch (NumberFormatException exc) {
                logger.log(Level.WARNING, "Non-numeric speed supplied", exc);
            }
        }
    }

}
