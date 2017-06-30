package silly511.aestheticeffect.helpers;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.registries.GameData;

public final class RegistryHelper {
	
	public static void register(Item item, String name) {
		GameData.register_impl(item.setRegistryName(name).setUnlocalizedName(item.getRegistryName().toString().replace(':', '.')));
	
		if (item instanceof IModelRegister && isClient()) ((IModelRegister)item).registerModels();
	}
	
	public static void register(Block block, String name) {
		GameData.register_impl(block.setRegistryName(name).setUnlocalizedName(block.getRegistryName().toString().replace(':', '.')));
		GameData.register_impl(new ItemBlock(block).setRegistryName(block.getRegistryName()));
		
		if (block instanceof IModelRegister && isClient()) ((IModelRegister)block).registerModels();
	}
	
	public static void registerModel(Item item, int meta, String name) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name, "inventory"));
	}
	
	public static String getActiveModId() {
		return Loader.instance().activeModContainer().getModId();
	}
	
	public static boolean isClient() {
		return FMLCommonHandler.instance().getSide().isClient();
	}
	
	public static interface IModelRegister {
		public void registerModels();
	}

}
