// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base - Generic Block Renderer
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.block.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.*;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class BaseBlockRenderer<BLOCK extends Block> implements ISimpleBlockRenderingHandler {

    public int renderID;

    Tessellator tess;
    IBlockAccess world;
    BLOCK block;
    int blockX, blockY, blockZ;
    int metadata;
    double u0, v0, u1, v1, us, vs;
    boolean textureOverridden;
    float cmr, cmg, cmb; // colour multiplier
    int blockBrightness;

    @Override
    public int getRenderId() {
        return renderID;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks rb) {
        // System.out.printf("BaseBlockRenderer.renderInventoryBlock\n");
        this.world = null;
        this.block = (BLOCK) block;
        blockX = 0;
        blockY = 0;
        blockZ = 0;
        setUpTextureOverride(rb);
        this.metadata = metadata;
        blockBrightness = 0xf000f0;
        setColorMultiplier(0xffffff);
        tess = Tessellator.instance;
        tess.setColorOpaque_F(1, 1, 1);
        // tess.setBrightness(15);
        // Trans3 t = new Trans3(0.5, 0.5, 0.5);
        Trans3 t = localToInventoryTransformation(metadata);
        tess.startDrawingQuads();
        renderBlock(t);
        tess.draw();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int renderID,
            RenderBlocks rb) {
        return renderStandardWorldBlock(world, x, y, z, block, renderID, rb);
    }

    public boolean renderStandardWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int renderID,
            RenderBlocks rb) {
        // System.out.printf("BaseBlockRenderer.renderWorldBlock at %d, %d, %d\n", x, y, z);
        // if (!(world instanceof World))
        // System.out.printf("BaseBlockRenderer.renderWorldBlock from %s\n", world);
        this.world = world;
        this.block = (BLOCK) block;
        blockX = x;
        blockY = y;
        blockZ = z;
        setUpTextureOverride(rb);
        metadata = world.getBlockMetadata(x, y, z);
        blockBrightness = block.getMixedBrightnessForBlock(world, x, y, z);
        setColorMultiplier(block.colorMultiplier(world, x, y, z));
        tess = Tessellator.instance;
        tess.setColorOpaque_F(1, 1, 1);
        Trans3 t = localToGlobalTransformation();
        return renderBlock(t);
    }

    Trans3 localToInventoryTransformation(int metadata) {
        return new Trans3(0, 0, 0);
    }

    Trans3 localToGlobalTransformation() {
        return new Trans3(blockX + 0.5, blockY + 0.5, blockZ + 0.5);
    }

    boolean renderBlock(Trans3 t) {
        renderCube(t);
        return true;
    }

    void setColorMultiplier(int color) {
        cmr = ((color >> 16) & 0xff) / 255.0F;
        cmg = ((color >> 8) & 0xff) / 255.0F;
        cmb = (color & 0xff) / 255.0F;
    }

    void setUpTextureOverride(RenderBlocks rb) {
        textureOverridden = false;
        if (rb != null) {
            IIcon icon = rb.overrideBlockTexture;
            if (icon != null) {
                useIcon(icon);
                textureOverridden = true;
            }
        }
    }

    void selectTile(IIcon icon) {
        selectTile(icon, 16, 16);
    }

    void selectTile(IIcon icon, double width, double height) {
        if (!textureOverridden) useIcon(icon);
        us = (u1 - u0) / width;
        vs = (v1 - v0) / height;
    }

    void useIcon(IIcon icon) {
        u0 = icon.getMinU();
        v0 = icon.getMinV();
        u1 = icon.getMaxU();
        v1 = icon.getMaxV();
    }

    public static double cubeFaces[][] = { { -0.5, -0.5, 0.5, 0, 0, -1, 1, 0, 0, 0, -1, 0 }, // DOWN
            { -0.5, 0.5, -0.5, 0, 0, 1, 1, 0, 0, 0, 1, 0 }, // UP
            { 0.5, 0.5, -0.5, 0, -1, 0, -1, 0, 0, 0, 0, -1 }, // NORTH
            { -0.5, 0.5, 0.5, 0, -1, 0, 1, 0, 0, 0, 0, 1 }, // SOUTH
            { -0.5, 0.5, -0.5, 0, -1, 0, 0, 0, 1, -1, 0, 0 }, // WEST
            { 0.5, 0.5, 0.5, 0, -1, 0, 0, 0, -1, 1, 0, 0 }, // EAST
    };

    void renderCube(Trans3 t) {
        renderCubeWithFaceDepths(t, null);
    }

    void renderCubeWithFaceDepths(Trans3 t, double[] faceDepths) {
        for (int i = 0; i < 6; i++) {
            selectTileForSide(i);
            setBrightnessForSide(t, i);
            cubeFace(t, cubeFaces[i]);
            if (faceDepths != null) {
                tess.setBrightness(blockBrightness);
                cubeBackFace(t, cubeFaces[i], faceDepths[i]);
            }
        }
    }

    void selectTileForSide(int side) {
        selectTile(block.getIcon(side, metadata));
    }

    void setBrightnessForSide(Trans3 t, int side) {
        if (world != null) {
            ForgeDirection d = ForgeDirection.getOrientation(side);
            Vector3 p = t.p(d.offsetX, d.offsetY, d.offsetZ);
            // System.out.printf("BaseBlockRenderer.setBrightnessForSide: side %d is next to (%d,%d,%d)\n",
            // side, p.floorX(), p.floorY(), p.floorZ());
            tess.setBrightness(block.getMixedBrightnessForBlock(world, p.floorX(), p.floorY(), p.floorZ()));
        } else setBrightnessForBlock();
    }

    void setBrightnessForBlock() {
        tess.setBrightness(blockBrightness);
    }

    void setNormal(Trans3 t, double nx, double ny, double nz) {
        setNormal(t, nx, ny, nz, 1.0);
    }

    void setNormal(Trans3 t, double nx, double ny, double nz, double shade) {
        Vector3 n = t.v(nx, ny, nz);
        float bm = (float) (shade * (0.6 * n.x * n.x + 0.8 * n.z * n.z + (n.y > 0 ? 1 : 0.5) * n.y * n.y));
        tess.setNormal((float) n.x, (float) n.y, (float) n.z);
        tess.setColorOpaque_F(bm * cmr, bm * cmg, bm * cmb);
    }

    void cubeFace(Trans3 t, double[] c) {
        setNormal(t, c[9], c[10], c[11]);
        face(t, c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7], c[8], 0, 0, 16, 16);
    }

    void cubeBackFace(Trans3 t, double[] c, double d) {
        double nx = -c[9], ny = -c[10], nz = -c[11];
        setNormal(t, nx, ny, nz, 0.5);
        backFace(t, c[0] + d * nx, c[1] + d * ny, c[2] + d * nz, c[3], c[4], c[5], c[6], c[7], c[8], 0, 0, 16, 16);
    }

    void face(Trans3 t, double x, double y, double z, double dx1, double dy1, double dz1, double dx2, double dy2,
            double dz2, double u, double v, double du, double dv) {
        vertex(t, x, y, z, u, v);
        vertex(t, x + dx1, y + dy1, z + dz1, u, v + dv);
        vertex(t, x + dx1 + dx2, y + dy1 + dy2, z + dz1 + dz2, u + du, v + dv);
        vertex(t, x + dx2, y + dy2, z + dz2, u + du, v);
    }

    void backFace(Trans3 t, double x, double y, double z, double dx1, double dy1, double dz1, double dx2, double dy2,
            double dz2, double u, double v, double du, double dv) {
        face(t, x + dx1, y + dy1, z + dz1, -dx1, -dy1, -dz1, dx2, dy2, dz2, u, v + dv, du, -dv);
    }

    void vertex(Trans3 t, double x, double y, double z, double u, double v) {
        Vector3 p = t.p(x, y, z);
        tess.addVertexWithUV(p.x, p.y, p.z, u0 + u * us, v0 + v * vs);
    }

    void renderBox(Trans3 t, AxisAlignedBB box, IIcon[] icons) {
        renderBox(
                t,
                box.minX,
                box.minY,
                box.minZ,
                box.maxX - box.minX,
                box.maxY - box.minY,
                box.maxZ - box.minZ,
                icons);
    }

    void renderBox(Trans3 t, double x0, double y0, double z0, double dx, double dy, double dz, IIcon[] icons) {
        double x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
        tess.setBrightness(blockBrightness);
        boxFace(t, x0, y0, z1, 0, 0, -dz, dx, 0, 0, 0, -1, 0, dx, dz, icons[0]); // DOWN
        boxFace(t, x0, y1, z0, 0, 0, dz, dx, 0, 0, 0, 1, 0, dx, dz, icons[1]); // UP
        boxFace(t, x1, y1, z0, 0, -dy, 0, -dx, 0, 0, 0, 0, -1, dx, dy, icons[2]); // NORTH
        boxFace(t, x0, y1, z1, 0, -dy, 0, dx, 0, 0, 0, 0, 1, dx, dy, icons[3]); // SOUTH
        boxFace(t, x0, y1, z0, 0, -dy, 0, 0, 0, dz, -1, 0, 0, dz, dy, icons[4]); // WEST
        boxFace(t, x1, y1, z1, 0, -dy, 0, 0, 0, -dz, 1, 0, 0, dz, dy, icons[5]); // EAST
    }

    void boxFace(Trans3 t, double x, double y, double z, double dx1, double dy1, double dz1, double dx2, double dy2,
            double dz2, double nx, double ny, double nz, double du, double dv, IIcon icon) {
        if (icon != null) {
            selectTile(icon);
            setNormal(t, nx, ny, nz);
            face(t, x, y, z, dx1, dy1, dz1, dx2, dy2, dz2, 0, 0, du * 16, dv * 16);
        }
    }

}
