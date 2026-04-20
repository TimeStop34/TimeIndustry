package io.github.timestop34.timeindustry.network.messages;

public class StartBuildingCommand extends BaseMessage {
    public String blockId;
    public int x, y;
    public StartBuildingCommand() { type = "start_building"; }
}