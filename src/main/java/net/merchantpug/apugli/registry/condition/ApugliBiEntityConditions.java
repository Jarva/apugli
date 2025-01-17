package net.merchantpug.apugli.registry.condition;

import net.merchantpug.apugli.condition.bientity.PrimeAdversaryCondition;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.merchantpug.apugli.condition.bientity.HitsOnTargetCondition;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;

public class ApugliBiEntityConditions {
    public static void register() {
        register(HitsOnTargetCondition.getFactory());
        register(PrimeAdversaryCondition.getFactory());
    }

    private static void register(ConditionFactory<Pair<Entity, Entity>> conditionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
