// ------------------------------------------------------------------------------------------------
//
// Project Blue - Base class for face parts that connect to wires or cables
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;

import codechicken.lib.vec.*;
import codechicken.multipart.*;
import mrtjp.projectred.api.*;
import mrtjp.projectred.core.libmc.PRLib;
import mrtjp.projectred.transmission.IRedwireEmitter;

public abstract class PBWiredFacePart extends PBFacePart implements IConnectable, IRedstonePart {

    public int connMap;

    // @Override
    // public void writeDesc(MCDataOutput data) {
    // super.writeDesc(data);
    // data.writeInt(connMap);
    // }
    //
    // @Override
    // public void readDesc(MCDataInput data) {
    // super.readDesc(data);
    // connMap = data.readInt();
    // }

    @Override
    public void save(NBTTagCompound nbt) {
        super.save(nbt);
        nbt.setInteger("connMap", connMap);
    }

    @Override
    public void load(NBTTagCompound nbt) {
        super.load(nbt);
        connMap = nbt.getInteger("connMap");
    }

    @Override
    public void onPartChanged(TMultiPart part) {
        if (!world().isRemote) {
            updateInternalConnections();
        }
    }

    @Override
    public void onNeighborChanged() {
        if (!world().isRemote) {
            updateExternalConnections();
        }
    }

    @Override
    public void onAdded() {
        super.onAdded();
        if (!world().isRemote) {
            updateConnections();
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (!world().isRemote) notifyNeighbors(0xF);
    }

    protected void updateConnections() {
        updateInternalConnections();
        updateExternalConnections();
    }

    protected boolean updateExternalConnections() {
        int newConn = 0;
        for (int r = 0; r < 4; r++) if (connectStraight(r)) newConn |= 0x10 << r;
        else if (connectCorner(r)) newConn |= 1 << r;
        if (newConn != (connMap & 0xf000ff)) {
            int diff = connMap ^ newConn;
            connMap = connMap & ~0xf000ff | newConn;
            for (int r = 0; r < 4; r++) if ((diff & 1 << r) != 0) notifyCornerChange(r);
            return true;
        }
        return false;
    }

    protected boolean updateInternalConnections() {
        int newConn = 0;
        for (int r = 0; r < 4; r++) if (connectInternal(r)) newConn |= 0x100 << r;
        if (newConn != (connMap & 0x10f00)) {
            connMap = connMap & ~0x10f00 | newConn;
            return true;
        }
        return false;
    }

    public boolean connectCorner(int r) {
        int absDir = Rotation.rotateSide(side, r);
        BlockCoord pos = new BlockCoord(tile()).offset(absDir);
        if (!world().isAirBlock(pos.x, pos.y, pos.z)) {
            int side1 = absDir ^ 1;
            int side2 = side;
            TileMultipart t = PRLib.getMultipartTile(world(), pos);
            if (t != null) if (t.partMap(side1) != null || t.partMap(side2) != null
                    || t.partMap(PartMap.edgeBetween(side1, side2)) != null)
                return false;
        }
        pos.offset(side);
        TileMultipart t = PRLib.getMultipartTile(world(), pos);
        if (t != null) {
            TMultiPart tp = t.partMap(absDir ^ 1);
            if (tp instanceof IConnectable) {
                IConnectable conn = (IConnectable) tp;
                int r2 = Rotation.rotationTo(absDir ^ 1, side ^ 1);
                return canConnectToGlobal(conn, r) && conn.canConnectCorner(r2) && conn.connectCorner(this, r2, -1);
            }
        }
        return false;
    }

    public boolean connectStraight(int r) {
        int absDir = Rotation.rotateSide(side, r);
        BlockCoord pos = new BlockCoord(tile()).offset(absDir);
        TileMultipart t = PRLib.getMultipartTile(world(), pos);
        if (t != null) {
            TMultiPart tp = t.partMap(side);
            if (tp instanceof IConnectable) {
                IConnectable conn = (IConnectable) tp;
                return canConnectToGlobal(conn, r) && conn.connectStraight(this, (r + 2) % 4, -1);
            }
        }
        return connectStraightOverride(absDir);
    }

    public boolean connectStraightOverride(int absDir) {
        return false;
    }

    public boolean connectInternal(int r) {
        int absDir = Rotation.rotateSide(side, r);
        if (tile().partMap(PartMap.edgeBetween(absDir, side)) != null) return false;
        TMultiPart tp = tile().partMap(absDir);
        if (tp instanceof IConnectable) {
            IConnectable conn = (IConnectable) tp;
            return canConnectToGlobal(conn, r) && conn.connectInternal(this, Rotation.rotationTo(absDir, side));
        }
        return false;
    }

    public void notifyCornerChange(int r) {
        int absDir = Rotation.rotateSide(side, r);
        BlockCoord pos = new BlockCoord(tile()).offset(absDir).offset(side);
        world().notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, ((TileEntity) tile()).getBlockType());
    }

    public void notifyStraightChange(int r) {
        int absDir = Rotation.rotateSide(side, r);
        BlockCoord pos = new BlockCoord(tile()).offset(absDir);
        world().notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, ((TileEntity) tile()).getBlockType());
    }

    public void notifyNeighbors(int mask) {
        for (int r = 0; r < 4; r++) if ((mask & 1 << r) != 0) notifyCornerChange(r);
        else if ((mask & 0x10 << r) != 0) notifyStraightChange(r);
    }

    @Override
    public boolean connectCorner(IConnectable part, int r, int edgeSide) {
        if (canConnectToGlobal(part, r)) {
            connMap |= 0x1 << r;
            return true;
        }
        return false;
    }

    @Override
    public boolean connectStraight(IConnectable part, int r, int edgeSide) {
        if (canConnectToGlobal(part, r)) {
            connMap |= 0x10 << r;
            return true;
        }
        return false;
    }

    @Override
    public boolean connectInternal(IConnectable part, int r) {
        if (r < 0) return false;
        if (canConnectToGlobal(part, r)) {
            connMap |= 0x100 << r;
            return true;
        }
        return false;
    }

    @Override
    public boolean canConnectCorner(int r) {
        return false;
    }

    public boolean canConnectToGlobal(IConnectable part, int r) {
        return canConnectTo(part, localEdge(r));
    }

    public int localEdge(int r) {
        return (r + rot) & 0x3;
    }

    public int globalEdge(int edge) {
        return (edge - rot) & 0x3;
    }

    public boolean canConnectTo(IConnectable part, int edge) {
        if (part instanceof IRedwireEmitter) return canConnectToRedstone(edge);
        else return false;
    }

    @Override
    public int strongPowerLevel(int side) {
        return 0;
    }

    @Override
    public int weakPowerLevel(int side) {
        return 0;
    }

    @Override
    public boolean canConnectRedstone(int side) {
        if ((side & 6) == (this.side & 6)) return false;
        else return canConnectToRedstone(localEdgeForSide(side));
    }

    public boolean canConnectToRedstone(int edge) {
        return false;
    }

    private int localEdgeForSide(int side) {
        return localEdge(Rotation.rotationTo(this.side, side));
    }

    public int getAllRedstoneInputs() {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            int signal = getRedstoneInput(i);
            if (signal > result) result = signal;
        }
        return result;
    }

    public int getRedstoneInput(int edge) {
        int r = globalEdge(edge);
        if ((connMap & 1 << r) != 0) return calculateCornerSignal(r);
        else if ((connMap & 0x10 << r) != 0) return calculateStraightSignal(r);
        else if ((connMap & 0x100 << r) != 0) return calculateInternalSignal(r);
        else return calculateRedstoneSignal(r, requiresStrongInput(edge));
    }

    public boolean requiresStrongInput(int edge) {
        return false;
    }

    private int calculateRedstoneSignal(int r, boolean requiresStrong) {
        int absDir = Rotation.rotateSide(side, r);
        int i = RedstoneInteractions.getPowerTo(this, absDir) * 17;
        if (i > 0 || requiresStrong) return i;
        BlockCoord pos = new BlockCoord(tile()).offset(absDir);
        if (world().isBlockNormalCubeDefault(pos.x, pos.y, pos.z, false))
            return world().getBlockPowerInput(pos.x, pos.y, pos.z) * 17;
        return 0;
    }

    private int calculateCornerSignal(int r) {
        int absDir = Rotation.rotateSide(side, r);
        BlockCoord pos = new BlockCoord(tile()).offset(absDir).offset(side);
        TileMultipart t = PRLib.getMultipartTile(world(), pos);
        if (t != null) return getPartSignal(t.partMap(absDir ^ 1), Rotation.rotationTo(absDir ^ 1, side ^ 1));
        else return 0;
    }

    private int calculateStraightSignal(int r) {
        int absDir = Rotation.rotateSide(side, r);
        BlockCoord pos = new BlockCoord(tile()).offset(absDir);
        TileMultipart t = PRLib.getMultipartTile(world(), pos);
        if (t != null) return getPartSignal(t.partMap(side), (r + 2) % 4);
        else return 0;
    }

    private int calculateInternalSignal(int r) {
        // System.out.printf("PBWiredFacePart.calculateInternalSignal for global edge %s\n", r);
        int absDir = Rotation.rotateSide(side, r);
        TMultiPart tp = tile().partMap(absDir);
        int i = getPartSignal(tp, Rotation.rotationTo(absDir, side));
        if (i > 0) return i;
        if (tp instanceof IRedstonePart) {
            IRedstonePart rp = (IRedstonePart) tp;
            return Math.max(rp.strongPowerLevel(side), rp.weakPowerLevel(side)) << 4;
        }
        return 0;
    }

    private int getPartSignal(TMultiPart part, int r) {
        if (part instanceof IRedwireEmitter) return ((IRedwireEmitter) part).getRedwireSignal(r);
        else return 0;
    }

}
