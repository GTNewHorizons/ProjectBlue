// ------------------------------------------------------------------------------------------------
//
// Project Blue - Rendering - Static renderer
//
// ------------------------------------------------------------------------------------------------

// For block rendering and static multipart rendering.
// Assumes tessellator is started in GL_QUADS mode.
// Can only use block texture icons and cannot use emissive lighting.

package gcewing.projectblue;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import net.minecraft.block.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import codechicken.multipart.*;

public class PBStaticRenderer implements IPBRenderer {

    Tessellator tess;
    int glMode; // GL_TRIANGLES or GL_QUADS
    World world;
    Block block; // Block being rendered
    int blockX, blockY, blockZ; // World coords of block being rendered
    double x0, y0, z0; // Origin of rendering coord system in world coords
    float cmr = 1, cmg = 1, cmb = 1; // Colour multiplier of block being rendered
    float red = 1, green = 1, blue = 1, alpha = 1; // Colour tint

    public PBStaticRenderer(TMultiPart part) {
        this(part.world(), null, part.x(), part.y(), part.z());
    }

    public PBStaticRenderer(World world, Block block, int blockX, int blockY, int blockZ) {
        if (block == null && world != null) block = world.getBlock(blockX, blockY, blockZ);
        this.world = world;
        this.block = block;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        tess = Tessellator.instance;
        tess.setBrightness(0xf000f0);
        tess.setColorOpaque_F(1, 1, 1);
    }

    public void setAlpha(double alpha) {
        this.alpha = (float) alpha;
    }

    public void begin(int mode) {
        glMode = mode;
    }

    public void bindTexture(ResourceLocation loc) {
        throw new RuntimeException("PBStaticRenderer cannot change texture atlas");
    }

    public void setColor(double red, double green, double blue) {
        this.red = (float) red;
        this.green = (float) green;
        this.blue = (float) blue;
    }

    public void setEmissive(boolean state) {}

    public void lightFace(Vector3 p, Vector3 n) {
        float bm;
        if (world != null) {
            int br = block.getMixedBrightnessForBlock(
                    world,
                    (int) floor(p.x + x0 + 0.01 * n.x),
                    (int) floor(p.y + y0 + 0.01 * n.y),
                    (int) floor(p.z + z0 + 0.01 * n.z));
            tess.setBrightness(br);
            bm = (float) (0.6 * n.x * n.x + 0.8 * n.z * n.z + (n.y > 0 ? 1 : 0.5) * n.y * n.y);
            // System.out.printf("PBStaticRenderer.lightFace: p=%s n=%s br=0x%x bm=%.3f\n",
            // p, n, br, bm);
        } else bm = 1.0F;
        tess.setColorRGBA_F(bm * cmr * red, bm * cmg * green, bm * cmb * blue, alpha);
    }

    public void renderVertex(int indexInFace, Vector3 p, Vector3 n, double u, double v) {
        tess.setNormal((float) n.x, (float) n.y, (float) n.z);
        tess.addVertexWithUV(p.x, p.y, p.z, u, v);
        if (indexInFace == 2 && glMode == GL_TRIANGLES) tess.addVertexWithUV(p.x, p.y, p.z, u, v);
    }

    public void end() {}

}
