// ------------------------------------------------------------------------------------------------
//
// Project Blue - Control Item Renderer
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.item.*;

public class ControlItemRenderer extends ItemRendererBase {

    void renderStack(ItemStack stack, ItemRenderType type) {
        ControlPanelRenderer.instance.renderControlItemStack(stack, type);
    }

}
