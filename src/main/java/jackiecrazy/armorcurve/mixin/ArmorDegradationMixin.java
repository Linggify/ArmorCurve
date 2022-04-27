package jackiecrazy.armorcurve.mixin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import jackiecrazy.armorcurve.CurveConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.management.Attribute;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Mixin(ItemStack.class)
public abstract class ArmorDegradationMixin {

    private static final Cache<ItemStack, ImmutableMultimap<Attribute, AttributeModifier>> cache = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(1, TimeUnit.SECONDS).build();

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract int getDamageValue();

    @Shadow
    public abstract int getMaxDamage();

    @Inject(cancellable = true, at = @At("RETURN"), method = "getAttributeModifiers", locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getAttributeModifiers(EquipmentSlot.Type equipmentSlot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> info, Multimap<Attribute, AttributeModifier> m) {
        if (!this.isEmpty() && equipmentSlot == EquipmentSlot.Type.ARMOR && this.getItem().isDamageable((ItemStack)(Object)this)) {
            ImmutableMultimap<Attribute, AttributeModifier> cached = cache.getIfPresent((ItemStack)(Object)this);
            if (cached != null) info.setReturnValue(cached);
            ImmutableMultimap.Builder<Attribute, AttributeModifier> copy = ImmutableMultimap.builder();
            if(CurveConfig.degrade==null)return;
            float degrade = CurveConfig.degrade.with("remaining", new BigDecimal(this.getMaxDamage() - this.getDamageValue())).and("max", new BigDecimal(this.getMaxDamage())).eval().floatValue();
            for (Attribute e : m.keySet())
                for (AttributeModifier eam : m.get(e)) {
                    AttributeModifier degradedEAM = new AttributeModifier(eam.getId(), eam.getName(), (degrade) * eam.getAmount(), eam.getOperation());
                    copy.put(e, degradedEAM);
                }
            cached = copy.build();
            cache.put((ItemStack)(Object)this, cached);
            info.setReturnValue(cached);
        }
    }
}
