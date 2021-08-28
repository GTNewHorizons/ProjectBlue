//------------------------------------------------------------------------------------------------
//
//   Project Blue - Base class for face parts
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.util.*;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.*;
import codechicken.microblock.FacePlacementGrid;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.lib.data.*;
import codechicken.lib.vec.*;

public abstract class PBFacePart extends JCuboidFacePart {

	public String type;
	public int side; // 0 to 5
	public int rot; // 0 to 3

	public static Cuboid6[] newBounds(
		double x0, double y0, double z0,
		double x1, double y1, double z1)
	{
		Cuboid6[] bounds = new Cuboid6[6];
		for (int i = 0; i < 6; i++) {
			Transformation t = Rotation.sideRotations[i].at(codechicken.lib.vec.Vector3.center);
			bounds[i] = new Cuboid6(x0, y0, z0, x1, y1, z1).apply(t);
		}
		return bounds;
	}
	
	protected PBFacePart() {
	}
	
	@Override
	public String getType() {
		return type;
	}

	@Override
	public Cuboid6 getBounds() {
		return getBoundsArray()[side];
	}
	
	public abstract Cuboid6[] getBoundsArray();
	
	@Override
	public int getSlotMask() {
		return 1 << side;
	}
	
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
	}
	
	@Override
	public boolean renderStatic(codechicken.lib.vec.Vector3 pos, int pass) {
		return renderStatic(transformation(pos.x, pos.y, pos.z), pass, new PBStaticRenderer(this));
	}

	@Override
	public void renderDynamic(codechicken.lib.vec.Vector3 pos, float frame, int pass) {
		renderDynamic(transformation(pos.x, pos.y, pos.z), frame, pass, new PBDynamicRenderer());
	}
	
	public Trans3 transformation(double x, double y, double z) {
		return new Trans3(x + 0.5, y + 0.5, z + 0.5).side(side);
	}
	
	public void renderStack(ItemStack stack, ItemRenderType type) {
		renderStack(transformation(0, 0, 0), stack, type, new PBDynamicRenderer());
	}
	
	public boolean renderStatic(Trans3 t, int pass, IPBRenderer r) {
		return false;
	}
	
	public void renderDynamic(Trans3 t, float frame, int pass, IPBRenderer r) {
	}
	
	public void renderStack(Trans3 t, ItemStack stack, ItemRenderType type, IPBRenderer r) {
		renderStatic(t, 0, r);
	}

	@Override
	public void writeDesc(MCDataOutput data) {
		data.writeByte(side);
		data.writeByte(rot);
	}
	
	@Override
	public void readDesc(MCDataInput data) {
		side = data.readByte();
		rot = data.readByte();
	}
	
	@Override
	public void save(NBTTagCompound nbt) {
		nbt.setInteger("side", side);
		nbt.setInteger("rot", rot);
	}
	
	@Override
	public void load(NBTTagCompound nbt) {
		side = nbt.getInteger("side");
		rot = nbt.getInteger("rot");
	}
	
	public TileEntity getAdjacentTileEntity() {
		ForgeDirection d = ForgeDirection.getOrientation(side);
		return world().getTileEntity(x() + d.offsetX, y() + d.offsetY, z() + d.offsetZ);
	}

	public void markDirty() {
		((TileEntity)tile()).markDirty();
	}
	
	public void changed() {
		markDirty();
		sendDescUpdate();
	}
		
//------------------------------------------------------------------------------------------------

	public static class Factory implements IPartFactory {
	
		String type;
		Class<? extends PBFacePart> cls;
		
		public Factory(Class<? extends PBFacePart> cls, String type) {
			this.type = type;
			this.cls = cls;
		}
			
		public PBFacePart createPart(String name, boolean client) {
			return createPart(0, 0);
		}

		public PBFacePart createPart(int side, int rot) {
			try {
				PBFacePart part = (PBFacePart)cls.newInstance();
				part.type = type;
				part.side = side;
				part.rot = rot;
				return part;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}

//------------------------------------------------------------------------------------------------

	public static class FaceItem extends ItemMultiPartJ {
	
		static List<FaceItem> instances = new ArrayList<FaceItem>();
	
		Factory factory;
		PBFacePart proxy;
		
		public FaceItem(Factory factory) {
			this.factory = factory;
			proxy = factory.createPart(0, 0);
			instances.add(this);
		}
		
		public static void registerRenderers(BaseModClient client) {
			for (FaceItem item : instances)
				client.addItemRenderer(item, new ItemRenderer(item));
		}
	
		@Override
		public int getSpriteNumber() {
			return 0;
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public void registerIcons(IIconRegister reg) {
			proxy.registerIcons(reg);
		}
	
		@Override
		public PBFacePart newPart(ItemStack stack, EntityPlayer player, World world, BlockCoord pos,
			int sideHit, codechicken.lib.vec.Vector3 vhit)
		{
			int side = sideHit ^ 1;
			int rot = Trans3.turnFor(player, side);
			return factory.createPart(side, rot);
		}
		
	}

//------------------------------------------------------------------------------------------------

	public static class ItemRenderer extends ItemRendererBase {
	
		FaceItem item;
	
		public ItemRenderer(FaceItem item) {
			this.item = item;
		}
		
		@Override
		void renderStack(ItemStack stack, ItemRenderType type) {
			item.proxy.renderStack(stack, type);
		}
	
	}

//------------------------------------------------------------------------------------------------

}
