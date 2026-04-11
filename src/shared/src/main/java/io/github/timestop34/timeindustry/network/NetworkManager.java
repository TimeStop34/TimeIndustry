package io.github.timestop34.timeindustry.network;

public interface NetworkManager {
    void sendMessage(String message);
    void setListener(NetworkListener listener);
    void close() throws InterruptedException;
}
