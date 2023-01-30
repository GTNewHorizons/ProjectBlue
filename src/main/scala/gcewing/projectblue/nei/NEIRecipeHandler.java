// ------------------------------------------------------------------------------------------------
//
// Project Blue - NEI Integration - Recipe handler
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue.nei;

import net.minecraft.item.ItemStack;

import codechicken.nei.recipe.ShapedRecipeHandler;
import codechicken.nei.recipe.ShapedRecipeHandler.CachedShapedRecipe;
import gcewing.projectblue.ControlPanelRecipes;
import gcewing.projectblue.ControlPanelRecipes.RecipeBase;

public class NEIRecipeHandler extends ShapedRecipeHandler implements INEIRecipeHandler {

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (RecipeBase r : ControlPanelRecipes.recipes) r.addCraftingToNEI(this, result);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (RecipeBase r : ControlPanelRecipes.recipes) r.addUsageToNEI(this, ingredient);
    }

    // @Override
    // public String getGuiTexture() {
    // return "textures/gui/container/crafting_table.png";
    // }

    // INEIRecipeHandler

    @Override
    public void addShapedRecipe(int width, int height, ItemStack out, Object... items) {
        System.out.printf("NEIRecipeHandler.addShapedRecipe: %sx%s producing %s from", width, height, out);
        for (Object item : items) System.out.printf(" %s", item);
        System.out.printf("\n");
        arecipes.add(new CachedShapedRecipe(width, height, items, out));
    }

}
