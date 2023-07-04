// ------------------------------------------------------------------------------------------------
//
// Project Blue - Rednet to Bundled Cable Adaptor GUI Container
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue.mfr;

import static gcewing.projectblue.BaseDataChannel.*;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.world.*;

import gcewing.projectblue.*;

public class RednetAdaptorContainer extends Container {

    RednetAdaptorTE te;

    public RednetAdaptorContainer(EntityPlayer player, World world, int x, int y, int z) {
        te = (RednetAdaptorTE) world.getTileEntity(x, y, z);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @ServerMessageHandler("toggleConfig")
    public void onToggleConfig(EntityPlayer player, ChannelInput data) {
        // System.out.printf("RednetAdaptorContainer.onToggleConfig\n");
        int i = data.readInt();
        int button = data.readInt();
        switch (button) {
            case 0:
                te.toggleSignalDirection(i);
                break;
            case 1:
                te.toggleSignalConnected(i);
                break;
        }
    }

}
