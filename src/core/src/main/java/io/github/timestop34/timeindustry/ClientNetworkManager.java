package io.github.timestop34.timeindustry;

import com.github.czyzby.websocket.CommonWebSockets;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;
import io.github.timestop34.timeindustry.network.NetworkListener;
import io.github.timestop34.timeindustry.network.NetworkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ClientNetworkManager implements NetworkManager {
    private final WebSocket socket;
    private NetworkListener listener;
    private static final Logger logger = LoggerFactory.getLogger(ClientNetworkManager.class);

    static {
        CommonWebSockets.initiate();
    }

    public ClientNetworkManager(String uri) {
        socket = WebSockets.newSocket(uri);
        socket.addListener(new WebSocketListener() {
            @Override
            public boolean onOpen(WebSocket webSocket) {
                logger.debug("Connected to server");
                return true;
            }

            @Override
            public boolean onClose(WebSocket webSocket, int code, String reason) {
                logger.debug("Disconnected: {}", reason);
                return true;
            }

            @Override
            public boolean onMessage(WebSocket webSocket, String message) {
                if (listener != null) {
                    listener.onMessageReceived(webSocket.getUrl(), message);
                }
                return true;
            }

            @Override
            public boolean onMessage(WebSocket webSocket, byte[] bytes) {
                return false;
            }

            @Override
            public boolean onError(WebSocket webSocket, Throwable error) {
                logger.error(Arrays.toString(error.getStackTrace()));
                return true;
            }
        });
        socket.connect();
    }

    @Override
    public void sendMessage(String message) {
        socket.send(message);
    }

    @Override
    public void setListener(NetworkListener listener) {
        this.listener = listener;
    }

    @Override
    public void close() {
        socket.close();
    }
}
