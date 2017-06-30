package silly511.aestheticeffect.packets;

import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import silly511.aestheticeffect.tile.TileHologramProjector;

public class PacketSetProjectorCode implements IMessage {
	
	private List<String> text;
	private BlockPos pos;
	
	public PacketSetProjectorCode() {}
	
	public PacketSetProjectorCode(List<String> text, BlockPos pos) {
		this.text = text;
		this.pos = pos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int length = buf.readInt();
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.text = Lists.newArrayList();
		
		if (length <= 0)
			text.add("");
		else for (int i = 0; i < length; i++)
			text.add(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(text.size());
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		
		for (String line : text)
			ByteBufUtils.writeUTF8String(buf, line);
	}
	
	public static class Handler implements IMessageHandler<PacketSetProjectorCode, IMessage> {

		@Override
		public IMessage onMessage(PacketSetProjectorCode message, MessageContext ctx) {
			EntityPlayer player = ctx.getServerHandler().player;
			
			if (player.getDistanceSq(message.pos) <= 64) {
				TileHologramProjector tile = (TileHologramProjector) player.world.getTileEntity(message.pos);
				
				tile.setCode(Lists.newArrayList(message.text));
				tile.syncData();
			}
			
			return null;
		}
		
	}

}
