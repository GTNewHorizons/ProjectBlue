//------------------------------------------------------------------------------------------------
//
//   Project Blue - Rendering - Dynamic renderer
//
//------------------------------------------------------------------------------------------------

//  Does not use tessellator. All rendering features available.

package gcewing.projectblue;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;

public class PBDynamicRenderer implements IPBRenderer {

	ResourceLocation currentTexture = null;
	boolean emissiveMode = false;
	boolean inventoryMode = false;
	
	public void begin(int mode) {
		glBegin(mode);
	}

	public void bindTexture(ResourceLocation loc) {
		if (currentTexture != loc) {
			BaseModClient.bindTexture(loc);
			currentTexture = loc;
		}
	}
	
	public void setColor(double red, double green, double blue) {
		glColor3d(red, green, blue);
	}

	public void setEmissive(boolean state) {
		if (emissiveMode != state) {
			glSetDisabled(GL_LIGHTING, state);
			if (!inventoryMode) {
				OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
				glSetDisabled(GL_TEXTURE_2D, state);
				OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
			}
			emissiveMode = state;
		}
	}
	
	void glSetDisabled(int mode, boolean state) {
		if (state)
			glDisable(mode);
		else
			glEnable(mode);
	}
	
	public void lightFace(Vector3 p, Vector3 n) {
	}

	public void renderVertex(int indexInFace, Vector3 p, Vector3 n, double u, double v) {
		//System.out.printf("PBDynamicRenderer.renderVertex: %d p=%s n=%s u=%.6f v=%.6f\n",
		//	indexInFace, p, n, u, v);
		glNormal3d(n.x, n.y, n.z);
		glTexCoord2d(u, v);
		glVertex3d(p.x, p.y, p.z);
	}
	
	public void end() {
		glEnd();
	}

}
