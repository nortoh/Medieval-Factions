/*
  Copyright (c) 2022 Daniel McCoy Stephenson
  GPL3 License
 */
package dansplugins.factionsystem.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dansplugins.factionsystem.MedievalFactions;
import dansplugins.factionsystem.commands.abs.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Callum Johnson
 */
@Singleton
public class VersionCommand extends SubCommand {
    private final MedievalFactions medievalFactions;

    @Inject
    public VersionCommand(final MedievalFactions medievalFactions) {
        super();
        this.medievalFactions = medievalFactions;
        this
            .setNames("version", LOCALE_PREFIX + "CmdVersion")
            .requiresPermissions("mf.version");
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
        sender.sendMessage(this.translate("&bMedieval-Factions-" + this.medievalFactions.getVersion()));
    }
}