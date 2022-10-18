/*
  Copyright (c) 2022 Daniel McCoy Stephenson
  GPL3 License
 */
package dansplugins.factionsystem.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dansplugins.factionsystem.data.EphemeralData;
import dansplugins.factionsystem.data.PersistentData;
import dansplugins.factionsystem.models.Command;
import dansplugins.factionsystem.models.CommandContext;
import dansplugins.factionsystem.models.Faction;
import dansplugins.factionsystem.services.DynmapIntegrationService;
import org.bukkit.entity.Player;

import dansplugins.factionsystem.builders.CommandBuilder;
import dansplugins.factionsystem.builders.ArgumentBuilder;

/**
 * @author Callum Johnson
 */
@Singleton
public class UnclaimCommand extends Command {

    private final EphemeralData ephemeralData;
    private final DynmapIntegrationService dynmapService;
    private final PersistentData persistentData;

    @Inject
    public UnclaimCommand(
        EphemeralData ephemeralData,
        DynmapIntegrationService dynmapService,
        PersistentData persistentData
    ) {
        super(
            new CommandBuilder()
                .withName("unclaim")
                .withAliases(LOCALE_PREFIX + "CmdUnclaim")
                .withDescription("Unclaims land for your faction.")
                .expectsPlayerExecution()
                .requiresPermissions("mf.unclaim")
                .expectsFactionMembership()
                .addArgument(
                    "radius",
                    new ArgumentBuilder()
                        .setDescription("the chunk radius to unclaim")
                        .expectsInteger()
                )
        );
        this.ephemeralData = ephemeralData;
        this.persistentData = persistentData;
        this.dynmapService = dynmapService;
    }

    public void execute(CommandContext context) {
        final Faction faction = context.getExecutorsFaction();
        final Player player = context.getPlayer();
        final boolean isPlayerBypassing = this.ephemeralData.getAdminsBypassingProtections().contains(player.getUniqueId());
        if ((boolean) faction.getFlag("mustBeOfficerToManageLand").toBoolean()) {
            // officer or owner rank required
            if (!faction.isOfficer(player.getUniqueId()) && !faction.isOwner(player.getUniqueId()) && !isPlayerBypassing) {
                context.replyWith("NotAbleToClaim");
                return;
            }
        }
        if (context.getRawArguments().length == 0) {
            this.persistentData.getChunkDataAccessor().removeChunkAtPlayerLocation(player, faction);
            this.dynmapService.updateClaimsIfAble();
            context.replyWith("UnClaimed");
            return;
        }
        int radius = context.getIntegerArgument("radius");
        if (radius <= 0) {
            radius = 1;
        }
        this.persistentData.getChunkDataAccessor().radiusUnclaimAtLocation(radius, player, faction);
        context.replyWith(
            this.constructMessage("UnClaimedRadius")
                .with("number", String.valueOf(radius))
        );
    }
}