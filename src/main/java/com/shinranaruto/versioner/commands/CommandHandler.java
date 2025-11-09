package com.shinranaruto.versioner.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.shinranaruto.versioner.Versioner;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public class CommandHandler {

    public static class SponsorsCommand {
        public static final String NAME = "sponsors";
        public static final String ARG_LIST = "list";
        public static final String ARG_CHECK = "check";

        public static LiteralArgumentBuilder<CommandSourceStack> build() {
            return Commands.literal(NAME)
                    .then(Commands.literal(ARG_LIST)
                            .executes(SponsorsCommand::executeList))
                    .then(Commands.literal(ARG_CHECK)
                            .then(Commands.argument("player", EntityArgument.players())
                                    .executes(SponsorsCommand::executeCheck)))
                    .executes(context -> {
                        context.getSource().sendFailure(Component.literal("Usage: /sponsors <list|check> [player]")
                                .withStyle(style -> style.withColor(ChatFormatting.RED)));
                        return 0;
                    });
        }

        private static int executeList(CommandContext<CommandSourceStack> context) {
            var source = context.getSource();
            var msg = Versioner.versionData != null ? Versioner.versionData.getSponsors().getFormattedText() : null;
            if (msg != null) {
                for (var line : msg) {
                    source.sendSuccess(() -> line, false);
                }
            } else {
                source.sendFailure(Component.translatable("versioner.command.sponsors.no_data"));
            }
            return Command.SINGLE_SUCCESS;
        }

        private static int executeCheck(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            var source = context.getSource();
            var players = EntityArgument.getPlayers(context, "player");

            for (var player : players) {
                var category = Versioner.versionData != null ?
                        Versioner.versionData.getSponsors().checkPlayer(player) : null;
                if (category != null) {
                    source.sendSuccess(() -> Component.translatable(
                            "versioner.command.sponsors.check_true",
                            player.getName(),
                            category.getFormattedName()
                    ), false);
                } else {
                    source.sendSuccess(() -> Component.translatable(
                            "versioner.command.sponsors.check_false",
                            player.getName()
                    ), false);
                }
            }
            return players.size();
        }
    }
}