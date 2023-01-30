// ------------------------------------------------------------------------------------------------
//
// Project Blue - Control Panel Material
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;

import codechicken.microblock.*;

public class ControlPanelMaterial {

    static Map<String, ControlPanelMaterial> cache = new HashMap<String, ControlPanelMaterial>();

    public static ControlPanelMaterial forStack(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null && nbt.hasKey("mat")) return ControlPanelMaterial.forName(nbt.getString("mat"));
        return null;
    }

    public static ControlPanelMaterial forName(String name) {
        ControlPanelMaterial result = cache.get(name);
        if (result == null) {
            result = new ControlPanelMaterial(name);
            cache.put(name, result);
        }
        return result;
    }

    public String name;
    public Block block;
    public int metadata;

    ControlPanelMaterial(String name) {
        this.name = name;
        MicroMaterialRegistry.IMicroMaterial mat = MicroMaterialRegistry.getMaterial(name);
        if (mat != null) {
            ItemStack stack = mat.getItem();
            Item item = stack.getItem();
            if (item instanceof ItemBlock) {
                block = ((ItemBlock) item).field_150939_a;
                metadata = stack.getItemDamage();
                return;
            }
        }
        block = Blocks.planks;
        metadata = 0;
    }

    public ItemStack newStack() {
        return newStack(ProjectBlue.controlPanelItem);
    }

    public ItemStack newStack(Item item) {
        return newStack(item, metadata);
    }

    public ItemStack newStack(Item item, int meta) {
        ItemStack stack = new ItemStack(item, 1, meta);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("mat", name);
        stack.setTagCompound(nbt);
        // System.out.printf("ControlPanelMaterial.newStack: returning %s\n", stack);
        return stack;
    }

    public String getLocalizedName() {
        return StatCollector.translateToLocal(block.getUnlocalizedName() + ".name");
    }

}
