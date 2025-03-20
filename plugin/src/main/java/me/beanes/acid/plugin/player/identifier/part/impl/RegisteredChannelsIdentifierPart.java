package me.beanes.acid.plugin.player.identifier.part.impl;

import me.beanes.acid.plugin.player.identifier.part.IdentifierPart;

import java.util.Arrays;

public class RegisteredChannelsIdentifierPart implements IdentifierPart {
    private String[] channels;

    @Override
    public int getHash() {
        return Arrays.hashCode(channels);
    }
}
