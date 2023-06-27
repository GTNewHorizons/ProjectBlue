// ------------------------------------------------------------------------------------------------
//
// Project Blue - Control Panel Renderer
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import static gcewing.projectblue.BaseUtils.*;
import static gcewing.projectblue.ProjectBlue.getModel;
import static gcewing.projectblue.Trans3GL.*;
import static gcewing.projectblue.Utils.*;
import static java.lang.Math.*;
import static net.minecraft.util.MovingObjectPosition.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.util.*;

import codechicken.lib.colour.*;
import codechicken.lib.vec.BlockCoord;
import codechicken.microblock.*;
import codechicken.multipart.*;
import cpw.mods.fml.relauncher.*;
import gcewing.projectblue.ControlPanelPart.ControlType;
import mrtjp.projectred.core.*;

public class ControlPanelRenderer extends BaseBlockRenderer<Block> {

    final static double h = 1 / 16.0; // Half thickness of control panel
    final static double d1 = 0.001, d2 = 0.01; // Offsets to prevent z-fighting

    static float sideShading[] = { 0.5F, 1F, 0.8F, 0.8F, 0.6F, 0.6F };
    static double colors[][] = { { 1, 1, 1 }, // white
            { 1, 0.5, 0 }, // orange
            { 1, 0, 1 }, // magenta
            { 0.5, 0.5, 1 }, // light blue
            { 1, 1, 0 }, // yellow
            { 0.5, 1, 0.5 }, // lime
            { 1, 0.5, 0.5 }, // pink
            { 0.5, 0.5, 0.5 }, // grey
            { 0.75, 0.75, 0.75 }, // light grey
            { 0, 1, 1 }, // cyan
            { 0.5, 0, 1 }, // purple
            { 0, 0, 1 }, // blue
            { 0.5, 0.25, 0 }, // brown
            { 0, 1, 0 }, // green
            { 1, 0, 0 }, // red
            { 0.25, 0.25, 0.25 } // black
    };

    static AxisAlignedBB panelBox = AxisAlignedBB.getBoundingBox(-0.5, -h - d1, -0.5, 0.5, h + d1, 0.5);

    static Matrix3 leverRotations[] = { Matrix3.rotX(45), Matrix3.rotX(-45) };

    public static ControlPanelRenderer instance;

    static IIcon frontCutoutIcon, backCutoutIcon, lampIcon;
    static PBModel leverBaseModel, leverHandleModel, buttonModel, lampModel, coverModel;

    ControlPanelPart part;
    Phase phase;
    int globalSide; // Global side being rendered
    float alpha;
    boolean renderingInInventory;
    boolean debugLightVertex = false;

    public static enum Phase {
        STATIC,
        DYNAMIC,
        BREAKING
    };

    public static void init() {
        leverBaseModel = getModel("lever_base");
        leverHandleModel = getModel("lever_handle");
        buttonModel = getModel("button");
        lampModel = getModel("lamp");
        coverModel = getModel("cover");
        instance = new ControlPanelRenderer();
    }

    public static void registerIcons(IIconRegister reg) {
        // System.out.printf("ControlPanelRenderer.registerIcons\n");
        frontCutoutIcon = reg.registerIcon("gcewing_projectblue:controlpanel_cutout_front");
        backCutoutIcon = reg.registerIcon("gcewing_projectblue:controlpanel_cutout_back");
        lampIcon = reg.registerIcon("gcewing_projectblue:controlpanel_lamp");
        System.out.printf(
                "ControlPanelRenderer.registerIcons: icons = %s %s %s\n",
                frontCutoutIcon,
                backCutoutIcon,
                lampIcon);
    }

    void bindBlockTextures() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    }

    public void renderStaticInWorld(ControlPanelPart part, double x, double y, double z) {
        renderInWorld(part, x, y, z, Phase.STATIC);
    }

    public void renderDynamicInWorld(ControlPanelPart part, double x, double y, double z, float frame) {
        bindBlockTextures();
        renderInWorld(part, x, y, z, Phase.DYNAMIC);
    }

    public boolean renderHighlight(ControlPanelPart part, DrawBlockHighlightEvent e, BlockCoord pos) {
        EntityPlayer p = e.player;
        Vec3 h = e.target.hitVec;
        int side = e.target.sideHit;
        float f = e.partialTicks;
        // System.out.printf(
        // "ControlPanelRenderer.renderHighlight: player at (%.3f,%.3f,%.3f) hit (%.3f,%.3f,%.3f) side %s " +
        // "stack %s nbt %s\n",
        // p.posX, p.posY, p.posZ,
        // h.xCoord, h.yCoord, h.zCoord,
        // side, e.currentItem, e.currentItem.getTagCompound());
        glPushMatrix();
        glPushAttrib(GL_ENABLE_BIT);
        glTranslated(
                -(p.lastTickPosX + (p.posX - p.lastTickPosX) * f),
                -(p.lastTickPosY + (p.posY - p.lastTickPosY) * f),
                -(p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * f));
        FacePlacementGrid.render(ccVector3(h), side);
        setup(part.base);
        world = p.worldObj;
        blockX = pos.x;
        blockY = pos.y;
        blockZ = pos.z;
        Trans3 t = part.localToGlobalTransformation(pos.x, pos.y, pos.z);
        phase = Phase.STATIC;
        bindBlockTextures();
        glEnable(GL_BLEND);
        alpha = 0.5F;
        tess.startDrawingQuads();
        debugLightVertex = true;
        renderPart(t);
        debugLightVertex = false;
        tess.draw();
        glPopAttrib();
        glPopMatrix();
        // System.out.printf("ControlPanelRenderer.renderHighlight: finished\n");
        return true;
    }

    public void renderStack(ItemStack stack, ItemRenderType type) {
        ControlPanelMaterial base = ControlPanelMaterial.forStack(stack);
        setup(base);
        int side = 2;
        if (type == ItemRenderType.INVENTORY) {
            renderingInInventory = true;
            side = 3;
        }
        part = new ControlPanelPart(base, side, stack.getTagCompound());
        Trans3 t = new Trans3(0.5, 0.5, 0.5).side(part.side);
        phase = Phase.STATIC;
        tess.startDrawingQuads();
        renderPart(t);
        tess.draw();
        phase = Phase.DYNAMIC;
        renderPart(t);
    }

    public void renderControlItemStack(ItemStack stack, ItemRenderType type) {
        setup(ControlPanelMaterial.forStack(stack));
        renderControlItem((ControlItem) stack.getItem(), stack.getItemDamage(), type);
    }

    void renderControlItem(ControlItem item, int meta, ItemRenderType type) {
        renderingInInventory = false;
        bindBlockTextures();
        Trans3 t = new Trans3(0.5, 0.5, 0.5);
        switch (type) {
            case INVENTORY:
                renderingInInventory = true;
                t = t.side(3).scale(4.0);
                break;
            case EQUIPPED:
                t = t.side(3).scale(3.0).translate(0, 0.2, 0.05);
                break;
            case EQUIPPED_FIRST_PERSON:
                t = t.side(2).translate(0, -0.5, 0.15);
                break;
            default:
                t = t.side(2).scale(2.0);
        }
        phase = Phase.STATIC;
        tess.startDrawingQuads();
        renderControlType(t, item.type, meta);
        tess.draw();
        phase = Phase.DYNAMIC;
        renderControlType(t, item.type, meta);
    }

    void renderControlType(Trans3 t, ControlType type, int meta) {
        switch (type) {
            case LEVER:
                renderLever(t, meta, 0);
                break;
            case BUTTON:
                renderButton(t, meta, 0);
                break;
            case LAMP:
                renderLamp(t, meta, 0);
                break;
            case BLANK:
                renderCover(t);
                break;
        }
    }

    void setup(ControlPanelMaterial base) {
        if (base != null) setup(base.block, base.metadata);
        else setup(Blocks.planks, 0);
    }

    void setup(Block block, int metadata) {
        renderingInInventory = false;
        part = null;
        world = null;
        this.block = block;
        this.metadata = metadata;
        blockX = 0;
        blockY = 0;
        blockZ = 0;
        setUpTextureOverride(null);
        blockBrightness = 0xf000f0;
        setColorMultiplier(0xffffff);
        alpha = 1.0F;
        tess = Tessellator.instance;
        // tess.setColorOpaque_F(1, 1, 1);
        // tess.setBrightness(blockBrightness);
    }

    void renderInWorld(ControlPanelPart part, double x, double y, double z, Phase phase) {
        // if (phase == Phase.DYNAMIC)
        // System.out.printf("ControlPanelRenderer.renderInWorld: dynamic at (%.3f,%.3f,%.3f)\n",
        // x, y, z);
        renderingInInventory = false;
        this.part = part;
        this.phase = phase;
        this.world = part.world();
        this.block = part.base.block;
        this.metadata = part.base.metadata;
        blockX = part.x();
        blockY = part.y();
        blockZ = part.z();
        setUpTextureOverride(null);
        blockBrightness = block.getMixedBrightnessForBlock(world, blockX, blockY, blockZ);
        setColorMultiplier(block.colorMultiplier(world, blockX, blockY, blockZ));
        alpha = 1.0F;
        tess = Tessellator.instance;
        tess.setColorOpaque_F(1, 1, 1);
        Trans3 t = part.localToGlobalTransformation(x, y, z);
        renderPart(t);
    }

    void renderPart(Trans3 t) {
        renderPanel(t);
        renderLabels(t);
        renderControls(t);
    }

    void renderPanel(Trans3 t) {
        switch (phase) {
            case STATIC:
                pbtBox(t, panelBox);
                renderBackCutouts(t);
                break;
        }
    }

    void renderLabels(Trans3 t) {
        if (part != null) {
            switch (phase) {
                case DYNAMIC:
                    FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
                    glPushAttrib(GL_LIGHTING_BIT | GL_TEXTURE_BIT);
                    glPushMatrix();
                    glMultTrans3(t);
                    glRotatef(-90, 1, 0, 0);
                    glRotatef(180, 0, 0, 1);
                    glTranslated(-0.5, -0.5, -h - 2 * d1);
                    glScaled(1 / 160.0, 1 / 160.0, 1);
                    for (int i = 0; i < 4; i++) for (int j = 0; j < 4; j++) for (int k = 0; k < 2; k++) {
                        String s = part.labels[4 * i + j][k];
                        if (s != null) {
                            int w = fr.getStringWidth(s) / 2;
                            fr.drawString(s, j * 40 + 20 - w, 1 + i * 40 + k * 30, 0);
                        }
                    }

                    glPopMatrix();
                    glPopAttrib();
            }
        }
    }

    void renderControls(Trans3 t) {
        selectSide(t, 0);
        lightVertex(t, 0, 0, 0);
        for (int i = 0; i < 4; i++) for (int j = 0; j < 4; j++) {
            Trans3 tij = t.translate(3 / 8.0 - j * 0.25, -h, -3 / 8.0 + i * 0.25);
            int k = 4 * i + j;
            if (part == null) renderFrontCutout(tij);
            else {
                int meta = part.getControlMeta(k);
                int state = part.getControlState(k);
                switch (part.getControlType(k)) {
                    case NONE:
                        renderFrontCutout(tij);
                        break;
                    case LEVER:
                        renderLever(tij, meta, state);
                        break;
                    case BUTTON:
                        renderButton(tij, meta, state);
                        break;
                    case LAMP:
                        renderLamp(tij, meta, state);
                        break;
                }
            }
        }
    }

    void renderBackCutouts(Trans3 t) {
        selectSideAndTile(t, 1, backCutoutIcon);
        lightVertex(t, 0, h, 0);
        for (int i = 0; i < 4; i++) for (int j = 0; j < 4; j++)
            face(t, -0.5 + i * 0.25, h + d2, -0.5 + j * 0.25, 0, 0, 0.25, 0.25, 0, 0, 0, 0, 1, 1);
    }

    void renderFrontCutout(Trans3 t) {
        switch (phase) {
            case STATIC:
                selectSideAndTile(t, 0, renderingInInventory ? backCutoutIcon : frontCutoutIcon);
                lightVertex(t, 0, -h, 0);
                face(t, -1 / 8.0, -d2, 1 / 8.0, 0, 0, -0.25, 0.25, 0, 0, 0, 0, 1, 1);
                break;
        }
    }

    void renderLever(Trans3 t, int meta, int state) {
        IIcon icon;
        switch (phase) {
            case STATIC:
                if (meta == 16) icon = Blocks.cobblestone.getIcon(0, 0);
                else icon = Blocks.wool.getIcon(0, meta & 0xf);
                leverBaseModel.tessellate(t, icon);
                break;
            case DYNAMIC:
                Trans3 tl = t.translate(0, -h, 0).rotate(leverRotations[state]);
                glColor3d(1, 1, 1);
                leverHandleModel.draw(tl, Blocks.lever.getIcon(0, 0));
                break;
        }
    }

    void renderButton(Trans3 t, int meta, int state) {
        IIcon icon;
        switch (phase) {
            case DYNAMIC:
                if (meta == 16) icon = Blocks.stone.getIcon(0, 0);
                else if (meta == 17) icon = Blocks.planks.getIcon(0, 0);
                else icon = Blocks.wool.getIcon(0, meta & 0xf);
                Trans3 tb = t.translate(0, 1 / 32.0 * state, 0);
                glColor3d(1, 1, 1);
                buttonModel.draw(tb, icon);
                break;
        }
    }

    void renderLamp(Trans3 t, int meta, int state) {
        switch (phase) {
            case DYNAMIC:
                glPushAttrib(GL_LIGHTING_BIT);
                setLightingDisabled(state != 0);
                double b = 0.25 + state * 0.75;
                double[] c = colors[meta & 0xf];
                glColor3d(b * c[0], b * c[1], b * c[2]);
                lampModel.draw(t, lampIcon);
                setLightingDisabled(false);
                glPopAttrib();
                break;
        }
    }

    void renderCover(Trans3 t) {
        switch (phase) {
            case DYNAMIC:
                glColor3d(1, 1, 1);
                coverModel.draw(t, block.getIcon(0, 0));
                break;
        }
    }

    // pbt = projected block texture

    void pbtBox(Trans3 t, AxisAlignedBB box) {
        pbtBox(t, box.minX, box.minY, box.minZ, box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ);
    }

    void pbtBox(Trans3 t, double x0, double y0, double z0, double dx, double dy, double dz) {
        double x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
        pbtBoxFace(t, 0, x0, y0, z1, 0, 0, -dz, dx, 0, 0); // DOWN
        pbtBoxFace(t, 1, x0, y1, z0, 0, 0, dz, dx, 0, 0); // UP
        pbtBoxFace(t, 2, x1, y1, z0, 0, -dy, 0, -dx, 0, 0); // NORTH
        pbtBoxFace(t, 3, x0, y1, z1, 0, -dy, 0, dx, 0, 0); // SOUTH
        pbtBoxFace(t, 4, x0, y1, z0, 0, -dy, 0, 0, 0, dz); // WEST
        pbtBoxFace(t, 5, x1, y1, z1, 0, -dy, 0, 0, 0, -dz); // EAST
    }

    void pbtBoxFace(Trans3 t, int side, double x, double y, double z, double dx1, double dy1, double dz1, double dx2,
            double dy2, double dz2) {
        selectSideAndTile(t, side, block.getIcon(side, metadata));
        ptRect(t, x, y, z, dx1, dy1, dz1, dx2, dy2, dz2);
    }

    // pt = projected texture

    void ptRect(Trans3 t, double x, double y, double z, double dx1, double dy1, double dz1, double dx2, double dy2,
            double dz2) {
        // if (debugLightVertex)
        // System.out.printf("ContolPanelRenderer.ptRect\n");
        lightVertex(t, x + 0.5 * (dx1 + dx2), y + 0.5 * (dy1 + dy2), z + 0.5 * (dz1 + dz2));
        ptVertex(t, x, y, z);
        ptVertex(t, x + dx1, y + dy1, z + dz1);
        ptVertex(t, x + dx1 + dx2, y + dy1 + dy2, z + dz1 + dz2);
        ptVertex(t, x + dx2, y + dy2, z + dz2);
    }

    void ptVertex(Trans3 t, double x, double y, double z) {
        Vector3 p = t.p(x, y, z);
        double u = 0, v = 0;
        switch (globalSide) {
            case 0: // DOWN
            case 1:
                u = p.x - blockX;
                v = p.z - blockZ;
                break; // UP
            case 2: // NORTH
            case 3:
                u = p.x - blockX;
                v = -p.y + blockY + 1;
                break; // SOUTH
            case 4: // WEST
            case 5:
                u = p.z - blockZ;
                v = -p.y + blockY + 1;
                break; // EAST
        }
        // if (debugLightVertex)
        // System.out.printf(
        // "ControlPanelRenderer.vertex: side %s (%s,%s,%s) -> (%s,%s,%s) uv (%s,%s) (%s,%s)\n",
        // globalSide, x, y, z, p.x, p.y, p.z, u, v, u0 + u * us, v0 + v * vs);
        tess.addVertexWithUV(p.x, p.y, p.z, u0 + u * us, v0 + v * vs);
    }

    // double frac(double x) {
    // return x - floor(x);
    // }

    void selectSideAndTile(Trans3 t, int i, IIcon icon) {
        selectSide(t, i);
        selectTile(icon, 1, 1);
    }

    void selectSide(Trans3 t, int i) {
        // Set up to render local side i
        ForgeDirection ld = ForgeDirection.getOrientation(i);
        ForgeDirection gd = t.t(ld);
        globalSide = gd.ordinal();
        tess.setNormal(gd.offsetX, gd.offsetY, gd.offsetZ);
    }

    void lightVertex(Trans3 t, double x, double y, double z) {
        if (world != null) {
            Vector3 p = t.p(x, y, z);
            ForgeDirection gd = ForgeDirection.getOrientation(globalSide);
            int br = block.getMixedBrightnessForBlock(
                    world,
                    (int) floor(p.x + 0.01 * gd.offsetX),
                    (int) floor(p.y + 0.01 * gd.offsetY),
                    (int) floor(p.z + 0.01 * gd.offsetZ));
            tess.setBrightness(br);
            float bm = sideShading[globalSide];
            // tess.setColorOpaque_F(bm * cmr, bm * cmg, bm * cmb);
            tess.setColorRGBA_F(bm * cmr, bm * cmg, bm * cmb, alpha);
            // if (debugLightVertex)
            // System.out.printf("ControlPanelRenderer.lightVertex: br = 0x%x bm = %.3f\n", br, bm);
        } else {
            tess.setColorOpaque_F(1, 1, 1);
            tess.setBrightness(blockBrightness);
        }
    }

    public void setLightingDisabled(boolean off) {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        if (off) {
            glDisable(GL_LIGHTING);
            if (!renderingInInventory) glDisable(GL_TEXTURE_2D);
        } else {
            glEnable(GL_LIGHTING);
            if (!renderingInInventory) glEnable(GL_TEXTURE_2D);
        }
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

}
