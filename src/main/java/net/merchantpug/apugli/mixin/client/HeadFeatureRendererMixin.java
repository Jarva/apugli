package net.merchantpug.apugli.mixin.client;

import net.merchantpug.apugli.power.ModifyEquippedItemRenderPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.merchantpug.apugli.util.ModifyEquippedItemRenderUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(HeadFeatureRenderer.class)
public abstract class HeadFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T> & ModelWithHead> extends FeatureRenderer<T, M> {
    @Shadow @Final private float scaleX;

    @Shadow @Final private float scaleY;

    @Shadow @Final private float scaleZ;

    @Shadow @Final private HeldItemRenderer heldItemRenderer;

    public HeadFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void preventHeadRender(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        List<ModifyEquippedItemRenderPower> modifyEquippedItemRenderPowers = PowerHolderComponent.getPowers(livingEntity, ModifyEquippedItemRenderPower.class);
        modifyEquippedItemRenderPowers.forEach(power -> {
            if (power.slot == EquipmentSlot.HEAD) ModifyEquippedItemRenderUtil.renderIndividualStackOnHead((HeadFeatureRenderer<?, ?>)(Object) this, matrixStack, vertexConsumerProvider, i, livingEntity, f, power.scale * this.scaleX, power.scale * this.scaleY, power.scale * this.scaleZ, power.stack, this.heldItemRenderer);
        });
        if (modifyEquippedItemRenderPowers.stream().anyMatch(power -> power.shouldOverride() && power.slot == EquipmentSlot.HEAD)) ci.cancel();
    }
}
