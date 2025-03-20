package me.beanes.acid.plugin.check.model;

import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import org.bson.Document;

public abstract class LocalCheck extends AbstractCheck {
    protected LocalCheck(PlayerData data, String name) {
        super(data, name);
    }

    public void log(Document flagData) {
        Acid.get().getLogManager().createLogAndEnqueue(data.getUser().getUUID(), this.name, flagData);
    }

    public void certainAlert(String simpleCheckName) {
        Acid.get().getAlertsManager().sendCertainAlert(data.getUser().getName(), simpleCheckName);
    }

    public void neutralAlert(String simpleCheckName) {
        Acid.get().getAlertsManager().sendNeutralAlert(data.getUser().getName(), simpleCheckName);
    }
}
