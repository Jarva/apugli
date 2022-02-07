package io.github.merchantpug.apugli.powers;

import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.ValueModifyingPower;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import io.github.merchantpug.apugli.Apugli;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;

public class ModifyStatusEffectDurationPower extends ValueModifyingPower {
    private final StatusEffect statusEffect;

    public ModifyStatusEffectDurationPower(PowerType<?> type, PlayerEntity entity, StatusEffect statusEffect) {
        super(type, entity);
        this.statusEffect = statusEffect;
    }

    public boolean doesApply(StatusEffect statusEffect) {
        return statusEffect.equals(this.statusEffect);
    }

    public static PowerFactory getFactory() {
        return new PowerFactory<ModifyStatusEffectDurationPower>(
                Apugli.identifier("modify_status_effect_duration"),
                new SerializableData()
                        .add("status_effect", SerializableDataType.STATUS_EFFECT)
                        .add("modifier", SerializableDataType.ATTRIBUTE_MODIFIER),
                data -> (type, player) -> {
                    ModifyStatusEffectDurationPower modifyStatusEffectAmplifierPower = new ModifyStatusEffectDurationPower(type, player, (StatusEffect) data.get("status_effect"));
                    modifyStatusEffectAmplifierPower.addModifier((EntityAttributeModifier) data.get("modifier"));
                    return modifyStatusEffectAmplifierPower;
                })
                .allowCondition();
    }
}