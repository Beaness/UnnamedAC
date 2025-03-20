package me.beanes.acid.plugin.util;

public class BoxUtil {
    public static final float PLAYER_WIDTH = 0.6F;
    public static final float PLAYER_HEIGHT = 1.8F;

    public static BoundingBox getPlayerBox(double x, double y, double z, double expand) {
        return new BoundingBox(
                x - (PLAYER_WIDTH / 2.0F) - expand,
                y - expand,
                z - (PLAYER_WIDTH / 2.0F) - expand,
                x + (PLAYER_WIDTH / 2.0F) + expand,
                y + PLAYER_HEIGHT + expand,
                z + (PLAYER_WIDTH / 2.0F) + expand
        );
    }
}
