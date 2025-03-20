package me.beanes.acid.plugin.check.impl.local.attack;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import me.beanes.acid.plugin.check.model.LocalCheck;
import me.beanes.acid.plugin.check.model.ReceivePacketCheck;
import me.beanes.acid.plugin.player.PlayerData;
import org.bson.Document;

import java.util.Optional;

public class AttackB extends LocalCheck implements ReceivePacketCheck {

    public AttackB(PlayerData data) {
        super(data, "AttackB");
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (data.getEntityTracker().getType(wrapper.getEntityId()) == EntityTypes.PLAYER) {
                final Optional<Vector3f> vec3 = wrapper.getTarget();
                if (vec3.isPresent()) {
                    final Vector3f hitVec = vec3.get();

                    if (Math.abs(hitVec.getX()) > 0.4001 || Math.abs(hitVec.getZ()) > 0.4001 || hitVec.getY() > 1.9001F || hitVec.getY() < -0.1001F) {
                        Document document = new Document();

                        document.put("x", hitVec.getX());
                        document.put("y", hitVec.getY());
                        document.put("z", hitVec.getZ());

                        log(document);
                        debug("hitVec=" + hitVec);
                        certainAlert("HitBoxes");

                        data.getMitigationRequestTracker().requestBlatantMitigation();
                    }
                }
            }
        }
    }
}
