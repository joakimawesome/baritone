package baritone.command.defaults;

import baritone.api.IBaritone;
import baritone.api.cache.IWaypoint;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.ForWaypoints;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandInvalidStateException;
import baritone.api.command.helpers.TabCompleteHelper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GatherCommand extends Command {

    public GatherCommand(IBaritone baritone) {
        super(baritone, "gather");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireExactly(2);
        String type = null;
        String level = null;
        if (args.has(1)) {
            type = args.getString();
        }
        if (args.has(1)) {
            level = args.getString();
        }
        String rawType = switch (type) {
            case "ingot", "gem" -> "mine";
            case "wood", "paper" -> "wood";
            case "string", "grains" -> "farm";
            case "oil", "meat" -> "fish";
            default -> null;
        };
        IWaypoint[] source = ForWaypoints.getWaypointsByName(baritone, rawType + level);
        if (source.length == 0) {
            throw new CommandInvalidStateException("No sources found for specified material");
        }
        baritone.getGatherProcess().gather(source, type);
        logDirect(String.format("Gathering lvl. %s %s for %s . . .",level,rawType,type));
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            // Tab completion for the first argument (material type)
            return new TabCompleteHelper()
                    .append(Stream.of("ingot", "gem", "wood", "paper", "string", "grains", "oil", "meat"))
                    .sortAlphabetically()
                    .filterPrefix(args.getString())
                    .stream();
        } else if (args.has(2)) {
            args.get();
            // Tab completion for the second argument (level)
            return new TabCompleteHelper()
                    .append(Stream.of("1", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110"))
                    .filterPrefix(args.getString())
                    .stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Gather crafting materials";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "The gather command automates the process of gathering crafting materials.",
                "",
                "Usage:",
                "> gather <material_type> <material_level> - gather crafting material specified by required proficiency level."
                // TODO: "> gather <material_type> <material_name> - gather by source name.",
                // TODO: "> gather <material_type> - gathers from nearest corresponding material cluster.",
        );
    }
}
