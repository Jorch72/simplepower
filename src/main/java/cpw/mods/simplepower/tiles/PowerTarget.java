/*
 * Simple Power
 *   A minecraft mod for simple wireless transportation of Forge Energy
 * Copyright © 2016 cpw
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cpw.mods.simplepower.tiles;

import com.google.common.base.Throwables;
import cpw.mods.simplepower.SimplePower;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import scala.tools.nsc.interpreter.Power;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by cpw on 17/09/16.
 */
public abstract class PowerTarget extends TileEntity
{
    protected List<PowerTarget> associates = new ArrayList<>();

    public void onLoad()
    {
        if (getWorld() instanceof WorldClient) return;
        FMLLog.info("Adding to power targets %s", this);
        try
        {
            SimplePower.instance.powerTargets.get(getWorld(), ArrayList::new).add(this);
            addToTargets(this);
        }
        catch (ExecutionException e)
        {
            Throwables.propagate(e);
        }
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        if (getWorld() instanceof WorldClient) return;
        FMLLog.info("Removing from power targets %s", this);
        try
        {
            SimplePower.instance.powerTargets.get(getWorld(), ArrayList::new).remove(this);
            clearFromTargets(this);
        }
        catch (ExecutionException e)
        {
            Throwables.propagate(e);
        }
    }

    private void clearFromTargets(PowerTarget t)
    {
        List<PowerTarget> targets = SimplePower.instance.powerTargets.getIfPresent(getWorld());
        if (targets == null) return;
        targets.stream().filter(target -> target.associates.contains(t)).forEach(target -> target.removeTarget(t));
    }

    private void removeTarget(PowerTarget t) {
        associates.remove(t);
        onRemoval(t);
    }

    protected abstract void onRemoval(PowerTarget t);

    private void addToTargets(PowerTarget t)
    {
        List<PowerTarget> targets = SimplePower.instance.powerTargets.getIfPresent(getWorld());
        if (targets == null) return;
        targets.stream().filter(target -> target.accepts(t)).forEach(target -> target.addTarget(t));
    }

    protected boolean accepts(PowerTarget t) {
        if (t.getPos().distanceSq(this.getPos()) < 100d) {
            return acceptsType(t);
        }
        return false;
    }

    protected abstract boolean acceptsType(PowerTarget t);

    private void addTarget(PowerTarget t) {
        associates.add(t);
        onAddition(t);
    }

    protected abstract void onAddition(PowerTarget t);
}
