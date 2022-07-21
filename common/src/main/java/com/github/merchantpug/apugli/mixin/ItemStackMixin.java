package com.github.merchantpug.apugli.mixin;

import com.github.merchantpug.apugli.access.ItemStackAccess;
import com.github.merchantpug.apugli.powers.ActionOnDurabilityChange;
import com.github.merchantpug.apugli.powers.EdibleItemPower;
import com.github.merchantpug.apugli.powers.ModifyEnchantmentLevelPower;
import io.github.apace100.origins.component.OriginComponent;
import com.github.merchantpug.apugli.util.ItemStackFoodComponentUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackAccess {
    @Shadow
    public abstract Item getItem();

    @Unique
    public Entity entity;

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void cacheEntity(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (this.getEntity() == null) this.setEntity(entity);
    }

    @Inject(method = "copy", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setCooldown(I)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void copyEntity(CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack) {
        if (this.getEntity() != null) {
            ((ItemStackAccess) (Object) itemStack).setEntity(this.getEntity());
        }
    }

    @Inject(method = "addEnchantment", at = @At(value = "TAIL"))
    private void addEnchantment(Enchantment enchantment, int level, CallbackInfo ci) {
        ModifyEnchantmentLevelPower.updateEnchantments((ItemStack) (Object) this);
    }

    public void setEntity(Entity entity) { this.entity = entity; }

    public Entity getEntity() {
        return this.entity;
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void executeEntityActions(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if(user != null) {
            OriginComponent.getPowers(user, EdibleItemPower.class).stream().filter(p -> p.doesApply((ItemStack)(Object)this)).forEach(EdibleItemPower::eat);
        }
    }

    @Inject(method = "damage(ILjava/util/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getDamage()I", ordinal = 1))
    private void executeActionOnDurabilityDecrease(int amount, Random random, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        OriginComponent.getPowers(player, ActionOnDurabilityChange.class).stream().filter(p -> p.doesApply((ItemStack)(Object)this)).forEach(ActionOnDurabilityChange::executeDecreaseAction);
    }

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private <T extends LivingEntity> void executeActionBroken(int amount, T entity, Consumer<T> breakCallback, CallbackInfo ci) {
        OriginComponent.getPowers(entity, ActionOnDurabilityChange.class).stream().filter(p -> p.doesApply((ItemStack)(Object)this)).forEach(ActionOnDurabilityChange::executeBreakAction);
    }

    @Unique
    protected FoodComponent stackFoodComponent;
    @Unique
    protected UseAction useAction;
    @Unique
    protected ItemStack returnStack;
    @Unique
    protected SoundEvent eatSound;


    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void finishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!(user instanceof PlayerEntity)) return;
        if (this.isItemStackFood()) {
            OriginComponent.getPowers(user, EdibleItemPower.class).stream().filter(p -> p.doesApply((ItemStack)(Object)this)).forEach(EdibleItemPower::eat);
            if (this.getReturnStack() != null) {
                cir.setReturnValue(user instanceof PlayerEntity && ((PlayerEntity)user).abilities.creativeMode ? user.eatFood(world, (ItemStack)(Object)this) : this.getReturnStack());
            }
            cir.setReturnValue(user.eatFood(world, (ItemStack)(Object)this));
        }
    }

    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    private void getUseAction(CallbackInfoReturnable<UseAction> cir) {
        if (this.isItemStackFood()) {
            cir.setReturnValue(this.getFoodUseAction() != null ? this.getFoodUseAction() : UseAction.EAT);
        }
    }

    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    private void getMaxUseTime(CallbackInfoReturnable<Integer> cir) {
        if (isItemStackFood()) {
            cir.setReturnValue(this.getItemStackFoodComponent().isSnack() ? 16 : 32);
        }
    }

    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    private void isFood(CallbackInfoReturnable<Integer> cir) {
        if (this.isItemStackFood()) {
            cir.setReturnValue(this.getItemStackFoodComponent().isSnack() ? 16 : 32);
        }
    }

    @Inject(method = "copy", at = @At("RETURN"))
    private void copyFoodComponent(CallbackInfoReturnable<ItemStack> cir) {
        if (this.isItemStackFood()) {
            ItemStackFoodComponentUtil.setStackFood(cir.getReturnValue(), this.stackFoodComponent, this.useAction, this.returnStack, this.eatSound);
        }
    }

    @Inject(method = "getDrinkSound", at = @At("HEAD"), cancellable = true)
    private void getDrinkSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.getStackEatSound() != null) {
            cir.setReturnValue(this.getStackEatSound());
        }
    }

    @Inject(method = "getEatSound", at = @At("HEAD"), cancellable = true)
    private void getEatSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.getStackEatSound() != null) {
            cir.setReturnValue(this.getStackEatSound());
        }
    }

    @Override
    public FoodComponent getItemStackFoodComponent() {
        return this.stackFoodComponent;
    }

    @Override
    public void setItemStackFoodComponent(FoodComponent stackFoodComponent) {
        this.stackFoodComponent = stackFoodComponent;
    }

    @Override
    public boolean isItemStackFood() {
        return this.stackFoodComponent != null;
    }

    @Override
    public UseAction getFoodUseAction() {
        return this.useAction;
    }

    @Override
    public void setFoodUseAction(UseAction useAction) {
        if (useAction == UseAction.EAT || useAction == UseAction.DRINK) {
            this.useAction = useAction;
        }
    }

    @Override
    public ItemStack getReturnStack() {
        return this.returnStack;
    }

    @Override
    public void setReturnStack(ItemStack stack) {
        this.returnStack = stack;
    }

    @Override
    public SoundEvent getStackEatSound() {
        return this.eatSound;
    }

    @Override
    public void setStackEatSound(SoundEvent sound) {
        this.eatSound = sound;
    }
}