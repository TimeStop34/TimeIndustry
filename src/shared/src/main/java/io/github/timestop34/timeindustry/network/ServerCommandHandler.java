package io.github.timestop34.timeindustry.network;

import io.github.timestop34.timeindustry.network.messages.BaseMessage;

public interface ServerCommandHandler {
    void handleCommand(Object playerId, BaseMessage command, String rawJson);
}