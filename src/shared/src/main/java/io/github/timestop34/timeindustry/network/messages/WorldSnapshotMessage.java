package io.github.timestop34.timeindustry.network.messages;

import io.github.timestop34.timeindustry.network.WorldSnapshot;

public class WorldSnapshotMessage extends BaseMessage {
    public WorldSnapshot snapshot;
    public WorldSnapshotMessage() { type = "snapshot"; }
}