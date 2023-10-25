/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.process;

import baritone.Baritone;
import baritone.api.cache.IWaypoint;
import baritone.api.pathing.goals.*;
import baritone.api.process.IGatherProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import baritone.utils.BaritoneProcessHelper;
import net.minecraft.core.BlockPos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GatherProcess extends BaritoneProcessHelper implements IGatherProcess {
    private boolean active;
    private List<BlockPos> sourceLocations;
    private String type;
    private int tickCount = 0;
    private final HashMap<BlockPos, Integer> cooldownMap = new HashMap<>();

    public GatherProcess(Baritone baritone) {
        super(baritone);
    }

    @Override
    public void gather(IWaypoint[] sources, String type) {
        active = true;
        sourceLocations = Arrays.stream(sources)
                .map(IWaypoint::getLocation)
                .collect(Collectors.toList());
        this.type = type;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private boolean readyForGather(BlockPos pos) {
        int COOLDOWN = 800;
        Integer tickGathered = cooldownMap.getOrDefault(pos, -COOLDOWN);
        return tickCount - tickGathered >= COOLDOWN;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        tickCount++;
        baritone.getInputOverrideHandler().clearAllKeys();
        List<BlockPos> readyLocations = sourceLocations.stream().filter(this::readyForGather).toList(); // filter out locations on cd
        if (readyLocations.isEmpty()) {
            if (tickCount % 200 == 0) {
                logDirect(cooldownMap.toString());
                logDirect(String.format("%ss",tickCount / 20));
            }
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }
        for (BlockPos pos : readyLocations) {
            Optional<Rotation> rot = RotationUtils.reachable(ctx, pos);
            if (rot.isPresent() && isSafeToCancel) {
                baritone.getLookBehavior().updateTarget(rot.get(), true);
                if (ctx.isLookingAt(pos)) {
                    logDirect("Gathering...");
                    if (isRightClick(this.type)) {
                        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
                    } else {
                        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
                    }
                    cooldownMap.put(pos, tickCount); // place location on cooldown
                }
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
            }
        }
        Goal goal = new GoalComposite(readyLocations.stream().map(this::createGoal).toArray(Goal[]::new));
        if (calcFailed) {
            logDirect("Gathering failed");
            if (Baritone.settings().notificationOnGatherFail.value) {
                logNotification("Gathering failed", true);
            }
            if (isSafeToCancel) {
                onLostControl();
            }
            return new PathingCommand(goal, PathingCommandType.CANCEL_AND_SET_GOAL);
        }
        return new PathingCommand(goal, PathingCommandType.REVALIDATE_GOAL_AND_PATH);
    }

    public Goal createGoal(BlockPos pos) {
        return new GoalGetToBlock(pos);
    }

    public boolean isRightClick(String type) {
        return "gem".equals(type)
            || "paper".equals(type)
            || "grains".equals(type)
            || "meat".equals(type);
    }

    @Override
    public void onLostControl() {
        active = false;
        sourceLocations = null;
        baritone.getInputOverrideHandler().clearAllKeys();
    }

    @Override
    public String displayName0() {
        return "Gathering";
    }
}
