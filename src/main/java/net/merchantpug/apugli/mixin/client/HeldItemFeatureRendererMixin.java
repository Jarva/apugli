package net.merchantpug.apugli.mixin.client;

import net.merchantpug.apugli.power.ModifyEquippedItemRenderPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.merchantpug.apugli.util.ModifyEquippedItemRenderUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T> & ModelWithArms> extends FeatureRenderer<T, M> {
    @Shadow @Final private HeldItemRenderer heldItemRenderer;
    @Unique boolean apugli$isRightMainHand;

    public HeldItemFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/HeldItemFeatureRenderer;getContextModel()Lnet/minecraft/client/render/entity/model/EntityModel;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void captureBoolean(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci, boolean bl) {
        this.apugli$isRightMainHand = bl;
    }

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void modifyEquippedItemsHand(LivingEntity entity, ItemStack stack, ModelTransformation.Mode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        List<ModifyEquippedItemRenderPower> modifyEquippedItemRenderPowers = PowerHolderComponent.getPowers(entity, ModifyEquippedItemRenderPower.class);
        modifyEquippedItemRenderPowers.forEach(power -> {
            ModifyEquippedItemRenderUtil.chooseArm(this, matrices, vertexConsumers, light, entity, power, this.heldItemRenderer);
        });
        if (modifyEquippedItemRenderPowers.stream().anyMatch(power -> power.shouldOverride() && ((power.slot == EquipmentSlot.MAINHAND && this.apugli$isRightMainHand) || (power.slot == EquipmentSlot.OFFHAND && !this.apugli$isRightMainHand))) && arm == Arm.RIGHT) ci.cancel();
        if (modifyEquippedItemRenderPowers.stream().anyMatch(power -> power.shouldOverride() && ((power.slot == EquipmentSlot.MAINHAND && !this.apugli$isRightMainHand) || (power.slot == EquipmentSlot.OFFHAND && this.apugli$isRightMainHand))) && arm == Arm.LEFT) ci.cancel();
    }
}
