package allout58.mods.prisoncraft.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.SaveHandler;

public class JailPermissions
{
    private static JailPermissions instance;
    private List canUse = new ArrayList();

    public static JailPermissions getInstance()
    {
        if (instance == null)
        {
            instance = new JailPermissions();
        }
        return instance;
    }

    public boolean playerCanUse(ICommandSender sender)
    {
        for (int i = 0; i < canUse.size(); i++)
        {
            if (((String) canUse.get(i)).equalsIgnoreCase(sender.getCommandSenderName()))
            {
                return true;
            }
        }
        if (sender.getCommandSenderName().equalsIgnoreCase("Server")) return true;
        return false;
    }

    public boolean playerCanUse(String senderName)
    {
        for (int i = 0; i < canUse.size(); i++)
        {
            if (((String) canUse.get(i)).equalsIgnoreCase(senderName))
            {
                return true;
            }
        }
        return false;
    }

    public boolean addUserPlayer(ICommandSender player)
    {
        if (!playerCanUse(player))
        {
            canUse.add(player.getCommandSenderName());
            this.save();
            return true;
        }
        return false;
    }

    public boolean addUserPlayer(String playerName)
    {
        if (!playerCanUse(playerName))
        {
            canUse.add(playerName);
            this.save();
            return true;
        }
        return false;
    }

    public boolean removeUserPlayer(ICommandSender player)
    {
        if (playerCanUse(player))
        {
            canUse.remove(player.getCommandSenderName());
            this.save();
            return true;
        }
        return false;
    }

    public boolean removeUserPlayer(String playerName)
    {
        if (playerCanUse(playerName))
        {
            canUse.remove(playerName);
            this.save();
            return true;
        }
        return false;
    }

    public void save()
    {
        MinecraftServer server = null;
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.SERVER)
        {
            // We are on the server side.
            server = MinecraftServer.getServer();
        }
        else if (side == Side.CLIENT)
        {
            // We are on the client side.
//            server = Minecraft.getMinecraft().getIntegratedServer();
        }
        else
        {
            // We have an errornous state!
        }
        if (server != null)
        {
            SaveHandler saveHandler = (SaveHandler) server.worldServerForDimension(0).getSaveHandler();
            String fileName = saveHandler.getWorldDirectory().getAbsolutePath() + "/PrisonCraftPerms.txt";
            File f=new File(fileName);
            if(!f.exists())
            {
                try
                {
                    f.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            FileWriter output = null;
            try
            {
                output = new FileWriter(fileName);
                BufferedWriter writer = new BufferedWriter(output);
                for (int i = 0; i < canUse.size(); i++)
                {
                    writer.write(((String) canUse.get(i))+"\n");
                }
                writer.close();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                if (output != null)
                {
                    try
                    {
                        output.close();
                    }
                    catch (IOException e)
                    {
                        // Ignore issues during closing
                    }
                }
            }

        }
    }

    public void load()
    {
        MinecraftServer server = null;
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.SERVER)
        {
            // We are on the server side.
            server = MinecraftServer.getServer();
        }
        else if (side == Side.CLIENT)
        {
            // We are on the client side.
        }
        else
        {
            // We have an errornous state!
        }
        if (server != null)
        {
            SaveHandler saveHandler = (SaveHandler) server.worldServerForDimension(0).getSaveHandler();
            String fileName = saveHandler.getWorldDirectory().getAbsolutePath() + "/PrisonCraftPerms.txt";
            File f=new File(fileName);
            if(!f.exists())
            {
                try
                {
                    f.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            FileReader file = null;
            try
            {
                file = new FileReader(fileName);
                BufferedReader reader = new BufferedReader(file);
                String line = "";
                while ((line = reader.readLine()) != null)
                {
                    addUserPlayer(line);
                }
                reader.close();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                if (file != null)
                {
                    try
                    {
                        file.close();
                    }
                    catch (IOException e)
                    {
                        // Ignore issues during closing
                    }
                }
            }
        }
    }
    
    public void clear()
    {
        canUse.clear();
    }
}
