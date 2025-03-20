package me.beanes.acid.plugin.player.identifier.part.impl;

import me.beanes.acid.plugin.player.identifier.part.IdentifierPart;

public class BrandIdentifierPart implements IdentifierPart {
    private String brand;
    @Override
    public int getHash() {
        return brand.hashCode();
    }
}
