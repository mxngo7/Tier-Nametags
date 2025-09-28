package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.widget.PlayerSkinWidget;

@Mixin(PlayerSkinWidget.class)
public interface IPlayerSkinWidgetAccessor {
	@Accessor("yRotation")
	public float getHorizontalRotation();
	
	@Accessor("yRotation")
	public void setHorizontalRotation(float rotation);
}
