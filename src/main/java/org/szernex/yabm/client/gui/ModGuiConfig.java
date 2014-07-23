package org.szernex.yabm.client.gui;

import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import org.szernex.yabm.handler.ConfigHandler;
import org.szernex.yabm.reference.Reference;

public class ModGuiConfig extends GuiConfig
{
	public ModGuiConfig(GuiScreen parentScreen)
	{
		super(parentScreen,
				new ConfigElement(ConfigHandler.configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
				Reference.MOD_ID,
				false,
				false,
				GuiConfig.getAbridgedConfigPath(ConfigHandler.configuration.toString()));
	}
}
