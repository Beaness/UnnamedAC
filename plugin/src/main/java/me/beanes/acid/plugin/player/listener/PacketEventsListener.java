package me.beanes.acid.plugin.player.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import lombok.NoArgsConstructor;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.impl.ResyncCommand;
import me.beanes.acid.plugin.player.PlayerData;
import org.bukkit.Bukkit;

@NoArgsConstructor
public class PacketEventsListener extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PlayerData data = Acid.get().getPlayerManager().get(event.getUser());

        if (data == null) {
            event.getUser().closeConnection();
            return;
        }

        data.getCheckManager().preReceivePacket(event);

        PacketTypeCommon type = event.getPacketType();

        boolean clientTick = WrapperPlayClientPlayerFlying.isFlying(type);

        if (clientTick) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            data.getPositionTracker().handleClientTick(wrapper);
            data.getRotationTracker().handleClientTick(wrapper);
            data.getSetbackTracker().handleClientTick(wrapper);
        }

        if (type == Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation wrapper = new WrapperPlayClientWindowConfirmation(event);
            data.getTransactionTracker().handleClientTransaction(wrapper);
        }

        if (type == Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);
            data.getActionTracker().handleEntityAction(wrapper);
        }

        if (type == Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            data.getUsingTracker().handleInteractEntity(wrapper);
        }

        if (type == Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
            data.getUsingTracker().handleBlockPlace(wrapper);
            data.getWorldTracker().handleBlockPlace(wrapper);
            data.getInventoryTracker().handleBlockPlace(wrapper);
        }

        if (type == Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            data.getUsingTracker().handleDigging(wrapper);
            data.getWorldTracker().handleDigging(wrapper);
        }

        if (type == Client.HELD_ITEM_CHANGE) {
            WrapperPlayClientHeldItemChange wrapper = new WrapperPlayClientHeldItemChange(event);
            data.getUsingTracker().handleHeldItemChange(wrapper);
            data.getInventoryTracker().handleClientHeldItemChange(wrapper);
        }

        if (type == Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);

            data.getInventoryTracker().handleClientClickWindow(wrapper);
        }

        if (type == Client.CREATIVE_INVENTORY_ACTION) {
            WrapperPlayClientCreativeInventoryAction wrapper = new WrapperPlayClientCreativeInventoryAction(event);

            data.getInventoryTracker().handleCreativeInventoryAction(wrapper);
        }

        if (type == Client.CLOSE_WINDOW) {
            data.getInventoryTracker().handleClientCloseWindow();
        }

        if (type == Client.KEEP_ALIVE) {
            data.getTransactionTracker().checkTransactionResponseTime();
        }

        if (type == Client.CLIENT_STATUS) {
            WrapperPlayClientClientStatus wrapper = new WrapperPlayClientClientStatus(event);

            System.out.println("-> Client Status: " + wrapper.getAction());
        }

        data.getCheckManager().receivePacket(event);

        if (clientTick) {
            // Don't interpolate / do end tick for the response of a teleport
            if (data.getPositionTracker().isTeleport()) {
                return;
            }

            data.getWorldTracker().onClientTick(); // First process all queued client world changes before doing simulation

            long start = System.nanoTime();

            boolean correctSimulation = true;
            System.out.println("--- start simulation ---");



            try {
                correctSimulation = data.getSimulationEngine().attemptSimulation();
            } catch (Throwable throwable) {
                // TODO: report to cloud the error
                System.out.println("Error!");
                throwable.printStackTrace();
                data.getUser().closeConnection(); // They might have found a bypass
            }

            double took = (System.nanoTime() - start) / 1_000_000D;

            System.out.println("Everything took " + took + " milliseconds");

            System.out.println("--- end simulation ---");

            data.getActionTracker().handleEndClientTick();
            data.getEntityTracker().handleEndClientTick();
            data.getUsingTracker().handleEndClientTick();
            data.getRespawnTracker().handleEndClientTick();

            // Strip the position of the flying packet if the player has a failed simulation or if there is a teleport queued
            if (!correctSimulation || data.getPositionTracker().isTeleportQueued()) {
                WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
                WrapperPlayClientPlayerFlying wrapperWithoutPosition = new WrapperPlayClientPlayerFlying(false, wrapper.hasRotationChanged(), wrapper.isOnGround(), wrapper.getLocation());

                // System.out.println("receive silently");
                PacketEvents.getAPI().getProtocolManager().receivePacketSilently(data.getUser().getChannel(), wrapperWithoutPosition);
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PlayerData data = Acid.get().getPlayerManager().get(event.getUser());

        if (data == null) {
            event.getUser().closeConnection();
            return;
        }

        PacketTypeCommon type = event.getPacketType();

        if (type == Server.PLAYER_POSITION_AND_LOOK) {
            WrapperPlayServerPlayerPositionAndLook wrapper = new WrapperPlayServerPlayerPositionAndLook(event);

            if (wrapper.getRelativeMask() > 0) {
                event.markForReEncode(true);
            }

            data.getPositionTracker().handleServerTeleport(wrapper);
        }

        if (type == Server.WINDOW_CONFIRMATION) {
            WrapperPlayServerWindowConfirmation wrapper = new WrapperPlayServerWindowConfirmation(event);
            data.getTransactionTracker().handleServerTransaction(wrapper);
        }

        if (type == Server.SPAWN_PLAYER) {
            WrapperPlayServerSpawnPlayer wrapper = new WrapperPlayServerSpawnPlayer(event);
            data.getEntityTracker().handleSpawnPlayer(wrapper);
        }

        if (type == Server.ENTITY_TELEPORT) {
            WrapperPlayServerEntityTeleport wrapper = new WrapperPlayServerEntityTeleport(event);
            data.getEntityTracker().handleEntityTeleport(wrapper);
        }

        if (type == Server.ENTITY_RELATIVE_MOVE) {
            WrapperPlayServerEntityRelativeMove wrapper = new WrapperPlayServerEntityRelativeMove(event);
            data.getEntityTracker().handleEntityRelativeMove(wrapper);
        }

        if (type == Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            WrapperPlayServerEntityRelativeMoveAndRotation wrapper = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
            data.getEntityTracker().handleEntityRelativeMoveAndRotation(wrapper);
        }

        if (type == Server.ENTITY_ROTATION) {
            WrapperPlayServerEntityRotation wrapper = new WrapperPlayServerEntityRotation(event);
            data.getEntityTracker().handleEntityRotation(wrapper);
        }

        if (type == Server.CHUNK_DATA) {
            WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);
            data.getWorldTracker().handleChunk(wrapper);
        }

        if (type == Server.MAP_CHUNK_BULK) {
            WrapperPlayServerChunkDataBulk wrapper = new WrapperPlayServerChunkDataBulk(event);
            data.getWorldTracker().handleChunkBulk(wrapper);
        }

        if (type == Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
            data.getWorldTracker().handleBlockChange(wrapper);
            if (ResyncCommand.DEBUG_WORLD) {
                event.setCancelled(true);
            }
        }

        if (type == Server.MULTI_BLOCK_CHANGE) {
            WrapperPlayServerMultiBlockChange wrapper = new WrapperPlayServerMultiBlockChange(event);
            data.getWorldTracker().handleMultiBlockChange(wrapper);
            if (ResyncCommand.DEBUG_WORLD) {
                event.setCancelled(true);
            }
        }

        if (type == Server.ENTITY_EFFECT) {
            WrapperPlayServerEntityEffect wrapper = new WrapperPlayServerEntityEffect(event);
            data.getPotionTracker().handleEffect(wrapper);
        }

        if (type == Server.REMOVE_ENTITY_EFFECT) {
            WrapperPlayServerRemoveEntityEffect wrapper = new WrapperPlayServerRemoveEntityEffect(event);
            data.getPotionTracker().handleRemoveEffect(wrapper);
        }

        if (type == Server.RESPAWN) {
            WrapperPlayServerRespawn wrapper = new WrapperPlayServerRespawn(event);
            data.getPotionTracker().handleRespawn();
            data.getStateTracker().handleRespawn(wrapper);
            data.getRespawnTracker().handleRespawn();
        }

        if (type == Server.CHANGE_GAME_STATE) {
            WrapperPlayServerChangeGameState wrapper = new WrapperPlayServerChangeGameState(event);
            data.getStateTracker().handleGameStateChange(wrapper);
        }

        if (type == Server.UPDATE_ATTRIBUTES) {
            WrapperPlayServerUpdateAttributes wrapper = new WrapperPlayServerUpdateAttributes(event);
            if (data.getAttributeTracker().handleUpdateAttribute(wrapper)) {
                event.setCancelled(true);
            }
        }

        if (type == Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity wrapper = new WrapperPlayServerEntityVelocity(event);
            data.getVelocityTracker().handleVelocity(wrapper);
        }

        if (type == Server.EXPLOSION) {
            WrapperPlayServerExplosion wrapper = new WrapperPlayServerExplosion(event);
            data.getVelocityTracker().handleExplosion(wrapper);
            data.getWorldTracker().handleExplosion(wrapper);

            // TODO: implement explosion packet support lol
            wrapper.setPlayerMotion(new Vector3f(0.0F, 0.0F, 0.0F));
            event.markForReEncode(true);

            if (ResyncCommand.DEBUG_WORLD) {
                event.setCancelled(true);
            }
        }

        if (type == Server.UPDATE_HEALTH) {
            WrapperPlayServerUpdateHealth wrapper = new WrapperPlayServerUpdateHealth(event);
            if (data.getStateTracker().handleUpdateHealth(wrapper)) {
                event.setCancelled(true);
            }
        }

        if (type == Server.JOIN_GAME) {
            WrapperPlayServerJoinGame wrapper = new WrapperPlayServerJoinGame(event);
            data.getStateTracker().handleJoinGame(wrapper);
        }

        if (type == Server.PLAYER_ABILITIES) {
            WrapperPlayServerPlayerAbilities wrapper = new WrapperPlayServerPlayerAbilities(event);
            data.getAbilitiesTracker().handleAbilities(wrapper);
        }

        if (type == Server.OPEN_WINDOW) {
            WrapperPlayServerOpenWindow wrapper = new WrapperPlayServerOpenWindow(event);
            data.getInventoryTracker().handleOpenWindow(wrapper);
        }

        if (type == Server.CLOSE_WINDOW) {
            data.getInventoryTracker().handleServerCloseWindow();
        }

        if (type == Server.WINDOW_ITEMS) {
            WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(event);

            data.getInventoryTracker().handleWindowItems(wrapper);
        }

        if (type == Server.ENTITY_METADATA) {
            WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);

            if (data.getActionTracker().handleEntityMetadata(wrapper)) {
                event.setCancelled(true);
            };
        }

        if (type == Server.SET_SLOT) {
            WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(event);
            if (data.getInventoryTracker().handleSetSlot(wrapper)) {
                event.setCancelled(true);
            }

            if (!event.isCancelled()) {
                data.getUsingTracker().handleSetSlot(wrapper);
            }
        }

        if (type == Server.ENTITY_STATUS) {
            WrapperPlayServerEntityStatus wrapper = new WrapperPlayServerEntityStatus(event);

            data.getEntityTracker().handleEntityStatus(wrapper);
        }

        if (type == Server.USE_BED) {
            WrapperPlayServerUseBed wrapper = new WrapperPlayServerUseBed(event);

            data.getEntityTracker().handleUseBed(wrapper);
        }

        if (type == Server.ENTITY_ANIMATION) {
            WrapperPlayServerEntityAnimation wrapper = new WrapperPlayServerEntityAnimation(event);

            data.getEntityTracker().handleEntityAnimation(wrapper, event);
        }

        // TODO: support entity attaching :P (and riding :()
        if (type == Server.ATTACH_ENTITY) {
            event.setCancelled(true);
        }

        // TODO: support boats :P (pain)
        if (type == Server.SPAWN_ENTITY) {
            WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(event);

            if (wrapper.getEntityType() == EntityTypes.BOAT) {
                event.setCancelled(true);
            }
        }

        data.getCheckManager().sendPacket(event);
    }

    @Override
    public void onUserConnect(UserConnectEvent event) {
        // We hook into the connect event because we want to register on the netty thread (because of the ThreadLocal in PlayerManager)
        PlayerData data = new PlayerData(event.getUser());
        Acid.get().getPlayerManager().add(data);

        // Also add the player to the server cache thread
        Bukkit.getScheduler().runTask(Acid.get(), () -> Acid.get().getPlayerManager().add(data));
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        Acid.get().getPlayerManager().remove(event.getUser());

        // Also remove from the server thread cache
        Bukkit.getScheduler().runTask(Acid.get(), () -> Acid.get().getPlayerManager().remove(event.getUser()));
    }
}
