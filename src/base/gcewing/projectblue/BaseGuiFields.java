//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - GUI Field Widgets
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatAllowedCharacters;

import gcewing.projectblue.BaseGui.*;

public class BaseGuiFields {

	public static class StringField extends Widget {
	
		public int left, top;
		public Object targeObject;
		public Ref target;
		public String format;
		public String charsAllowed;
		protected GuiTextField base;
		protected IWidgetContainer parent;
		protected boolean isOpen;
		
		public StringField(int charsWide, Ref target) {
			this((charsWide + 1) * 6, 12, target);
		}
				
		public StringField(int width, int height, Ref target) {
			super(width, height);
			this.base = new GuiTextField(Minecraft.getMinecraft().fontRenderer,
				0, 0, width, height);
			this.target = target;
		}
		
		@Override
		public void draw(Screen scr, int mouseX, int mouseY) {
			if (!isOpen)
				load();
			base.drawTextBox();
		}
		
		@Override
		public void mousePressed(MouseCoords m, int button) {
			open();
			base.mouseClicked(m.x, m.y, button);
		}

		@Override
		public boolean keyPressed(char c, int key) {
			if (key == 1) {
				close();
				return false;
			}
			else if (c == '\r') {
				close();
				return true;
			}
			else if (c < 32 || c == 127 || key == 14 || key == 199 || key == 203 || key == 205 ||
				key == 207 || key == 211 || charAllowed(c))
			{
				open();
				base.textboxKeyTyped(c, key);
				return true;
			}
			else
				return false;
		}
		
		protected
		boolean charAllowed(char c) {
			return charsAllowed == null || charsAllowed.indexOf(c) >= 0;
		}
		
		@Override
		public void focusChanged(boolean state) {
			//System.out.printf("BaseGuiFields.StringField.focusChanged: %s\n", state);
			if (state)
				open();
			else
				close();
			base.setFocused(state);
		}
		
		protected void open() {
			if (!isOpen) {
				isOpen = true;
				load();
				//base.setFocused(true);
			}
		}
		
		@Override
		public void close() {
			if (isOpen) {
				save();
				isOpen = false;
				//base.setFocused(false);
			}
		}
	
		protected void load() {
			base.setText(formatValue(target.get()));
		}
		
		protected void save() {
			try {
				target.set(parseValue(base.getText()));
			}
			catch (IllegalArgumentException e) {
				load();
			}
		}
		
		protected String formatValue(Object value) {
			if (format != null)
				return String.format(format, value);
			else
				return (String)value;
		}
		
		protected Object parseValue(String text) {
			return text;
		}
	
	}
	
//------------------------------------------------------------------------------------------------

	public static class IntField extends StringField {
	
		public int minValue = Integer.MIN_VALUE;
		public int maxValue = Integer.MAX_VALUE;
	
		public IntField(int charsWide, Ref target) {
			super(charsWide, target);
			format = "%d";
			charsAllowed = "0123456789-";
		}
		
		@Override
		protected Object parseValue(String text) {
			int value = 0;
			if (text.length() > 0)
				value = Integer.parseInt(text);
			if (value < minValue)
				value = minValue;
			if (value > maxValue)
				value = maxValue;
			return new Integer(value);
		}
	
	}

//------------------------------------------------------------------------------------------------

	public static class FloatField extends StringField {
	
		public double minValue = Double.MIN_VALUE;
		public double maxValue = Double.MAX_VALUE;
	
		public FloatField(int charsWide, String format, Ref target) {
			super(charsWide, target);
			this.format = format;
			charsAllowed = "0123456789.-eE";
		}
		
		@Override
		protected Object parseValue(String text) {
			double value = 0;
			if (text.length() > 0)
				value = Double.parseDouble(text);
			if (value < minValue)
				value = minValue;
			if (value > maxValue)
				value = maxValue;
			return new Double(value);
		}
	
	}

//------------------------------------------------------------------------------------------------

}
