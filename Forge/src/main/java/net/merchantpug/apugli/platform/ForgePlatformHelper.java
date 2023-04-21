package net.merchantpug.apugli.platform;

import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import io.github.edwinmindcraft.apoli.api.power.IActivePower;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredModifier;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import io.github.edwinmindcraft.apoli.common.power.ModelColorPower;
import io.github.edwinmindcraft.apoli.common.power.configuration.ColorConfiguration;
import io.github.edwinmindcraft.apoli.common.registry.ApoliPowers;
import net.merchantpug.apugli.capability.KeyPressCapability;
import net.merchantpug.apugli.client.ApugliForgeClientEventHandler;
import net.merchantpug.apugli.data.ApoliForgeDataTypes;
import net.merchantpug.apugli.network.ApugliPacketHandler;
import net.merchantpug.apugli.networking.c2s.ApugliPacketC2S;
import net.merchantpug.apugli.networking.s2c.ApugliPacketS2C;
import net.merchantpug.apugli.platform.services.IPlatformHelper;
import com.google.auto.service.AutoService;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.extensions.IForgePlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
@AutoService(IPlatformHelper.class)
public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }
    
    @Override
    public double getReachDistance(Entity entity) {
        return entity instanceof IForgePlayer player ? player.getReachDistance() : 4.5;
    }
    
    @Override
    public double getAttackRange(Entity entity) {
        return entity instanceof IForgePlayer player ? player.getAttackRange() : 3;
    }

    @Override
    public SerializableDataType<IActivePower.Key> getKeyDataType() {
        return ApoliForgeDataTypes.KEY;
    }

    @Override
    public SerializableDataType<ConfiguredModifier<?>> getModifierDataType() {
        return ApoliForgeDataTypes.MODIFIER;
    }

    @Override
    public SerializableDataType<List<ConfiguredModifier<?>>> getModifiersDataType() {
        return SerializableDataType.list(ApoliForgeDataTypes.MODIFIER);
    }

    @Override
    public double applyModifiers(Entity entity, List<?> modifiers, double value) {
        if (modifiers.stream().anyMatch(o -> o instanceof ConfiguredModifier<?>))
            return ModifierUtil.applyModifiers(entity, (List<ConfiguredModifier<?>>) modifiers, value);
        return value;
    }

    @Override
    public void sendS2C(ApugliPacketS2C packet, ServerPlayer player) {
        ApugliPacketHandler.sendS2C(packet, player);
    }

    @Override
    public void sendS2CTrackingAndSelf(ApugliPacketS2C packet, ServerPlayer player) {
        ApugliPacketHandler.sendS2CTrackingAndSelf(packet, player);
    }

    @Override
    public void sendC2S(ApugliPacketC2S packet) {
        ApugliPacketHandler.sendC2S(packet);
    }

    @Override
    public float[] getColorPowerRgba(LivingEntity entity) {
        List<Holder<ConfiguredPower<ColorConfiguration, ModelColorPower>>> modelColorPowers = IPowerContainer.getPowers(entity, ApoliPowers.MODEL_COLOR.get());
        if (modelColorPowers.size() > 0) {
            float red = modelColorPowers.stream().map(holder -> holder.get().getConfiguration().red()).reduce((a, b) -> a * b).get();
            float green = modelColorPowers.stream().map(holder -> holder.get().getConfiguration().green()).reduce((a, b) -> a * b).get();
            float blue = modelColorPowers.stream().map(holder -> holder.get().getConfiguration().blue()).reduce((a, c) -> a * c).get();
            float alpha = modelColorPowers.stream().map(holder -> holder.get().getConfiguration().alpha()).min(Float::compare).get();
            return new float[] { red, green, blue, alpha };
        }
        return new float[0];
    }

    @Override
    public void updateKeys(SerializableData.Instance data, Player player) {
        player.getCapability(KeyPressCapability.INSTANCE).ifPresent(cap -> {
            IActivePower.Key key = data.get("key");
            if (!cap.getKeysToCheck().contains(key)) {
                cap.addKeyToCheck(key);
                cap.changePreviousKeysToCheckToCurrent();
            } else if (player.level.isClientSide && player instanceof LocalPlayer) {
                ApugliForgeClientEventHandler.handleActiveKeys();
            }
        });
    }

    @Override
    public boolean isCurrentlyUsingKey(SerializableData.Instance data, Player player) {
        Optional<KeyPressCapability> capability = player.getCapability(KeyPressCapability.INSTANCE).resolve();
        if (capability.isPresent()) {
            IActivePower.Key key = data.get("key");
            return capability.get().getCurrentlyUsedKeys().contains(key);
        }
        return false;
    }

    // TODO: Implement this.
    @Override
    public Tuple<Integer, Integer> getHitsOnTarget(Entity actor, LivingEntity target) {
        return null;
    }

}