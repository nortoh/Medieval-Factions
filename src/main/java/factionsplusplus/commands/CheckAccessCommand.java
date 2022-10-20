/*
  Copyright (c) 2022 Daniel McCoy Stephenson
  GPL3 License
 */
package factionsplusplus.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import factionsplusplus.data.EphemeralData;
import factionsplusplus.models.Command;
import factionsplusplus.models.CommandContext;
import org.bukkit.entity.Player;

import factionsplusplus.builders.CommandBuilder;

import java.util.List;

/**
 * @author Callum Johnson
 */
@Singleton
public class CheckAccessCommand extends Command {

    private final EphemeralData ephemeralData;

    @Inject
    public CheckAccessCommand(EphemeralData ephemeralData) {
        super(
            new CommandBuilder()
                .withName("checkaccess")
                .withAliases("ca", LOCALE_PREFIX + "CmdCheckAccess")
                .withDescription("Checks access to a locked block.")
                .requiresPermissions("mf.checkaccess")
                .expectsPlayerExecution()
                .addSubCommand(
                    new CommandBuilder()
                        .withName("cancel")
                        .withAliases(LOCALE_PREFIX + "CmdCheckAccessCancel")
                        .withDescription("Cancels pending check access request")
                        .setExecutorMethod("cancelCommand")
                )
        );
        this.ephemeralData = ephemeralData;
    }

    public void execute(CommandContext context) {
        if (this.ephemeralData.getPlayersCheckingAccess().contains(context.getPlayer().getUniqueId())) {
            context.replyWith("AlreadyEnteredCheckAccess");
            return;
        }
        this.ephemeralData.getPlayersCheckingAccess().add(context.getPlayer().getUniqueId());
        context.replyWith("RightClickCheckAccess");
    }

    public void cancelCommand(CommandContext context) {
        this.ephemeralData.getPlayersCheckingAccess().remove(context.getPlayer().getUniqueId());
        context.replyWith("Cancelled");
    }
}