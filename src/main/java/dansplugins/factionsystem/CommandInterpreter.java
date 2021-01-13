package dansplugins.factionsystem;

import dansplugins.factionsystem.commands.*;
import dansplugins.factionsystem.data.PersistentData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandInterpreter {

    private static CommandInterpreter instance;

    private CommandInterpreter() {

    }

    public static CommandInterpreter getInstance() {
        if (instance == null) {
            instance = new CommandInterpreter();
        }
        return instance;
    }

    public boolean interpretCommand(CommandSender sender, String label, String[] args) {
        // mf commands
        if (label.equalsIgnoreCase("mf") || label.equalsIgnoreCase("f") ||
                label.equalsIgnoreCase("medievalfactions") || label.equalsIgnoreCase("factions")) {

            // no arguments check
            if (args.length == 0) {
                // send plugin information
                sender.sendMessage(ChatColor.AQUA + String.format(LocaleManager.getInstance().getText("MedievalFactionsTitle"), MedievalFactions.getInstance().getVersion()));
                sender.sendMessage(ChatColor.AQUA + LocaleManager.getInstance().getText("DeveloperList") + " " + "DanTheTechMan, Pasarus, Caibinus");
                sender.sendMessage(ChatColor.AQUA + LocaleManager.getInstance().getText("WikiLink"));
                sender.sendMessage(ChatColor.AQUA + String.format(LocaleManager.getInstance().getText("CurrentLanguageID"), MedievalFactions.getInstance().getConfig().getString("languageid")));
                sender.sendMessage(ChatColor.AQUA + String.format(LocaleManager.getInstance().getText("SupportedLanguageIDList"), LocaleManager.getInstance().getSupportedLanguageIDsSeparatedByCommas()));
                return true;
            }

            // argument check
            if (args.length > 0) {

                // default commands ----------------------------------------------------------------------------------

                // help command
                if (args[0].equalsIgnoreCase("help")) {
                    HelpCommand command = new HelpCommand();
                    command.sendHelpMessage(sender, args);
                    return true;
                }

                // create command
                if (args[0].equalsIgnoreCase("create") ) {
                    CreateCommand command = new CreateCommand();
                    command.createFaction(sender, args);
                    return true;
                }

                // list command
                if  (args[0].equalsIgnoreCase("list")) {
                    ListCommand command = new ListCommand();
                    command.listFactions(sender);
                    return true;
                }

                // disband command
                if (args[0].equalsIgnoreCase("disband")) {
                    DisbandCommand command = new DisbandCommand();
                    command.deleteFaction(sender, args);
                    return true;
                }

                // members command
                if (args[0].equalsIgnoreCase("members")) {
                    MembersCommand command = new MembersCommand();
                    command.showMembers(sender, args);
                    return true;
                }

                // info command
                if (args[0].equalsIgnoreCase("info")) {
                    InfoCommand command = new InfoCommand();
                    command.showInfo(sender, args);
                    return true;
                }

                // desc command
                if (args[0].equalsIgnoreCase("desc")) {
                    DescCommand command = new DescCommand();
                    command.setDescription(sender, args);
                    return true;
                }

                // invite command
                if (args[0].equalsIgnoreCase("invite")) {
                    InviteCommand command = new InviteCommand();
                    command.invitePlayer(sender, args);
                    return true;
                }

                // join command
                if (args[0].equalsIgnoreCase("join")) {
                    JoinCommand command = new JoinCommand();
                    command.joinFaction(sender, args);
                    return true;
                }

                // kick command
                if (args[0].equalsIgnoreCase("kick")) {
                    KickCommand command = new KickCommand();
                    command.kickPlayer(sender, args);
                    return true;
                }

                // leave commmand
                if (args[0].equalsIgnoreCase("leave")) {
                    LeaveCommand command = new LeaveCommand();
                    command.leaveFaction(sender);
                    return true;
                }

                // transfer command
                if (args[0].equalsIgnoreCase("transfer")) {
                    TransferCommand command = new TransferCommand();
                    command.transferOwnership(sender, args);
                    return true;
                }

                // declare war command
                if (args[0].equalsIgnoreCase("declarewar") || args[0].equalsIgnoreCase("dw")) {
                    DeclareWarCommand command = new DeclareWarCommand();
                    command.declareWar(sender, args);
                    return true;
                }

                // make peace command
                if (args[0].equalsIgnoreCase("makepeace") || args[0].equalsIgnoreCase("mp")) {
                    MakePeaceCommand command = new MakePeaceCommand();
                    command.makePeace(sender, args);
                    return true;
                }

                // claim command
                if (args[0].equalsIgnoreCase("claim")) {
                    ClaimCommand command = new ClaimCommand();
                    return command.claim(sender, args);
                }

                // TODO: move into command class
                // unclaim command
                if (args[0].equalsIgnoreCase("unclaim")) {
                    if (sender.hasPermission("mf.unclaim")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            if (PersistentData.getInstance().isInFaction(player.getUniqueId())) {
                                ChunkManager.getInstance().removeChunkAtPlayerLocation(player);
                                DynmapManager.updateClaims();
                                return true;
                            }
                            else {
                                player.sendMessage(ChatColor.RED + LocaleManager.getInstance().getText("MustBeInFaction"));
                                return false;
                            }

                        }
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + String.format(LocaleManager.getInstance().getText("PermissionNeeded"), "mf.unclaim"));
                        return false;
                    }
                }

                // unclaimall command
                if (args[0].equalsIgnoreCase("unclaimall") || args[0].equalsIgnoreCase("ua")) {
                    UnclaimallCommand command = new UnclaimallCommand();
                    return command.unclaimAllLand(sender, args);
                }

                // checkclaim command
                if (args[0].equalsIgnoreCase("checkclaim")|| args[0].equalsIgnoreCase("cc")) {
                    CheckClaimCommand command = new CheckClaimCommand();
                    return command.showClaim(sender);
                }

                // autoclaim command
                if (args[0].equalsIgnoreCase("autoclaim")|| args[0].equalsIgnoreCase("ac")) {
                    AutoClaimCommand command = new AutoClaimCommand();
                    return command.toggleAutoClaim(sender);
                }

                // promote command
                if (args[0].equalsIgnoreCase("promote")) {
                    PromoteCommand command = new PromoteCommand();
                    command.promotePlayer(sender, args);
                    return true;
                }

                // demote command
                if (args[0].equalsIgnoreCase("demote")) {
                    DemoteCommand command = new DemoteCommand();
                    command.demotePlayer(sender, args);
                    return true;
                }

                // power command
                if  (args[0].equalsIgnoreCase("power")) {
                    PowerCommand command = new PowerCommand();
                    command.powerCheck(sender, args);
                    return true;
                }

                // sethome command
                if (args[0].equalsIgnoreCase("sethome")|| args[0].equalsIgnoreCase("sh")) {
                    SetHomeCommand command = new SetHomeCommand();
                    command.setHome(sender);
                    return true;
                }

                // home command
                if (args[0].equalsIgnoreCase("home")) {
                    HomeCommand command = new HomeCommand();
                    command.teleportPlayer(sender);
                    return true;
                }

                // getVersion() command
                if (args[0].equalsIgnoreCase("version")) {
                    VersionCommand command = new VersionCommand();
                    return command.showVersion(sender);
                }

                // who command
                if (args[0].equalsIgnoreCase("who")) {
                    WhoCommand command = new WhoCommand();
                    command.sendInformation(sender, args);
                    return true;
                }

                // ally command
                if (args[0].equalsIgnoreCase("ally")) {
                    AllyCommand command = new AllyCommand();
                    command.requestAlliance(sender, args);
                    return true;
                }

                // breakalliance command
                if (args[0].equalsIgnoreCase("breakalliance")|| args[0].equalsIgnoreCase("ba")) {
                    BreakAllianceCommand command = new BreakAllianceCommand();
                    command.breakAlliance(sender, args);
                    return true;
                }

                // rename command
                if (args[0].equalsIgnoreCase("rename")) {
                    RenameCommand command = new RenameCommand();
                    command.renameFaction(sender, args);
                    return true;
                }

                // lock command
                if (args[0].equalsIgnoreCase("lock")) {
                    LockCommand command = new LockCommand();
                    command.lockBlock(sender, args);
                    return true;
                }

                // unlock command
                if (args[0].equalsIgnoreCase("unlock")) {
                    UnlockCommand command = new UnlockCommand();
                    command.unlockBlock(sender, args);
                    return true;
                }

                // grantaccess command
                if (args[0].equalsIgnoreCase("grantaccess")|| args[0].equalsIgnoreCase("ga")) {
                    GrantAccessCommand command = new GrantAccessCommand();
                    command.grantAccess(sender, args);
                    return true;
                }

                // checkaccess command
                if (args[0].equalsIgnoreCase("checkaccess")|| args[0].equalsIgnoreCase("ca")) {
                    CheckAccessCommand command = new CheckAccessCommand();
                    command.checkAccess(sender, args);
                    return true;
                }

                // revokeaccess command
                if (args[0].equalsIgnoreCase("revokeaccess")|| args[0].equalsIgnoreCase("ra")) {
                    RevokeAccessCommand command = new RevokeAccessCommand();
                    command.revokeAccess(sender, args);
                    return true;
                }

                // laws command
                if (args[0].equalsIgnoreCase("laws")) {
                    LawsCommand command = new LawsCommand();
                    command.showLawsToPlayer(sender, args);
                    return true;
                }

                // addlaw command
                if (args[0].equalsIgnoreCase("addlaw")|| args[0].equalsIgnoreCase("al")) {
                    AddLawCommand command = new AddLawCommand();
                    command.addLaw(sender, args);
                    return true;
                }

                // removelaw command
                if (args[0].equalsIgnoreCase("removelaw")|| args[0].equalsIgnoreCase("rl")) {
                    RemoveLawCommand command = new RemoveLawCommand();
                    command.removeLaw(sender, args);
                    return true;
                }

                // editlaw command
                if (args[0].equalsIgnoreCase("editlaw") || args[0].equalsIgnoreCase("el")) {
                    EditLawCommand command = new EditLawCommand();
                    command.editLaw(sender, args);
                    return true;
                }

                // chat command
                if (args[0].equalsIgnoreCase("chat")) {
                    ChatCommand command = new ChatCommand();
                    command.toggleFactionChat(sender);
                    return true;
                }

                // vassalize command
                if (args[0].equalsIgnoreCase("vassalize")) {
                    VassalizeCommand command = new VassalizeCommand();
                    command.sendVassalizationOffer(sender, args);
                    return true;
                }

                // swearfealty command
                if (args[0].equalsIgnoreCase("swearfealty") || args[0].equalsIgnoreCase("sf")) {
                    SwearFealtyCommand command = new SwearFealtyCommand();
                    command.swearFealty(sender, args);
                    return true;
                }

                // declare independence command
                if (args[0].equalsIgnoreCase("declareindependence") || args[0].equalsIgnoreCase("di")) {
                    DeclareIndependenceCommand command = new DeclareIndependenceCommand();
                    command.declareIndependence(sender);
                    return true;
                }

                // grant independence command
                if (args[0].equalsIgnoreCase("grantindependence") || args[0].equalsIgnoreCase("gi")) {
                    GrantIndependenceCommand command = new GrantIndependenceCommand();
                    command.grantIndependence(sender, args);
                    return true;
                }

                // gate management commands
                if (args[0].equalsIgnoreCase("gate") || args[0].equalsIgnoreCase("gt")) {
                	GateCommand command = new GateCommand();
                	command.handleGate(sender, args);
                	return true;
                }

                // duel command
                if (args[0].equalsIgnoreCase("duel") || args[0].equalsIgnoreCase("dl")) {
                	DuelCommand command = new DuelCommand();
                	command.handleDuel(sender, args);
                	return true;
                }

                // invoke command
                if (args[0].equalsIgnoreCase("invoke")) {
                    InvokeCommand command = new InvokeCommand();
                    return command.invokeAlliance(sender, args);
                }
                
                // admin commands ----------------------------------------------------------------------------------

                // force command
                if (args[0].equalsIgnoreCase("force")) {
                    ForceCommand command = new ForceCommand();
                    return command.force(sender, args);
                }

                // reset power levels command
                if (args[0].equalsIgnoreCase("resetpowerlevels")|| args[0].equalsIgnoreCase("rpl")) {
                    ResetPowerLevelsCommand command = new ResetPowerLevelsCommand();
                    return command.resetPowerLevels(sender);
                }

                // bypass command
                if (args[0].equalsIgnoreCase("bypass")) {
                    BypassCommand command = new BypassCommand();
                    command.toggleBypass(sender);
                    return true;
                }

                // config command
                if (args[0].equalsIgnoreCase("config")) {
                    ConfigCommand command = new ConfigCommand();
                    command.handleConfigAccess(sender, args);
                    return true;
                }

            }
            sender.sendMessage(ChatColor.RED + LocaleManager.getInstance().getText("CommandNotRecognized"));
        }
        return false;
    }

}
