package net.merchantpug.apugli.platform;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.ModelColorPower;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.merchantpug.apugli.client.ApugliClientFabric;
import net.merchantpug.apugli.component.ApugliEntityComponents;
import net.merchantpug.apugli.component.KeyPressComponent;
import net.merchantpug.apugli.networking.ApugliPackets;
import net.merchantpug.apugli.networking.c2s.ApugliPacketC2S;
import net.merchantpug.apugli.networking.s2c.ApugliPacketS2C;
import net.merchantpug.apugli.platform.services.IPlatformHelper;
import com.google.auto.service.AutoService;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

@SuppressWarnings("unchecked")
@AutoService(IPlatformHelper.class)
public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
    
    @Override
    public double getReachDistance(Entity entity) {
        double base = (entity instanceof Player player && player.getAbilities().instabuild) ? 5 : 4.5;
        return (entity instanceof LivingEntity living && isModLoaded("reach-entity-attributes")) ?
            ReachEntityAttributes.getReachDistance(living, base) : base;
    }
    
    @Override
    public double getAttackRange(Entity entity) {
        double base = (entity instanceof Player player && player.getAbilities().instabuild) ? 6 : 3;
        return (entity instanceof LivingEntity living && isModLoaded("reach-entity-attributes")) ?
            ReachEntityAttributes.getAttackRange(living, base) : base;
    }

    @Override
    public SerializableDataType<Active.Key> getKeyDataType() {
        return ApoliDataTypes.KEY;
    }

    @Override
    public <M> SerializableDataType<M> getModifierDataType() {
        return null;
    }

    @Override
    public <M> SerializableDataType<List<M>> getModifiersDataType() {
        return null;
    }

    @Override
    public double applyModifiers(Entity entity, List<?> modifiers, double value) {
        return 0;
    }

    @Override
    public void sendS2C(ApugliPacketS2C packet, ServerPlayer player) {
        ApugliPackets.sendS2C(packet, player);
    }

    @Override
    public void sendS2CTrackingAndSelf(ApugliPacketS2C packet, ServerPlayer player) {
        for (ServerPlayer otherPlayer : PlayerLookup.tracking(player))
            ApugliPackets.sendS2C(packet, otherPlayer);
        ApugliPackets.sendS2C(packet, player);
    }

    @Override
    public void sendC2S(ApugliPacketC2S packet) {
        ApugliPackets.sendC2S(packet);
    }

    @Override
    public float[] getColorPowerRgba(LivingEntity entity) {
        List<ModelColorPower> modelColorPowers = PowerHolderComponent.getPowers(entity, ModelColorPower.class);
        if (modelColorPowers.size() > 0) {
            float red = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).get();
            float green = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).get();
            float blue = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, c) -> a * c).get();
            float alpha = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).get();
            return new float[] { red, green, blue, alpha };
        }
        return new float[0];
    }

    @Override
    public void updateKeys(SerializableData.Instance data, Player player) {
        KeyPressComponent component = ApugliEntityComponents.KEY_PRESS_COMPONENT.get(player);
        Active.Key key = data.get("key");
        if (!component.getKeysToCheck().contains(key)) {
            component.addKeyToCheck(key);
            component.changePreviousKeysToCheckToCurrent();
        } else if (player.level.isClientSide && player instanceof LocalPlayer) {
            ApugliClientFabric.handleActiveKeys();
        }
    }

    @Override
    public boolean isCurrentlyUsingKey(SerializableData.Instance data, Player player) {
        KeyPressComponent component = ApugliEntityComponents.KEY_PRESS_COMPONENT.get(player);
        Active.Key key = data.get("key");
        return component.getCurrentlyUsedKeys().contains(key);
    }

    @Override
    public Tuple<Integer, Integer> getHitsOnTarget(Entity actor, LivingEntity target) {
        return ApugliEntityComponents.HITS_ON_TARGET_COMPONENT.get(target).getHits().getOrDefault(actor.getId(), new Tuple<>(0, 0));
    }

}