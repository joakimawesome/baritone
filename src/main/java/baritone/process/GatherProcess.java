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
import baritone.api.pathing.goals.*;
import baritone.api.process.IGatherProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import baritone.pathing.movement.MovementHelper;
import baritone.utils.BaritoneProcessHelper;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Optional;

public final class GatherProcess extends BaritoneProcessHelper implements IGatherProcess {
    private boolean active;
    private List<BlockPos> knownLocations;
    private int tickCount;
    private MaterialType type;
    private int level;

    public GatherProcess(Baritone baritone) {
        super(baritone);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void gather(String type, String level) {
        active = true;
        knownLocations = null;
    }

    private boolean readyForGather(BlockPos pos, int tickNextGather) {
        // TODO: Check if 2400 ticks had passed since gather
        return tickCount >= tickNextGather;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        /**
         * Have list of waypoints of material sources
         * Must left or right click upon waypoint destination
         * Sleep code for duration of proficiency-level function
         * Begin 60s (2400 ticks) timer on
         */
        // TODO: if (all material sources not available because 2400 tick cool-down) {
//            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
//          }
        baritone.getInputOverrideHandler().clearAllKeys();
        for (BlockPos pos : materialSource) {
            Optional<Rotation> rot = RotationUtils.reachable(ctx, pos);
            if (rot.isPresent() && isSafeToCancel) {
                baritone.getLookBehavior().updateTarget(rot.get(), true);
            }
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE); // Pause pathing for gather duration
        }

        // TODO: handle calcFailed ->
        //  onLostControl(),
        //  new PathingCommand(null, PathingCommandType.REQUEST_PAUSE)
        // if material type A:
        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
        // if material type B:
        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
        Goal goal = new GoalComposite(knownLocations.stream().map(this::createGoal).toArray(Goal[]::new));
        return new PathingCommand(goal, PathingCommandType.SET_GOAL_AND_PATH);
    }

    public Goal createGoal(BlockPos pos) {
        return new GoalGetToBlock(pos);
    }

    @Override
    public void onLostControl() {
        active = false;
    }

    @Override
    public String displayName0() {
        return "Gathering " + type + "of level " + level;
    }
}
