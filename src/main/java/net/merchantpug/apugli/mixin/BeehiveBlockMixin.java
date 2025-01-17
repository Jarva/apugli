package net.merchantpug.apugli.mixin;

import net.merchantpug.apugli.power.PreventBeeAngerPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BeehiveBlock.class)
public class BeehiveBlockMixin {
    @Inject(method = "afterBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BeehiveBlockEntity;angerBees(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BeehiveBlockEntity$BeeState;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void releaseBeesIfAngerPrevented(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack, CallbackInfo ci, BeehiveBlockEntity beehiveBlockEntity) {
        if (!PowerHolderComponent.hasPower(player, PreventBeeAngerPower.class)) return;
        ((BeehiveBlockEntityAccessor)beehiveBlockEntity).invokeTryReleaseBee(state, BeehiveBlockEntity.BeeState.EMERGENCY);
    }

    @Inject(method = "angerNearbyBees", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void dontAngerBees(World world, BlockPos pos, CallbackInfo ci, List<BeeEntity> list, List<PlayerEntity> list2) {
        if (list2.stream().anyMatch(player -> PowerHolderComponent.hasPower(player, PreventBeeAngerPower.class))) {
            ci.cancel();
        }
    }
}
