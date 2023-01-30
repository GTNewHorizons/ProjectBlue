// ------------------------------------------------------------------------------------------------
//
// Project Blue - Rendering - Interface for vertex renderers
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.util.*;

public interface IPBRenderer {

    void begin(int mode); // GL_TRIANGLES or GL_QUADS

    void bindTexture(ResourceLocation loc);

    void setColor(double red, double green, double blue);

    void setEmissive(boolean state);

    void lightFace(Vector3 p, Vector3 n);

    void renderVertex(int indexInFace, Vector3 p, Vector3 n, double u, double v);

    void end();

}
