// ------------------------------------------------------------------------------------------------
//
// Project Blue - Client Proxy
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import static net.minecraft.util.MovingObjectPosition.*;

import java.io.*;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.*;

import codechicken.multipart.*;
import cpw.mods.fml.common.eventhandler.*;
import gcewing.projectblue.ProjectBlueChannel.Message;

public class ProjectBlueClient extends BaseModClient<ProjectBlue> {

    public ProjectBlueClient(ProjectBlue mod) {
        super(mod);
        MinecraftForge.EVENT_BUS.register(this);
        mod.integrateWith("NotEnoughItems", "gcewing.projectblue.nei.NEIIntegration");
    }

    @Override
    void registerBlockIcons(IIconRegister reg) {
        // PneumaticTubeRenderer.registerIcons(reg);
        // PneumaticTubeWireRenderer.registerIcons(reg);
    }

    @Override
    void registerScreens() {
        // addScreen(PBGui.PneumaticExtractor, PneumaticExtractorScreen.class);
    }

    @Override
    void registerRenderers() {
        ControlPanelRenderer.init();
        // PneumaticTubeRenderer.init();
        // PneumaticTubeWireRenderer.init();
        PBFacePart.FaceItem.registerRenderers(this);
        addItemRenderer(ProjectBlue.controlPanelItem, new ControlPanelItemRenderer());
        addItemRenderer(ProjectBlue.miniatureLever, new ControlItemRenderer());
        addItemRenderer(ProjectBlue.miniatureButton, new ControlItemRenderer());
        addItemRenderer(ProjectBlue.miniatureLamp, new ControlItemRenderer());
        addItemRenderer(ProjectBlue.miniatureCover, new ControlItemRenderer());
        // addItemRenderer(ProjectBlue.pneumaticTube, new PneumaticTubeItemRenderer());
    }

    @SubscribeEvent
    public void drawBlockHighlight(DrawBlockHighlightEvent e) {
        // System.out.printf("ProjectBlueClient.drawBlockHighlightEvent\n");
        if (e.currentItem != null && e.target != null && e.target.typeOfHit == MovingObjectType.BLOCK) {
            Item item = e.currentItem.getItem();
            if (item instanceof IBlockHighlighting)
                if (((IBlockHighlighting) item).renderHighlight(e)) e.setCanceled(true);
        }
    }

    public void onReceiveFromServer(Message type, NBTTagCompound nbt) {
        switch (type) {
            case EDIT_CONTROL_PANEL_TEXT:
                onReceiveEditControlPanelText(nbt);
                break;
        }
    }

    void onReceiveEditControlPanelText(NBTTagCompound nbt) {
        System.out.printf("ProjectBlueChannel.onReceiveEditControlPanelText: %s\n", nbt);
        TMultiPart part = getClientPart(nbt);
        System.out.printf("ProjectBlueChannel.onReceiveEditControlPanelText: part = %s\n", part);
        if (part instanceof ControlPanelPart) {
            int cell = nbt.getInteger("cell");
            openClientGui(new ControlPanelLabelScreen((ControlPanelPart) part, cell));
        }
    }

    TMultiPart getClientPart(NBTTagCompound nbt) {
        return ProjectBlueChannel.getPart(nbt, Minecraft.getMinecraft().theWorld);
    }

    // void openGui(GuiScreen screen) {
    // Minecraft.getMinecraft().displayGuiScreen(screen);
    // }

}
