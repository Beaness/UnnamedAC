package me.beanes.acid.plugin.bson.codec;

import me.beanes.acid.plugin.player.tracker.impl.entity.EntityArea;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class EntityAreaCodec implements Codec<EntityArea> {
    @Override
    public EntityArea decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readStartDocument();
        double minX = bsonReader.readDouble();
        double minY = bsonReader.readDouble();
        double minZ = bsonReader.readDouble();
        double maxX = bsonReader.readDouble();
        double maxY = bsonReader.readDouble();
        double maxZ = bsonReader.readDouble();
        bsonReader.readEndDocument();;

        return new EntityArea(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void encode(BsonWriter bsonWriter, EntityArea area, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeDouble("minX", area.minX);
        bsonWriter.writeDouble("minY", area.minY);
        bsonWriter.writeDouble("minZ", area.minZ);
        bsonWriter.writeDouble("maxX", area.maxX);
        bsonWriter.writeDouble("maxY", area.maxY);
        bsonWriter.writeDouble("maxZ", area.maxZ);
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<EntityArea> getEncoderClass() {
        return EntityArea.class;
    }
}
