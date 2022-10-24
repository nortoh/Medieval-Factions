package factionsplusplus.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import factionsplusplus.models.Command;
import factionsplusplus.models.CommandContext;
import factionsplusplus.models.ConfigurationFlag;
import org.bukkit.ChatColor;

import java.util.stream.Collectors;
import factionsplusplus.builders.CommandBuilder;
import factionsplusplus.builders.MessageBuilder;
import factionsplusplus.builders.ArgumentBuilder;

// TODO: implement tab complete for basic values (i.e. true/false for boolean)

/**
 * @author Callum Johnson
 */
@Singleton
public class WorldFlagCommand extends Command {

    @Inject
    public WorldFlagCommand() {
        super(
            new CommandBuilder()
                .withName("worldflag")
                .withAliases(LOCALE_PREFIX + "CmdWorldFlag")
                .withDescription("Manage world flags.")
                .requiresPermissions("mf.admin", "mf.admin.world")
                .requiresSubCommand()
                .expectsPlayerExecution()
                .addSubCommand(
                    new CommandBuilder()
                        .withName("show")
                        .withAliases(LOCALE_PREFIX + "CmdWorldFlagShow")
                        .withDescription("Shows a worlds flags.")
                        .setExecutorMethod("showCommand")
                )
                .addSubCommand(
                    new CommandBuilder()
                        .withName("set")
                        .withAliases(LOCALE_PREFIX + "CmdWorldFlagSet")
                        .withDescription("Sets a faction flag.")
                        .setExecutorMethod("setCommand")
                        .addArgument(
                            "flag name",
                            new ArgumentBuilder()
                                .setDescription("the flag you are setting")
                                .expectsWorldFlagName()
                                .isRequired()
                        )
                        .addArgument(
                            "value",
                            new ArgumentBuilder()
                                .setDescription("the value of the flag you are setting")
                                .expectsString()
                                .consumesAllLaterArguments()
                                .isRequired()
                        )
                )
        );
    }
    
    public void setCommand(CommandContext context) {
        final ConfigurationFlag flag = context.getConfigurationFlagArgument("flag name");
        final String flagValue = context.getStringArgument("value");
        String newValue = flag.set(flagValue);
        if (newValue == null) {
            context.replyWith(
                new MessageBuilder("ConfigurationFlagValueInvalid")
                    .with("type", flag.getRequiredType().toString())
            );
            return;
        }
        context.replyWith(
            new MessageBuilder("ConfigurationFlagValueSet")
                .with("value", newValue)
        );
    }

    public void showCommand(CommandContext context) {
        String flagOutput = context.getExecutorsWorld().getFlags()
            .keySet()
            .stream()
            .map(flagKey -> String.format("%s: %s", flagKey, context.getExecutorsWorld().getFlags().get(flagKey).toString()))
            .collect(Collectors.joining(", "));
        context.reply(ChatColor.AQUA + "[World Flags] " + flagOutput);
    }
}