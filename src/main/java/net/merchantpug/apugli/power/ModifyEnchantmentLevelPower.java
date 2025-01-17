package net.merchantpug.apugli.power;

import net.merchantpug.apugli.Apugli;
import net.merchantpug.apugli.access.ItemStackAccess;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.ValueModifyingPower;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ModifyEnchantmentLevelPower extends ValueModifyingPower {
    private static final ConcurrentHashMap<String, ConcurrentHashMap<NbtList, NbtList>> ENTITY_ITEM_ENCHANTS = new ConcurrentHashMap<>();
    private final Enchantment enchantment;

    public ModifyEnchantmentLevelPower(PowerType<?> type, LivingEntity entity, Enchantment enchantment) {
        super(type, entity);
        this.enchantment = enchantment;
    }

    public Enchantment getEnchantment() {
        return this.enchantment;
    }

    public boolean doesApply(Enchantment enchantment) {
        return enchantment.equals(this.enchantment);
    }

    private static Optional<Integer> findEnchantIndex(Identifier id, NbtList enchants) {
        for (int i = 0; i < enchants.size(); ++i) {
            String string = enchants.getCompound(i).getString("id");
            Identifier enchantId = Identifier.tryParse(string);
            if (enchantId != null && enchantId.equals(id)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public static NbtList generateEnchantments(NbtList enchants, ItemStack self) {
        Entity entity = ((ItemStackAccess) (Object) self).getEntity();

        if (!(entity instanceof PlayerEntity)) return enchants;
        
        NbtList newEnchants = enchants.copy();

        for (ModifyEnchantmentLevelPower power : PowerHolderComponent.getPowers(entity, ModifyEnchantmentLevelPower.class)) {
            Identifier id = Registries.ENCHANTMENT.getId(power.getEnchantment());
            Optional<Integer> idx = findEnchantIndex(id, newEnchants);
            if (idx.isPresent()) {
                NbtCompound existingEnchant = newEnchants.getCompound(idx.get());
                int lvl = existingEnchant.getInt("lvl");
                int newLvl = (int) PowerHolderComponent.modify(entity, ModifyEnchantmentLevelPower.class, lvl, powerFilter -> powerFilter.doesApply(power.getEnchantment()));
                existingEnchant.putInt("lvl", newLvl);
                newEnchants.set(idx.get(), existingEnchant);
            } else {
                NbtCompound newEnchant = new NbtCompound();
                newEnchant.putString("id", id.toString());
                newEnchant.putInt("lvl", (int) PowerHolderComponent.modify(entity, ModifyEnchantmentLevelPower.class, 0, powerFilter -> powerFilter.doesApply(power.getEnchantment())));
                newEnchants.add(newEnchant);
            }
        }
        return newEnchants;
    }

    public static NbtList getEnchantments(ItemStack self) {
        Entity entity = ((ItemStackAccess) (Object) self).getEntity();
        if (entity == null) return self.getEnchantments();
        ConcurrentHashMap<NbtList, NbtList> itemEnchants = ENTITY_ITEM_ENCHANTS.computeIfAbsent(entity.getUuidAsString(), (_uuid) -> new ConcurrentHashMap<>());
        return itemEnchants.compute(self.getEnchantments(), (tag, tag2) -> ModifyEnchantmentLevelPower.generateEnchantments(tag, self));
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<ModifyEnchantmentLevelPower>(
                Apugli.identifier("modify_enchantment_level"),
                new SerializableData()
                        .add("enchantment", SerializableDataTypes.ENCHANTMENT)
                        .add("modifier", Modifier.DATA_TYPE, null)
                        .add("modifiers", Modifier.LIST_TYPE, null),
                data -> (type, entity) -> {
                    ModifyEnchantmentLevelPower power = new ModifyEnchantmentLevelPower(type, entity, (Enchantment) data.get("enchantment"));
                    if (data.isPresent("modifier")) {
                        power.addModifier((Modifier) data.get("modifier"));
                    }
                    if (data.isPresent("modifiers")) {
                        ((List<Modifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    return power;
                })
                .allowCondition();
    }
}