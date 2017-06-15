package silly511.aestheticeffect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import silly511.aestheticeffect.blocks.BlockHologramProjector;
import silly511.aestheticeffect.gui.GuiHologramProjector;
import silly511.aestheticeffect.helpers.RegistryHelper;
import silly511.aestheticeffect.packets.PacketSetProjectorCode;
import silly511.aestheticeffect.render.HologramTESR;
import silly511.aestheticeffect.tile.TileHologramProjector;

@Mod(modid = AestheticEffect.modid, name = "Aesthetic Effect", version = "beta-1")
public class AestheticEffect implements IGuiHandler {
	
	public static final String modid = "aestheticeffect";

	@Mod.Instance(modid)
	public static AestheticEffect instance;
	public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(modid);
	public static final Logger logger = LogManager.getLogger();
	
	public static CreativeTabs mainTab = new CreativeTabs(modid + ".main") {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(hologramProjector);
		}
	};
	
	public static Block hologramProjector;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		RegistryHelper.register(hologramProjector = new BlockHologramProjector(), "hologram_projector");
		
		GameRegistry.registerTileEntity(TileHologramProjector.class, hologramProjector.getRegistryName().toString());
	}
	
	@SideOnly(Side.CLIENT)
	public void registerRenders() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileHologramProjector.class, new HologramTESR());
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		if (RegistryHelper.isClient()) this.registerRenders();
		
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, this);
		
		this.network.registerMessage(PacketSetProjectorCode.Handler.class, PacketSetProjectorCode.class, 0, Side.SERVER);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0)
			return new GuiHologramProjector(x, y, z, world);
		
		return null;
	}

}
