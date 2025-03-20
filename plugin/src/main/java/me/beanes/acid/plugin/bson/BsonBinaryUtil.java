package me.beanes.acid.plugin.bson;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.BasicOutputBuffer;

import java.nio.ByteBuffer;

public class BsonBinaryUtil {

    private static final CodecRegistry DEFAULT_REGISTRY = CodecRegistries.fromProviders(
            new ValueCodecProvider(),
            new CollectionCodecProvider(),
            new IterableCodecProvider(),
            new BsonValueCodecProvider(),
            new DocumentCodecProvider(),
            new MapCodecProvider(),
            new CustomCodecProvider()
    );

    private static final Codec<Document> DOCUMENT_CODEC = new DocumentCodec(DEFAULT_REGISTRY);

    public static byte[] toBytes(Document document) {
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(buffer); // Technically could wrap this around bytebuf but bytebuf hasnt an absolute write so not sure
        DOCUMENT_CODEC.encode(writer, document, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
        byte[] toReturn = buffer.toByteArray();
        buffer.close();
        return toReturn;
    }
}