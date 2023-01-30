// ------------------------------------------------------------------------------------------------
//
// Project Blue - NEI Integration - Recipe handler interface
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue.nei;

import net.minecraft.item.ItemStack;

public interface INEIRecipeHandler {

    public void addShapedRecipe(int width, int height, ItemStack out, Object... items);

}
