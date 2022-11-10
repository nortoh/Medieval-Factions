package factionsplusplus.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import factionsplusplus.models.Command;
import factionsplusplus.models.CommandContext;
import factionsplusplus.models.Faction;
import factionsplusplus.models.FactionBase;
import factionsplusplus.services.ClaimService;
import factionsplusplus.services.ConfigService;
import factionsplusplus.services.DataService;
import factionsplusplus.utils.StringUtils;
import factionsplusplus.utils.extended.Scheduler;
import factionsplusplus.builders.CommandBuilder;
import factionsplusplus.constants.GroupRole;
import factionsplusplus.builders.ArgumentBuilder;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class BaseCommand extends Command {
    private final ConfigService configService;
    private final ClaimService claimService;
    private final DataService dataService;
    private final Scheduler scheduler;

    @Inject
    public BaseCommand(ConfigService configService, DataService dataService, ClaimService claimService, Scheduler scheduler) {
        super(
            new CommandBuilder()
                .withName("base")
                .withAliases("home", LOCALE_PREFIX + "CmdBase")
                .withDescription("Manage your factions bases.")
                .expectsPlayerExecution()
                .expectsFactionMembership()
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
                        .expectsFactionOfficership()
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
                        .expectsFactionOfficership()
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
                        .expectsFactionOfficership()
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
                        .withName("rename")
                        .withAliases(LOCALE_PREFIX + "CmdRenameBase")
                        .requiresPermissions("mf.renamebase")
                        .withDescription("Rename a base")
                        .setExecutorMethod("renameCommand")
                        .expectsFactionOfficership()
                        .addArgument(
                            "name",
                            new ArgumentBuilder()
                                .setDescription("the current name of the base to rename")
                                .expectsFactionBaseName()
                                .expectsDoubleQuotes()
                                .isRequired()
                        )
                        .addArgument(
                            "new name",
                            new ArgumentBuilder()
                                .setDescription("the new name of the base you wish to rename")
                                .expectsString()
                                .expectsDoubleQuotes()
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
                                .expectsString()
                                .expectsDoubleQuotes()
                                .isRequired()
                        )
                        .addArgument(
                            "faction name",
                            new ArgumentBuilder()
                                .setDescription("the faction who owns the base you wish to teleport to")
                                .expectsFaction()
                                .expectsDoubleQuotes()
                                .isOptional()
                        )
                )
        );
        this.configService = configService;
        this.claimService = claimService;
        this.scheduler = scheduler;
        this.dataService = dataService;
    }

    public void createCommand(CommandContext context) {
        if (context.getExecutorsFaction().getBases().size() >= this.configService.getInt("faction.limits.base.count")) {
            context.error("Error.Base.MaximumReached");
            return;
        }
        final Faction chunkOwner = this.claimService.checkOwnershipAtPlayerLocation(context.getPlayer());
        if (chunkOwner == null || ! chunkOwner.equals(context.getExecutorsFaction())) {
            context.error("Error.Base.ClaimedTerritory");
            return;
        }
        final String baseName = context.getStringArgument("name");
        final boolean ok = context.getExecutorsFaction().addBase(baseName, context.getPlayer().getLocation());
        if (ok) {
            context.success("CommandResponse.Base.Created", baseName);
            return;
        }
        context.error("Error.Base.Creating");
    }

    public void configCommand(CommandContext context) {
        final FactionBase base = context.getFactionBaseArgument("base to edit");
        final String option = context.getStringArgument("option");
        final String value = context.getStringArgument("value");
        switch(option.toLowerCase()) {
            case "factiondefault":
                boolean isDefault = StringUtils.parseAsBoolean(value);
                if (isDefault == true) {
                    FactionBase defaultBase = context.getExecutorsFaction().getDefaultBase();
                    if (defaultBase != null && defaultBase.equals(base)) {
                        context.error("Error.Base.Default.AlreadyDefault");
                        return;
                    } else {
                        if (defaultBase != null) { 
                            defaultBase.toggleDefault();
                            context.getExecutorsFaction().persistBase(defaultBase);
                        }
                        base.toggleDefault();
                        context.success("CommandResponse.Base.Set.Default.On", base.getName());
                    }
                } else {
                    if (base.isFactionDefault()) {
                        base.toggleDefault();
                        context.success("CommandResponse.Base.Set.Default.Off", base.getName());
                    } else {
                        context.error("Error.Base.Default.NotDefault");
                        return;
                    }
                }
                break;
            case "allowallies":
                String newText = base.shouldAllowAllies() ? "Off" : "On";
                base.toggleAllowAllies();
                context.success("CommandResponse.Base.Set.Allies."+newText, base.getName());
                break;
            case "allowallfactionmembers":
                newText = base.shouldAllowAllFactionMembers() ? "Off" : "On";
                base.toggleAllowAllFactionMembers();
                context.success("CommandResponse.Base.Set.AllFactionMembers."+newText, base.getName());
                break;
            default:
                context.error("Error.Setting.NotFound", option);
                return;
        }
        context.getExecutorsFaction().persistBase(base);
    }

    public void renameCommand(CommandContext context) {
        final FactionBase base = context.getFactionBaseArgument("name");
        final String oldName = base.getName();
        final String newName = context.getStringArgument("new name");
        if (context.getExecutorsFaction().getBase(newName) != null) {
            context.error("Error.Base.AlreadyExists", newName);
            return;
        }
        context.getExecutorsFaction().renameBase(oldName, newName);
        context.getExecutorsFaction().persistBase(base);
        context.success("CommandResponse.Base.Renamed", oldName, newName);
    }

    public void listCommand(CommandContext context) {
        if (context.getExecutorsFaction().getBases().isEmpty()) {
            context.error("Error.Base.NoneAccessible");
            return;
        }
        context.replyWithMiniMessage("<color:light_purple><lang:BaseList.Title>");
        List<FactionBase> accessibleBases = context.getFPPPlayer().getAccessibleFactionBases();
        if (accessibleBases.size() == 0) {
            context.error("Error.Base.NoneAccessible");
            return;
        }
        context.replyWithMiniMessage(
            accessibleBases
                .stream()
                .map(base -> {
                    final boolean isOwnBase = base.getFaction().equals(context.getExecutorsFaction().getUUID());
                    final String baseName = (isOwnBase ? base.getName() : String.format(
                        "%s (%s)",
                        base.getName(),
                        this.dataService.getFaction(base.getFaction()).getName()
                    ));
                    return String.format("<color:yellow><lang:BaseList.Base:'<color:white>%s'>", baseName);
                }).collect(Collectors.joining("<newline>"))
        );
    }

    public void removeCommand(CommandContext context) {
        final FactionBase base = context.getFactionBaseArgument("base to remove");
        final boolean ok = context.getExecutorsFaction().removeBase(base.getName());
        if (ok) {
            context.success("CommandResponse.Base.Removed", base.getName());
            return;
        }
        context.error("Error.Base.Removing");
    }

    public void teleportCommand(CommandContext context) {
        final String baseName = context.getStringArgument("name");
        Faction baseFaction = context.getFactionArgument("faction name");
        FactionBase base = null;
        if (baseName == null && baseFaction == null) {
            // must be a fall through, try to final a default base
            base = context.getExecutorsFaction().getDefaultBase();
            if (base == null) {
                // if there's only one base, assume it's the default
                base = context.getExecutorsFaction().getBases().size() == 1 ? context.getExecutorsFaction().getBases().values().iterator().next() : null;
                if (base == null) {
                    context.error("Error.Base.NoFactionDefault");
                    return;
                }
            }
        }
        if (base == null) base = baseFaction != null ? baseFaction.getBase(baseName) : context.getExecutorsFaction().getBase(baseName);
        if (base == null) {
            context.error("Error.Base.NotFound");
            return;
        }
        // Check if they have permissions if they are a member of the faction who owns this base
        if (baseFaction != null || context.getExecutorsFaction().getUUID().equals(base.getFaction())) {
            if (! base.shouldAllowAllFactionMembers() && ! context.getExecutorsFaction().getMember(context.getPlayer().getUniqueId()).hasRole(GroupRole.Officer)) {
                context.error("Error.Base.NotAccessible", base.getName());
                return;
            }
        }

        // Check if we're targeting a base of a potential ally
        if (baseFaction != null && ! context.getExecutorsFaction().getUUID().equals(base.getFaction())) {
            if (
                (
                    base.shouldAllowAllies() &&
                    ! baseFaction.isAlly(context.getExecutorsFaction().getUUID())
                ) || ! base.shouldAllowAllies()
            ) {
                context.error("Error.Base.NotAccessible.Remote", baseFaction.getName(), base.getName());
                return;
            }
        }
        this.scheduler.scheduleTeleport(context.getPlayer(), base.getBukkitLocation());
    }

    public List<String> autocompleteBaseConfigValues(CommandSender sender, String argument, List<String> rawArguments) {
        if (! (sender instanceof Player)) return List.of();
        Faction playersFaction = this.dataService.getPlayersFaction((Player)sender);
        if (playersFaction == null || playersFaction.getBases().isEmpty()) return List.of();
        List<String> completions = new ArrayList<>();
        List<String> options = List.of("allowAllies", "allowAllFactionMembers", "factionDefault");
        org.bukkit.util.StringUtil.copyPartialMatches(argument, options, completions);
        return completions;
    }

}
