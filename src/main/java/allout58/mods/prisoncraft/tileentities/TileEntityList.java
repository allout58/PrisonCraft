package allout58.mods.prisoncraft.tileentities;

import cpw.mods.fml.common.registry.GameRegistry;

public class TileEntityList
{
    public static void init()
    {
        GameRegistry.registerTileEntity(TileEntityPrisonUnbreakable.class, "prisonUnbreakable");
        GameRegistry.registerTileEntity(TileEntityPrisonManager.class, "prisonManager");
        GameRegistry.registerTileEntity(TileEntityJailView.class, "jailView");
    }
}
