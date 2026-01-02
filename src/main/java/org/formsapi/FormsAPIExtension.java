package org.formsapi;

import org.formsapi.channel.FormChannelHandler;
import org.formsapi.config.FormsConfig;
import org.formsapi.tcp.TcpFormServer;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.bedrock.SessionDisconnectEvent;
import org.geysermc.geyser.api.event.java.ServerDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserShutdownEvent;
import org.geysermc.geyser.api.extension.Extension;

/**
 * Forms API - A Geyser Extension that provides a reliable way to send
 * Bedrock forms to players via TCP connection.
 *
 * This extension listens for form requests from Spigot/Paper plugins
 * via TCP and uses session.sendForm() directly for reliable form delivery.
 */
public class FormsAPIExtension implements Extension {

    private static FormsAPIExtension INSTANCE;
    private FormsConfig config;
    private FormChannelHandler channelHandler;
    private TcpFormServer tcpServer;

    public FormsAPIExtension() {
        INSTANCE = this;
    }

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        logger().info("FormsAPI Pre-Initializing...");
    }

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        logger().info("=== FormsAPI Extension Starting ===");

        // Load configuration
        this.config = new FormsConfig(this);

        // Initialize the channel handler (for session tracking)
        this.channelHandler = new FormChannelHandler(this);

        // Start TCP server for receiving form requests
        this.tcpServer = new TcpFormServer(this, config.getPort());
        this.tcpServer.start();

        logger().info("FormsAPI ready! Listening for form requests on TCP port " + config.getPort());
    }

    @Subscribe
    public void onShutdown(GeyserShutdownEvent event) {
        if (tcpServer != null) {
            tcpServer.stop();
        }
        logger().info("FormsAPI shutdown complete");
    }

    /**
     * Track player sessions when they fully login (have Java UUID).
     */
    @Subscribe
    public void onServerDefineCommands(ServerDefineCommandsEvent event) {
        channelHandler.trackPlayer(event.connection());
    }

    /**
     * Remove player sessions when they disconnect.
     */
    @Subscribe
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        channelHandler.untrackPlayer(event.connection());
    }

    public static FormsAPIExtension getInstance() {
        return INSTANCE;
    }

    public FormChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public FormsConfig getConfig() {
        return config;
    }
}
