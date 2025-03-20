package me.beanes.acid.plugin.simulation.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

@AllArgsConstructor
@Getter
public class ForwardStrafe {
    // yes some bulky dumb code but hey this saves doing a lot of useless calculations!!!
    // I just made this for fun (insane microoptimizations!!!)

    // (I was bored and couldnt be asked to work on other stuff)

    private static final ForwardStrafe[][] all = new ForwardStrafe[2 * 3][]; // 2 sneak states possible * 3 using possibilities

    static {
        // Indexes: 0 1 2 3 4 5
        // 3-5: sneaking
        // 0-2: not sneaking

        // 0 & 3: using: true
        // 1 & 4: using: false
        // 2 & 5: using: possible
        for (int i = 0; i < all.length; i++) {
            SplitStateBoolean using;
            if (i >= 3) {
                using = SplitStateBoolean.values()[i - 3];
            } else {
                using = SplitStateBoolean.values()[i];
            }

            int sizeForIndex = 3 * 3 * (using == SplitStateBoolean.POSSIBLE ? 2 : 1) - (using == SplitStateBoolean.POSSIBLE ? 2 : 1);

            all[i] = new ForwardStrafe[sizeForIndex];
        }

        // There is probably a more optimal / cleaner way to do this lol
        float[] INPUTS = new float[]{1, 0, -1};
        boolean[] TRUE_FALSE = new boolean[]{true, false};

        int[] indexes = new int[all.length];

        for (float moveForward : INPUTS) {
            for (float moveStrafe : INPUTS) {
                for (boolean sneaking : TRUE_FALSE) {
                    for (boolean using : TRUE_FALSE) {
                        if (moveForward == 0 && moveStrafe == 0) continue; // 0 input is useless

                        float forward = moveForward;
                        float strafe = moveStrafe;

                        if (sneaking) {
                            forward = (float)((double)forward * 0.3D);
                            strafe = (float)((double)strafe * 0.3D);
                        }

                        if (using) {
                            forward *= 0.2F;
                            strafe *= 0.2F;
                        }

                        forward *= 0.98F;
                        strafe *= 0.98F;

                        int firstIndex = getIndex(sneaking, using ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE);
                        int secondIndex = getIndex(sneaking, SplitStateBoolean.POSSIBLE);

                        all[firstIndex][indexes[firstIndex]] = new ForwardStrafe(forward, strafe, sneaking, using);
                        all[secondIndex][indexes[secondIndex]] = new ForwardStrafe(forward, strafe, sneaking, using);

                        System.out.println("forward=" + forward + " strafe=" + strafe);

                        indexes[firstIndex]++;
                        indexes[secondIndex]++;
                    }
                }
            }
        }
    }

    private float forward;
    private float strafe;
    private boolean sneaking, using;

    private static int getIndex(boolean sneaking, SplitStateBoolean using) {
        return (using.ordinal()) + (sneaking ? 3 : 0); // If sneaking offset by 3
    }

    public static ForwardStrafe[] get(boolean sneaking, SplitStateBoolean using) {
        int index = getIndex(sneaking, using);

        return all[index];
    }
}
