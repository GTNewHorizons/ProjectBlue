//------------------------------------------------------------------------------------------------
//
//   Project Blue - Rednet to Bundled Cable Adaptor GUI Screen
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue.mfr;

import static org.lwjgl.opengl.GL11.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.world.*;

import gcewing.projectblue.*;
import gcewing.projectblue.BaseDataChannel.ChannelOutput;
import static gcewing.projectblue.mfr.RednetAdaptorTE.*;
import static gcewing.projectblue.BaseColorUtils.colors;

public class RednetAdaptorGui extends BaseGuiContainer {

	final static int guiWidth = 216, guiHeight = 84;
	final static int wireLeft = 12, wireTop = 30,
		wireWidth = 10, wireHeight = 26, wireSpacing = 12;
	
//	final static int colors[] = {
//		0xffffff, // white
//		0xff8000, // orange
//		0xff00ff, // magenta
//		0x8080ff, // light blue
//		0xffff00, // yellow
//		0x00ff00, // lime
//		0xff8080, // pink
//		0x404040, // dark grey
//		0x808080, // light grey
//		0x00ffff, // cyan
//		0x8000ff, // purple
//		0x0000ff, // blue
//		0x804000, // brown
//		0x008000, // green
//		0xff0000, // red
//		0x202020  // black
//	};

	RednetAdaptorTE te;

	public RednetAdaptorGui(RednetAdaptorContainer container) {
		super(container, guiWidth, guiHeight);
		this.te = container.te;
	}
	
	@Override
	protected void drawBackgroundLayer() {
		bindTexture("gui/gui_rednet_adaptor.png", 256, 128);
		drawTexturedRect(0, 0, guiWidth, guiHeight, 0, 0); // Background
		for (int i = 0; i < 16; i++) {
			int c = colors[i];
			setColor(c);
			int wireX = wireLeft + wireSpacing * i;
			int wireY = wireTop;
			int wireU = 216, wireV = 0;
			int arrowU = 216, arrowV = 52;
			boolean showArrow = true;
			switch (te.signalConfig[i]) {
				case CONNECTED_BIT | BUNDLED_TO_REDNET:
					break;
				case CONNECTED_BIT | REDNET_TO_BUNDLED:
					arrowV += 8;
					break;
				default:
					wireU += 20;
					showArrow = false;
					break;
			}
			drawTexturedRect(wireX, wireY, wireWidth, wireHeight, wireU, wireV, 20, 52); // wire
			setColor(1, 1, 1);
			if (showArrow)
				drawTexturedRect(wireX, wireY + 11, wireWidth, 4, arrowU, arrowV, 20, 8); // arrow
			else
				drawTexturedRect(wireX, wireY + 8, wireWidth, 2, 216, 68, 20, 4); // cut wire end
		}
		drawCenteredString("Bundled Cable", guiWidth / 2, 6);
		drawCenteredString("RedNet", guiWidth / 2, 71);
	}
	
	@Override
	protected void mouseClicked(int sx, int sy, int button) {
		//System.out.printf("RednetAdaptorGui.mouseClicked at (%s, %s) with button %s\n", sx, sy, button);
		int x = sx - guiLeft, y = sy - guiTop;
		if (x > wireLeft && y > wireTop && y < wireTop + wireHeight) {
			int i = (x - wireLeft) / wireSpacing;
			if (i < 16) {
				ChannelOutput out = ProjectBlue.dataChannel.openServerContainer("toggleConfig");
				out.writeInt(i);
				out.writeInt(button);
				out.close();
			}
		}	
	}
	
}
