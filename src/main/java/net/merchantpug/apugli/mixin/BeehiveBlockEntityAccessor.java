<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/BeehiveBlockEntityAccessor.java
package net.merchantpug.apugli.mixin;
========
package com.github.merchantpug.apugli.mixin.xplatforn.common.accessor;
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/common/accessor/BeehiveBlockEntityAccessor.java

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BeehiveBlockEntity.class)
public interface BeehiveBlockEntityAccessor {
    @Invoker("tryReleaseBee")
    List<Entity> invokeTryReleaseBee(BlockState state, BeehiveBlockEntity.BeeReleaseStatus beeState);
}
