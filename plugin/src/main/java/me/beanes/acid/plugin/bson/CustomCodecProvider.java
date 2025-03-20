package me.beanes.acid.plugin.bson;

import com.github.retrooper.packetevents.util.Vector3d;
import me.beanes.acid.plugin.bson.codec.EntityAreaCodec;
import me.beanes.acid.plugin.bson.codec.Vector3dArrayCodec;
import me.beanes.acid.plugin.bson.codec.Vector3dCodec;
import me.beanes.acid.plugin.player.tracker.impl.entity.EntityArea;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class CustomCodecProvider implements CodecProvider {
    private final Vector3dCodec vector3dCodec = new Vector3dCodec();
    private final Vector3dArrayCodec vector3dArrayCodec = new Vector3dArrayCodec(vector3dCodec);
    private final EntityAreaCodec entityAreaCodec = new EntityAreaCodec();

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry codecRegistry) {
        if (clazz == Vector3d.class) {
            return (Codec<T>) vector3dCodec;
        } else if (clazz == Vector3d[].class) {
            return (Codec<T>) vector3dArrayCodec;
        } else if (clazz == EntityArea.class) {
            return (Codec<T>) entityAreaCodec;
        }

        return null;
    }
}