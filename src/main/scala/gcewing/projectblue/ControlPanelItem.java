// ------------------------------------------------------------------------------------------------
//
// Project Blue - Control Panel Item
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.util.*;

import net.minecraft.client.renderer.texture.*;
import net.minecraft.creativetab.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.*;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.*;
import codechicken.multipart.*;
import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.relauncher.*;

// public class ControlPanelItem extends JItemMultiPart implements IBlockHighlighting {
public class ControlPanelItem extends ItemMultiPartJ implements IBlockHighlighting {

    public static String getMaterial(ItemStack stack) {
        if (stack != null) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && nbt.hasKey("mat")) return nbt.getString("mat");
        }
        return "tile.wood";
    }

    @Override
    public TMultiPart newPart(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int sideHit,
            Vector3 vhit) {
        // System.out.printf("ControlPanelItem.newPart: side %s in %s\n", side, world);
        return newPart(stack, player, sideHit, vhit);
    }

    ControlPanelPart newPart(ItemStack stack, EntityPlayer player, int sideHit, Vector3 vhit) {
        String material = getMaterial(stack);
        int slot = FacePlacementGrid.getHitSlot(vhit, sideHit);
        ControlPanelPart part = new ControlPanelPart(material, slot, stack.getTagCompound());
        part.rot = Trans3.turnFor(player, slot);
        if (slot == (sideHit ^ 1)) part.setSurfaceMounting();
        else {
            part.setFlushMounting();
            if (slot <= 1) part.rot ^= 2;
        }
        return part;
    }

    @Override
    public int getSpriteNumber() {
        return 0;
    }

    @Override
    public String getIconString() {
        return "controlPanelPartDoesNotHaveAnIcon";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister reg) {
        ControlPanelRenderer.registerIcons(reg);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List result) {
        result.add(ControlPanelMaterial.forName("tile.wood").newStack());
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        ControlPanelMaterial base = ControlPanelMaterial.forName(getMaterial(stack));
        String uln = base.block.getUnlocalizedName() + ".name";
        list.add("Made from " + StatCollector.translateToLocal(uln));
    }

    @Override
    public boolean renderHighlight(DrawBlockHighlightEvent e) {
        ItemStack stack = e.currentItem;
        EntityPlayer player = e.player;
        World world = e.player.worldObj;
        BlockCoord pos = new BlockCoord(e.target.blockX, e.target.blockY, e.target.blockZ);
        int sideHit = e.target.sideHit;
        Vec3 h = e.target.hitVec;
        Vector3 vhit = new Vector3(h.xCoord - pos.x, h.yCoord - pos.y, h.zCoord - pos.z);
        pos.offset(sideHit);
        ControlPanelPart part = (ControlPanelPart) newPart(stack, player, sideHit, vhit);
        if (TileMultipart.canPlacePart(world, pos, part))
            return ControlPanelRenderer.instance.renderHighlight(part, e, pos);
        return false;
    }

}
