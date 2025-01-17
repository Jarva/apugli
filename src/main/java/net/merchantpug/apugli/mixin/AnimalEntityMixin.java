package net.merchantpug.apugli.mixin;

import net.merchantpug.apugli.power.ModifyBreedingCooldownPower;
import net.merchantpug.apugli.power.PreventBreedingPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity {
    @Shadow public abstract boolean isBreedingItem(ItemStack stack);

    @Shadow public abstract boolean canEat();

    @Shadow private int loveTicks;
    @Unique private AnimalEntity apugli$otherAnimalEntity;
    @Unique private ServerPlayerEntity apugli$serverPlayerEntity;

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void preventMobBreeding(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir, ItemStack itemStack) {
        List<PreventBreedingPower> preventBreedingPowerList = PowerHolderComponent.getPowers(player, PreventBreedingPower.class).stream().filter(power -> power.doesApply(this)).collect(Collectors.toList());
        if (preventBreedingPowerList.isEmpty()) return;
        if (this.isBreedingItem(itemStack)) {
            int i = this.getBreedingAge();
            if (i == 0 && this.canEat()) {
                if (preventBreedingPowerList.stream().anyMatch(PreventBreedingPower::hasAction)) {
                    preventBreedingPowerList.forEach(power -> power.executeAction(this));
                    this.loveTicks = (int)PowerHolderComponent.modify(player, ModifyBreedingCooldownPower.class, 600);
                    cir.setReturnValue(ActionResult.SUCCESS);
                } else {
                    cir.setReturnValue(ActionResult.FAIL);
                }
            }
        }
    }

    @Inject(method = "breed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;incrementStat(Lnet/minecraft/util/Identifier;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void captureBreedLocals(ServerWorld world, AnimalEntity other, CallbackInfo ci, PassiveEntity passiveEntity, ServerPlayerEntity serverPlayerEntity) {
        this.apugli$otherAnimalEntity = other;
        this.apugli$serverPlayerEntity = serverPlayerEntity;
    }

    @ModifyArg(method = "breed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AnimalEntity;setBreedingAge(I)V", ordinal = 0))
    private int modifyThisAnimalBreed(int age) {
        return (int)PowerHolderComponent.modify(apugli$serverPlayerEntity, ModifyBreedingCooldownPower.class, age, p -> p.doesApply((Entity)(Object)this));
    }

    @ModifyArg(method = "breed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AnimalEntity;setBreedingAge(I)V", ordinal = 1))
    private int modifyOtherAnimalBreed(int age) {
        return (int)PowerHolderComponent.modify(apugli$serverPlayerEntity, ModifyBreedingCooldownPower.class, age, p -> p.doesApply(apugli$otherAnimalEntity));
    }
}
