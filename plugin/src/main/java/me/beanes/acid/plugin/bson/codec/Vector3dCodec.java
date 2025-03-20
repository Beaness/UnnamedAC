package me.beanes.acid.plugin.bson.codec;

import com.github.retrooper.packetevents.util.Vector3d;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class Vector3dCodec implements Codec<Vector3d> {
    @Override
    public Vector3d decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readStartDocument();
        double x = bsonReader.readDouble();
        double y = bsonReader.readDouble();
        double z = bsonReader.readDouble();
        bsonReader.readEndDocument();;

        return new Vector3d(x, y, z);
    }

    @Override
    public void encode(BsonWriter bsonWriter, Vector3d vector3d, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeDouble("x", vector3d.getX());
        bsonWriter.writeDouble("y", vector3d.getY());
        bsonWriter.writeDouble("z", vector3d.getZ());
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<Vector3d> getEncoderClass() {
        return Vector3d.class;
    }
}
