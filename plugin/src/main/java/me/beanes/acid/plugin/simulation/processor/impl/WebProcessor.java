package me.beanes.acid.plugin.simulation.processor.impl;

import lombok.Getter;
import lombok.Setter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.MotionArea;
import me.beanes.acid.plugin.simulation.processor.AreaProcessor;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class WebProcessor extends AreaProcessor {
    public WebProcessor(PlayerData data) {
        super(data);
    }

    @Getter @Setter
    private SplitStateBoolean state = SplitStateBoolean.FALSE;

    public void processArea(MotionArea area) {
        if (state == SplitStateBoolean.TRUE) {
            area.minX *= 0.25D;
            area.maxX *= 0.25D;
            area.minY *= 0.05000000074505806D;
            area.maxY *= 0.05000000074505806D;
            area.minZ *= 0.25D;
            area.maxZ *= 0.25D;
        } else if (state == SplitStateBoolean.POSSIBLE) {
            area.allowX(area.minX * 0.25D);
            area.allowX(area.maxX * 0.25D);
            area.allowY(area.minY * 0.05000000074505806D);
            area.allowY(area.maxY * 0.05000000074505806D);
            area.allowZ(area.minZ * 0.25D);
            area.allowZ(area.maxZ * 0.25D);
        }
    }
}
