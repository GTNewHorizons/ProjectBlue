//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - GUI widgets for selecting colours
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatAllowedCharacters;
//import net.minecraft.block.material.MapColor;

import gcewing.projectblue.BaseGui.*;

public class BaseGuiColor {

//------------------------------------------------------------------------------------------------

	public static class ColorField extends Widget {
	
		protected Ref target;
	
		public ColorField(Ref ref) {
			super(8, 8);
			target = ref;
		}
		
		@Override
		public void draw(Screen s, int mx, int my) {
			s.setColor(0x606060);
			s.drawRect(-1, -1, width + 2, height + 2);
			int c = (Integer)target.get();
			if (c >= 0) {
				s.setColor(colorForIndex(c));
				s.drawRect(0, 0, width, height);
			}
			else {
				s.setColor(0x808080);
				s.drawRect(0, 0, width, height);
				s.drawCenteredString("x", width / 2, 0);
			}
		}
		
		@Override
		public void mousePressed(MouseCoords m, int button) {
			IWidget chooser = new ColorChooser(target);
			addPopup(width + 2, -1, chooser);
		}
	
	}

//------------------------------------------------------------------------------------------------

	public static class ColorChooser extends Widget {
	
		protected final static int cellSize = 10;
	
		protected Ref target;
		
		public ColorChooser(Ref ref) {
			super(4 * cellSize + 8, 5 * cellSize + 8);
			target = ref;
		}
		
		@Override
		public void draw(Screen s, int mx, int my) {
			int c;
			int sel = (Integer)target.get();
			if (sel < 0)
				sel = 16;
			s.drawGuiBackground(0, 0, width, height);
			for (int i = 0; i <= 16; i++) {
				int x = 4 + (i & 0x3) * cellSize;
				int y = 4 + (i >> 2) * cellSize;
				if (i == sel) {
					s.setColor(0x808080);
					s.drawRect(x, y, cellSize, cellSize);
				}
				if (i <= 15)
					c = colorForIndex(i);
				else
					c = 0xa0a0a0;
				s.setColor(c);
				s.drawRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
				if (i == 16)
					s.drawCenteredString("x", x + cellSize / 2, y + 1);
			}
		}
		
		@Override
		public void mousePressed(MouseCoords m, int button) {
			if (m.x < 0 || m.x >= width() || m.y < 0 || m.y >= height()) {
				close();
				return;
			}
			int col = (m.x - 4) / cellSize;
			int row = (m.y - 4) / cellSize;
			if (row >= 0 && row < 5 && col >= 0 && col < 4) {
				int i = row * 4 + col;
				if (i <= 16) {
					if (i > 15)
						i = -1;
					target.set(i);
					close();
				}
			}
		}
		
		@Override
		public boolean keyPressed(char c, int key) {
			if (key == 1) {
				close();
				return true;
			}
			else
				return false;
		}
		
		@Override
		public void close() {
			removePopup();
		}
	
	}

//------------------------------------------------------------------------------------------------

	public static int colorForIndex(int i) {
		//return MapColor.getMapColorForBlockColored(i).colorValue;
		return BaseColorUtils.colors[i];
	}

}
