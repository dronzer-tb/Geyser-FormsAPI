package org.formsapi.tcp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.formsapi.FormsAPIExtension;
import org.formsapi.forms.FormBuilder;
import org.formsapi.protocol.FormRequest;
import org.geysermc.cumulus.form.Form;
import org.geysermc.geyser.session.GeyserSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP Server that listens for form requests from Spigot plugins.
 */
public class TcpFormServer {

    private final FormsAPIExtension extension;
    private final Gson gson;
    private final ExecutorService executor;
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public TcpFormServer(FormsAPIExtension extension, int port) {
        this.extension = extension;
        this.port = port;
        this.gson = new GsonBuilder().create();
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Start the TCP server.
     */
    public void start() {
        executor.submit(() -> {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                extension.logger().info("TCP Form Server started on port " + port);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        executor.submit(() -> handleClient(clientSocket));
                    } catch (IOException e) {
                        if (running) {
                            extension.logger().warning("Error accepting connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                extension.logger().error("Failed to start TCP server on port " + port + ": " + e.getMessage());
            }
        });
    }

    /**
     * Handle a client connection.
     */
    private void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                extension.logger().info("Received form request: " + line);

                try {
                    FormRequest request = gson.fromJson(line, FormRequest.class);
                    processFormRequest(request);
                } catch (Exception e) {
                    extension.logger().warning("Failed to parse form request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            extension.logger().warning("Client connection error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Process a form request and send the form to the player.
     */
    private void processFormRequest(FormRequest request) {
        if (request.getPlayerUuid() == null) {
            extension.logger().warning("Form request missing player UUID");
            return;
        }

        UUID playerUuid;
        try {
            playerUuid = UUID.fromString(request.getPlayerUuid());
        } catch (IllegalArgumentException e) {
            extension.logger().warning("Invalid player UUID: " + request.getPlayerUuid());
            return;
        }

        // Find the player's session
        GeyserSession session = extension.getChannelHandler().findSession(playerUuid);
        if (session == null) {
            extension.logger().warning("Player session not found: " + request.getPlayerUuid());
            return;
        }

        // Build the form with response handling
        Form form = FormBuilder.buildForm(request, response -> {
            extension.logger().info("Form response: " + gson.toJson(response));

            // Execute command based on response
            if (response.isSuccess() && "modal".equalsIgnoreCase(request.getFormType())) {
                if (response.isConfirmed() && request.getCommandAccept() != null) {
                    String command = request.getCommandAccept();
                    session.sendCommand(command);
                    extension.logger().info("Executed accept command: " + command);
                } else if (!response.isConfirmed() && request.getCommandDeny() != null) {
                    String command = request.getCommandDeny();
                    session.sendCommand(command);
                    extension.logger().info("Executed deny command: " + command);
                }
            }
        }, session);

        // Send the form
        session.sendForm(form);
        extension.logger().info("Sent form to player: " + session.javaUuid());
    }

    /**
     * Stop the TCP server.
     */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
        executor.shutdownNow();
        extension.logger().info("TCP Form Server stopped");
    }
}
