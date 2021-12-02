//------------------------------------------------------------------------------------------------
//
//   Project Blue - Control Panel Recipes
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.init.*;
import net.minecraft.item.crafting.*;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraftforge.oredict.*;
import static net.minecraftforge.oredict.RecipeSorter.Category.*;
import cpw.mods.fml.common.registry.*;
import codechicken.microblock.*;
import gcewing.projectblue.nei.INEIRecipeHandler;

public class ControlPanelRecipes {

	public final static int coverMeta = 1;
	public final static int hollowCoverMeta = 257;
	public static List<RecipeBase> recipes = new ArrayList<RecipeBase>();

	public static boolean isEmpty(InventoryCrafting ic, int c, int r) {
		return ic.getStackInRowAndColumn(c, r) == null;
	}
	
	public static boolean isSaw(InventoryCrafting ic, int c, int r) {
		return isSaw(ic.getStackInRowAndColumn(c, r));
	}
	
	public static boolean isSaw(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemSaw;
	}
	
	public static boolean isHollowCover(InventoryCrafting ic, int c, int r) {
		return isHollowCover(ic.getStackInRowAndColumn(c, r));
	}
	
	public static boolean isHollowCover(ItemStack stack) {
		return stack != null && stack.getItem() == ProjectBlue.itemMicroPart
			&& stack.getItemDamage() == hollowCoverMeta;
	}
	
	public static boolean isFullSizeControl(InventoryCrafting ic, int c, int r) {
		return miniatureStackFor(ic, r, c) != null;
	}
	
	public static boolean isFullSizeControl(ItemStack stack) {
		return miniatureStackFor(stack) != null;
	}
	
	public static boolean isControlPanel(ItemStack stack) {
		return stack.getItem() instanceof ControlPanelItem;
	}
	
	public static ItemStack miniatureStackFor(InventoryCrafting ic, int c, int r) {
		return miniatureStackFor(ic.getStackInRowAndColumn(c, r));
	}
	
	public static ItemStack miniatureStackFor(ItemStack stack) {
		if (stack == null)
			return null;
		Item item = stack.getItem();
		int meta = stack.getItemDamage();
		if (item instanceof ItemBlock) {
			Block block = Block.getBlockFromItem(item);
			if (block == Blocks.lever)
				return new ItemStack(ProjectBlue.miniatureLever, 1, 16);
			if (block == Blocks.stone_button)
				return new ItemStack(ProjectBlue.miniatureButton, 1, 16);
			if (block == Blocks.wooden_button)
				return new ItemStack(ProjectBlue.miniatureButton, 1, 17);
		}
		if (item == ProjectBlue.itemPartFixture)
			return new ItemStack(ProjectBlue.miniatureLamp, 1, meta);
		if (item == ProjectBlue.itemMicroPart && meta == coverMeta)
			return ControlPanelMaterial.forStack(stack).newStack(ProjectBlue.miniatureCover);
		return null;
	}
	
	public static ItemStack fullSizeStackFor(ItemStack stack) {
		Item item = stack.getItem();
		int meta = stack.getItemDamage();
		if (item == ProjectBlue.miniatureLever && meta == 16)
			return new ItemStack(Blocks.lever);
		if (item == ProjectBlue.miniatureButton && meta == 16)
			return new ItemStack(Blocks.stone_button);
		if (item == ProjectBlue.miniatureButton && meta == 17)
			return new ItemStack(Blocks.wooden_button);
		if (item == ProjectBlue.miniatureLamp)
			return new ItemStack(ProjectBlue.itemPartFixture, 1, meta);
		if (item == ProjectBlue.miniatureCover)
			return ControlPanelMaterial.forStack(stack).newStack(ProjectBlue.itemMicroPart, coverMeta);
		return null;
	}
	
	public static String materialAt(InventoryCrafting ic, int c, int r) {
		ItemStack stack = ic.getStackInRowAndColumn(c, r);
		return ControlPanelItem.getMaterial(stack);
	}

	//------------------------------------------------------------------------------------------------

	public static abstract class RecipeBase implements IRecipe {

		@Override
		public int getRecipeSize() {
			return 4;
		}
		
		@Override
		public ItemStack getRecipeOutput() {
			return null;
		}
		
		public void addCraftingToNEI(INEIRecipeHandler h, ItemStack result) {
		}
		
		public void addUsageToNEI(INEIRecipeHandler h, ItemStack ingredient) {
		}
	
	}
	
	//------------------------------------------------------------------------------------------------

	static class CraftControlPanel extends RecipeBase {
	
		@Override
		public boolean matches(InventoryCrafting ic, World world) {
			return
				isSaw(ic, 0, 0) && isEmpty(ic, 0, 1) &&
				isEmpty(ic, 1, 0) && isHollowCover(ic, 1, 1);
		}
		
		@Override
		public ItemStack getCraftingResult(InventoryCrafting ic) {
			String material = materialAt(ic, 1, 1);
			ControlPanelMaterial base = ControlPanelMaterial.forName(material);
			return base.newStack();
		}
		
		@Override
		public void addCraftingToNEI(INEIRecipeHandler h, ItemStack result) {
			if (isControlPanel(result))
				addRecipeToNEI(h, null, ControlPanelMaterial.forStack(result));
		}
		
		@Override
		public void addUsageToNEI(INEIRecipeHandler h, ItemStack ingredient) {
			if (isSaw(ingredient))
				addRecipeToNEI(h, ingredient, ControlPanelMaterial.forName("tile.wood"));
			else if (isHollowCover(ingredient))
				addRecipeToNEI(h, null, ControlPanelMaterial.forStack(ingredient));
		}
		
		void addRecipeToNEI(INEIRecipeHandler h, ItemStack saw, ControlPanelMaterial base) {
			if (saw == null)
				saw = ProjectBlue.stackStoneSaw;
			ItemStack cover = base.newStack(ProjectBlue.itemMicroPart, hollowCoverMeta);
			ItemStack panel = base.newStack();
			h.addShapedRecipe(2, 2, panel, saw, null, null, cover);
		}
		
	}
	
	//------------------------------------------------------------------------------------------------

	static class CraftMiniatureItem extends RecipeBase {
	
		@Override
		public boolean matches(InventoryCrafting ic, World world) {
			return
				isSaw(ic, 0, 0) && isEmpty(ic, 0, 1) &&
				isEmpty(ic, 1, 0) && isFullSizeControl(ic, 1, 1);
		}
		
		@Override
		public ItemStack getCraftingResult(InventoryCrafting ic) {
			ItemStack stack = miniatureStackFor(ic, 1, 1);
			if (stack != null)
				stack.stackSize = 8;
			return stack;
		}
	
		@Override
		public void addCraftingToNEI(INEIRecipeHandler h, ItemStack result) {
			ItemStack ingredient = fullSizeStackFor(result);
			if (ingredient != null)
				addRecipeToNEI(h, null, ingredient);
		}
		
		@Override
		public void addUsageToNEI(INEIRecipeHandler h, ItemStack ingredient) {
			if (isSaw(ingredient)) {
				ControlPanelMaterial base = ControlPanelMaterial.forName("tile.wood");
				addRecipeToNEI(h, ingredient, new ItemStack(Blocks.lever));
				addRecipeToNEI(h, ingredient, new ItemStack(Blocks.stone_button));
				addRecipeToNEI(h, ingredient, new ItemStack(Blocks.wooden_button));
				addRecipeToNEI(h, ingredient, base.newStack(ProjectBlue.itemMicroPart, coverMeta));
				for (int i = 0; i < 16; i++)
					addRecipeToNEI(h, ingredient, new ItemStack(ProjectBlue.itemPartFixture, 1, i));
			}
			else if (isFullSizeControl(ingredient))
				addRecipeToNEI(h, null, ingredient);
		}
		
		void addRecipeToNEI(INEIRecipeHandler h, ItemStack saw, ItemStack ingredient) {
			if (saw == null)
				saw = ProjectBlue.stackStoneSaw;
			ItemStack result = miniatureStackFor(ingredient);
			result.stackSize = 8;
			h.addShapedRecipe(2, 2, result, saw, null, null, ingredient);
		}
		
	}

	//------------------------------------------------------------------------------------------------
	
	static boolean isPaintableControl(InventoryCrafting ic, int c, int r) {
		return isPaintableControl(ic.getStackInRowAndColumn(c, r));
	}

	static boolean isPaintableControl(ItemStack stack) {
		if (stack != null) {
			Item item = stack.getItem();
			if (item instanceof ControlItem)
				switch (((ControlItem)item).type) {
					case LEVER:
					case BUTTON:
						return true;
				}
		}
		return false;
	}
	
	static boolean isPaintedControl(ItemStack stack) {
		return isPaintableControl(stack) && stack.getItemDamage() < 16;
	}
	
	static boolean isSprayCan(InventoryCrafting ic, int c, int r) {
		return isSprayCan(ic.getStackInRowAndColumn(c, r));
	}

	static boolean isSprayCan(ItemStack stack) {
		if (stack != null)
			return stack.getItem() instanceof SprayCanItem;
		return false;
	}

	static class PaintControl extends RecipeBase {
	
		@Override
		public boolean matches(InventoryCrafting ic, World world) {
			return
				isPaintableControl(ic, 0, 0) && isSprayCan(ic, 1, 0);
		}
		
		@Override
		public ItemStack getCraftingResult(InventoryCrafting ic) {
			ItemStack ctrl = ic.getStackInRowAndColumn(0, 0);
			ItemStack paint = ic.getStackInRowAndColumn(1, 0);
			SprayCanItem can = (SprayCanItem)paint.getItem();
			return new ItemStack(ctrl.getItem(), 1, can.getColor(paint));
		}
	
		@Override
		public void addCraftingToNEI(INEIRecipeHandler h, ItemStack result) {
			if (isPaintedControl(result)) {
				Item item = result.getItem();
				int color = result.getItemDamage();
				ItemStack paint = ProjectBlue.sprayCan.newStack(1, color);
				addRecipeToNEI(h, new ItemStack(item, 1, 16), paint);
			}
		}
		
		@Override
		public void addUsageToNEI(INEIRecipeHandler h, ItemStack ingredient) {
			if (isSprayCan(ingredient)) {
				addRecipeToNEI(h, new ItemStack(ProjectBlue.miniatureLever, 1, 16), ingredient);
				addRecipeToNEI(h, new ItemStack(ProjectBlue.miniatureButton, 1, 16), ingredient);
				addRecipeToNEI(h, new ItemStack(ProjectBlue.miniatureButton, 1, 17), ingredient);
			}
			else if (isPaintableControl(ingredient)) {
				for (int i = 0; i < 16; i++) {
					ItemStack paint = ProjectBlue.sprayCan.newStack(1, i);
					addRecipeToNEI(h, ingredient, paint);
				}
			}
		}
		
		void addRecipeToNEI(INEIRecipeHandler h, ItemStack ctrl, ItemStack paint) {
			Item item = ctrl.getItem();
			int color = ProjectBlue.sprayCan.getColor(paint);
			ItemStack result = new ItemStack(item, 1, color);
			h.addShapedRecipe(2, 1, result, ctrl, paint);
		}

	}

	//------------------------------------------------------------------------------------------------

	static void addRecipe(RecipeBase r) {
		recipes.add(r);
		GameRegistry.addRecipe(r);
	}

	public static void registerRecipes() {
		RecipeSorter.register("projectblue:controlpanel", RecipeBase.class, SHAPED, "");
		addRecipe(new CraftControlPanel());
		addRecipe(new CraftMiniatureItem());
		addRecipe(new PaintControl());
	}

}
