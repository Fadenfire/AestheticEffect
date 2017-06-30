package silly511.aestheticeffect.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import silly511.aestheticeffect.AestheticEffect;
import silly511.aestheticeffect.helpers.RegistryHelper;
import silly511.aestheticeffect.helpers.RegistryHelper.IModelRegister;
import silly511.aestheticeffect.tile.TileHologramProjector;

public class BlockHologramProjector extends Block implements IModelRegister {

	public BlockHologramProjector() {
		super(Material.GLASS, MapColor.LIGHT_BLUE);
		this.setCreativeTab(AestheticEffect.mainTab);
		this.setLightLevel(0.8f);
		this.setLightOpacity(255);
		this.setSoundType(SoundType.GLASS);
		this.setHardness(30);
		this.setResistance(2000);
		this.setHarvestLevel("pickaxe", 0);
	}
	
	@Override
	public void registerModels() {
		RegistryHelper.registerModel(Item.getItemFromBlock(this), 0, this.getRegistryName().toString());
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) player.openGui(AestheticEffect.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}

	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileHologramProjector tile = (TileHologramProjector) world.getTileEntity(pos);
		ItemStack item = new ItemStack(this);
		NBTTagList codeNBTList = new NBTTagList();
		
		tile.getCode().forEach((String line) -> codeNBTList.appendTag(new NBTTagString(line)));
		
		item.setTagCompound(new NBTTagCompound());
		item.getTagCompound().setTag("BlockEntityTag", new NBTTagCompound());
		item.getTagCompound().getCompoundTag("BlockEntityTag").setTag("code", codeNBTList);
		
		spawnAsEntity(world, pos, item);
		
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileHologramProjector();
	}

}
