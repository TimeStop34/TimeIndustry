package io.github.timestop34.timeindustry.network;

public interface NetworkListener {
    void onMessageReceived(String url, String message);
    void onPlayerDisconnected(String playerId);
}
