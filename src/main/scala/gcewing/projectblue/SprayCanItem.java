//------------------------------------------------------------------------------------------------
//
//   Project Blue - Spray Can Item
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.util.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.creativetab.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import cpw.mods.fml.relauncher.*;

public class SprayCanItem extends Item {

	IIcon[] icons = new IIcon[17];
	
//	public SprayCanItem() {
//		setHasSubtypes(true);
//	}

	public ItemStack newStack(int qty, int color) {
		ItemStack stack = new ItemStack(this, qty);
		setColor(stack, color);
		return stack;
	}
	
	public int getColor(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null)
			return nbt.getInteger("color");
		return 0;
	}
	
	public void setColor(ItemStack stack, int color) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		nbt.setInteger("color", color);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister reg) {
		String prefix = getIconString();
		for (int i = 0; i <= 16; i++)
			icons[i] = reg.registerIcon(String.format("%s-%d", prefix, i));
	}
	
	@Override
	public int getMaxDamage() {
		return 256;
	}
	
	@Override
	public IIcon getIconIndex(ItemStack stack) {
		return icons[getColor(stack)];
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List result) {
		for (int i = 0; i <= 16; i++)
			result.add(this.newStack(1, i));
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		int color = getColor(stack);
		String name = (color == 16) ? "item.gcewing_projectblue:paintRemover" : getUnlocalizedName();
		String text = StatCollector.translateToLocal(name + ".name");
		return String.format(text, ProjectBlue.getColorName(color));
	}
	
	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return stack.getItemDamage() < getMaxDamage();
	}
	
	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		ItemStack result = null;
		int damage = stack.getItemDamage() + 1;
		if (damage <= getMaxDamage()) {
			result = newStack(1, getColor(stack));
			result.setItemDamage(damage);
		}
		else
			result = new ItemStack(ProjectBlue.emptySprayCan);
		return result;
	}
	
	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack par1ItemStack) {
		return false;
	}

}
