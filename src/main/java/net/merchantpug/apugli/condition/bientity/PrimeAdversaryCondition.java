package net.merchantpug.apugli.condition.bientity;

import net.merchantpug.apugli.Apugli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

public class PrimeAdversaryCondition {
    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        return pair.getRight() instanceof LivingEntity && ((LivingEntity) pair.getRight()).getPrimeAdversary() != null && ((LivingEntity) pair.getRight()).getPrimeAdversary().equals(pair.getLeft());
    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(Apugli.identifier("prime_adversary"), new SerializableData(),
                PrimeAdversaryCondition::condition
        );
    }
}
