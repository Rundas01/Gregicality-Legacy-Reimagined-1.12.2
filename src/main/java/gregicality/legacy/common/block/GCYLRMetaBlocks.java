package gregicality.legacy.common.block;

import gregicality.legacy.common.block.blocks.BlockAdvancedFusionCoil;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import gregtech.common.blocks.MetaBlocks;

import gregicality.legacy.common.block.blocks.BlockLargeMultiblockCasing;
import gregicality.legacy.common.block.blocks.BlockUniqueCasing;

public final class GCYLRMetaBlocks {

    private GCYLRMetaBlocks() {}

    public static BlockUniqueCasing UNIQUE_CASING;
    public static BlockLargeMultiblockCasing LARGE_MULTIBLOCK_CASING;
    public static BlockAdvancedFusionCoil ADVANCED_FUSION_COIL;

    public static void init() {
        UNIQUE_CASING = new BlockUniqueCasing();
        UNIQUE_CASING.setRegistryName("unique_casing");
        ADVANCED_FUSION_COIL = new BlockAdvancedFusionCoil();
        ADVANCED_FUSION_COIL.setRegistryName("advanced_fusion_coil");
        LARGE_MULTIBLOCK_CASING = new BlockLargeMultiblockCasing();
        LARGE_MULTIBLOCK_CASING.setRegistryName("large_multiblock_casing");
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        UNIQUE_CASING.onModelRegister();
        registerItemModel(LARGE_MULTIBLOCK_CASING);
        registerItemModel(ADVANCED_FUSION_COIL);
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(@NotNull Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            // noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            MetaBlocks.statePropertiesToString(state.getProperties())));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> @NotNull String getPropertyName(@NotNull IProperty<T> property,
                                                                             Comparable<?> value) {
        return property.getName((T) value);
    }
}
