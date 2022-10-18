/*
  Copyright (c) 2022 Daniel McCoy Stephenson
  GPL3 License
 */
package dansplugins.factionsystem.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dansplugins.factionsystem.commands.abs.SubCommand;
import dansplugins.factionsystem.data.PersistentData;
import dansplugins.factionsystem.events.FactionWarStartEvent;
import dansplugins.factionsystem.models.Faction;
import dansplugins.factionsystem.repositories.FactionRepository;
import dansplugins.factionsystem.services.ConfigService;
import dansplugins.factionsystem.services.LocaleService;
import dansplugins.factionsystem.services.MessageService;
import dansplugins.factionsystem.services.PlayerService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * @author Callum Johnson
 */
@Singleton
public class DeclareIndependenceCommand extends SubCommand {

    private final PlayerService playerService;
    private final MessageService messageService;
    private final LocaleService localeService;
    private final ConfigService configService;
    private final PersistentData persistentData;
    private final FactionRepository factionRepository;

    @Inject
    public DeclareIndependenceCommand(
        PlayerService playerService,
        LocaleService localeService,
        MessageService messageService,
        ConfigService configService,
        PersistentData persistentData,
        FactionRepository factionRepository
    ) {
        super();
        this.localeService = localeService;
        this.playerService = playerService;
        this.messageService = messageService;
        this.configService = configService;
        this.persistentData = persistentData;
        this.factionRepository = factionRepository;
        this
            .setNames("declareindependence", "di", LOCALE_PREFIX + "CmdDeclareIndependence")
            .requiresPermissions("mf.declareindependence")
            .isPlayerCommand()
            .requiresPlayerInFaction()
            .requiresFactionOwner();
    }

    /**
     * Method to execute the command for a player.
     *
     * @param player who sent the command.
     * @param args   of the command.
     * @param key    of the sub-command (e.g. Ally).
     */
    @Override
    public void execute(Player player, String[] args, String key) {
        if (!(this.faction.hasLiege()) || this.faction.getLiege() == null) {
            this.playerService.sendMessage(player, "&c" + this.localeService.getText("NotAVassalOfAFaction"), "NotAVassalOfAFaction", false);
            return;
        }

        final Faction liege = this.factionRepository.getByID(this.faction.getLiege());
        if (liege == null) {
            this.playerService.sendMessage(
                player, 
                "&c" + this.localeService.getText("FactionNotFound"), 
                Objects.requireNonNull(this.messageService.getLanguage().getString("FactionNotFound"))
                    .replace("#faction#", String.join(" ", args)), true
            );
            return;
        }

        // break vassal agreement.
        liege.removeVassal(this.faction.getID());
        this.faction.setLiege(null);

        if (!this.configService.getBoolean("allowNeutrality") || (!(this.faction.getFlag("neutral").toBoolean()) && !(liege.getFlag("neutral").toBoolean()))) {
            // make enemies if (1) neutrality is disabled or (2) declaring faction is not neutral and liege is not neutral
            FactionWarStartEvent warStartEvent = new FactionWarStartEvent(this.faction, liege, player);
            Bukkit.getPluginManager().callEvent(warStartEvent);

            if (!warStartEvent.isCancelled()) {
                this.faction.addEnemy(liege.getID());
                liege.addEnemy(this.faction.getID());

                // break alliance if allied
                if (this.faction.isAlly(liege.getID())) {
                    this.faction.removeAlly(liege.getID());
                    liege.removeAlly(faction.getID());
                }
            }
        }
        this.messageService.messageServer(
            "&c" + this.localeService.getText("HasDeclaredIndependence", this.faction.getName(), liege.getName()), 
            Objects.requireNonNull(this.messageService.getLanguage().getString("HasDeclaredIndependence"))
                .replace("#faction_a#", this.faction.getName())
                .replace("#faction_b#", liege.getName())
        );

    }

    /**
     * Method to execute the command.
     *
     * @param sender who sent the command.
     * @param args   of the command.
     * @param key    of the command.
     */
    @Override
    public void execute(CommandSender sender, String[] args, String key) {

    }
}