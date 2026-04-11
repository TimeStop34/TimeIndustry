package io.github.timestop34.timeindustry.server;

import io.github.timestop34.timeindustry.network.NetworkListener;
import io.github.timestop34.timeindustry.network.NetworkManager;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class ServerNetworkManager implements NetworkManager {
    private final WebSocketServer server;
    private NetworkListener listener;
    private static final Logger logger = LoggerFactory.getLogger(ServerNetworkManager.class);

    public ServerNetworkManager(int port) {
        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                logger.debug("New client connected: {}", conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                if (listener != null) {
                    String clientId = conn.getRemoteSocketAddress().toString();
                    listener.onMessageReceived(clientId, message);
                }
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
                if (listener != null) {
                    String clientId = conn.getRemoteSocketAddress().toString();
                    listener.onPlayerDisconnected(clientId);
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                logger.error(Arrays.toString(ex.getStackTrace()));
            }

            @Override
            public void onStart() {
                logger.info("Server started on port: {}", getPort());
            }
        };
        server.start();
    }

    @Override
    public void sendMessage(String message) {
        // Рассылаем сообщение всем подключенным клиентам
        server.broadcast(message);
    }

    @Override
    public void setListener(NetworkListener listener) {
        this.listener = listener;
    }

    @Override
    public void close() throws InterruptedException {
        server.stop();
    }
}
