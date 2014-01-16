package allout58.mods.prisoncraft.blocks;

import allout58.mods.prisoncraft.PrisonCraft;
import allout58.mods.prisoncraft.PrisonCraftWorldSave;
import allout58.mods.prisoncraft.constants.ModConstants;
import allout58.mods.prisoncraft.constants.TextureConstants;
import allout58.mods.prisoncraft.items.ItemList;
import allout58.mods.prisoncraft.tileentities.TileEntityPrisonManager;
import allout58.mods.prisoncraft.tileentities.TileEntityPrisonUnbreakable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockPrisonManager extends BlockContainer
{
    public Icon top, bottom, side, side_uninit;

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
        int meta = world.getBlockMetadata(x, y, z);
        if (side == ForgeDirection.DOWN.ordinal()) return this.bottom;
        if (side == ForgeDirection.UP.ordinal()) return this.top;
        if (meta == 0)
        {
            return this.side_uninit;
        }
        else
        {
            return this.side;
        }

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister ir)
    {
        this.side = ir.registerIcon(TextureConstants.RESOURCE_CONTEXT + ":" + this.getUnlocalizedName().substring(5) + "_side");
        this.side_uninit = ir.registerIcon(TextureConstants.RESOURCE_CONTEXT + ":" + this.getUnlocalizedName().substring(5) + "_side_uninit");
        this.bottom = this.top = ir.registerIcon(TextureConstants.RESOURCE_CONTEXT + ":" + this.getUnlocalizedName().substring(5) + "_top_bottom");
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack par6ItemStack)
    {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof TileEntityPrisonManager)
        {
            int coord[] = new int[3];
            coord[0] = te.xCoord;
            coord[1] = te.yCoord;
            coord[2] = te.zCoord;
            PrisonCraftWorldSave.forWorld(world).getTesList().add(coord);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityPrisonManager();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {
        TileEntity logic = world.getBlockTileEntity(x, y, z);
        if (logic instanceof TileEntityPrisonManager)
        {
            PrisonCraftWorldSave ws = PrisonCraftWorldSave.forWorld(world);
            for (int i = 0; i < ws.getTesList().size(); i++)
            {
                int coord[] = (int[]) ws.getTesList().get(i);
                if (coord[0] == logic.xCoord && coord[1] == logic.yCoord && coord[2] == logic.zCoord)
                {
                    PrisonCraftWorldSave.forWorld(world).getTesList().remove(i);
                }
            }
            ((TileEntityPrisonManager) logic).revertBlocks();
            ((TileEntityPrisonManager) logic).unjailPlayer();
        }
        super.breakBlock(world, x, y, z, par5, par6);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        super.onBlockActivated(world, x, y, z, entityPlayer, par6, par7, par8, par9);
        if (!world.isRemote)
        {
            TileEntity te = world.getBlockTileEntity(x, y, z);
            if (te instanceof TileEntityPrisonManager)
            {
                if (entityPlayer.inventory.getCurrentItem() != null)
                {
                    if (entityPlayer.inventory.getCurrentItem().itemID == ItemList.configWand.itemID)
                    {
                        if (entityPlayer.inventory.getCurrentItem().stackTagCompound != null)
                        {
                            NBTTagCompound tags = entityPlayer.inventory.getCurrentItem().stackTagCompound;
                            if (tags.hasKey("jailCoord1") && tags.hasKey("jailCoord2") && tags.hasKey("tpIn") && tags.hasKey("tpOut"))
                            {
                                if (((TileEntityPrisonManager) te).changeBlocks(entityPlayer.inventory.getCurrentItem().stackTagCompound))
                                {
                                    entityPlayer.sendChatToPlayer(new ChatMessageComponent().addText("[" + ModConstants.NAME + "] " + EnumChatFormatting.RED.toString() + EnumChatFormatting.ITALIC.toString()).addKey("string.blockprisonmanager.cell.success"));
                                }
                                else
                                {
                                    entityPlayer.sendChatToPlayer(new ChatMessageComponent().addText("[" + ModConstants.NAME + "] " + EnumChatFormatting.RED.toString() + EnumChatFormatting.ITALIC.toString()).addKey("string.blockprisonmanager.cell.failoverwrite"));
                                }
                                return true;
                            }
                            else
                            {
                                entityPlayer.sendChatToPlayer(new ChatMessageComponent().addText("[" + ModConstants.NAME + "] " + EnumChatFormatting.RED.toString() + EnumChatFormatting.ITALIC.toString()).addKey("string.blockprisonmanager.cell.fail"));
                                return true;
                            }
                        }
                    }
                }
            }
        }
        if (entityPlayer.isSneaking()) return false;
        else
        {
            TileEntity te = world.getBlockTileEntity(x, y, z);
            if (te instanceof TileEntityPrisonManager && !te.worldObj.isRemote)
            {
                ChatMessageComponent chat = new ChatMessageComponent();
                chat.addKey("string.playerInJail");
                if (((TileEntityPrisonManager) te).hasJailedPlayer) chat.addText(((TileEntityPrisonManager) te).playerName);
                else chat.addKey("string.noOne");
                entityPlayer.sendChatToPlayer(chat);
            }
            return true;
        }
    }
}