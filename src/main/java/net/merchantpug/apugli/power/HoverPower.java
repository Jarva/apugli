package net.merchantpug.apugli.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.merchantpug.apugli.Apugli;
import net.minecraft.entity.LivingEntity;

public class HoverPower extends Power {
    private final float correctionRange;

    public HoverPower(PowerType<?> type, LivingEntity entity, float correctionRange) {
        super(type, entity);
        this.correctionRange = correctionRange;
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<HoverPower>(Apugli.identifier("hover"),
                new SerializableData()
                        .add("step_assist", SerializableDataTypes.FLOAT, 0.0F),
                data ->
                        (type, entity) -> new HoverPower(type, entity, data.getFloat("step_assist")))
                .allowCondition();
    }

    public boolean canCorrectHeight() {
        return correctionRange > 0.0F;
    }

    public float getCorrectionRange() {
        return correctionRange;
    }
}
