package org.formsapi.channel;

import org.formsapi.FormsAPIExtension;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.session.GeyserSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player session tracking.
 */
public class FormChannelHandler {

    private final FormsAPIExtension extension;

    // Cache of connected Bedrock players by their Java UUID
    private final Map<UUID, GeyserSession> playerSessions = new ConcurrentHashMap<>();

    public FormChannelHandler(FormsAPIExtension extension) {
        this.extension = extension;
        extension.logger().info("FormChannelHandler initialized");
    }

    /**
     * Find a Geyser session by Java UUID.
     */
    public GeyserSession findSession(UUID javaUuid) {
        // First check cache
        GeyserSession session = playerSessions.get(javaUuid);
        if (session != null) {
            return session;
        }

        // Try to find by iterating connections
        for (GeyserConnection connection : extension.geyserApi().onlineConnections()) {
            if (connection.javaUuid() != null && connection.javaUuid().equals(javaUuid)) {
                return (GeyserSession) connection;
            }
        }
        return null;
    }

    /**
     * Track a player session when they join.
     */
    public void trackPlayer(GeyserConnection connection) {
        if (connection.javaUuid() != null) {
            playerSessions.put(connection.javaUuid(), (GeyserSession) connection);
            extension.logger().info("Tracking player session: " + connection.javaUuid());
        }
    }

    /**
     * Remove a player session when they disconnect.
     */
    public void untrackPlayer(GeyserConnection connection) {
        if (connection.javaUuid() != null) {
            playerSessions.remove(connection.javaUuid());
            extension.logger().info("Removed player session: " + connection.javaUuid());
        }
    }
}
