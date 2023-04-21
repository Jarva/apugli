package net.merchantpug.apugli.power;

import com.google.auto.service.AutoService;
import com.mojang.serialization.Codec;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import net.merchantpug.apugli.power.configuration.FabricValueModifyingConfiguration;
import net.merchantpug.apugli.power.factory.ModifyBreedingCooldownPowerFactory;
import net.merchantpug.apugli.power.factory.ModifySoulSpeedPowerFactory;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

@AutoService(ModifySoulSpeedPowerFactory.class)
public class ModifySoulSpeedPower extends AbstractValueModifyingPower implements ModifySoulSpeedPowerFactory<ConfiguredPower<FabricValueModifyingConfiguration, ?>> {

    public ModifySoulSpeedPower() {
        super(ModifyBreedingCooldownPowerFactory.getSerializableData().xmap(
                FabricValueModifyingConfiguration::new,
                FabricValueModifyingConfiguration::data
        ).codec());
    }

}
