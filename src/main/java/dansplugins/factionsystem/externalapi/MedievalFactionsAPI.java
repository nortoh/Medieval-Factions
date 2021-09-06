package dansplugins.factionsystem.externalapi;

import dansplugins.factionsystem.MedievalFactions;
import dansplugins.factionsystem.data.EphemeralData;
import dansplugins.factionsystem.data.PersistentData;
import dansplugins.factionsystem.managers.ConfigManager;
import dansplugins.factionsystem.objects.Faction;
import org.bukkit.entity.Player;

import java.util.UUID;

/*
    This class gives developers access to the external API for Medieval Factions.
*/
public class MedievalFactionsAPI implements IMedievalFactionsAPI {

    // accessors

    @Override
    public MF_Faction getFaction(String factionName) {
        Faction faction = PersistentData.getInstance().getFaction(factionName);
        if (faction == null) {
            return null;
        }
        return new MF_Faction(faction);
    }

    @Override
    public MF_Faction getFaction(Player player) {
        Faction faction = PersistentData.getInstance().getPlayersFaction(player.getUniqueId());
        if (faction == null) {
            return null;
        }
        return new MF_Faction(faction);
    }

    @Override
    public boolean isPlayerInFactionChat(Player player) {
        return EphemeralData.getInstance().isPlayerInFactionChat(player);
    }

    @Override
    public boolean isPrefixesFeatureEnabled() {
        return MedievalFactions.getInstance().getConfig().getBoolean("playersChatWithPrefixes");
    }

    // mutators

    @Override
    public void forcePlayerToLeaveFactionChat(UUID uuid) {
        EphemeralData.getInstance().getPlayersInFactionChat().remove(uuid);
    }
}
