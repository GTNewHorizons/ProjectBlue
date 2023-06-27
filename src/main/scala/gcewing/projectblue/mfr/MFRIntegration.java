//------------------------------------------------------------------------------------------------
//
//   Project Blue - MineFactory Reloaded Integration
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue.mfr;

import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import cpw.mods.fml.common.registry.*;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import gcewing.projectblue.*;

public class MFRIntegration extends gcewing.projectblue.BaseIntegration {

	public static ProjectBlue mod = ProjectBlue.mod;
	public static Block rednetAdaptorBlock;
	
	static String[] mfrPlasticSheetNames;
	
	@Override
	public void configure(BaseConfiguration config) {
		mfrPlasticSheetNames = config.getStringList("MineFactoryReloaded", "plasticSheetNames",
			"MineFactoryReloaded:item.mfr.plastic.sheet", "MineFactoryReloaded:plastic.sheet");
	}

	@Override
	protected void registerBlocks() {
		System.out.printf("ProjectBlue: MFRIntegration.registerBlocks\n");
		rednetAdaptorBlock = mod.newBlock("rednetAdaptor", RednetAdaptorBlock.class);
	}
	
	@Override
	protected void registerContainers() {
		mod.addContainer(PBGui.RednetAdaptor, RednetAdaptorContainer.class);
	}

	@Override
	protected void registerScreens() {
		System.out.printf("MFRIntegration.registerScreens\n");
		mod.client.addScreen(PBGui.RednetAdaptor, RednetAdaptorGui.class);
	}
	
	@Override
	protected void registerRecipes() {
		//Item plasticSheet = GameRegistry.findItem("MineFactoryReloaded", "plastic.sheet");
		Item plasticSheet = searchForItem(mfrPlasticSheetNames);
		if (plasticSheet != null)
			mod.newRecipe(rednetAdaptorBlock, 1, "plp", "lrl", "pip",
				'p', plasticSheet,
				'l', new ItemStack(Items.dye, 1, 4),
				'r', Items.redstone,
				'i', Items.iron_ingot);
	}

}
