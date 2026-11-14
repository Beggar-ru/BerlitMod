package ru.beggar.berlit.mixin;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.blocks.RenderBlockCraftingCPU;
import appeng.client.texture.ExtraBlockTextures;
import ru.beggar.berlit.ae2.BlockCraftingCpuBlock;

/**
 * AE2's CPU renderer draws the connected multiblock shell from a fixed set of textures in
 * {@link ExtraBlockTextures}. Berlit's CPU parts reuse that renderer, so this hook swaps the four
 * shell textures for Berlit-coloured versions while the renderer is working on a Berlit block.
 */
@Mixin(value = RenderBlockCraftingCPU.class, remap = false)
public class MixinRenderBlockCraftingCPU {

    private static BlockCraftingUnit berlit$current;

    @Inject(
        method = "renderInWorld(Lappeng/block/crafting/BlockCraftingUnit;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/client/renderer/RenderBlocks;)Z",
        at = @At("HEAD"))
    private void berlit$enter(BlockCraftingUnit blk, IBlockAccess world, int x, int y, int z, RenderBlocks renderer,
        CallbackInfoReturnable<Boolean> callback) {
        berlit$current = blk;
    }

    @Inject(
        method = "renderInWorld(Lappeng/block/crafting/BlockCraftingUnit;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/client/renderer/RenderBlocks;)Z",
        at = @At("RETURN"))
    private void berlit$leave(BlockCraftingUnit blk, IBlockAccess world, int x, int y, int z, RenderBlocks renderer,
        CallbackInfoReturnable<Boolean> callback) {
        berlit$current = null;
    }

    @Redirect(
        method = "renderCorner",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/client/texture/ExtraBlockTextures;getIcon()Lnet/minecraft/util/IIcon;"),
        remap = false)
    private IIcon berlit$cornerIcon(ExtraBlockTextures texture) {
        BlockCraftingCpuBlock block = berlit$block();
        if (block != null && texture == ExtraBlockTextures.BlockCraftingUnitRing) return block.getRingIcon();
        return texture.getIcon();
    }

    @Redirect(
        method = "handleSide",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/client/texture/ExtraBlockTextures;getIcon()Lnet/minecraft/util/IIcon;"),
        remap = false)
    private IIcon berlit$sideIcon(ExtraBlockTextures texture) {
        BlockCraftingCpuBlock block = berlit$block();
        if (block == null) return texture.getIcon();

        if (texture == ExtraBlockTextures.BlockCraftingFitSolid) return block.getFitSolidIcon();
        if (texture == ExtraBlockTextures.BlockCraftingUnitFit) return block.getFitSolidIcon();
        if (texture == ExtraBlockTextures.BlockCraftingUnitRingLong) return block.getRingLongIcon();
        if (texture == ExtraBlockTextures.BlockCraftingUnitRingLongRotated) return block.getRingLongRotatedIcon();
        return texture.getIcon();
    }

    private static BlockCraftingCpuBlock berlit$block() {
        return berlit$current instanceof BlockCraftingCpuBlock ? (BlockCraftingCpuBlock) berlit$current : null;
    }
}
