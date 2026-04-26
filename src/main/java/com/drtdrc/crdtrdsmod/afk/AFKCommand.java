package com.drtdrc.crdtrdsmod.afk;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class AFKCommand {
    private AFKCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        dispatcher.register(
                Commands.literal("afk")
                        .requires(src -> src.getEntity() instanceof ServerPlayer)
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            AFKManager.goAFKAndKick(player);
                            return 1;
                        })
        );

        dispatcher.register(
                Commands.literal("afk")
                        .then(Commands.literal("anchor")
                                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.literal("add")
                                        .executes(ctx -> {
                                            var src = ctx.getSource();
                                            var level = src.getLevel();
                                            BlockPos pos = BlockPos.containing(src.getPosition());
                                            boolean ok = AFKManager.addAnchor(level, pos, AFKManager.getDefaultName(pos));
                                            src.sendSuccess(() -> Component.literal(ok ? "Anchor added at your position" : "Anchor already exists here"), true);
                                            return ok ? 1 : 0;
                                        })
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(ctx -> {
                                                    var src = ctx.getSource();
                                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                                    boolean ok = AFKManager.addAnchor(src.getLevel(), pos, AFKManager.getDefaultName(pos));
                                                    src.sendSuccess(() -> Component.literal(ok ? "Anchor added" : "Anchor already exists here"), true);
                                                    return ok ? 1 : 0;
                                                })
                                                .then(Commands.argument("name", StringArgumentType.string())
                                                        .executes(ctx -> {
                                                            var src = ctx.getSource();
                                                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                                            String name = StringArgumentType.getString(ctx, "name");
                                                            boolean ok = AFKManager.addAnchor(src.getLevel(), pos, name);
                                                            src.sendSuccess(() -> Component.literal(ok ? "Anchor added" : "Anchor already exists here"), true);
                                                            return ok ? 1 : 0;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(ctx -> {
                                                    var src = ctx.getSource();
                                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                                    boolean ok = AFKManager.removeAnchor(src.getLevel(), pos, AFKManager.getDefaultName(pos));
                                                    if (ok) src.sendSuccess(() -> Component.literal("Anchor removed"), true);
                                                    else src.sendFailure(Component.literal("No anchor at this position"));
                                                    return ok ? 1 : 0;
                                                })
                                        )
                                        .then(Commands.argument("name", StringArgumentType.word())
                                                .suggests(ANCHOR_SUGGESTIONS)
                                                .executes(ctx -> {
                                                    var src = ctx.getSource();
                                                    var level = src.getLevel();
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    boolean ok = AFKManager.removeAnchor(level, null, name);
                                                    if (ok) src.sendSuccess(() -> Component.literal("Removed anchors named " + name), true);
                                                    else src.sendFailure(Component.literal("No anchors named " + name + " in this world"));
                                                    return ok ? 1 : 0;
                                                })
                                        )
                                        .then(Commands.literal("all")
                                                .executes(ctx -> {
                                                    var src = ctx.getSource();
                                                    var level = src.getLevel();
                                                    List<AFKAnchorsState.AFKAnchor> anchors = AFKAnchorsState.get(level).getAllEntries();
                                                    if (anchors.isEmpty()) {
                                                        src.sendSuccess(() -> Component.literal("No anchors removed"), true);
                                                        return 0;
                                                    }
                                                    for (AFKAnchorsState.AFKAnchor a : anchors) {
                                                        AFKManager.removeAnchor(level, a.pos(), a.name());
                                                    }
                                                    src.sendSuccess(() -> Component.literal("All anchors removed"), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private static final SuggestionProvider<CommandSourceStack> ANCHOR_SUGGESTIONS = (ctx, builder) -> {
        ServerLevel level = ctx.getSource().getLevel();
        for (var e : AFKAnchorsState.get(level).getAllEntries()) {
            builder.suggest(e.name());
        }
        return builder.buildFuture();
    };
}
