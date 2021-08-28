//------------------------------------------------------------------------------------------------
//
//   Project Blue - 3D Model
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import java.io.*;
import com.google.gson.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.util.*;

public class PBModel {

	public double[] bounds;
	public Face[] faces;
	
	public static class Face {
		int texture;
		double[][] vertices;
		int[][] triangles;
		Vector3 centroid;
		Vector3 normal;
	}
	
	static Gson gson = new Gson();
	
	public static PBModel fromResource(ResourceLocation location) {
		try {
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
			PBModel model = gson.fromJson(new InputStreamReader(in), PBModel.class);
			model.prepare();
			return model;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public AxisAlignedBB getBounds() {
		return AxisAlignedBB.getBoundingBox(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
	}
	
	void prepare() {
		for (Face face : faces) {
			double[][] p = face.vertices;
			int[] t = face.triangles[0];
			face.centroid = Vector3.average(p[t[0]], p[t[1]], p[t[2]]);
			face.normal = Vector3.unit(Vector3.sub(p[t[1]], p[t[0]]).cross(Vector3.sub(p[t[2]], p[t[0]])));
		}
	}

	//------------------------------------------------------------------------------------------------

	public void tessellate(Trans3 t, IIcon... icons) {
		Tessellator tess = Tessellator.instance;
		Vector3 p = null, n = null;
		double u = 0, v = 0;
		for (Face face : faces) {
			IIcon icon = icons[face.texture];
			double u0 = icon.getMinU(), v0 = icon.getMinV();
			double usize = icon.getMaxU() - u0, vsize = icon.getMaxV() - v0;
			for (int[] tri : face.triangles) {
				for (int i : tri) {
					double[] c = face.vertices[i];
					p = t.p(c[0], c[1], c[2]);
					n = t.v(c[3], c[4], c[5]);
					u = u0 + usize * c[6];
					v = v0 + vsize * c[7];
					tess.setNormal((float)n.x, (float)n.y, (float)n.z);
					tess.addVertexWithUV(p.x, p.y, p.z, u, v);
				}
				tess.setNormal((float)n.x, (float)n.y, (float)n.z);
				tess.addVertexWithUV(p.x, p.y, p.z, u, v);
			}
		}
	}
	
	public void draw(Trans3 t, IIcon... icons) {
		Vector3 p = null, n = null;
		double u = 0, v = 0;
		glBegin(GL_TRIANGLES);
		for (Face face : faces) {
			IIcon icon = icons[face.texture];
			double u0 = icon.getMinU(), v0 = icon.getMinV();
			double usize = icon.getMaxU() - u0, vsize = icon.getMaxV() - v0;
			for (int[] tri : face.triangles) {
				for (int i : tri) {
					double[] c = face.vertices[i];
					p = t.p(c[0], c[1], c[2]);
					n = t.v(c[3], c[4], c[5]);
					u = u0 + usize * c[6];
					v = v0 + vsize * c[7];
//					System.out.printf("PBModel.draw: p(%.3f,%.3f,%.3f) n(%.3f,%.3f,%.3f) uv(%.3f,%.3f)\n",
//						p.x, p.y, p.z, n.x, n.y, n.z, u, v);
					glNormal3d(n.x, n.y, n.z);
					glTexCoord2d(u, v);
					glVertex3d(p.x, p.y, p.z);
				}
			}
		}
		glEnd();
	}
	
	public void render(Trans3 t, IPBRenderer pbr, PBTexture... textures) {
		Vector3 p = null, n = null;
		PBTexture currentTexture = null;
		pbr.begin(GL_TRIANGLES);
		for (Face face : faces) {
			PBTexture texture = textures[face.texture];
			if (currentTexture != texture) {
				texture.activate(pbr);
				currentTexture = texture;
			}
			pbr.lightFace(t.p(face.centroid), t.v(face.normal));
			for (int[] tri : face.triangles) {
				for (int i = 0; i < 3; i++) {
					int j = tri[i];
					double[] c = face.vertices[j];
					p = t.p(c[0], c[1], c[2]);
					n = t.v(c[3], c[4], c[5]);
					texture.renderVertex(pbr, i, p, n, c[6], c[7]);
				}
			}
		}
		pbr.end();
	}

}
