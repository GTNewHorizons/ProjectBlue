// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base - 3D Transformation
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import static java.lang.Math.*;

import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraftforge.common.util.*;

// import static gcewing.projectblue.Utils.*;

public class Trans3 {

    public static Trans3 ident = new Trans3(Vector3.zero, Matrix3.ident, 1.0);

    public Vector3 offset;
    public Matrix3 rotation;
    public double scaling;

    Trans3(Vector3 v, Matrix3 m, double s) {
        offset = v;
        rotation = m;
        scaling = s;
    }

    Trans3(double dx, double dy, double dz) {
        this(new Vector3(dx, dy, dz), Matrix3.ident, 1.0);
    }

    public Trans3 translate(double dx, double dy, double dz) {
        return new Trans3(offset.add(rotation.mul(dx * scaling, dy * scaling, dz * scaling)), rotation, scaling);
    }

    public Trans3 rotate(Matrix3 m) {
        return new Trans3(offset, rotation.mul(m), scaling);
    }

    public Trans3 scale(double s) {
        return new Trans3(offset, rotation, scaling * s);
    }

    public Trans3 side(int i) {
        return rotate(Matrix3.sideRotations[i]);
    }

    public Trans3 turn(int i) {
        return rotate(Matrix3.turnRotations[i]);
    }

    public Trans3 t(Trans3 t) {
        return new Trans3(
                offset.add(rotation.mul(t.offset).mul(scaling)),
                rotation.mul(t.rotation),
                scaling * t.scaling);
    }

    public Vector3 p(double x, double y, double z) {
        return p(new Vector3(x, y, z));
    }

    public Vector3 p(Vector3 u) {
        return offset.add(rotation.mul(u.mul(scaling)));
    }

    public Vector3 ip(double x, double y, double z) {
        return ip(new Vector3(x, y, z));
    }

    public Vector3 ip(Vector3 u) {
        return rotation.imul(u.sub(offset)).mul(1.0 / scaling);
    }

    public Vector3 v(double x, double y, double z) {
        return v(new Vector3(x, y, z));
    }

    public Vector3 iv(double x, double y, double z) {
        return iv(new Vector3(x, y, z));
    }

    public Vector3 v(Vector3 u) {
        return rotation.mul(u.mul(scaling));
    }

    public Vector3 iv(Vector3 u) {
        return rotation.imul(u).mul(1.0 / scaling);
    }

    public AxisAlignedBB t(AxisAlignedBB box) {
        return boxEnclosing(p(box.minX, box.minY, box.minZ), p(box.maxX, box.maxY, box.maxZ));
    }

    public AxisAlignedBB box(Vector3 p0, Vector3 p1) {
        return boxEnclosing(p(p0), p(p1));
    }

    public static int turnFor(Entity e, int side) {
        if (side > 1) return 0;
        int rot = (int) round(e.rotationYaw / 90);
        if (side == 0) rot = 2 - rot;
        else rot = 2 + rot;
        return rot & 0x3;
    }

    static AxisAlignedBB boxEnclosing(Vector3 p, Vector3 q) {
        return AxisAlignedBB.getBoundingBox(
                min(p.x, q.x),
                min(p.y, q.y),
                min(p.z, q.z),
                max(p.x, q.x),
                max(p.y, q.y),
                max(p.z, q.z));
    }

    // -------------------------- Forge Directions --------------------------

    public static ForgeDirection forgeDirection(double dx, double dy, double dz) {
        if (dy < -0.5) return ForgeDirection.DOWN;
        if (dy > 0.5) return ForgeDirection.UP;
        if (dx < -0.5) return ForgeDirection.WEST;
        if (dx > 0.5) return ForgeDirection.EAST;
        if (dz < -0.5) return ForgeDirection.NORTH;
        return ForgeDirection.SOUTH;
    }

    public static ForgeDirection forgeDirection(Vector3 v) {
        return forgeDirection(v.x, v.y, v.z);
    }

    public ForgeDirection t(ForgeDirection dir) {
        Vector3 d = v(dir.offsetX, dir.offsetY, dir.offsetZ);
        ForgeDirection result = forgeDirection(d);
        return result;
    }

    public ForgeDirection it(ForgeDirection dir) {
        return forgeDirection(iv(dir.offsetX, dir.offsetY, dir.offsetZ));
    }

}
