package gregicality.legacy.common.metatileentities.multiblock.electric;

import gregicality.legacy.common.block.GCYLRMetaBlocks;
import gregicality.legacy.common.block.blocks.BlockLargeMultiblockCasing;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

import static gregicality.legacy.api.recipe.GCYLRRecipeMaps.ADVANCED_ARC_RECIPES;
import static gregtech.client.renderer.texture.Textures.ARC_FURNACE_OVERLAY;

public class MetaTileEntityAdvancedArcFurnace extends RecipeMapMultiblockController {
    public MetaTileEntityAdvancedArcFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, ADVANCED_ARC_RECIPES);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAdvancedArcFurnace(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" AAA ", " AAA ", " EEE ", "     ")
                .aisle("AAAAA", "A#C#A", "E#C#E", " ACA ")
                .aisle("CAAAC", "C###C", "C###C", "CAAAC")
                .aisle("AAAAA", "A###A", "E###E", " AAA ")
                .aisle(" AAA ", " ASA ", " EEE ", "     ")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)).setMinGlobalLimited(28)
                        .or(autoAbilities()))
                .where('C', states(GCYLRMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.CARBON_ELECTRODE)))
                .where('D', states(MetaBlocks.BOILER_CASING.getState((BlockBoilerCasing.BoilerCasingType.STEEL_PIPE))))
                .where('E', states(MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX)))
                .where(' ', any())
                .where('#', air())
                .build();
    }
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return ARC_FURNACE_OVERLAY;
    }
}
