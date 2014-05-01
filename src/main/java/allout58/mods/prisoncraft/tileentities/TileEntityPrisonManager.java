package allout58.mods.prisoncraft.tileentities;

import allout58.mods.prisoncraft.PrisonCraft;
import allout58.mods.prisoncraft.blocks.BlockList;
import allout58.mods.prisoncraft.config.Config;
import allout58.mods.prisoncraft.config.ConfigChangableBlocks;
import allout58.mods.prisoncraft.constants.ModConstants;
import allout58.mods.prisoncraft.jail.JailManRef;
import allout58.mods.prisoncraft.jail.JailedPersonData;
import allout58.mods.prisoncraft.jail.PrisonCraftWorldSave;
import allout58.mods.prisoncraft.network.JVSendPersonPacket;
import allout58.mods.prisoncraft.permissions.JailPermissions;
import allout58.mods.prisoncraft.permissions.PermissionLevel;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldSettings.GameType;

import java.util.ArrayList;
import java.util.List;

public class TileEntityPrisonManager extends TileEntity// implements IInventory
{
    public static final int INVENTORY_SIZE = 40;
    public static final int START_MAIN = 0;
    public static final int START_HOTBAR = 32; // ??
    public static final int START_ARMOR = 36; // ??
    public boolean hasJailedPlayer = false;
    public int dimension = 0;
    public int jailCoord1[] = new int[3];
    public int jailCoord2[] = new int[3];
    public int tpCoordIn[] = new int[3];
    public int tpCoordOut[] = new int[3];
    public String playerName;
    public String jailname;
    public String reason;
    public int secsLeftJailTime;
    private ItemStack[] playerInventory;
    private List signs = new ArrayList();
    private GameType jailedPlayerGM;
    private EntityPlayer jailedPlayer;
    private PermissionLevel jailedPlayerPrevJailPerms;
    private boolean isDirty = false;

    public TileEntityPrisonManager()
    {
        playerInventory = new ItemStack[INVENTORY_SIZE];
    }

    public boolean changeBlocks(NBTTagCompound locs)
    {
        if (jailCoord1[0] == 0 && jailCoord1[1] == 0 && jailCoord1[2] == 0)
        {
            // playerInventory[0] = new ItemStack(Block.stone);
            dimension = locs.getInteger("jailDim");
            tpCoordIn = locs.getIntArray("tpIn");
            tpCoordOut = locs.getIntArray("tpOut");
            jailCoord1 = locs.getIntArray("jailCoord1");
            jailCoord2 = locs.getIntArray("jailCoord2");
            // give xyz names
            int x1 = jailCoord1[0];
            int y1 = jailCoord1[1];
            int z1 = jailCoord1[2];
            int x2 = jailCoord2[0];
            int y2 = jailCoord2[1];
            int z2 = jailCoord2[2];
            // force ..1 to be lower than ..2
            if (x1 > x2)
            {
                x1 += x2;
                x2 = x1 - x2;
                x1 -= x2;
            }
            if (y1 > y2)
            {
                y1 += y2;
                y2 = y1 - y2;
                y1 -= y2;
            }
            if (z1 > z2)
            {
                z1 += z2;
                z2 = z1 - z2;
                z1 -= z2;
            }
            // loop through each block
            for (int i = x1; i <= x2; i++)
            {
                for (int j = y1; j <= y2; j++)
                {
                    for (int k = z1; k <= z2; k++)
                    {
                        if (worldObj.getTileEntity(i, j, k) != null) continue;
                        Block block = worldObj.getBlock(i, j, k);
                        int meta = worldObj.getBlockMetadata(i, j, k);
                        if (ConfigChangableBlocks.getInstance().isValidName(block.blockRegistry.getNameForObject(block)))
                        {
                            if (block == Blocks.iron_bars)
                            {
                                worldObj.setBlock(i, j, k, BlockList.prisonUnbreakPaneIron, 0, 3);
                            }
                            else if (block == Blocks.glass_pane)
                            {
                                worldObj.setBlock(i, j, k, BlockList.prisonUnbreakPaneGlass, 0, 3);
                            }
                            else if (block == Blocks.stained_glass_pane)
                            {
                                worldObj.setBlock(i, j, k, BlockList.prisonUnbreakPaneStained, meta, 3);
                            }
                            else if (!block.isOpaqueCube())
                            {
                                worldObj.setBlock(i, j, k, BlockList.prisonUnbreakGlass, 0, 3);
                            }
                            else
                            {
                                worldObj.setBlock(i, j, k, BlockList.prisonUnbreak, 0, 3);
                            }
                            TileEntity te = worldObj.getTileEntity(i, j, k);
                            if (te instanceof TileEntityPrisonUnbreakable)
                            {
                                ((TileEntityPrisonUnbreakable) te).setFakeBlock(block);
                                ((TileEntityPrisonUnbreakable) te).setFakeBlockMeta(meta);
                            }
                        }
                        TileEntity te = worldObj.getTileEntity(i, j, k);
                        if (te instanceof TileEntitySign)
                        {
                            int coord[] = new int[3];
                            coord[0] = ((TileEntitySign) te).xCoord;
                            coord[1] = ((TileEntitySign) te).yCoord;
                            coord[2] = ((TileEntitySign) te).zCoord;
                            signs.add(coord);
                        }
                    }
                }
            }
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 3);
            isDirty = true;
            return true;
        }
        else
        {
            isDirty = true;
            return false;
        }
    }

    public void revertBlocks()
    {
        isDirty = true;
        // give xyz names
        int x1 = jailCoord1[0];
        int y1 = jailCoord1[1];
        int z1 = jailCoord1[2];
        int x2 = jailCoord2[0];
        int y2 = jailCoord2[1];
        int z2 = jailCoord2[2];
        // force ..1 to be lower than ..2
        if (x1 > x2)
        {
            x1 += x2;
            x2 = x1 - x2;
            x1 -= x2;
        }
        if (y1 > y2)
        {
            y1 += y2;
            y2 = y1 - y2;
            y1 -= y2;
        }
        if (z1 > z2)
        {
            z1 += z2;
            z2 = z1 - z2;
            z1 -= z2;
        }
        // loop through each block
        for (int i = x1; i <= x2; i++)
        {
            for (int j = y1; j <= y2; j++)
            {
                for (int k = z1; k <= z2; k++)
                {
                    TileEntity te = worldObj.getTileEntity(i, j, k);
                    if (te instanceof TileEntityPrisonUnbreakable)
                    {
                        ((TileEntityPrisonUnbreakable) te).revert();
                    }
                }
            }
        }
    }

    public boolean setJailName(String name)
    {
        if (!(jailname != null && !jailname.isEmpty()))
        {
            jailname = name;
            this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 3);
            int coord[] = new int[3];
            coord[0] = xCoord;
            coord[1] = yCoord;
            coord[2] = zCoord;
            JailManRef ref = new JailManRef();
            ref.coord = coord;
            ref.jailName = jailname;
            PrisonCraftWorldSave.forWorld(worldObj).getTesList().add(ref);
            PrisonCraftWorldSave.forWorld(worldObj).addJailName(ref.jailName);
            isDirty = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    public void setReason(String reason)
    {
        this.reason = reason;
        isDirty = true;
        List<JailedPersonData> list = PrisonCraftWorldSave.forWorld(worldObj).people;
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).name == playerName)
            {
                list.get(i).reason = reason;
            }
        }
        PrisonCraft.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        PrisonCraft.channels.get(Side.SERVER).writeOutbound(new JVSendPersonPacket(list));
    }

    public boolean isInitialized()
    {
        return !(jailCoord1[0] == 0 && jailCoord1[1] == 0 && jailCoord1[2] == 0 && jailname != null && !jailname.isEmpty());
    }

    public boolean jailPlayer(EntityPlayer player, double time)
    {
        if (isInitialized())
        {
            if (!playerIsJailed(player.getDisplayName()))
            {
                isDirty = true;
                jailedPlayer = player;
                playerName = player.getDisplayName();

                PrisonCraftWorldSave.forWorld(worldObj).people.add(getJailedPersonData());

                // time in min.->secsLeft in sec.
                secsLeftJailTime = (int) (time * 60);
                if (Config.changeGameMode)
                {
                    if (player instanceof EntityPlayerMP)
                    {
                        jailedPlayerGM = ((EntityPlayerMP) player).theItemInWorldManager.getGameType();
                        ((EntityPlayerMP) player).setGameType(GameType.ADVENTURE);
                    }
                    else
                    {
                        PrisonCraft.logger.error("Gamemode not set. Player obj not of type EntityPlayerMP in jailing.");
                    }
                }
                hasJailedPlayer = true;
                player.mountEntity(null);
                if (player instanceof EntityPlayerMP && player.dimension != dimension)
                {
                    ((EntityPlayerMP) player).travelToDimension(dimension);
                }
                else
                {
                    PrisonCraft.logger.error("Gamemode not set. Player obj not of type EntityPlayerMP.");
                }
                player.setPositionAndUpdate(tpCoordIn[0] + .5, tpCoordIn[1], tpCoordIn[2] + .5);
                if (Config.takeInventory)
                {
                    // Store their inventory
                    for (int i = START_MAIN; i < START_HOTBAR; i++)
                    {
                        playerInventory[i] = player.inventory.mainInventory[i];
                    }
                    for (int i = START_HOTBAR; i < START_ARMOR; i++)
                    {
                        playerInventory[i] = player.inventory.mainInventory[i];
                    }
                    for (int i = START_ARMOR; i < INVENTORY_SIZE; i++)
                    {
                        playerInventory[i] = player.inventory.armorInventory[i - START_ARMOR];
                    }
                    // Clears all inventory items
                    player.inventory.clearInventory(null, -1);
                }
                if (Config.noMovement)
                {
                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 60, 300, false));
                }
                if (Config.removeJailPerms)
                {
                    jailedPlayerPrevJailPerms = JailPermissions.getInstance().getPlayerPermissionLevel(player);
                    JailPermissions.getInstance().removeUserPlayer(player);
                }
                player.addChatMessage(new ChatComponentText("[" + ModConstants.NAME + "] " + StatCollector.translateToLocal("string.jailed")));
                for (int i = 0; i < signs.size(); i++)
                {
                    int coord[] = (int[]) signs.get(i);
                    TileEntity te = worldObj.getTileEntity(coord[0], coord[1], coord[2]);
                    if (te instanceof TileEntitySign)
                    {
                        ((TileEntitySign) te).signText[0] = StatCollector.translateToLocal("string.jailedplayer");
                        ((TileEntitySign) te).signText[1] = EnumChatFormatting.ITALIC.toString() + playerName;

                        //Taken from the old Forge PacketHandler
                        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                        if (server != null)
                        {
                            server.getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 100, this.worldObj.provider.dimensionId, new S33PacketUpdateSign(te.xCoord, te.yCoord, te.zCoord, ((TileEntitySign) te).signText));
                        }
                        else
                        {
                            PrisonCraft.logger.error("Attempt to send packet to all around without a server instance available");
                        }
                    }
                }
                PrisonCraftWorldSave ws = PrisonCraftWorldSave.forWorld(MinecraftServer.getServer().worldServerForDimension(0));
                PrisonCraft.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
                PrisonCraft.channels.get(Side.SERVER).writeOutbound(new JVSendPersonPacket(ws.people));
                return true;
            }
            else return false;
        }
        else return false;
    }

    public boolean unjailPlayer()
    {
        if (jailedPlayer != null && hasJailedPlayer)
        {
            isDirty = true;

            if (Config.takeInventory)
            {
                // give their inventory
                for (int i = START_MAIN; i < START_HOTBAR; i++)
                {
                    jailedPlayer.inventory.mainInventory[i] = playerInventory[i];
                }
                for (int i = START_HOTBAR; i < START_ARMOR; i++)
                {
                    jailedPlayer.inventory.mainInventory[i] = playerInventory[i];
                }
                for (int i = START_ARMOR; i < INVENTORY_SIZE; i++)
                {
                    jailedPlayer.inventory.armorInventory[i - START_ARMOR] = playerInventory[i];
                }
                jailedPlayer.inventory.markDirty();
            }
            if (Config.noMovement)
            {
                jailedPlayer.removePotionEffect(Potion.moveSlowdown.id);
            }
            jailedPlayer.setPositionAndUpdate(tpCoordOut[0] + .5, tpCoordOut[1], tpCoordOut[2] + .5);
            if (Config.changeGameMode)
            {
                if (jailedPlayer instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) jailedPlayer).setGameType(jailedPlayerGM);
                }
                else
                {
                    PrisonCraft.logger.error("Game mode could not be reverted. Jailed Player obj in not of type EntityPlayerMP.");
                }
            }
            if (Config.removeJailPerms)
            {
                if (jailedPlayerPrevJailPerms != PermissionLevel.Default)
                {
                    JailPermissions.getInstance().addUserPlayer(jailedPlayer, jailedPlayerPrevJailPerms);
                }
            }
            jailedPlayer.addChatMessage(new ChatComponentText("[" + ModConstants.NAME + "] " + StatCollector.translateToLocal("string.unjailed")));
            for (int i = 0; i < signs.size(); i++)
            {
                int coord[] = (int[]) signs.get(i);
                TileEntity te = worldObj.getTileEntity(coord[0], coord[1], coord[2]);
                if (te instanceof TileEntitySign)
                {
                    ((TileEntitySign) te).signText[0] = "";
                    ((TileEntitySign) te).signText[1] = "";
                    ((TileEntitySign) te).signText[2] = "";
                    ((TileEntitySign) te).signText[3] = "";

                    // Taken from the old Forge PacketHandler
                    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                    if (server != null)
                    {
                        server.getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 100, this.worldObj.provider.dimensionId, new S33PacketUpdateSign(te.xCoord, te.yCoord, te.zCoord, ((TileEntitySign) te).signText));
                    }
                    else
                    {
                        PrisonCraft.logger.error("Attempt to send packet to all around without a server instance available");
                    }
                }
            }
            playerInventory = new ItemStack[INVENTORY_SIZE];
            List<JailedPersonData> l = PrisonCraftWorldSave.forWorld(worldObj).people;
            for (int i = 0; i < l.size(); i++)
            {
                if (l.get(i).name == playerName)
                {
                    PrisonCraftWorldSave.forWorld(worldObj).people.remove(i);
                }
            }
            hasJailedPlayer = false;
            jailedPlayer = null;
            playerName = "";
            reason = "";
            isDirty = true;
            PrisonCraft.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
            PrisonCraft.channels.get(Side.SERVER).writeOutbound(new JVSendPersonPacket(l));
            return true;
        }
        else return false;
    }

    @Override
    public void updateEntity()
    {
        if (isDirty)
        {
            isDirty = false;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
        if (hasJailedPlayer)
        {
            if (jailedPlayer != null && jailedPlayer.isDead)
            {
                jailedPlayer = null;
            }
            if (jailedPlayer != null)
            {
                if (Config.noMovement && worldObj.getTotalWorldTime() % 50 == 0)
                {
                    jailedPlayer.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 120, 300, false));
                }
                if (Config.noJumping && worldObj
                        .getTotalWorldTime() % 20 == 0)
                {
                    jailedPlayer.setPositionAndUpdate(jailedPlayer.posX, tpCoordIn[1], jailedPlayer.posZ);
                }
                if (!worldObj.isRemote)
                {
                    // TP back in
                    if (worldObj.getTotalWorldTime() % 200 == 0)
                    {
                        float x = (float) (jailedPlayer.posX - tpCoordIn[0]);
                        float y = (float) (jailedPlayer.posY - tpCoordIn[1]);
                        float z = (float) (jailedPlayer.posZ - tpCoordIn[2]);
                        float len = (float) Math.sqrt(x * x + y * y + z * z);
                        if (len > Config.holdingRadius || len < -Config.holdingRadius)
                        {
                            jailedPlayer.setPositionAndUpdate(tpCoordIn[0] + .5, tpCoordIn[1], tpCoordIn[2] + .5);
                        }
                    }

                    // Auto-unjail
                    if (secsLeftJailTime == -1)
                    {
                        this.unjailPlayer();
                    }
                    if (worldObj.getTotalWorldTime() % 20 == 0 && secsLeftJailTime > -1)
                    {
                        secsLeftJailTime--;
                        for (int i = 0; i < signs.size(); i++)
                        {
                            int coord[] = (int[]) signs.get(i);
                            TileEntity te = worldObj.getTileEntity(coord[0], coord[1], coord[2]);
                            if (te instanceof TileEntitySign)
                            {
                                ((TileEntitySign) te).signText[2] = "Time Left:";
                                ((TileEntitySign) te).signText[3] = String.valueOf(secsLeftJailTime);

                                // Taken from the old Forge PacketHandler
                                MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                                if (server != null)
                                {
                                    server.getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 100, this.worldObj.provider.dimensionId, new S33PacketUpdateSign(te.xCoord, te.yCoord, te.zCoord, ((TileEntitySign) te).signText));
                                }
                                else
                                {
                                    PrisonCraft.logger.error("Attempt to send packet to all around without a server instance available");
                                }
                            }
                        }
                        isDirty = true;
                    }
                }
            }
            else
            {
                if (worldObj.getTotalWorldTime() % 60 == 0)
                {
                    jailedPlayer = findPlayerFromName(playerName);
                }
            }
        }
    }

    /* Utility */

    private EntityPlayer findPlayerFromName(String uname)
    {
        if (worldObj != null)
        {
            ArrayList players = (ArrayList) worldObj.playerEntities;
            for (int i = 0; i < players.size(); i++)
            {
                if (((EntityPlayer) players.get(i)).getDisplayName().equalsIgnoreCase(uname))
                {
                    return ((EntityPlayer) players.get(i));
                }
            }
        }
        return null;
    }

    public boolean playerIsJailed(String username)
    {
        List<JailManRef> tesList = PrisonCraftWorldSave.forWorld(worldObj).getTesList();
        for (int i = 0; i < tesList.size(); i++)
        {
            int coord[] = tesList.get(i).coord;
            TileEntity te = worldObj.getTileEntity(coord[0], coord[1], coord[2]);
            if (te instanceof TileEntityPrisonManager)
            {
                if (((TileEntityPrisonManager) te).playerName != null && ((TileEntityPrisonManager) te).playerName.equalsIgnoreCase(username))
                    return true;
            }
        }
        return false;
    }

    public JailedPersonData getJailedPersonData()
    {
        JailedPersonData pd = new JailedPersonData();
        pd.name = jailedPlayer.getDisplayName();
        pd.time = secsLeftJailTime;
        pd.coord = new int[] { xCoord, yCoord, zCoord };
        pd.reason = reason;
        pd.jail = jailname;
        return pd;
    }

    /* NBT */
    @Override
    public void readFromNBT(NBTTagCompound tags)
    {
        super.readFromNBT(tags);
        hasJailedPlayer = tags.getBoolean("HasJailedPlayer");
        if (tags.hasKey("JailPlayerPreviousPerms"))
        {
            jailedPlayerPrevJailPerms = PermissionLevel.fromInt(tags.getInteger("JailPlayerPreviousPerms"));
        }
        dimension = tags.getInteger("jailDim");
        tpCoordIn = tags.getIntArray("tpCoordIn");
        tpCoordOut = tags.getIntArray("tpCoordOut");
        jailCoord1 = tags.getIntArray("jailCoord1");
        jailCoord2 = tags.getIntArray("jailCoord2");
        if (tags.hasKey("jailName"))
        {
            jailname = tags.getString("jailName");
        }
        if (tags.hasKey("reason"))
        {
            reason = tags.getString("reason");
        }
        secsLeftJailTime = tags.getInteger("secLeftJailTime");
        NBTTagCompound signTags = tags.getCompoundTag("SignTags");
        int numSize = tags.getInteger("numSigns");
        for (int i = 0; i < numSize; i++)
        {
            signs.add(signTags.getIntArray("Sign" + i));
        }
        if (tags.hasKey("gameMode"))
        {
            jailedPlayerGM = GameType.getByID(tags.getInteger("gameMode"));
        }
        playerName = tags.getString("PlayerUsername");
        jailedPlayer = findPlayerFromName(tags.getString("PlayerUsername"));
        NBTTagList tagList = tags.getTagList("Items", INVENTORY_SIZE);
        playerInventory = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < tagList.tagCount(); ++i)
        {
            NBTTagCompound tagCompound = (NBTTagCompound) tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if (slot >= 0 && slot < playerInventory.length)
            {
                playerInventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tags)
    {
        super.writeToNBT(tags);
        tags.setBoolean("HasJailedPlayer", hasJailedPlayer);
        tags.setInteger("jailDim", dimension);
        tags.setIntArray("tpCoordIn", tpCoordIn);
        tags.setIntArray("tpCoordOut", tpCoordOut);
        tags.setIntArray("jailCoord1", jailCoord1);
        tags.setIntArray("jailCoord2", jailCoord2);
        if (jailedPlayerPrevJailPerms != null)
        {
            tags.setInteger("JailPlayerPreviousPerms", jailedPlayerPrevJailPerms.getValue());
        }
        tags.setInteger("numSigns", signs.size());
        tags.setInteger("secLeftJailTime", secsLeftJailTime);
        NBTTagCompound signTags = new NBTTagCompound();
        for (int i = 0; i < signs.size(); i++)
        {
            signTags.setIntArray("Sign" + i, (int[]) signs.get(i));
        }
        tags.setTag("SignTags", signTags);
        if (jailedPlayerGM != null)
        {
            tags.setInteger("gameMode", jailedPlayerGM.getID());
        }
        if (jailname != null && !jailname.isEmpty())
        {
            tags.setString("jailName", jailname);
        }
        if (reason != null && !reason.isEmpty())
        {
            tags.setString("reason", reason);
        }
        if (hasJailedPlayer)
        {
            tags.setString("PlayerUsername", playerName);
        }
        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for (int currentIndex = 0; currentIndex < playerInventory.length; ++currentIndex)
        {
            if (playerInventory[currentIndex] != null)
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) currentIndex);
                playerInventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tags.setTag("Items", tagList);

    }

    /* Packets */
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        readFromNBT(packet.func_148857_g());
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
}
