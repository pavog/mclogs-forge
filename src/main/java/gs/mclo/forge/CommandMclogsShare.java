package gs.mclo.forge;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.Commands.literal;

public class CommandMclogsShare {
    static ArgumentBuilder<CommandSource, ?> register() {
        return literal("share")
            .requires(source -> source.hasPermissionLevel(2))
            .then(Commands.argument("filename", StringArgumentType.greedyString())
            .suggests(CommandMclogsShare::suggest)
            .executes(context ->  MclogsForgeLoader.share(context.getSource(), context.getArgument("filename",String.class))));
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        ImmutableList.Builder<Suggestion> suggestions = ImmutableList.builder();
        String[] logs, reports;
        try {
            logs = MclogsForgeLoader.getLogs(context);
            reports = MclogsForgeLoader.getCrashReports(context);
        } catch (IOException e) {
            MclogsForgeLoader.logger.error("Failed to suggest log files", e);
            return Suggestions.empty();
        }

        String argument = context.getArgument("filename", String.class);
        int start = "/mclogs share ".length();

        for (String log: logs) {
            if (!log.startsWith(argument)) continue;
            suggestions.add(new Suggestion(StringRange.between(start, context.getInput().length()), log));
        }

        for (String report: reports) {
            if (!report.startsWith(argument)) continue;
            suggestions.add(new Suggestion(StringRange.between(start, context.getInput().length()), report));
        }

        return CompletableFuture.completedFuture(Suggestions.create("mclogs", suggestions.build()));
    }
}
