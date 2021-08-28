//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - GUI layout classes
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import static java.lang.Math.*;

import gcewing.projectblue.BaseGui.*;

public class BaseGuiLayout {

//------------------------------------------------------------------------------------------------

	public static class Label extends Widget {
	
		public String text;
		
		public Label(String text) {
			super(stringWidth(text), 7);
			this.text = text;
		}
		
		@Override
		public void draw(Screen scr, int mx, int my) {
			scr.drawString(text, 0, 0);
		}
	
	}
	
	//------------------------------------------------------------------------------------------------

	public static class Grid extends Group {
	
		public int rowSpacing = 4;
		public int colSpacing = 4;
		int numRows, numCols;
		Widget[][] grid;
	
		public Grid(int numCols, Object... items) {
			this.numCols = numCols;
			this.numRows = (items.length + numCols - 1) / numCols;
			this.grid = new Widget[numRows][numCols];
			for (int i = 0; i < items.length; i++) {
				Object item = items[i];
				int row = i / numCols;
				int col = i % numCols;
				Widget widget = null;
				if (item != null) {
					if (item instanceof String)
						widget = new Label((String)item);
					else
						widget = (Widget)item;
				}
				grid[row][col] = widget;
				if (widget != null)
					add(0, 0, widget);
			}
		}
		
		@Override
		public void layout() {
			super.layout();
			int colWidths[] = new int[numCols];
			int rowHeights[] = new int[numRows];
			for (int row = 0; row < numRows; row++)
				for (int col = 0; col < numCols; col++) {
					Widget widget = grid[row][col];
					rowHeights[row] = max(rowHeights[row], widget.height);
					colWidths[col] = max(colWidths[col], widget.width);
				}
			int x = 0;
			for (int col = 0; col < numCols; col++) {
				if (col > 0)
					x += colSpacing;
				int w = colWidths[col];
				for (int row = 0; row < numRows; row++) {
					Widget widget = grid[row][col];
					if (widget != null)
						widget.left = x;
				}
				x += w;
			}
			width = x;
			int y = 0;
			for (int row = 0; row < numRows; row++) {
				if (row > 0)
					y += rowSpacing;
				int h = rowHeights[row];
				for (int col = 0; col < numCols; col++) {
					Widget widget = grid[row][col];
					if (widget != null)
						widget.top = y + (h - widget.height) / 2;
				}
				y += h;
			}
			height = y;
		}
	
	}

	//------------------------------------------------------------------------------------------------
		
}
