package me.beanes.acid.plugin.util.pledge;

import com.github.retrooper.packetevents.util.reflection.Reflection;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

// Source https://github.com/ThomasOM/Pledge/blob/master-3.0/src/main/java/dev/thomazz/pledge/util/TickEndTask.java
// Licensed under MIT

public class TickEndUtil {

    public static void injectRunnable(Runnable runnable) {
        try {
            Object mcConnectionInstance = SpigotReflectionUtil.getMinecraftServerConnectionInstance();

            // Same list Grim injects into (this gets called just before flush)
            Field connectedChannelsFields = Reflection.getField(mcConnectionInstance.getClass(), List.class, 1);
            List<Object> endOfTickObject = (List<Object>) connectedChannelsFields.get(mcConnectionInstance);

            List<?> wrapper = Collections.synchronizedList(new HookedListWrapper<Object>(endOfTickObject) {
                @Override
                public void onIterator() {
                    runnable.run();
                }
            });

            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);

            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            unsafe.putObject(mcConnectionInstance, unsafe.objectFieldOffset(connectedChannelsFields), wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
