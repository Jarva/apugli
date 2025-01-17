package net.merchantpug.apugli.power;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.merchantpug.apugli.Apugli;
import net.merchantpug.apugli.access.ExplosionAccess;
import net.merchantpug.apugli.networking.ApugliPackets;
import net.merchantpug.apugli.networking.s2c.SyncRocketJumpExplosionPacket;
import net.merchantpug.apugli.registry.ApugliDamageSources;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.merchantpug.apugli.registry.ApugliTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class RocketJumpPower extends ActiveCooldownPower {
    private Key key;
    private final double distance;
    private final DamageSource source;
    private final float amount;
    private final double horizontalVelocity;
    private final double verticalVelocity;
    private final double velocityClampMultiplier;
    private final boolean useCharged;
    private final List<Modifier> chargedModifiers = new LinkedList<>();
    private final List<Modifier> waterModifiers = new LinkedList<>();
    private final List<Modifier> damageModifiers = new LinkedList<>();
    private final Predicate<Pair<Entity, Entity>> targetableBiEntityCondition;
    private final Predicate<Pair<Entity, Entity>> damageBiEntityCondition;

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<RocketJumpPower>(Apugli.identifier("rocket_jump"),
                new SerializableData()
                        .add("cooldown", SerializableDataTypes.INT, 1)
                        .add("distance", SerializableDataTypes.DOUBLE, Double.NaN)
                        .add("source", SerializableDataTypes.DAMAGE_SOURCE, null)
                        .add("amount", SerializableDataTypes.FLOAT, 0.0F)
                        .add("velocity", SerializableDataTypes.DOUBLE, 1.0D)
                        .addFunctionedDefault("horizontal_velocity", SerializableDataTypes.DOUBLE, data -> data.getDouble("velocity"))
                        .addFunctionedDefault("vertical_velocity", SerializableDataTypes.DOUBLE, data -> data.getDouble("velocity"))
                        .add("velocity_clamp_multiplier", SerializableDataTypes.DOUBLE, 1.8D)
                        .add("use_charged", SerializableDataTypes.BOOLEAN, false)
                        .add("charged_modifier", Modifier.DATA_TYPE, null)
                        .add("charged_modifiers", Modifier.LIST_TYPE, null)
                        .add("water_modifier", Modifier.DATA_TYPE, null)
                        .add("water_modifiers", Modifier.LIST_TYPE, null)
                        .add("damage_modifier", Modifier.DATA_TYPE, null)
                        .add("damage_modifiers", Modifier.LIST_TYPE, null)
                        .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                        .add("targetable_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("damage_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("key", ApoliDataTypes.KEY, new Active.Key()),
                (data) ->
                        (type, entity) ->  {
                            RocketJumpPower power = new RocketJumpPower(
                                    type,
                                    entity,
                                    data.getInt("cooldown"),
                                    data.get("hud_render"),
                                    data.getDouble("distance"),
                                    data.get("source"),
                                    data.getFloat("amount"),
                                    data.getDouble("horizontal_velocity"),
                                    data.getDouble("vertical_velocity"),
                                    data.getDouble("velocity_clamp_multiplier"),
                                    data.getBoolean("use_charged"),
                                    data.get("damage_bientity_condition"),
                                    data.get("targetable_bientity_condition"));
                            power.setKey(data.get("key"));
                            if(data.isPresent("charged_modifier")) {
                                power.addChargedJumpModifier(data.get("charged_modifier"));
                            }
                            if(data.isPresent("charged_modifiers")) {
                                ((List<Modifier>)data.get("charged_modifiers")).forEach(power::addChargedJumpModifier);
                            }
                            if(data.isPresent("water_modifier")) {
                                power.addWaterJumpModifier(data.get("water_modifier"));
                            }
                            if(data.isPresent("water_modifiers")) {
                                ((List<Modifier>)data.get("water_modifiers")).forEach(power::addWaterJumpModifier);
                            }
                            if(data.isPresent("damage_modifier")) {
                                power.addDamageModifier(data.get("damage_modifier"));
                            }
                            if(data.isPresent("damage_modifiers")) {
                                ((List<Modifier>)data.get("damage_modifiers")).forEach(power::addDamageModifier);
                            }
                            return power;
                        })
                .allowCondition();
    }

    public RocketJumpPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, double distance, DamageSource source, float amount, double horizontalVelocity, double verticalVelocity, double velocityClampMultiplier, boolean useCharged, Predicate<Pair<Entity, Entity>> damageBiEntityCondition, Predicate<Pair<Entity, Entity>> targetableBiEntityCondition) {
        super(type, entity, cooldownDuration, hudRender, null);
        this.distance = distance;
        this.source = source;
        this.amount = amount;
        this.horizontalVelocity = horizontalVelocity;
        this.verticalVelocity = verticalVelocity;
        this.velocityClampMultiplier = velocityClampMultiplier;
        this.useCharged = useCharged;
        this.damageBiEntityCondition = damageBiEntityCondition;
        this.targetableBiEntityCondition = targetableBiEntityCondition;
    }

    @Override
    public void onUse() {
        if (canUse() && !entity.world.isClient()) {
            double baseReach = (entity instanceof PlayerEntity && ((PlayerEntity) entity).getAbilities().creativeMode) ? 5.0D : 4.5D;
            double reach = FabricLoader.getInstance().isModLoaded("reach-entity-attributes") ? ReachEntityAttributes.getReachDistance(entity, baseReach) : baseReach;
            double distance = !Double.isNaN(this.distance) ? this.distance : reach;
            Vec3d eyePosition = entity.getCameraPosVec(0);
            Vec3d lookVector = entity.getRotationVec(0).multiply(distance);
            Vec3d traceEnd = eyePosition.add(lookVector);

            RaycastContext context = new RaycastContext(eyePosition, traceEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity);
            BlockHitResult blockHitResult = entity.world.raycast(context);

            double baseEntityAttackRange = (entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().creativeMode) ? 6.0D : 3.0D;
            double entityAttackRange = FabricLoader.getInstance().isModLoaded("reach-entity-attributes") ? ReachEntityAttributes.getAttackRange(entity, baseEntityAttackRange) : baseEntityAttackRange;
            double entityDistance = !Double.isNaN(this.distance) ? this.distance : entityAttackRange;
            Vec3d entityLookVector = entity.getRotationVec(0).multiply(entityDistance);
            Vec3d entityTraceEnd = eyePosition.add(entityLookVector);
            Box entityBox = entity.getBoundingBox().stretch(lookVector).expand(1.0D);

            double blockHitResultSquaredDistance = blockHitResult != null ? blockHitResult.getBlockPos().getSquaredDistance(eyePosition.x, eyePosition.y, eyePosition.z) : entityDistance * entityDistance;
            double entityReach = Math.min(blockHitResultSquaredDistance, entityDistance * entityDistance);
            EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, eyePosition, entityTraceEnd, entityBox, (traceEntity) -> !traceEntity.isSpectator() && traceEntity.isCollidable() && (targetableBiEntityCondition == null || targetableBiEntityCondition.test(new Pair<>(entity, traceEntity))), entityReach);

            HitResult.Type blockHitResultType = blockHitResult.getType();
            HitResult.Type entityHitResultType = entityHitResult != null ? entityHitResult.getType() : null;

            boolean isCharged = entity.getStatusEffects().stream().anyMatch(effect -> Registries.STATUS_EFFECT.getKey(effect.getEffectType()).isPresent() && Registries.STATUS_EFFECT.entryOf(Registries.STATUS_EFFECT.getKey(effect.getEffectType()).get()).isIn(ApugliTags.CHARGED_EFFECTS));

            if (blockHitResultType == HitResult.Type.MISS && entityHitResultType == HitResult.Type.MISS) return;

            if (entityHitResultType == HitResult.Type.ENTITY) {
                this.handleRocketJump(entityHitResult, isCharged);
                return;
            }
            if (blockHitResultType == HitResult.Type.BLOCK) {
                this.handleRocketJump(blockHitResult, isCharged);
            }
        }
    }

    private void handleRocketJump(HitResult hitResult, boolean isCharged) {
        double horizontalVelocity = isCharged && this.useCharged && !this.getChargedModifiers().isEmpty() ? ModifierUtil.applyModifiers(entity, chargedModifiers, this.horizontalVelocity) : this.horizontalVelocity;
        double verticalVelocity = isCharged && this.useCharged && !this.getChargedModifiers().isEmpty() ? ModifierUtil.applyModifiers(entity, chargedModifiers, this.verticalVelocity) : this.verticalVelocity;
        float e = isCharged && this.useCharged ? 2.0F : 1.5F;
        if (this.source != null && this.amount != 0.0F) entity.damage(this.source, this.amount);
        float f = MathHelper.sin(entity.getYaw() * 0.017453292F) * MathHelper.cos(entity.getPitch() * 0.017453292F);
        float g = MathHelper.sin(entity.getPitch() * 0.017453292F);
        float h = -MathHelper.cos(entity.getYaw() * 0.017453292F) * MathHelper.cos(entity.getPitch() * 0.017453292F);

        Explosion explosion = new Explosion(entity.world, entity, ApugliDamageSources.jumpExplosion(entity), null, hitResult.getPos().getX(), hitResult.getPos().getY(), hitResult.getPos().getZ(), e, false, Explosion.DestructionType.KEEP);
        ((ExplosionAccess)explosion).setRocketJump(true);
        ((ExplosionAccess)explosion).setExplosionDamageModifiers(this.getDamageModifiers());
        ((ExplosionAccess)explosion).setBiEntityPredicate(this.getDamageBiEntityCondition());
        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(false);

        sendExplosionToClient(hitResult, e);

        if (entity.isTouchingWater()) {
            horizontalVelocity = !this.waterModifiers.isEmpty() ? ModifierUtil.applyModifiers(entity, waterModifiers, horizontalVelocity) : horizontalVelocity;
            verticalVelocity = !this.waterModifiers.isEmpty() ? ModifierUtil.applyModifiers(entity, waterModifiers, verticalVelocity) : verticalVelocity;
        }
        Vec3d vec = entity.getVelocity().add(f * horizontalVelocity, g * verticalVelocity, h * horizontalVelocity);
        double horizontalClamp = isCharged ? ModifierUtil.applyModifiers(entity, getChargedModifiers(), horizontalVelocity * velocityClampMultiplier) : horizontalVelocity * velocityClampMultiplier;
        double verticalClamp = isCharged ? ModifierUtil.applyModifiers(entity, getChargedModifiers(), verticalVelocity * velocityClampMultiplier) : verticalVelocity * velocityClampMultiplier;
        entity.setVelocity(MathHelper.clamp(vec.x, -horizontalClamp, horizontalClamp), MathHelper.clamp(vec.y, -verticalClamp, verticalClamp), MathHelper.clamp(vec.z, -horizontalClamp, horizontalClamp));
        entity.velocityModified = true;
        entity.fallDistance = 0;
        this.use();
    }

    public void sendExplosionToClient(HitResult hitResult, float radius) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(entity.getId());
        buf.writeDouble(hitResult.getPos().getX());
        buf.writeDouble(hitResult.getPos().getY());
        buf.writeDouble(hitResult.getPos().getZ());
        buf.writeFloat(radius);
        buf.writeIdentifier(this.getType().getIdentifier());
        SyncRocketJumpExplosionPacket packet = new SyncRocketJumpExplosionPacket(entity.getId(), hitResult.getPos().getX(), hitResult.getPos().getY(), hitResult.getPos().getZ(), radius, this.getType().getIdentifier());

        for (ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
            ApugliPackets.sendS2C(packet, player);
        }
        if (!(entity instanceof ServerPlayerEntity serverHolder)) return;
        ApugliPackets.sendS2C(packet, serverHolder);
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    public void addChargedJumpModifier(Modifier modifier) {
        this.chargedModifiers.add(modifier);
    }

    public List<Modifier> getChargedModifiers() {
        return chargedModifiers;
    }

    public void addWaterJumpModifier(Modifier modifier) {
        this.waterModifiers.add(modifier);
    }

    public List<Modifier> getWaterModifiers() {
        return waterModifiers;
    }

    public void addDamageModifier(Modifier modifier) {
        this.damageModifiers.add(modifier);
    }

    public List<Modifier> getDamageModifiers() {
        return damageModifiers;
    }

    public Predicate<Pair<Entity, Entity>> getTargetableBiEntityCondition() {
        return targetableBiEntityCondition;
    }

    public Predicate<Pair<Entity, Entity>> getDamageBiEntityCondition() {
        return damageBiEntityCondition;
    }
}
