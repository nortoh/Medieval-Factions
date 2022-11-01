package factionsplusplus.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import factionsplusplus.models.Command;
import factionsplusplus.models.CommandContext;
import factionsplusplus.models.Faction;
import factionsplusplus.models.FactionBase;
import factionsplusplus.services.ConfigService;
import factionsplusplus.services.DataService;
import factionsplusplus.utils.extended.Scheduler;
import factionsplusplus.builders.CommandBuilder;
import factionsplusplus.builders.ArgumentBuilder;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class BaseCommand extends Command {
    private final ConfigService configService;
    private final DataService dataService;
    private final Scheduler scheduler;

    @Inject
    public BaseCommand(ConfigService configService, DataService dataService, Scheduler scheduler) {
        super(
            new CommandBuilder()
                .withName("base")
                .withAliases("home", LOCALE_PREFIX + "CmdBase")
                .withDescription("Manage your factions bases.")
                .expectsPlayerExecution()
                .expectsFactionMembership()
                .expectsFactionOfficership()
                .setExecutorMethod("teleportCommand")
                .addSubCommand(
                    new CommandBuilder()
                        .withName("list")
                        .withAliases(LOCALE_PREFIX + "CmdListBases")
                        .requiresPermissions("mf.listbases")
                        .withDescription("Lists your factions bases.")
                        .setExecutorMethod("listCommand")
                )
                .addSubCommand(
                    new CommandBuilder()
                        .withName("add")
                        .withAliases("create", "al", LOCALE_PREFIX + "CmdAddBase")
                        .requiresPermissions("mf.addbase")
                        .withDescription("Adds a new base to your faction.")
                        .setExecutorMethod("createCommand")
                        .addArgument(
                            "name",
                            new ArgumentBuilder()
                                .setDescription("the name of the base to add")
                                .expectsString()
                                .consumesAllLaterArguments()
                                .isRequired()
                        )
                )
                .addSubCommand(
                    new CommandBuilder()
                        .withName("remove")
                        .withAliases("delete", LOCALE_PREFIX + "CmdRemoveBase")
                        .requiresPermissions("mf.removebase")
                        .withDescription("Removes a base from your faction.")
                        .setExecutorMethod("removeCommand")
                        .addArgument(
                            "base to remove",
                            new ArgumentBuilder()
                                .setDescription("the name of the base to remove")
                                .expectsFactionBaseName()
                                .consumesAllLaterArguments()
                                .isRequired()
                        )
                )
                .addSubCommand(
                    new CommandBuilder()
                        .withName("edit")
                        .withAliases("modify", LOCALE_PREFIX + "CmdEditBase")
                        .requiresPermissions("mf.editbase")
                        .withDescription("Edits a base for your faction.")
                        .setExecutorMethod("editCommand")
                        .addArgument(
                            "base to edit",
                            new ArgumentBuilder()
                                .setDescription("the name of the base to edit")
                                .expectsDoubleQuotes()
                                .expectsFactionBaseName()
                                .isRequired()
                        )
                        .addArgument(
                            "option", 
                            new ArgumentBuilder()
                                .setDescription("the option to edit for the base")
                                .expectsString()
                                .isRequired()
                                .setTabCompletionHandler("autocompleteBaseConfigValues")
                        )
                        .addArgument(
                            "value",
                            new ArgumentBuilder()
                                .setDescription("the value to set for the option for the base")
                                .expectsString()
                                .consumesAllLaterArguments()
                                .isRequired()
                        )
                )
                .addSubCommand(
                    new CommandBuilder()
                        .withName("teleport")
                        .withAliases("go", LOCALE_PREFIX + "CmdTeleportBase")
                        .requiresPermissions("mf.teleport")
                        .withDescription("teleport to a base")
                        .setExecutorMethod("teleportCommand")
                        .addArgument(
                            "name",
                            new ArgumentBuilder()
                                .setDescription("the name of the base to teleport to")
                                .expectsFactionBaseName()
                                .consumesAllLaterArguments()
                                .isRequired()
                        )
                )
        );
        this.configService = configService;
        this.scheduler = scheduler;
        this.dataService = dataService;
    }

    public void createCommand(CommandContext context) {
        // TODO: implement max bases and faction flag to only allow owners to create bases
        final String baseName = context.getStringArgument("name");
        final boolean ok = context.getExecutorsFaction().addBase(baseName, context.getPlayer().getLocation());
        if (ok) {
            context.replyWith(
                this.constructMessage("BaseCreated")
                    .with("name", baseName)
            );
            return;
        }
        context.replyWith("ErrorCreatingBase");
    }

    public void editCommand(CommandContext context) {
        // TODO: all of this
    }

    public void renameCommand(CommandContext context) {
        // TODO: all of this
    }

    public void listCommand(CommandContext context) {
        // TODO: only show bases the executor has access to (i.e. if allow all members is off, only officers and above can tp to it)
        if (context.getExecutorsFaction().getBases().isEmpty()) {
            context.replyWith("NoBases");
            return;
        }
        context.replyWith("FactionBaseList.Title");
        context.getExecutorsFaction().getBases().keySet().stream()
            .forEach(baseName -> {
                context.replyWith(
                    this.constructMessage("FactionBaseList.Base")
                        .with("name", baseName)
                );
            });
    }

    public void removeCommand(CommandContext context) {
        final FactionBase base = context.getFactionBaseArgument("name");
        final boolean ok = context.getExecutorsFaction().removeBase(base.getName());
        if (ok) {
            context.replyWith(
                this.constructMessage("BaseRemoved")
                    .with("name", base.getName())
            );
            return;
        }
        context.replyWith("ErrorRemovingBase");
    }

    public void teleportCommand(CommandContext context) {
        // TODO: make sure executor has access to the base
        // TODO: add ability for allies to go to an allied factions bases if permissable, probably adding an optional faction param 
        final FactionBase base = context.getFactionBaseArgument("name");
        if (base == null) {
            // must be a fall through, try to final a default base
            return;
        }
        this.scheduler.scheduleTeleport(context.getPlayer(), base.getBukkitLocation());
    }

    public List<String> autocompleteBaseConfigValues(CommandSender sender, String argument) {
        if (! (sender instanceof Player)) return List.of();
        Faction playersFaction = this.dataService.getPlayersFaction((Player)sender);
        if (playersFaction == null || playersFaction.getBases().isEmpty()) return List.of();
        List<String> completions = new ArrayList<>();
        List<String> options = List.of("allowAllies", "allowAllFactionMembers", "factionDefault");
        org.bukkit.util.StringUtil.copyPartialMatches(argument, options, completions);
        return completions;
    }

}
