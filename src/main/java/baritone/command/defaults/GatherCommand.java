package baritone.command.defaults;

import baritone.api.IBaritone;
import baritone.api.cache.IWaypoint;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.ForWaypoints;
import baritone.api.command.exception.CommandException;
import baritone.api.command.helpers.TabCompleteHelper;
import baritone.api.utils.BetterBlockPos;

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
        //type
        if (args.has(1)) {
            type = args.getString();
        }
        //level
        if (args.has(1)) {
            level = args.getString();
        }
        baritone.getGatherProcess().gather(type, level);
        logDirect("Gathering");
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            // Tab completion for the first argument (material type)
            return new TabCompleteHelper()
                    .append(Stream.of("wood", "paper", "ingot", "gem", "string", "grain", "oil", "meat"))
        } else if (args.has(2)) {
            // Tab completion for the second argument (level)
            return Stream.of("1", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110")
                    .filter(s -> s.startsWith(args.getString()))
                    .sorted();
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
                // TODO: "> gather <material_type> - gathers from nearest material cluster.",
        );
    }
}
