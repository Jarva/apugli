package net.merchantpug.apugli.registry.condition;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.merchantpug.apugli.condition.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;

public class ApugliEntityConditions {
    public static void register() {
        register(AttackerConditionCondition.getFactory());
        register(AttackTargetConditionCondition.getFactory());
        register(CanHaveEffectCondition.getFactory());
        register(CanTakeDamageCondition.getFactory());
        register(CompareResourceCondition.getFactory());
        register(EntityInRadiusCondition.getFactory());
        register(HostileCondition.getFactory());
        register(JoinInvulnerabilityTicksCondition.getFactory());
        register(KeyPressedCondition.getFactory());
        register(MaxHealthCondition.getFactory());
        register(MovingCondition.getFactory());
        register(ParticleInRadiusCondition.getFactory());
        register(PlayerModelTypeCondition.getFactory());
        register(RaycastCondition.getFactory());
        register(StructureCondition.getFactory());
        register(TridentEnchantmentCondition.getFactory());
        register(VelocityCondition.getFactory());
    }

    protected static void register(ConditionFactory<Entity> conditionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
