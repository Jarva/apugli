<<<<<<<< HEAD:src/main/java/net/merchantpug/apugli/mixin/client/AbstractSoundInstanceMixin.java
package net.merchantpug.apugli.mixin.client;

import net.merchantpug.apugli.access.AbstractSoundInstanceAccess;
import net.merchantpug.apugli.access.ExplosionAccess;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
========
package com.github.merchantpug.apugli.mixin.xplatforn.client;

import com.github.merchantpug.apugli.access.AbstractSoundInstanceAccess;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
>>>>>>>> pr/25:Common/src/main/java/com/github/merchantpug/apugli/mixin/xplatforn/client/AbstractSoundInstanceMixin.java
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSoundInstance.class)
@Implements(@Interface(iface = AbstractSoundInstanceAccess.class, prefix = "apugli$"))
public class AbstractSoundInstanceMixin {
    @Unique private SoundEvent apugli$soundEvent;

    @Inject(method = "<init>(Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;Lnet/minecraft/util/math/random/Random;)V", at = @At("TAIL"))
    private void captureSoundEvent(SoundEvent sound, SoundSource category, RandomSource random, CallbackInfo ci) {
        apugli$soundEvent = sound;
    }

    public void apugli$setSoundEvent(SoundEvent soundEvent) {
        apugli$soundEvent = soundEvent;
    }

    public SoundEvent apugli$getSoundEvent() {
        return apugli$soundEvent;
    }
}
