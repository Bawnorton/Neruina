package com.bawnorton.neruina.render.overlay;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Cube extends Cuboid {
    public Cube(BlockPos pos, Colour color) {
        super(pos, new Vec3i(1, 1, 1), color);
    }
}
