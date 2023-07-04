// ------------------------------------------------------------------------------------------------
//
// Project Blue - Rednet to Bundled Cable Adaptor Tile Entity
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue.mfr;

import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;

import gcewing.projectblue.*;
import mrtjp.projectred.api.*;

public class RednetAdaptorTE extends BaseTileEntity implements IBundledTile, IBundledEmitter {

    public final static int DIRECTION_BIT = 1;
    public final static int CONNECTED_BIT = 2;

    public final static int BUNDLED_TO_REDNET = 0;
    public final static int REDNET_TO_BUNDLED = 1;

    byte[] bundledInput = new byte[16];
    byte[] bundledOutput = new byte[16];
    int[] rednetInput = new int[16];
    int[] rednetOutput = new int[16];
    int[] signalConfig = new int[16];

    {
        for (int i = 0; i < 8; i++) {
            signalConfig[i] = CONNECTED_BIT | BUNDLED_TO_REDNET;
            signalConfig[8 + i] = CONNECTED_BIT | REDNET_TO_BUNDLED;
        }
    }

    @Override
    public void readContentsFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("bundledInput")) {
            bundledInput = nbt.getByteArray("bundledInput");
            bundledOutput = nbt.getByteArray("bundledOutput");
            rednetInput = nbt.getIntArray("rednetInput");
            rednetOutput = nbt.getIntArray("rednetOutput");
            signalConfig = nbt.getIntArray("signalConfig");
        }
    }

    @Override
    public void writeContentsToNBT(NBTTagCompound nbt) {
        nbt.setByteArray("bundledInput", bundledInput);
        nbt.setByteArray("bundledOutput", bundledOutput);
        nbt.setIntArray("rednetInput", rednetInput);
        nbt.setIntArray("rednetOutput", rednetOutput);
        nbt.setIntArray("signalConfig", signalConfig);
    }

    public void toggleSignalDirection(int i) {
        int s = signalConfig[i];
        if ((s & CONNECTED_BIT) == 0) setSignalConfig(i, s | CONNECTED_BIT);
        else setSignalConfig(i, s ^ DIRECTION_BIT);
    }

    public void toggleSignalConnected(int i) {
        setSignalConfig(i, signalConfig[i] ^ CONNECTED_BIT);
    }

    public void setSignalConfig(int i, int state) {
        signalConfig[i] = state;
        markDirty();
        markBlockForUpdate();
    }

    public void onNeighborBlockChange() {
        // System.out.printf("RednetAdaptorTE.onNeighborBlockChange:\n");
        // for (int i = 0; i < 16; i++)
        // bundledInput[i] = 0;
        byte[] newInput = new byte[16];
        boolean changed = false;
        for (int side = 0; side < 6; side++) {
            byte[] signal = ProjectRedAPI.instance.transmissionAPI
                    .getBundledInput(worldObj, xCoord, yCoord, zCoord, side);
            if (signal != null) {
                // System.out.printf("Side %s:", side);
                // dumpSignal(signal);
                for (int i = 0; i < 16; i++) {
                    byte b = signal[i];
                    if ((b & 0xff) > (newInput[i] & 0xff)) newInput[i] = b;
                }
            }
        }
        // System.out.printf("Merged:");
        // dumpSignal(newInput);
        for (int i = 0; i < 16; i++) {
            if (bundledInput[i] != newInput[i]) {
                bundledInput[i] = newInput[i];
                changed = true;
            }
        }
        if (changed) transferSignals();
        notifyNeighbours();
    }

    public void onRednetInputsChanged(int[] input) {
        for (int i = 0; i < 16; i++) rednetInput[i] = input[i];
        transferSignals();
        notifyNeighbours();
    }

    void transferSignals() {
        for (int i = 0; i < 16; i++) {
            switch (signalConfig[i]) {
                case CONNECTED_BIT | BUNDLED_TO_REDNET:
                    rednetOutput[i] = bundledInput[i] != 0 ? 15 : 0;
                    break;
                case CONNECTED_BIT | REDNET_TO_BUNDLED:
                    bundledOutput[i] = (byte) (rednetInput[i] != 0 ? 255 : 0);
                    break;
                default:
                    rednetOutput[i] = 0;
                    bundledOutput[i] = 0;
            }
        }
        markDirty();
    }

    void notifyNeighbours() {
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, MFRIntegration.rednetAdaptorBlock);
    }

    void dumpSignal(byte[] signal) {
        for (int i : signal) System.out.printf(" %02x", i & 0xff);
        System.out.printf("\n");
    }

    void dumpSignal(int[] signal) {
        for (int i : signal) System.out.printf(" %02x", i);
        System.out.printf("\n");
    }

    public int[] getRednetOutput() {
        return rednetOutput;
    }

    //
    // Bundled Cable
    //

    public boolean canConnectBundled(int side) {
        return true;
    }

    public byte[] getBundledSignal(int dir) {
        return bundledOutput;
    }

}
