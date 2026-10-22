package de.selsus74.kilnexpansion.kiln;

import de.selsus74.kilnexpansion.kiln.draft_kiln.DraftKilnControllerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class KilnParticleRenderer {
    //---this is serverside particles send to clients nearby via sendParticles---
    //---this should only be used if the server needs to keep track of it---
    //---e.g. with events like structure formed---
    //---makes campfire like smoke---
    public static void spawnFormedParticle(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server)) return;

        //---particle spawn position---
        Direction front = state.getValue(DraftKilnControllerBlock.FACING);
        BlockPos vent = pos.relative(front);

        double x = vent.getX() + 0.5;
        double y = vent.getY() + 0.5;
        double z = vent.getZ() + 1;

        //---count = 0 -> the next three values are the particle's velocity (times speed 1.0)---
        server.sendParticles(ParticleTypes.FIREWORK, x, y, z, 10, 0.0, 0.1, 0.0, 0.1);
    }
}