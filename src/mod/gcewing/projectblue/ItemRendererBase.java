//------------------------------------------------------------------------------------------------
//
//   Project Blue - Base class for item renderers
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import net.minecraft.item.*;
import net.minecraftforge.client.*;

public abstract class ItemRendererBase implements IItemRenderer {

	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}
	
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		switch (helper) {
			case ENTITY_ROTATION:
			case ENTITY_BOBBING:
			case INVENTORY_BLOCK:
				return true;
		}
		return false;
	}
	
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		glEnable(GL_ALPHA_TEST);
		glDisable(GL_BLEND);
		switch (type) {
			case ENTITY:
				glScaled(0.5, 0.5, 0.5);
				glTranslated(-0.5, -0.5, -0.5);
				renderStack(stack, type);
				break;
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
			case INVENTORY:
				renderStack(stack, type);
				break;
		}
	}
	
	// Render in coords (0,0,0) - (1,1,1)
	abstract void renderStack(ItemStack stack, ItemRenderType type);

}
