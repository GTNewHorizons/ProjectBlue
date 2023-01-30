// ------------------------------------------------------------------------------------------------
//
// Project Blue - NEI Integration
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue.nei;

import codechicken.nei.api.API;

public class NEIIntegration extends gcewing.projectblue.BaseIntegration {

    @Override
    protected void registerRecipes() {
        NEIRecipeHandler h = new NEIRecipeHandler();
        API.registerRecipeHandler(h);
        API.registerUsageHandler(h);
    }

}
