// ------------------------------------------------------------------------------------------------
//
// Project Blue - Control Panel Label GUI
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import static org.lwjgl.input.Keyboard.*;

import net.minecraft.client.gui.*;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.input.Keyboard;

public class ControlPanelLabelScreen extends GuiScreen {

    static String screenTitle = "Edit Control Panel Text";
    static int numLines = 2;
    static int maxLength = 6;

    ControlPanelPart part;
    int editingCell;
    int editingLine;
    GuiButton doneButton;
    int updateCounter;

    public ControlPanelLabelScreen(ControlPanelPart part, int cell) {
        this.part = part;
        this.editingCell = cell;
        this.editingLine = 0;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        doneButton = new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, "Done");
        buttonList.add(doneButton);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        close();
    }

    void close() {
        this.mc.displayGuiScreen(null);
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == KEY_UP) editingLine = (editingLine - 1) % numLines;
        else if (key == KEY_DOWN || key == KEY_RETURN || key == KEY_NUMPADENTER)
            editingLine = (editingLine + 1) % numLines;
        else if (key == KEY_BACK) backspace();
        else if (ChatAllowedCharacters.isAllowedCharacter(c)) insert(c);
        else if (key == KEY_ESCAPE) close();
    }

    void backspace() {
        String s = getLine();
        int n = s.length() - 1;
        if (n >= 0) setLine(s.substring(0, n));
    }

    void insert(char c) {
        String s = getLine();
        // if (s.length() < maxLength)
        setLine(s + c);
    }

    String getLine() {
        return getLine(editingLine);
    }

    String getLine(int i) {
        return part.labels[editingCell][i];
    }

    void setLine(String s) {
        part.labels[editingCell][editingLine] = s;
        ProjectBlue.channel.sendUpdateControlPanelText(part, editingCell, editingLine, s);
    }

    @Override
    public void updateScreen() {
        ++this.updateCounter;
    }

    @Override
    public void drawScreen(int x, int y, float f) {
        FontRenderer fr = fontRendererObj;
        int color = 0xffffff;
        int cx = width / 2;
        drawDefaultBackground();
        ProjectBlue.mod.client.bindTexture("gui/controlpanel_cutout-24.png");
        func_146110_a(cx - 12, 92, 0, 0, 24, 24, 24, 24);
        drawCenteredString(fr, screenTitle, cx, 40, color);
        for (int i = 0; i < numLines; i++) {
            String s = getLine(i);
            if (i == editingLine && updateCounter / 6 % 2 == 0) s = "> " + s + " <";
            drawCenteredString(fr, s, cx, 80 + i * 40, color);
        }
        super.drawScreen(x, y, f);
    }

}
