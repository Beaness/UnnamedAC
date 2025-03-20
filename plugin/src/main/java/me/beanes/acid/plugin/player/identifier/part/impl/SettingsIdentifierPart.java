package me.beanes.acid.plugin.player.identifier.part.impl;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import lombok.AllArgsConstructor;
import me.beanes.acid.plugin.player.identifier.part.IdentifierPart;

import java.util.Objects;

@AllArgsConstructor
public class SettingsIdentifierPart implements IdentifierPart {
    private String lang;
    private int view;
    private WrapperPlayClientSettings.ChatVisibility chatVisibility;
    private boolean enableColors;
    private int modelPartFlags;

    @Override
    public int getHash() {
        return Objects.hash(lang, view, chatVisibility, enableColors, modelPartFlags);
    }
}
