package silly511.aestheticeffect.tile;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.AxisAlignedBB;

public class TileHologramProjector extends TileSyncable {
	
	private List<String> code = Lists.newArrayList("");

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("code", 8);
		code.clear();
		
		for (int i = 0; i < list.tagCount(); i++)
			code.add(list.getStringTagAt(i));
		
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();
		
		code.forEach((String line) -> list.appendTag(new NBTTagString(line)));
		nbt.setTag("code", list);
		
		return super.writeToNBT(nbt);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	public List<String> getCode() {
		return code;
	}

	public void setCode(List<String> code) {
		this.code = code;
	}

}
