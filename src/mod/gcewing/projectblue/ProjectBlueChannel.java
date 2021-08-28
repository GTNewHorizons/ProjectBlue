//------------------------------------------------------------------------------------------------
//
//   Project Blue - Packet Channel
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.client.gui.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import codechicken.multipart.*;

public class ProjectBlueChannel extends BaseNBTChannel<ProjectBlueChannel.Message> {

	static enum Message {
		EDIT_CONTROL_PANEL_TEXT,
		UPDATE_CONTROL_PANEL_TEXT
	};
	
	public ProjectBlueChannel(String name) {
		super(name);
	}
	
	@Override
	void onReceiveFromClient(Message type, NBTTagCompound nbt, EntityPlayer player) {
		switch (type) {
			case UPDATE_CONTROL_PANEL_TEXT:
				onReceiveUpdateControlpanelText(nbt, player);
				break;
		}
	}
	
	@Override
	void onReceiveFromServer(Message type, NBTTagCompound nbt) {
		ProjectBlue.mod.client.onReceiveFromServer(type, nbt);
	}
	
	public void sendEditControlPanelText(EntityPlayer player, ControlPanelPart part, int cell) {
		NBTTagCompound nbt = new NBTTagCompound();
		setPart(nbt, part);
		nbt.setInteger("cell", cell);
		System.out.printf("ProjectBlueChannel.sendEditControlPanelText: to %s\n", player);
		sendToPlayer(Message.EDIT_CONTROL_PANEL_TEXT, nbt, player);
	}
	
	public void sendUpdateControlPanelText(ControlPanelPart part, int cell, int line, String text) {
		NBTTagCompound nbt = new NBTTagCompound();
		setPart(nbt, part);
		nbt.setInteger("cell", cell);
		nbt.setInteger("line", line);
		nbt.setString("text", text);
		sendToServer(Message.UPDATE_CONTROL_PANEL_TEXT, nbt);
	}
	
	void onReceiveUpdateControlpanelText(NBTTagCompound nbt, EntityPlayer player) {
		TMultiPart part = getServerPart(nbt, player);
		if (part instanceof ControlPanelPart) {
			int cell = nbt.getInteger("cell");
			int line = nbt.getInteger("line");
			String text = nbt.getString("text");
			((ControlPanelPart)part).setLabel(cell, line, text);
		}
	}
	
	void setPart(NBTTagCompound nbt, TMultiPart part) {
		nbt.setInteger("blockX", part.x());
		nbt.setInteger("blockY", part.y());
		nbt.setInteger("blockZ", part.z());
		nbt.setInteger("partIndex", part.tile().jPartList().indexOf(part));
	}
	
	TMultiPart getServerPart(NBTTagCompound nbt, EntityPlayer player) {
		return getPart(nbt, player.getEntityWorld());
	}

	public static TMultiPart getPart(NBTTagCompound nbt, World world) {
		int x = nbt.getInteger("blockX");
		int y = nbt.getInteger("blockY");
		int z = nbt.getInteger("blockZ");
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileMultipart) {
			TileMultipart tmp = (TileMultipart)te;
			List<TMultiPart> parts = tmp.jPartList();
			int i = nbt.getInteger("partIndex");
			if (i >= 0 && i < parts.size())
				return parts.get(i);
		}
		return null;
	}
	
}
