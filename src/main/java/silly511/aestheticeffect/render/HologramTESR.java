package silly511.aestheticeffect.render;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import silly511.aestheticeffect.tile.TileHologramProjector;
import silly511.aestheticeffect.tools.ProjectorCodeExecuter;

public class HologramTESR extends TileEntitySpecialRenderer<TileHologramProjector> {

	@Override
	public void render(TileHologramProjector tile, double x, double y, double z, float partialTicks, int destroyStage, float thing) {
		if (tile.getWorld().isBlockPowered(tile.getPos())) return;
		
		boolean isLookingAt = tile.getPos().equals(this.rendererDispatcher.cameraHitResult.getBlockPos());
		float yaw = this.rendererDispatcher.entityYaw;
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		this.setLightmapDisabled(true);
		GlStateManager.color(1, 1, 1);
		
		Map<Integer, String> errors = ProjectorCodeExecuter.execute(tile.getCode());
		
		GlStateManager.scale(0.4, 0.4, 0.4);
		
		if (isLookingAt)
			if (errors.isEmpty()) {
				EntityRenderer.drawNameplate(getFontRenderer(), TextFormatting.GREEN + I18n.format("hologramProjector.noErrors"), 0, 1.5f, 0, 0, yaw, 0, false, false);
			} else {
				int i = 0;
				
				for (Entry<Integer, String> error : errors.entrySet()) {
					i++;
					EntityRenderer.drawNameplate(getFontRenderer(), TextFormatting.DARK_RED + I18n.format("hologramProjector.errorOnLine", error.getKey() + 1, error.getValue()), 0, 1.5f + i * 0.3f, 0, 0, yaw, 0, false, false);
				}
				
				EntityRenderer.drawNameplate(getFontRenderer(), TextFormatting.RED + I18n.format("hologramProjector.errors"), 0, 1.5f + (i + 1) * 0.3f, 0, 0, yaw, 0, false, false);
			}
		
		this.setLightmapDisabled(false);
		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean isGlobalRenderer(TileHologramProjector tile) {
		return true;
	}

}
