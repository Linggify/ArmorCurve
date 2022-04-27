package jackiecrazy.armorcurve.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ValueUpdateMixin {

    @Shadow public abstract AttributeInstance getAttribute(Attribute attribute);

    @Inject(at = @At("HEAD"), method = "actuallyHurt")
    protected void applyArmorToDamage(DamageSource source, float damage, CallbackInfo cbInfo) {
        ((AttributeUpdater)(this.getAttribute(Attributes.ARMOR))).invokeUpdateAttribute();
    }
}
