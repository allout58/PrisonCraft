package allout58.mods.prisoncraft.blocks;

import java.util.Random;

import allout58.mods.prisoncraft.PrisonCraft;
import allout58.mods.prisoncraft.tileentities.TileEntityPrisonUnbreakable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPrisonUnbreakable extends BlockContainer
{
    public BlockPrisonUnbreakable(int par1, Material par2Material)
    {
        super(par1, par2Material);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setUnlocalizedName("prisonUnbreakable");
    }

    @Override
    public int quantityDropped(Random rand)
    {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        // return null;
        // return new TileEntityPrisonUnbreakable();
        return new TileEntityPrisonUnbreakable();
    }

    @Override
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        int id = 100;
        TileEntity logic = world.getBlockTileEntity(x, y, z);
        if (logic instanceof TileEntityPrisonUnbreakable)
        {
            id = ((TileEntityPrisonUnbreakable) logic).getFakeBlockID();
        }
        Block fake = Block.blocksList[id];
        return fake.getBlockTexture(world, x, y, z, side);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {
        TileEntity logic = world.getBlockTileEntity(x, y, z);
        if (logic instanceof TileEntityPrisonUnbreakable)
        {
            if (((TileEntityPrisonUnbreakable) logic).canDestroy())
            {
                super.breakBlock(world, x, y, z, par5, par6);
            }
            else
            {
                int fakeID=((TileEntityPrisonUnbreakable)logic).getFakeBlockID();
                super.breakBlock(world, x, y, z, par5, par6);
                world.setBlock(x, y, z, BlockList.prisonUnbreak.blockID, 0, 3);
                TileEntity te = world.getBlockTileEntity(x, y, z);
                if(te instanceof TileEntityPrisonUnbreakable)
                {
                    ((TileEntityPrisonUnbreakable)te).setFakeBlockID(fakeID);
                }
            }
        }
    }

}
