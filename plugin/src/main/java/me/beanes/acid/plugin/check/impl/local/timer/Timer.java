package me.beanes.acid.plugin.check.impl.local.timer;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.beanes.acid.plugin.check.model.LocalCheck;
import me.beanes.acid.plugin.check.model.PreReceivePacketCheck;
import me.beanes.acid.plugin.player.PlayerData;
import org.bson.Document;


public class Timer extends LocalCheck implements PreReceivePacketCheck {
    private static final int CATCHUP_TICKS = 10;
    private static final long TICK_MS = 50L;
    private static final long DRIFT_LENIENCY = 10;
    private long clientTime = 0;

    public Timer(PlayerData data) {
        super(data, "Timer");
    }

    @Override
    public void onPacketPreReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (data.getPositionTracker().isTeleport()) {
                return;
            }

            // Lower bound is the last synced timestamp and the maximum amount of ticks the client can lag for
            long maxCatchupTime = CATCHUP_TICKS * TICK_MS;
            long lowerBound = Math.max(data.getTransactionTracker().getPingTimePassed() - maxCatchupTime, 0L);
            long upperBound = System.currentTimeMillis() - data.getTransactionTracker().getLoginTime();
            this.clientTime = Math.max(this.clientTime + TICK_MS, lowerBound);

            long timeOver = this.clientTime - upperBound; // Time over the upper bound

            // If the client runs faster than our server time
            if (timeOver > DRIFT_LENIENCY) {
                debug("time0ver=" + timeOver + " upperBound=" + upperBound + " lowerBound=" + lowerBound);

                event.setCancelled(true); // Do not allow timer ticks :P
                log(new Document()
                    .append("timeOver", timeOver)
                    .append("clientTime", clientTime)
                    .append("upperBound", upperBound)
                );

                data.getUser().closeConnection();
                neutralAlert("Timer");
            }
        }
    }
}
