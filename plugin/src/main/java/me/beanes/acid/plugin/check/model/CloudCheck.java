package me.beanes.acid.plugin.check.model;

import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.cloud.packet.Packet;
import me.beanes.acid.plugin.player.PlayerData;

public abstract class CloudCheck extends AbstractCheck {
    protected CloudCheck(PlayerData data, String name) {
        super(data, name);
    }

    protected void sendToCloud(Packet packet) {
        Acid.get().getCloudManager().sendPacket(packet);
    }
}
