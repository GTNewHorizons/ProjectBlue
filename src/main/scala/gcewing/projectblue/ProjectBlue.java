// ------------------------------------------------------------------------------------------------
//
// Project Blue - Main Class
//
// ------------------------------------------------------------------------------------------------

package gcewing.projectblue;

import static gcewing.projectblue.BaseDataChannel.*;
import static gcewing.projectblue.ControlPanelPart.ControlType.*;

import java.io.*;
import java.util.*;

import net.minecraft.block.*;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.relauncher.*;

@Mod(modid = Info.MODID, name = Info.MODNAME, version = Info.VERSION)

public class ProjectBlue extends BaseMod<ProjectBlueClient> {

    public static ProjectBlue mod;

    public static final Logger logger = LogManager.getLogger(Info.MODID);
    public static final Marker securityMarker = MarkerManager.getMarker("SuspiciousPackets");

    public static Item controlPanelItem;
    public static Item miniatureLever, miniatureButton, miniatureLamp, miniatureCover, emptySprayCan;
    public static SprayCanItem sprayCan;
    public static Item itemPartFixture;
    public static Item itemMicroPart;
    public static Item itemStoneSaw;
    public static ItemStack stackStoneSaw;
    public static ProjectBlueChannel channel;
    public static BaseDataChannel dataChannel;

    // public static Item pneumaticTube;

    public static PBModel getModel(String name) {
        return PBModel.fromResource(mod.resourceLocation("models/" + name + ".json"));
    }

    public ProjectBlue() {
        mod = this;
        // debugGui = true;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        System.out.printf("ProjectBlue.preInit\n");
        integrateWith("MineFactoryReloaded", "gcewing.projectblue.mfr.MFRIntegration");
        super.preInit(e);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        System.out.printf("ProjectBlue.init\n");
        super.init(e);
        configure();
        registerMultiParts();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        System.out.printf("ProjectBlue.postInit\n");
        super.postInit(e);
        channel = new ProjectBlueChannel("gce.projectblue");
        dataChannel = new BaseDataChannel("projectblue.data", this, client);
        itemPartFixture = findItem("ProjRed|Illumination", "projectred.illumination.fixture");
        itemMicroPart = findItem("ForgeMicroblock", "microblock");
        itemStoneSaw = findItem("ForgeMicroblock", "sawStone");
        stackStoneSaw = new ItemStack(itemStoneSaw);
    }

    Item findItem(String modID, String name) {
        Item item = GameRegistry.findItem(modID, name);
        if (item == null) throw new RuntimeException("Can't find item " + modID + ":" + name);
        return item;
    }

    @Override
    ProjectBlueClient initClient() {
        return new ProjectBlueClient(this);
    }

    void configure() {}

    @Override
    protected void registerBlocks() {}

    void registerMultiParts() {
        addMultiPart(new ControlPanelPart.Factory(), "pb_controlpanel");
        // addMultiPart(new PneumaticTubePart.Factory(), "pb_pneumatictube");
        // addFacePart(PneumaticExtractorPart.class, "pneumatic_extractor");
        // addMultiPart(new PneumaticTubePart.WireFactory(), "pb_pr_insulated");
    }

    @Override
    protected void registerItems() {
        System.out.printf("ProjectBlue.registerItems\n");
        controlPanelItem = addItem(new ControlPanelItem(), "controlPanel");
        miniatureLever = addItem(new ControlItem(LEVER), "miniatureLever");
        miniatureButton = addItem(new ControlItem(BUTTON), "miniatureButton");
        miniatureLamp = addItem(new ControlItem(LAMP), "miniatureLamp");
        miniatureCover = addItem(new ControlItem(BLANK), "miniatureCover");
        emptySprayCan = newItem("emptySprayCan");
        sprayCan = addItem(new SprayCanItem(), "sprayCan");
        // pneumaticTube = addItem(new PneumaticTubeItem(), "pneumaticTube");
    }

    @Override
    protected void registerRecipes() {
        ControlPanelRecipes.registerRecipes();
        newRecipe(emptySprayCan, 4, "b", "I", "I", 'b', Blocks.stone_button, 'I', Items.iron_ingot);
        for (int i = 0; i < 16; i++) newShapelessRecipe(
                sprayCan.newStack(1, i),
                emptySprayCan,
                Items.milk_bucket,
                Items.egg,
                new ItemStack(Items.dye, 1, 15 - i));
    }

    @Override
    protected void registerContainers() {
        // addContainer(PBGui.PneumaticExtractor, PneumaticExtractorContainer.class);
    }

    // ------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public void onServerTick(ServerTickEvent e) {
        switch (e.phase) {
            case END:
                onServerTickEnd();
                break;
        }
    }

    void onServerTickEnd() {
        // PneumaticTubePart.onServerTickEnd();
    }

    // ------------------------------------------------------------------------------------------------

    public static void addMultiPart(IPartFactory factory, String... types) {
        System.out.printf("ProjectBlue.addMultiPart: using %s:", factory);
        for (String s : types) System.out.printf(" %s", s);
        System.out.printf("\n");
        MultiPartRegistry.registerParts(factory, types);
    }

    void addFacePart(Class<? extends PBFacePart> cls, String name) {
        String type = "pb_" + name.toLowerCase();
        PBFacePart.Factory factory = new PBFacePart.Factory(cls, type);
        Item item = new PBFacePart.FaceItem(factory);
        addMultiPart(factory, type);
        addItem(item, name);
    }

    public static String getColorName(ItemStack stack) {
        return getColorName(stack.getItemDamage());
    }

    public static String getColorName(int color) {
        return StatCollector.translateToLocal(String.format("color.gcewing_projectblue:%s", color));
    }

    @SideOnly(Side.CLIENT)
    public PBTexture[] getTextures(IIconRegister reg, String... names) {
        PBTexture[] result = new PBTexture[names.length];
        for (int i = 0; i < names.length; i++) result[i] = new PBTexture(getIcon(reg, names[i]));
        return result;
    }

}
