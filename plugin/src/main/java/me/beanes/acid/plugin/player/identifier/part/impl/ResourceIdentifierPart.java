package me.beanes.acid.plugin.player.identifier.part.impl;

import com.google.common.base.Objects;
import me.beanes.acid.plugin.player.identifier.part.IdentifierPart;

public class ResourceIdentifierPart implements IdentifierPart {
    private ResourcePackSetting setting;
    private String javaIdentifier;

    @Override
    public int getHash() {
        return Objects.hashCode(setting, javaIdentifier);
    }

    public enum ResourcePackSetting {
        LEGACY, DISABLED, PROMPT, AUTO_ACCEPT
    }
}
