package com.leocth.gravityguns.mixin.client;

import com.leocth.gravityguns.client.BipedEntityModelDelegate;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity>
    extends AnimalModel<T>
    implements ModelWithArms, ModelWithHead {

    @Inject(method = "positionRightArm", at = @At("TAIL"))
    private void positionRightArm(T entity, CallbackInfo ci) {
        //noinspection ALL
        BipedEntityModelDelegate.positionRightArm(entity, (BipedEntityModel<T>)(Object)this);
    }

    @Inject(method = "positionLeftArm", at = @At("TAIL"))
    private void positionLeftArm(T entity, CallbackInfo ci) {
        //noinspection ALL
        BipedEntityModelDelegate.positionLeftArm(entity, (BipedEntityModel<T>)(Object)this);
    }
}
