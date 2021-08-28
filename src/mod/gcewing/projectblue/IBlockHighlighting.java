//------------------------------------------------------------------------------------------------
//
//   Project Blue - Interface for block placement feedback
//
//------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.client.event.*;

public interface IBlockHighlighting {

	public boolean renderHighlight(DrawBlockHighlightEvent e);
	
}
