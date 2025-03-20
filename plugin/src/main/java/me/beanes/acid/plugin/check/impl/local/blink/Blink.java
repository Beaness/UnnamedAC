package me.beanes.acid.plugin.check.impl.local.blink;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.beanes.acid.plugin.check.model.LocalCheck;
import me.beanes.acid.plugin.check.model.PreReceivePacketCheck;
import me.beanes.acid.plugin.check.model.ReceivePacketCheck;
import me.beanes.acid.plugin.player.PlayerData;

// Not a true check in the way of detecting blink
// This just prevents blink by setting back anyone that lags longer than the leniency provided
public class Blink extends LocalCheck implements PreReceivePacketCheck {
    public Blink(PlayerData data) {
        super(data, "Blink");
    }


    private long lastSync = System.currentTimeMillis();

    @Override
    public void onPacketPreReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (System.currentTimeMillis() - lastSync > 2000) {
                debug("lastSync=" + lastSync);
                data.getSetbackTracker().setback(); // Setback
            }

            lastSync = System.currentTimeMillis();
        }
    }
}
