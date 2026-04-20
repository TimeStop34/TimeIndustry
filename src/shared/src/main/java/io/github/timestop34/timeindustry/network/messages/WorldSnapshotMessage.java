package io.github.timestop34.timeindustry.network.messages;

import io.github.timestop34.timeindustry.network.WorldStateSnapshot;

public class WorldSnapshotMessage extends BaseMessage {
    public WorldStateSnapshot snapshot;
    public WorldSnapshotMessage() { type = "snapshot"; }
}