//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - GUI Button Widget
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import gcewing.projectblue.BaseGui.*;

public class BaseGuiButtons {

    protected static int nextId = 1;

	public static class Button extends Widget {
	
		protected GuiButton base;
		protected IWidgetContainer parent;
		public int left, top;
		public Action action;
		
		public Button(int width, int height, String title, Action action) {
			super(width, height);
			this.base = new GuiButton(nextId++, 0, 0, width, height, title);
			this.action = action;
		}
		
		@Override
		public void draw(Screen scr, int mouseX, int mouseY) {
			base.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
		}
		
		@Override
		public void mousePressed(MouseCoords m, int button) {
			base.mousePressed(Minecraft.getMinecraft(), m.x, m.y);
		}
		
		@Override
		public void mouseReleased(MouseCoords m, int button) {
			if (action != null)
				action.perform();
		}
	
	}
		
}
