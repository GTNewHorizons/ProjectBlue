//------------------------------------------------------------------------------------------------
//
//   Project Blue - Rendering - Texture
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.client.renderer.texture.*;
import net.minecraft.util.*;

public class PBTexture {

	public ResourceLocation location;
	public IIcon icon;
	public double red = 1.0, green = 1.0, blue = 1.0;
	public boolean isEmissive = false;
	
//	public PBTexture(ResourceLocation location) {
//		this(location, 0.0, 0.0, 1.0, 1.0);
//	}

	public PBTexture(PBTexture base) {
		this(base.icon);
		location = base.location;
	}
	
	public PBTexture(IIcon icon) {
		this.icon = icon;
	}
	
//	public PBTexture(ResourceLocation location, double u0, double v0, double usize, double vsize) {
//		this.location = location;
//		this.u0 = u0;
//		this.v0 = v0;
//		this.usize = usize;
//		this.vsize = vsize;
//	}
	
	public PBTexture tinted(int hex) {
		return tinted(((hex >> 16) & 0xff) / 255.0, ((hex >> 8) & 0xff) / 255.0, (hex & 0xff) / 255.0);
	}

	public PBTexture tinted(double red, double green, double blue) {
		PBTexture t = new PBTexture(this);
		t.red = red; t.green = green; t.blue = blue;
		return t;
	}
	
	public PBTexture emissive(int hex) {
		return emissive(((hex >> 16) & 0xff) / 255.0, ((hex >> 8) & 0xff) / 255.0, (hex & 0xff) / 255.0);
	}

	public PBTexture emissive(double red, double green, double blue) {
		PBTexture t = tinted(red, green, blue);
		t.isEmissive = true;
		return t;
	}
	
	public void activate(IPBRenderer pbr) {
		if (location != null)
			pbr.bindTexture(location);
		pbr.setColor(red, green, blue);
		pbr.setEmissive(isEmissive);
	}
	
	public void renderVertex(IPBRenderer pbr, int i, Vector3 p, Vector3 n, double u, double v) {
		pbr.renderVertex(i, p, n, interpolateU(u), interpolateV(v));
	}
	
	double interpolateU(double u) {
		double u0 = icon.getMinU(), u1 = icon.getMaxU();
		return u0 + u * (u1 - u0);
	}
	
	double interpolateV(double v) {
		double v0 = icon.getMinV(), v1 = icon.getMaxV();
		return v0 + v * (v1 - v0);
	}
	
}

