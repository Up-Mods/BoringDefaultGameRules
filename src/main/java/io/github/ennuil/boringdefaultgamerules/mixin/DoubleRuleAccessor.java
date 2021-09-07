package io.github.ennuil.boringdefaultgamerules.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;

@Mixin(DoubleRule.class)
public interface DoubleRuleAccessor {
    @Accessor
    double getMinimumValue();
    
    @Accessor
    void setMinimumValue(double minimumValue);

    @Accessor
    double getMaximumValue();
    
    @Accessor
    void setMaximumValue(double maximumValue);

    @Accessor
    double getValue();
    
    @Accessor
    void setValue(double value);
}