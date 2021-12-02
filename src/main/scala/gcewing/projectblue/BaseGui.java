//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Generic GUI Screen
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.lang.reflect.*;
import java.util.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.util.*;

import net.minecraftforge.client.*;

//------------------------------------------------------------------------------------------------

public class BaseGui {

	public static class Screen extends GuiContainer implements BaseMod.ISetMod {
	
		final static int defaultTextColor = 0x404040;
	
		BaseMod mod;
		double uscale, vscale;
		float red = 1.0F, green = 1.0F, blue = 1.0F;
		public int textColor = defaultTextColor;
		public boolean textShadow = false;
		public Root root;
		public String title;
		protected IWidget mouseWidget;
	
		public Screen(Container container, int width, int height) {
			super(container);
			xSize = width;
			ySize = height;
			root = new Root(this);
		}
		
		public Screen(BaseContainer container) {
			this(container, container.xSize, container.ySize);
		}
		
		public int getWidth() {return xSize;}
		public int getHeight() {return ySize;}
		
		@Override
		public void setMod(BaseMod mod) {
			this.mod = mod;
		}
		
		@Override
		public void initGui() {
			super.initGui();
			root.layout();
		}
		
		@Override
		public void drawScreen(int par1, int par2, float par3) {
			resetColor();
			textColor = defaultTextColor;
			textShadow = false;
			super.drawScreen(par1, par2, par3);
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
			GL11.glPushMatrix();
			GL11.glTranslatef(guiLeft, guiTop, 0.0F);
			drawBackgroundLayer();
			if (title != null)
				drawTitle(title);
			root.draw(this, mouseX - guiLeft, mouseY - guiTop);
			GL11.glPopMatrix();
		}
		
		protected void drawBackgroundLayer() {
			drawGuiBackground(0, 0, xSize, ySize);
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int par1, int par2) {
			drawForegroundLayer();
		}
		
		protected void drawForegroundLayer() {
		}
	
		public void close() {
			dispatchClosure(root);
			onClose();
			mc.thePlayer.closeScreen();
		}
		
		protected void onClose() {
		}
		
		public void bindTexture(String path) {
			bindTexture(path, 1, 1);
		}
		
		public void bindTexture(String path, int usize, int vsize) {
			bindTexture(mod.client.textureLocation(path), usize, vsize);
		}
	
		public void bindTexture(ResourceLocation rsrc) {
			bindTexture(rsrc, 1, 1);
		}
		
		public void bindTexture(ResourceLocation rsrc, int usize, int vsize) {
			mc.getTextureManager().bindTexture(rsrc);
			uscale = 1.0 / usize;
			vscale = 1.0 / vsize;
			//System.out.printf("BaseGuiContainer.bindTexture: %s size (%s, %s) scale (%s, %s)\n",
			//	rsrc, usize, vsize, uscale, vscale);
		}
		
		public void drawRect(double x, double y, double w, double h) {
			glDisable(GL_TEXTURE_2D);
			glColor3d(red, green, blue);
			glBegin(GL_QUADS);
			glVertex3d(x, y+h, zLevel);
			glVertex3d(x+w, y+h, zLevel);
			glVertex3d(x+w, y, zLevel);
			glVertex3d(x, y, zLevel);
			glEnd();
			glEnable(GL_TEXTURE_2D);
		}
		
		public void drawBorderedRect(double x, double y, double w, double h,
			double u, double v, double uSize, double vSize, double cornerWidth, double cornerHeight)
		{
			double cw = cornerWidth, ch = cornerHeight;
			double sw = w - 2 * cornerWidth;       // side width
			double sh = h - 2 * cornerHeight;      // side height
			double usw = uSize - 2 * cornerWidth;  // u side width
			double ush = vSize - 2 * cornerHeight; // v side height
			double x1 = x + cw, x2 = w - cw;
			double y1 = y + ch, y2 = h - ch;
			double u1 = u + cw, u2 = uSize - cw;
			double v1 = v + ch, v2 = vSize - cw;
			drawTexturedRect(x, y, cw, ch, u, v);               // top left corner
			drawTexturedRect(x2, y, cw, ch, u2, v);             // top right corner
			drawTexturedRect(x, y2, cw, ch, u, v2);             // bottom left corner
			drawTexturedRect(x2, y2, cw, ch, u2, v2);           // bottom right corner
			drawTexturedRect(x1, y, sw, ch, u1, v, usw, ch);    // top side
			drawTexturedRect(x1, y2, sw, ch, u1, v2, usw, ch);  // bottom side
			drawTexturedRect(x, y1, cw, sh, u, v1, cw, ush);    // left side
			drawTexturedRect(x2, y1, cw, sh, u2, v1, cw, ush);  // right side
			drawTexturedRect(x1, y1, sw, sh, u1, v1, usw, ush); // centre
		}

		public void drawGuiBackground(double x, double y, double w, double h) {
			bindTexture("gui/gui_background.png", 16, 16);
			setColor(0xffffff);
			drawBorderedRect(x, y, w, h, 0, 0, 16, 16, 4, 4);
		}

		public void drawTexturedRect(double x, double y, double w, double h) {
			drawTexturedRectUV(x, y, w, h, 0, 0, 1, 1);
		}
		
		public void drawTexturedRect(double x, double y, double w, double h, double u, double v) {
			drawTexturedRect(x, y, w, h, u, v, w, h);
		}
	
		public void drawTexturedRect(double x, double y, double w, double h, double u, double v, double us, double vs) {
			drawTexturedRectUV(x, y, w, h, u * uscale, v * vscale, us * uscale, vs * vscale);
		}
		
		public void drawTexturedRectUV(double x, double y, double w, double h,
			double u, double v, double us, double vs)
		{
			//System.out.printf("BaseGuiContainer.drawTexturedRectUV: (%s,%s,%s,%s) (%s,%s,%s,%s)\n",
			//	x, y, w, h, u, v, us, vs);
			Tessellator tess = Tessellator.instance;
			tess.startDrawingQuads();
			tess.setColorOpaque_F(red, green, blue);
			tess.addVertexWithUV(x, y+h, zLevel, u, v+vs);
			tess.addVertexWithUV(x+w, y+h, zLevel, u+us, v+vs);
			tess.addVertexWithUV(x+w, y, zLevel, u+us, v);
			tess.addVertexWithUV(x, y, zLevel, u, v);
			tess.draw();
		}
		
		public void setColor(int hex) {
			setColor((hex >> 16) / 255.0, ((hex >> 8) & 0xff) / 255.0, (hex & 0xff) / 255.0);
		}
		
		public void setColor(double r, double g, double b) {
			red = (float)r;
			green = (float)g;
			blue = (float)b;
		}
		
		public void resetColor() {
			setColor(1, 1, 1);
		}
	
		public void drawString(String s, int x, int y) {
			fontRendererObj.drawString(s, x, y, textColor, textShadow);
		}
	
		public void drawCenteredString(String s, int x, int y) {
			fontRendererObj.drawString(s, x - fontRendererObj.getStringWidth(s) / 2, y, textColor, textShadow);
		}
		
		public void drawRightAlignedString(String s, int x, int y) {
			fontRendererObj.drawString(s, x - fontRendererObj.getStringWidth(s), y, textColor, textShadow);
		}
		
		public void drawTitle(String s) {
			drawCenteredString(s, xSize / 2, 4);
		}
		
		public void drawInventoryName(IInventory inv, int x, int y) {
			drawString(inventoryName(inv), x, y);
		}
		
		public void drawPlayerInventoryName() {
			drawString(playerInventoryName(), 8, ySize - 96 + 2);
		}
		
		public static String inventoryName(IInventory inv) {
			String name = inv.getInventoryName();
			if (!inv.hasCustomInventoryName())
				name = StatCollector.translateToLocal(name);
			return name;
		}
		
		public static String playerInventoryName() {
			return StatCollector.translateToLocal("container.inventory");
		}
		
		@Override
		protected void mouseMovedOrUp(int x, int y, int button) {
			super.mouseMovedOrUp(x, y, button);
			if (mouseWidget != null) {
				MouseCoords m = new MouseCoords(mouseWidget, x, y);
				if (button == -1)
					mouseWidget.mouseMoved(m);
				else {
					mouseWidget.mouseReleased(m, button);
					mouseWidget = null;
				}
			}
		}
		
		@Override
		public void mouseClicked(int x, int y, int button) {
			super.mouseClicked(x, y, button);
			mouseWidget = root.dispatchMousePress(x - guiLeft, y - guiTop, button);
			//System.out.printf("BaseGui.mouseClicked: mouseWidget = %s\n",
			//	mouseWidget.getClass().getSimpleName());
			if (mouseWidget != null) {
				closeOldFocus(mouseWidget);
				focusOn(mouseWidget);
				mouseWidget.mousePressed(new MouseCoords(mouseWidget, x - guiLeft, y - guiTop), button);
			}
		}
		
		void closeOldFocus(IWidget clickedWidget) {
			if (!isFocused(clickedWidget)) {
				IWidgetContainer parent = clickedWidget.parent();
				while (!isFocused(parent))
					parent = parent.parent();
				dispatchClosure(parent.getFocus());
			}
		}
		
		void dispatchClosure(IWidget target) {
			while (target != null) {
				target.close();
				target = getFocusOf(target);
			}
		}

		IWidget getFocusOf(IWidget widget) {
			if (widget instanceof IWidgetContainer)
				return ((IWidgetContainer)widget).getFocus();
			else
				return null;
		}
		
		@Override
		public void keyTyped(char c, int key) {
			if (!root.dispatchKeyPress(c, key)) {
				if (key == 1 || key == mc.gameSettings.keyBindInventory.getKeyCode())
					close();
				else
					super.keyTyped(c, key);
			}
		}
			
		public void focusOn(IWidget newFocus) {
			//System.out.printf("BaseGui.Screen.focusOn: %s\n", name(newFocus));
			IWidgetContainer parent = newFocus.parent();
			if (parent != null) {
				IWidget oldFocus = parent.getFocus();
				//System.out.printf("BaseGui.Screen.focusOn: Old parent focus = %s\n", name(oldFocus));
				if (isFocused(parent)) {
					//System.out.printf("BaseGui.Screen.focusOn: Parent is focused\n");
					if (oldFocus != newFocus) {
						tellFocusChanged(oldFocus, false);
						parent.setFocus(newFocus);
						tellFocusChanged(newFocus, true);
					}
				}
				else {
					//System.out.printf("BaseGui.Screen.focusOn: Parent is not focused\n");
					parent.setFocus(newFocus);
					focusOn(parent);
				}
			}
		}
		
		public void focusChanged(boolean state) {
		}
		
	//	public void onAction(IWidget sender, String action) {
	//	}

	}
	
	static boolean isFocused(IWidget widget) {
		if (widget == null)
			return false;
		else if (widget instanceof Root)
			return true;
		else {
			IWidgetContainer parent = widget.parent();
			return (parent != null && parent.getFocus() == widget && isFocused(parent));
		}
	}
	
	static void tellFocusChanged(IWidget widget, boolean state) {
		//System.out.printf("BaseGui.tellFocusChanged: to %s for %s\n", state, name(widget));
		if (widget != null) {
			widget.focusChanged(state);
			if (widget instanceof IWidgetContainer)
				tellFocusChanged(((IWidgetContainer)widget).getFocus(), state);
		}
	}
		
	static String name(Object obj) {
		if (obj != null)
			return obj.getClass().getSimpleName();
		else
			return "null";
	}
		
	//------------------------------------------------------------------------------------------------
	
	public static class MouseCoords {
	
		int x, y;
		
		public MouseCoords(IWidget widget, int x, int y) {
			while (widget != null) {
				x -= widget.left();
				y -= widget.top();
				widget = widget.parent();
			}
			this.x = x;
			this.y = y;
		}
		
	}
	
	//------------------------------------------------------------------------------------------------
	
	public interface IWidget {
		IWidgetContainer parent();
		void setParent(IWidgetContainer widget);
		int left();
		int top();
		int width();
		int height();
		void setLeft(int x);
		void setTop(int y);
		void draw(Screen scr, int mouseX, int mouseY);
		IWidget dispatchMousePress(int x, int y, int button);
		boolean dispatchKeyPress(char c, int key);
		void mousePressed(MouseCoords m, int button);
		void mouseMoved(MouseCoords m);
		void mouseReleased(MouseCoords m, int button);
		boolean keyPressed(char c, int key);
		void focusChanged(boolean state);
		void close();
		void layout();
	}
	
	//------------------------------------------------------------------------------------------------
	
	public static class Widget implements IWidget {
	
		public IWidgetContainer parent;
		//public IWidget focus;
		public int left, top, width, height;
		
		public Widget() {
		}
		
		public Widget(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		public IWidgetContainer parent() {return parent;}
		public void setParent(IWidgetContainer widget) {parent = widget;}
		public int left() {return left;}
		public int top() {return top;}
		public int width() {return width;}
		public int height() {return height;}
		public void setLeft(int x) {left = x;}
		public void setTop(int y) {top = y;}
		
		public void draw(Screen scr, int mouseX, int mouseY) {}
		public void mousePressed(MouseCoords m, int button) {}
		public void mouseMoved(MouseCoords m) {}
		public void mouseReleased(MouseCoords m, int button) {}
		public boolean keyPressed(char c, int key) {return false;}
		public void focusChanged(boolean state) {}
		public void close() {}
		public void layout() {}

		public IWidget dispatchMousePress(int x, int y, int button) {
			//System.out.printf("BaseGui.Widget.dispatchMousePress: (%s, %s) in %s\n",
			//	x, y, getClass().getSimpleName());
			return this;
		}
	
		public boolean dispatchKeyPress(char c, int key) {
			return this.keyPressed(c, key);
		}
		
		public static int stringWidth(String s) {
			return Minecraft.getMinecraft().fontRenderer.getStringWidth(s);
		}
		
		public void addPopup(int x, int y, IWidget widget) {
			IWidget w = this;
			while (!(w instanceof Root)) {
				x += w.left();
				y += w.top();
				w = w.parent();
			}
			((Root)w).addPopup(x, y, widget);
		}
		
		public void removePopup() {
			Root root = getRoot();
			root.remove(this);
		}
		
		public Root getRoot() {
			IWidget w = this;
			while (w != null && !(w instanceof Root))
				w = w.parent();
			return (Root)w;
		}

	}
	
	//------------------------------------------------------------------------------------------------
	
	public interface IWidgetContainer extends IWidget {
		//void add(int left, int top, IWidget widget);
		IWidget getFocus();
		void setFocus(IWidget widget);
		//void onAction(IWidget sender, String action);
	}
	
	//------------------------------------------------------------------------------------------------
	
	public static class Group extends Widget implements IWidgetContainer {
	
		protected List<IWidget> widgets = new ArrayList<IWidget>();
		protected IWidget focus;
		
		public IWidget getFocus() {return focus;}
		
		public void setFocus(IWidget widget) {
			//System.out.printf("BaseGui.Group.setFocus: of %s to %s\n",
			//	getClass().getSimpleName(), widget.getClass().getSimpleName());
			focus = widget;
		}
		
		public void add(int left, int top, IWidget widget) {
			widget.setLeft(left);
			widget.setTop(top);
			widget.setParent(this);
			widgets.add(widget);
		}
		
		public void remove(IWidget widget) {
			widgets.remove(widget);
			if (getFocus() == widget) {
				if (isFocused(this))
					tellFocusChanged(widget, false);
				setFocus(null);
			}
		}
		
		@Override
		public void draw(Screen scr, int mouseX, int mouseY) {
			super.draw(scr, mouseX, mouseY);
			for (IWidget w : widgets) {
				int dx = w.left(), dy = w.top();
				glPushMatrix();
				glTranslated(dx, dy, 0);
				w.draw(scr, mouseX - dx, mouseY - dy);
				glPopMatrix();
			}
		}
		
		@Override
		public IWidget dispatchMousePress(int x, int y, int button) {
			//System.out.printf("BaseGui.Group.dispatchMousePress: (%s, %s) in %s\n",
			//	x, y, getClass().getSimpleName());
			IWidget target = findWidget(x, y);
			if (target != null)
				return target.dispatchMousePress(x - target.left(), y - target.top(), button);
			else
				return this;
		}
		
		@Override
		public boolean dispatchKeyPress(char c, int key) {
			IWidget focus = getFocus();
			if (focus != null && focus.dispatchKeyPress(c, key))
				return true;
			else
				return super.dispatchKeyPress(c, key);
		}
		
		public IWidget findWidget(int x, int y) {
			for (int i = widgets.size() - 1; i >= 0; i--) {
				IWidget w = widgets.get(i);
				int l = w.left(), t = w.top();
				if (x >= l && y >= t && x < l + w.width() && y < t + w.height())
					return w;
			}
			return null;
		}
		
		@Override
		public void layout() {
			for (IWidget w : widgets)
				w.layout();
		}

	}

	//------------------------------------------------------------------------------------------------
	
	public static class Root extends Group {
	
		public Screen screen;
		public List<IWidget> popupStack;
		
		public Root(Screen screen) {
			this.screen = screen;
			popupStack = new ArrayList<IWidget>();
		}
		
		@Override
		public int width() {return screen.getWidth();}
		
		@Override
		public int height() {return screen.getHeight();}
		
		@Override
		public IWidget dispatchMousePress(int x, int y, int button) {
			IWidget w = topPopup();
			if (w == null)
				w = super.dispatchMousePress(x, y, button);
			return w;
		}
		
		@Override
		public void addPopup(int x, int y, IWidget widget) {
			add(x, y, widget);
			popupStack.add(widget);
			screen.focusOn(widget);
		}
		
		@Override
		public void remove(IWidget widget) {
			super.remove(widget);
			popupStack.remove(widget);
			focusTopPopup();
		}
		
		public IWidget topPopup() {
			int n = popupStack.size();
			if (n > 0)
				return popupStack.get(n - 1);
			else
				return null;
		}
		
		void focusTopPopup() {
			IWidget w = topPopup();
			if (w != null)
				screen.focusOn(w);
		}

	}

//------------------------------------------------------------------------------------------------

	public interface Ref {
		Object get();
		void set(Object value);
	}
	
	public static class FieldRef implements Ref {
		
		public Object target;
		public Field field;
		
		public FieldRef(Object target, String name) {
			try {
				this.target = target;
				this.field = target.getClass().getField(name);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public Object get() {
			try {
				return field.get(target);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public void set(Object value) {
			try {
				field.set(target, value);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	
	}
	
	public static class PropertyRef implements Ref {
	
		public Object target;
		public Method getter, setter;
		
		public PropertyRef(Object target, String getterName, String setterName) {
			this.target = target;
			try {
				Class cls = target.getClass();
				getter = cls.getMethod(getterName);
				setter = cls.getMethod(setterName, getter.getReturnType());
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public Object get() {
			try {
				return getter.invoke(target);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public void set(Object value) {
			try {
				setter.invoke(target, value);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	
	}
	
	public static Ref ref(Object target, String name) {
		return new FieldRef(target, name);
	}

	public static Ref ref(Object target, String getterName, String setterName) {
		return new PropertyRef(target, getterName, setterName);
	}

	//------------------------------------------------------------------------------------------------
	
	public interface Action {
		public void perform();
	}
	
	public static class MethodAction implements Action {
	
		Object target;
		Method method;
	
		public MethodAction(Object target, String name) {
			try {
				this.target = target;
				method = target.getClass().getMethod(name);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public void perform() {
			try {
				method.invoke(target);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	
	}
	
	public static Action action(Object target, String name) {
		return new MethodAction(target, name);
	}

	//------------------------------------------------------------------------------------------------
	
}
