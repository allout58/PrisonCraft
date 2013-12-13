package allout58.mods.prisoncraft.blocks;

import allout58.mods.prisoncraft.PrisonCraft;
import allout58.mods.prisoncraft.constants.TextureConstants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockPrisonManager extends Block
{
    public Icon top, bottom, side;

    public BlockPrisonManager(int par1, Material par2Material)
    {
        super(par1, par2Material);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setUnlocalizedName("prisonManager");
        setCreativeTab(PrisonCraft.creativeTab);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        if (side == 1) return this.top;
        if (side == 0) return this.bottom;
        return this.side;
    }

    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        if (side == ForgeDirection.DOWN.ordinal()) return this.bottom;
        if (side == ForgeDirection.UP.ordinal()) return this.top;
        return this.side;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister ir)
    {
        this.side = ir.registerIcon(TextureConstants.RESOURCE_CONTEXT + ":" + this.getUnlocalizedName().substring(5) + "_side");
        this.bottom = this.top = ir.registerIcon(TextureConstants.RESOURCE_CONTEXT + ":"+this.getUnlocalizedName().substring(5) + "_top_bottom");
    }
    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack par6ItemStack)
    {
        
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        super.onBlockActivated(world, x, y, z, entityPlayer, par6, par7, par8, par9);
        if (entityPlayer.isSneaking()) return false;
        else
        {
            return true;
        }
    }
}
