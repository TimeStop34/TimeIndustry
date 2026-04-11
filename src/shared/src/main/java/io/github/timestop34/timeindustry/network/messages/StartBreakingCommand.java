package io.github.timestop34.timeindustry.network.messages;

public class StartBreakingCommand extends BaseMessage {
    public int x, y;
    public StartBreakingCommand() { type = "start_breaking"; }
}
