package net.merchantpug.apugli.power;

import com.google.auto.service.AutoService;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import net.merchantpug.apugli.power.configuration.FabricValueModifyingConfiguration;
import net.merchantpug.apugli.power.factory.ModifyEnchantmentLevelPowerFactory;
import net.merchantpug.apugli.registry.power.ApugliPowers;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.ConcurrentHashMap;

@AutoService(ModifyEnchantmentLevelPowerFactory.class)
public class ModifyEnchantmentLevelPower extends AbstractValueModifyingPower implements ModifyEnchantmentLevelPowerFactory<ConfiguredPower<FabricValueModifyingConfiguration, ?>> {
    private static final ConcurrentHashMap<String, ConcurrentHashMap<ListTag, ListTag>> ENTITY_ITEM_ENCHANTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ConcurrentHashMap<ConfiguredPower<FabricValueModifyingConfiguration, ?>, Integer>> POWER_MODIFIER_CACHE = new ConcurrentHashMap<>();

    public ModifyEnchantmentLevelPower() {
        super(ModifyEnchantmentLevelPowerFactory.getSerializableData().xmap(
                FabricValueModifyingConfiguration::new,
                FabricValueModifyingConfiguration::data
        ).codec());
    }

    @Override
    public void onAdded(ConfiguredPower<FabricValueModifyingConfiguration, ?> configuration, Entity entity) {
        ApugliPowers.MODIFY_ENCHANTMENT_LEVEL.get().onAdded(configuration, entity);
    }

    @Override
    public void onRemoved(ConfiguredPower<FabricValueModifyingConfiguration, ?> configuration, Entity entity) {
        ApugliPowers.MODIFY_ENCHANTMENT_LEVEL.get().onRemoved(configuration, entity);
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<ListTag, ListTag>> getEntityItemEnchants() {
        return ENTITY_ITEM_ENCHANTS;
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<ConfiguredPower<FabricValueModifyingConfiguration, ?>, Integer>> getPowerModifierCache() {
        return POWER_MODIFIER_CACHE;
    }

}
