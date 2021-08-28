//------------------------------------------------------------------------------------------------
//
//   Project Blue - Control Item
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.util.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.creativetab.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import cpw.mods.fml.relauncher.*;
import gcewing.projectblue.ControlPanelPart.ControlType;

public class ControlItem extends Item {

	ControlType type;
	
	public ControlItem(ControlType type) {
		this.type = type;
		hasSubtypes = true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister reg) {
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List result) {
		switch (type) {
			case BLANK:
				result.add(ControlPanelMaterial.forName("tile.wood").newStack(this));
				break;
			case LEVER:
				for (int i = 0; i <= 16; i++)
					result.add(new ItemStack(this, 1, i));
				break;
			case BUTTON:
				for (int i = 0; i <= 17; i++)
					result.add(new ItemStack(this, 1, i));
				break;
			case LAMP:
				for (int i = 0; i < 16; i++)
					result.add(new ItemStack(this, 1, i));
				break;
		}
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		ControlItem item = (ControlItem)stack.getItem();
		String text = StatCollector.translateToLocal(item.getUnlocalizedName() + ".name");
		switch (item.type) {
			case BLANK:
				text = String.format(text, ControlPanelMaterial.forStack(stack).getLocalizedName());
				break;
			//case LAMP:
			default:
				text = String.format(text, ProjectBlue.getColorName(stack));
				break;
		}
		return text;
	}
	
}
