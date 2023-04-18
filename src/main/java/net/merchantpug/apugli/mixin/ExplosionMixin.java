<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/ExplosionMixin.java
package net.merchantpug.apugli.mixin;
========
package com.github.merchantpug.apugli.mixin.xplatforn.common;
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/common/ExplosionMixin.java

import net.merchantpug.apugli.access.ExplosionAccess;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/ExplosionMixin.java
import net.merchantpug.apugli.access.ParticleAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
========
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/common/ExplosionMixin.java
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(Explosion.class)
@Implements(@Interface(iface = ExplosionAccess.class, prefix = "apugli$"))
public abstract class ExplosionMixin {
    @Unique private boolean apugli$rocketJumpExplosion;
    @Unique private Entity apugli$affectedEntity;
    @Unique private List<Modifier> apugli$explosionDamageModifiers;
    @Unique private Predicate<Pair<Entity, Entity>> apugli$rocketJumpBiEntityCondition;

    @Shadow @Final private Level world;

    @Shadow public abstract @Nullable LivingEntity getCausingEntity();

    @ModifyArg(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float changeDamage(float amount) {
<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/ExplosionMixin.java
        if (this.apugli$isRocketJump()) {
========
        if(this.isRocketJump()) {
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/common/ExplosionMixin.java
            return amount = (float) ModifierUtil.applyModifiers(this.getCausingEntity(), apugli$explosionDamageModifiers, amount);
        }
        return amount;
    }

    @ModifyArgs(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"))
    private void modifyOtherEntitiesKnockback(Args args) {
<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/ExplosionMixin.java
        if (this.apugli$isRocketJump()) {
========
        if(this.isRocketJump()) {
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/common/ExplosionMixin.java
            args.set(0, (double)args.get(0) * 0.75);
            args.set(1, (double)args.get(1) * 0.5);
            args.set(2, (double)args.get(2) * 0.75);
        }
    }

<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/ExplosionMixin.java
    @Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isImmuneToExplosion()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void collectAffectedEntity(CallbackInfo ci, Set set, int k, float f, int l, int m, int r, int s, int t, int u, List list, Vec3d vec3d, int v, Entity entity) {
========
    @Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isImmuneToExplosion()Z"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void collectAffectedEntity(CallbackInfo ci, Set set, int k, float f, int l, int m, int r, int s, int t, int u, List list, Vec3 vec3d, int v, Entity entity) {
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/common/ExplosionMixin.java
        this.apugli$affectedEntity = entity;
    }

    @ModifyExpressionValue(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isImmuneToExplosion()Z"))
    private boolean cancelDamagedEntity(boolean original) {
<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/ExplosionMixin.java
        if (this.apugli$isRocketJump()) {
            return original || apugli$rocketJumpBiEntityCondition != null && !apugli$rocketJumpBiEntityCondition.test(new Pair<>(this.getCausingEntity(), this.apugli$affectedEntity));
========
        if(this.isRocketJump()) {
            return original || !(apugli$affectedEntity instanceof LivingEntity) || apugli$affectedEntity instanceof ArmorStand;
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/common/ExplosionMixin.java
        }
        return original;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"), index = 5)
    private float reduceVolumeOfRocketJump(float volume) {
<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/ExplosionMixin.java
        if (this.apugli$isRocketJump()) {
========
        if(this.isRocketJump()) {
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/common/ExplosionMixin.java
            return volume = 0.25F;
        }
        return volume;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"), index = 6)
    private float increasePitchOfRocketJump(float pitch) {
<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/ExplosionMixin.java
        if (this.apugli$isRocketJump()) {
========
        if(this.isRocketJump()) {
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/common/ExplosionMixin.java
            return pitch = (1.4f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f;
        }
        return pitch;
    }

    public void apugli$setRocketJump(boolean value) {
        this.apugli$rocketJumpExplosion = value;
    }

    public boolean apugli$isRocketJump() {
        return this.apugli$rocketJumpExplosion;
    }

    public void apugli$setExplosionDamageModifiers(List<Modifier> value) {
        this.apugli$explosionDamageModifiers = value;
    }

    public void apugli$setBiEntityPredicate(Predicate<Pair<Entity, Entity>> value) {
        this.apugli$rocketJumpBiEntityCondition = value;
    }
}
