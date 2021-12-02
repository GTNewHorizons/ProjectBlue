//------------------------------------------------------------------------------------------------
//
//   Project Blue - Control Panel Part
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.util.*;
import static java.lang.Math.*;
import net.minecraft.init.*;
import net.minecraft.block.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import static net.minecraftforge.common.util.Constants.NBT.*;
import codechicken.microblock.*;
import codechicken.multipart.*;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.lib.data.*;
import codechicken.lib.lighting.*;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.*;
import mrtjp.projectred.*;
import mrtjp.projectred.api.*;
import mrtjp.projectred.core.*;
import mrtjp.projectred.illumination.*;
import mrtjp.projectred.transmission.*;

public class ControlPanelPart extends PBFacePart /*JCuboidFacePart*/
	implements IConnectable, /*IRedwireEmitter,*/ IBundledEmitter
{

	final static int buttonDownTime = 20; // ticks
	
//	static Cuboid6 bounds[] = new Cuboid6[6];
//	static
//	{
//		for (int i = 0; i < 6; i++)
//		{
//			Transformation t = Rotation.sideRotations[i].at(codechicken.lib.vec.Vector3.center);
//			bounds[i] = new Cuboid6(0, 0, 0, 1, 1/8.0, 1).apply(t);
//		}
//	}
	
	static Cuboid6[] bounds = newBounds(0, 0, 0, 1, 1/8.0, 1);
	static Matrix3 surfaceMountRotation = Matrix3.rotZ(180);

	// Persistent
	public int mounting; // 0 = flush, 1 = surface
	public int connectionMask;
	byte[] controlTypes = new byte[16];
	byte[] controlStates = new byte[16];
	byte[] controlMetadata = new byte[16];
	public String[][] labels = new String[16][2];
	
	public ControlPanelMaterial base;
	long[] releaseTime = new long[16];
	byte[] signal = new byte[16];
	
	public ControlPanelPart() {
		for (int i = 0; i < 16; i++)
			for (int j = 0; j < 2; j++)
				labels[i][j] = "";
	}
	
	public ControlPanelPart(String material, int side) {
		this(material, side, null);
	}

	public ControlPanelPart(String material, int side, NBTTagCompound nbt) {
		this(ControlPanelMaterial.forName(material), side, nbt);
	}

	public ControlPanelPart(ControlPanelMaterial base, int side, NBTTagCompound nbt) {
		this();
		this.base = base;
		this.side = side;
		this.mounting = 1;
		if (nbt != null)
			loadControls(nbt);
	}
	
	@Override
	public Cuboid6[] getBoundsArray() {
		return bounds;
	}

	void setMaterial(String material) {
		this.base = ControlPanelMaterial.forName(material);
	}
	
	public ControlType getControlType(int i) {
		return ControlType.values[controlTypes[i]];
	}
	
	public void setControlType(int i, ControlType type) {
		controlTypes[i] = (byte)type.ordinal();
	}
	
	public int getControlState(int i) {
		return controlStates[i];
	}
	
	public void setControlState(int i, int state) {
		controlStates[i] = (byte)state;
	}
	
	public int getControlMeta(int i) {
		return controlMetadata[i] & 0xff;
	}
	
	public void setControlMeta(int i, int meta) {
		controlMetadata[i] = (byte)meta;
	}
	
	public void setFlushMounting() {
		mounting = 0;
	}
	
	public void setSurfaceMounting() {
		mounting = 1;
	}
	
	@Override
	public String getType() {
		return "pb_controlpanel";
	}

	@Override
	public Cuboid6 getBounds() {
		return bounds[side];
	}

//	@Override
//	public Iterable<IndexedCuboid6> getSubParts() {
//		return subParts[side];
//	}

//	@Override
//	public int getSlotMask() {
//		return 1 << side;
//	}

	@Override
	public void writeDesc(MCDataOutput data) {
//		System.out.printf("ControlPanelPart.writeDesc\n");
		super.writeDesc(data);
		data.writeString(base.name);
		data.writeByte(mounting);
		for (int i = 0; i < 16; i++) {
			data.writeByte(controlTypes[i]);
			data.writeByte(controlStates[i]);
			data.writeByte(controlMetadata[i]);
			for (int j = 0; j < 2; j++)
				data.writeString(labels[i][j]);
		}
	}
	
	@Override
	public void readDesc(MCDataInput data) {
//		System.out.printf("ControlPanelPart.readDesc\n");
		super.readDesc(data);
		String material = data.readString();
		if (base == null)
			setMaterial(material);
		mounting = data.readByte();
		for (int i = 0; i < 16; i++) {
			controlTypes[i] = data.readByte();
			controlStates[i] = data.readByte();
			controlMetadata[i] = data.readByte();
			for (int j = 0; j < 2; j++)
				labels[i][j] = data.readString();
		}
	}
	
	@Override
	public void save(NBTTagCompound nbt) {
		super.save(nbt);
		nbt.setString("material", base.name);
		nbt.setInteger("mounting", mounting);
		nbt.setInteger("connectionMask", connectionMask);
		saveControls(nbt);
		nbt.setByteArray("controlStates", controlStates);
	}
	
	public void saveControls(NBTTagCompound nbt) {
		nbt.setByteArray("controlTypes", controlTypes);
		nbt.setByteArray("controlMetadata", controlMetadata);
		NBTTagList nbtLabels = new NBTTagList();
		for (int i = 0; i < 16; i++)
			for (int j = 0; j < 2; j++)
				nbtLabels.appendTag(new NBTTagString(labels[i][j]));
		nbt.setTag("labels", nbtLabels);
	}
	
	@Override
	public void load(NBTTagCompound nbt) {
		super.load(nbt);
		if (nbt.hasKey("material"))
			setMaterial(nbt.getString("material"));
		else
			setMaterial("tile.wood");
		mounting = nbt.getInteger("mounting");
		if (nbt.hasKey("connectionMask"))
			connectionMask = nbt.getInteger("connectionMask");
		else
			connectionMask = 0x3;
		loadControls(nbt);
		if (nbt.hasKey("controlStates"))
			controlStates = nbt.getByteArray("controlStates");
	}
		
	public void loadControls(NBTTagCompound nbt) {
		if (nbt.hasKey("controlTypes"))
			controlTypes = nbt.getByteArray("controlTypes");
		if (nbt.hasKey("controlMetadata"))
			controlMetadata = nbt.getByteArray("controlMetadata");
		if (nbt.hasKey("labels")) {
			NBTTagList nbtLabels = nbt.getTagList("labels", TAG_STRING);
			for (int i = 0; i < 16; i++)
				for (int j = 0; j < 2; j++) {
					int k = i * 2 + j;
					labels[i][j] = nbtLabels.getStringTagAt(k);
				}
		}
	}

	@Override
	public boolean renderStatic(codechicken.lib.vec.Vector3 pos, int pass) {
		if (pass == 0)
			ControlPanelRenderer.instance.renderStaticInWorld(this, pos.x, pos.y, pos.z);
		return false;
	}
	
	@Override
	public void renderDynamic(codechicken.lib.vec.Vector3 pos, float frame, int pass) {
		if (pass == 0)
			ControlPanelRenderer.instance.renderDynamicInWorld(this, pos.x, pos.y, pos.z, frame);
	}
	
	@Override
	public void onPartChanged(TMultiPart part) {
//		System.out.printf("ControlPanelPart.onPartChanged\n");
		updateInputs();
	}
	
	@Override
	public void onNeighborChanged() {
//		System.out.printf("ControlPanelPart.onNeighborChanged\n");
		updateInputs();
	}
	
	void updateInputs() {
		if (!world().isRemote) {
//			System.out.printf("ControlPanelPart.updateInputs\n");
			getInputs();
			((TileEntity)tile()).markDirty();
			sendDescUpdate();
		}
	}
	
	void getInputs() {
		byte[] sig = BundledUtils.getAllSignals(this, side, connectionMask);
		for (int i = 0; i <= 15; i++) {
			switch (getControlType(i)) {
				case LAMP:
					setControlState(i, sig[i] != 0 ? 1 : 0);
					break;
			}
		}
	}
	
	static long lastActivateTime = 0;
	
	@Override
	public void click(EntityPlayer player, MovingObjectPosition hit, ItemStack stack) {
		if (!world().isRemote && player.isSneaking() && stack != null && stack.getItem() instanceof ItemScrewdriver) {
			harvest(hit, player);
		}
	}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack stack) {
		if (!world().isRemote) {
			long time = world().getTotalWorldTime();
//			System.out.printf("ControlPanelPart.activate: at %s\n", time);
			// Seems to get called twice when stack != null
			if (time != lastActivateTime) {
				lastActivateTime = time;
				//System.out.printf("ControlPanelPart.activate: side %s of (%s,%s,%s) at (%s,%s,%s)\n",
				//	hit.sideHit, hit.blockX, hit.blockY, hit.blockZ,
				//	hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord);
				//System.out.printf("ControlPanelPart.activate: block is at (%s,%s,%s)\n", x(), y(), z());
				Trans3 t = localToGlobalTransformation(hit.blockX, hit.blockY, hit.blockZ);
				Vector3 p = t.ip(hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord);
				//System.out.printf("ControlPanelPart.activate: local (%s,%s,%s)\n", p.x, p.y, p.z);
				int i = (int)floor((p.z + 0.5) * 4);
				int j = (int)floor((-p.x + 0.5) * 4);
				//System.out.printf("ControlPanelPart.activate: row %s col %s\n", i, j);
				if (i >= 0 && i <= 3 && j >= 0 && j <= 3)
					activateCell(i * 4 + j, player, stack);
			}
		}
		return true;
	}
	
	void activateCell(int i, EntityPlayer player, ItemStack stack) {
		System.out.printf("ControlPanelPart.activateCell: %s containing %s with %s\n",
			i, getControlType(i), stack);
		if (stack != null) {
			Item item = stack.getItem();
			int meta = stack.getItemDamage();
			NBTTagCompound nbt = stack.getTagCompound();
//			System.out.printf("ControlPanelPart.activateCell: item = %s\n", item);
//			System.out.printf("ControlPanelPart.activateCell: nbt = %s\n", nbt);
			if (item instanceof ItemScrewdriver) {
				if (player.isSneaking())
					changeMounting();
				else if (getControlType(i) != ControlType.NONE)
					removeControl(i, player);
				else
					changeRotation();
				return;
			}
			if (item == Items.coal && meta == 1) { // charcoal
				editText(i, player);
				return;
			}
			if (getControlType(i) == ControlType.NONE) {
				if (item == ProjectBlue.miniatureLever) {
					placeControl(i, ControlType.LEVER, player, meta, stack);
					return;
				}
				if (item == ProjectBlue.miniatureButton) {
					placeControl(i, ControlType.BUTTON, player, meta, stack);
					return;
				}
				if (item == ProjectBlue.miniatureLamp) {
					placeControl(i, ControlType.LAMP, player, meta, stack);
					return;
				}
				if (item == ProjectBlue.miniatureCover
						&& nbt.getString("mat").equals(base.name)) {
					placeControl(i, ControlType.BLANK, player, 0, stack);
					return;
				}
			}
		}
		activateControl(i);
	}
	
	void placeControl(int i, ControlType type, EntityPlayer player, int meta, ItemStack stack) {
		if (!player.capabilities.isCreativeMode)
			stack.stackSize -= 1;
		setControlType(i, type);
		setControlMeta(i, meta);
		getInputs();
		changed();
	}
	
	void removeControl(int i, EntityPlayer player) {
		if (!player.capabilities.isCreativeMode)
			drop(stackForControl(i));
		setControlType(i, ControlType.NONE);
		setControlState(i, 0);
		changed();
	}
	
	void changeMounting() {
		mounting ^= 1;
		changed();
	}
	
	void changeRotation() {
		rot = (rot + 1) & 0x3;
		changed();
	}
	
	void editText(int i, EntityPlayer player) {
		ProjectBlue.channel.sendEditControlPanelText(player, this, i);
	}
	
	@Override
	public void harvest(MovingObjectPosition hit, EntityPlayer player) {
		tile().dropItems(getDrops());
		tile().remPart(this);
	}
	
	public Iterable<ItemStack> getDrops() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack stack = base.newStack();
		saveControls(stack.getTagCompound());
		list.add(stack);
		return list;
	}

	ItemStack stackForControl(int i) {
		ItemStack stack = null;
		NBTTagCompound nbt = null;
		int meta = getControlMeta(i);
		switch (getControlType(i)) {
			case LEVER:
				stack = new ItemStack(ProjectBlue.miniatureLever, 1, meta);
				break;
			case BUTTON:
				stack = new ItemStack(ProjectBlue.miniatureButton, 1, meta);
				break;
			case LAMP:
				stack = new ItemStack(ProjectBlue.miniatureLamp, 1, meta);
				break;
			case BLANK:
				stack = new ItemStack(ProjectBlue.miniatureCover);
				nbt = new NBTTagCompound();
				nbt.setString("mat", base.name);
				break;
		}
		if (stack != null)
			stack.setTagCompound(nbt);
		return stack;
	}
	
	void drop(ItemStack stack) {
		if (stack != null) {
			Trans3 t = localToGlobalTransformation();
			Vector3 p = t.p(0, -0.6, 0);
			World world = world();
			EntityItem item = new EntityItem(world, p.x, p.y, p.z, stack);
			item.motionX = world.rand.nextGaussian() * 0.05;
			item.motionY = world.rand.nextGaussian() * 0.05 + 0.2;
			item.motionZ = world.rand.nextGaussian() * 0.05;
			item.delayBeforeCanPickup = 10;
			world.spawnEntityInWorld(item);
		}
	}

	void activateControl(int i) {
//		System.out.printf("ControlPanelPart.activateControl(%s) in %s\n", i, world());
		switch (getControlType(i)) {
			case LEVER:
				toggleLever(i);
				break;
			case BUTTON:
				pressButton(i);
				break;
		}
	}
	
	void toggleLever(int i) {
		int state = getControlState(i) ^ 1;
		setControlState(i, state);
		playClick(state > 0 ? 0.6 : 0.5);
		changed();
	}
	
	void pressButton(int i) {
		setControlState(i, 1);
		releaseTime[i] = world().getTotalWorldTime() + buttonDownTime;
//		System.out.printf("ControlPanelPart.pressButton: Scheduling tick\n");
		scheduleTick(buttonDownTime);
		playClick(0.6);
		changed();
	}
	
	@Override
	public void scheduledTick() {
		long t_now = world().getTotalWorldTime();
		long t_next = -1;
		boolean release_occurred = false;
//		System.out.printf("ControlPanelPart.scheduledTick: at %s\n", t_now);
		for (int i = 0; i < 16; i++)
			switch (getControlType(i)) {
				case BUTTON:
					if (getControlState(i) > 0) {
						if (t_now >= releaseTime[i]) {
							setControlState(i, 0);
							release_occurred = true;
						}
						else {
							if (t_next < 0 || t_next > releaseTime[i])
								t_next = releaseTime[i];
						}
					}
					break;
			}
		if (t_next >= 0) {
//			System.out.printf("Rescheduling tick after %s\n", t_next - t_now);
			scheduleTick((int)(t_next - t_now));
		}
		if (release_occurred) {
			playClick(0.5);
			changed();
		}
	}
		
	void playClick(double pitch) {
		world().playSoundEffect(x() + 0.5, y() + 0.5, z() + 0.5, "random.click", 0.3F, (float)pitch);
	}
	
	public void setLabel(int cell, int line, String text) {
		labels[cell][line] = text;
		changed();
	}
	
	@Override
	public void changed() {
		FaceUtils.notifyAllNeighbors(this, side);
		super.changed();
	}
	
	public Trans3 localToGlobalTransformation() {
		return localToGlobalTransformation(x(), y(), z());
	}

	public Trans3 localToGlobalTransformation(double x, double y, double z) {
		Trans3 t = new Trans3(x + 0.5, y + 0.5, z + 0.5).side(side).turn(rot).translate(0, -0.5 + 1/16.0, 0);
		if (mounting == 1)
			t = t.rotate(surfaceMountRotation);
		return t;
	}
	
	//
	//   IConnectable
	//
	
	@Override
	public boolean connectStraight(IConnectable part, int r, int edgeRot) {
//		System.out.printf("ControlPanelPart.connectStraight: to %s r=%s edgeRot=%s\n", part, r, edgeRot);
		return connectTo(part, r);
	}
	
	@Override
	public boolean connectInternal(IConnectable part, int r) {
//		System.out.printf("ControlPanelPart.connectInternal: to %s r=%s\n", part, r);
		return connectTo(part, r); //r == -1;
	}
	
	@Override
	public boolean connectCorner(IConnectable part, int r, int edgeRot) {
		return connectTo(part, r);
	}
	
	@Override
	public boolean canConnectCorner(int r) {
		return true;
	}
	
	boolean connectTo(IConnectable part, int r) {
		if (!(part instanceof ControlPanelPart)) {
			connectionMask |= 1 << r;
			markDirty();
			return true;
		}
		else
			return false;
	}
	
	//
	//   IBundledEmitter
	//
	
	@Override
	public byte[] getBundledSignal(int side) {
		for (int i = 0; i < 16; i++)
			switch (getControlType(i)) {
				case LEVER:
				case BUTTON:
					signal[i] = (byte)(255 * getControlState(i));
					break;
				default:
					signal[i] = 0;
			}
		return signal;
	}
	
//------------------------------------------------------------------------------------------------

	public static class Factory implements IPartFactory {
	
		public TMultiPart createPart(String name, boolean client) {
			TMultiPart part = new ControlPanelPart();
//			System.out.printf("ControlPanelPart.Factory.createPart: Created %s\n", part);
			return part;
		}
	
	}
	
//------------------------------------------------------------------------------------------------

	public static enum ControlType {
		NONE, BLANK, LEVER, BUTTON, LAMP;
		
		public static ControlType[] values = values();
	}	

}
