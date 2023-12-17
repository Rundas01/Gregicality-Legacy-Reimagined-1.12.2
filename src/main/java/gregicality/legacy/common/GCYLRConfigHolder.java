package gregicality.legacy.common;

import gregicality.legacy.GregicalityLegacyReimagined;
import gregtech.api.GTValues;
import net.minecraftforge.common.config.Config;

@Config(modid = GregicalityLegacyReimagined.MODID, name = GregicalityLegacyReimagined.NAME + '/' + GTValues.MODID)
public class GCYLRConfigHolder {
    @Config.Comment("Config options for GCYLR Machines")
    @Config.Name("GCYLR Machine Options")
    @Config.RequiresMcRestart
    public static GCYLRMachineOptions machines = new GCYLRMachineOptions();

    public static class GCYLRMachineOptions {
        @Config.Comment({"Up to how many elements a fission fuel can split into.", "Default: 2"})
        public int fuelSplitAmount = 2;

        @Config.Comment({"How many different fission processes a material can undergo.", "Default: 1"})
        public int fuelSplitOffset = 1;

        @Config.Comment({"Whether depleted fuel pellets are allowed to contain liquids and gases.", "Default: false"})
        public boolean allowLiquidsInReprocessing = false;
    }
}
