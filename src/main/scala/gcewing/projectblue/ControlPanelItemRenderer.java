// ------------------------------------------------------------------------------------------------
//
// Project Blue - Control Panel Item Renderer
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import net.minecraft.item.*;
import net.minecraftforge.client.*;

public class ControlPanelItemRenderer extends ItemRendererBase {

    @Override
    void renderStack(ItemStack stack, ItemRenderType type) {
        ControlPanelRenderer.instance.renderStack(stack, type);
    }

}
