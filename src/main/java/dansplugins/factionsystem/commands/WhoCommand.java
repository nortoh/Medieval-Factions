/*
  Copyright (c) 2022 Daniel McCoy Stephenson
  GPL3 License
 */
package dansplugins.factionsystem.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dansplugins.factionsystem.data.PersistentData;
import dansplugins.factionsystem.models.Command;
import dansplugins.factionsystem.models.CommandContext;
import dansplugins.factionsystem.models.Faction;
import dansplugins.factionsystem.services.PlayerService;
import dansplugins.factionsystem.utils.TabCompleteTools;
import dansplugins.factionsystem.utils.extended.Messenger;
import org.bukkit.entity.Player;

import dansplugins.factionsystem.builders.CommandBuilder;
import dansplugins.factionsystem.builders.ArgumentBuilder;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Callum Johnson
 */
@Singleton
public class WhoCommand extends Command {
    private final PlayerService playerService;
    private final PersistentData persistentData;
    private final Messenger messenger;

    @Inject
    public WhoCommand(
        PlayerService playerService,
        PersistentData persistentData,
        Messenger messenger
    ) {
        super(
            new CommandBuilder()
                .withName("who")
                .withAliases(LOCALE_PREFIX + "CmdWho")
                .withDescription("Look up a players faction.")
                .requiresPermissions("mf.who")
                .expectsPlayerExecution()
                .addArgument(
                    "player",
                    new ArgumentBuilder()
                        .setDescription("the player to look up their joined faction")
                        .expectsAnyPlayer()
                        .isRequired()
                )
        );
        this.playerService = playerService;
        this.persistentData = persistentData;
        this.messenger = messenger;
    }

    public void execute(CommandContext context) {
        final UUID targetUUID = context.getOfflinePlayerArgument("player").getUniqueId();
        final Faction temp = this.playerService.getPlayerFaction(targetUUID);
        if (temp == null) {
            context.replyWith("PlayerIsNotInAFaction");
            return;
        }
        this.messenger.sendFactionInfo(
            context.getPlayer(), 
            temp,
            this.persistentData.getChunkDataAccessor().getChunksClaimedByFaction(temp.getID())
        );
    }

    /**
     * Method to handle tab completion.
     * 
     * @param player who sent the command.
     * @param args   of the command.
     */
    @Override
    public List<String> handleTabComplete(Player player, String[] args) {
        return TabCompleteTools.allOnlinePlayersMatching(args[0]);
    }
}