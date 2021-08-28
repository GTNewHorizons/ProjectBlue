package gcewing.projectblue;

import java.nio.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class Trans3GL {

	static double buf[] = new double[] {
		1, 0, 0, 0,
		0, 1, 0, 0,
		0, 0, 1, 0,
		0, 0, 0, 1
	};
	
	static DoubleBuffer nbuf = ByteBuffer.allocateDirect(16 * 8).order(ByteOrder.nativeOrder()).asDoubleBuffer();

	public static void glMultTrans3(Trans3 t) {
		Vector3 p = t.offset;
		double[][] m = t.rotation.m;
		double s = t.scaling;
		buf[0] = s * m[0][0]; buf[4] = s * m[0][1]; buf[8] = s * m[0][2]; buf[12] = p.x;
		buf[1] = s * m[1][0]; buf[5] = s * m[1][1]; buf[9] = s * m[1][2]; buf[13] = p.y;
		buf[2] = s * m[2][0]; buf[6] = s * m[2][1]; buf[10] = s * m[2][2]; buf[14] = p.z;
//		System.out.printf("Trans3GL.glMultTrans3:\n");
//		for (int i = 0; i < 16; i += 4)
//			System.out.printf("%8.3f %8.3f %8.3f %8.3f\n", buf[i], buf[i+1], buf[i+2], buf[i+3]);
		nbuf.clear();
		nbuf.put(buf);
		nbuf.flip();
		glMultMatrix(nbuf);
	}

}
