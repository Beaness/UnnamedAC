package me.beanes.acid.plugin.bson.codec;

import com.github.retrooper.packetevents.util.Vector3d;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;
import java.util.List;

public class Vector3dArrayCodec implements Codec<Vector3d[]> {

    private final Codec<Vector3d> vector3dCodec;

    public Vector3dArrayCodec(Vector3dCodec vector3dCodec) {
        this.vector3dCodec = vector3dCodec;
    }

    @Override
    public Vector3d[] decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readStartArray();
        List<Vector3d> vectorList = new ArrayList<>();

        while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            vectorList.add(vector3dCodec.decode(bsonReader, decoderContext));
        }

        bsonReader.readEndArray();

        return vectorList.toArray(new Vector3d[0]);
    }

    @Override
    public void encode(BsonWriter bsonWriter, Vector3d[] vector3dArray, EncoderContext encoderContext) {
        bsonWriter.writeStartArray();
        for (Vector3d vector3d : vector3dArray) {
            vector3dCodec.encode(bsonWriter, vector3d, encoderContext);
        }
        bsonWriter.writeEndArray();
    }

    @Override
    public Class<Vector3d[]> getEncoderClass() {
        return Vector3d[].class;
    }
}