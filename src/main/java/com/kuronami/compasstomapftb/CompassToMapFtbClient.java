package com.kuronami.compasstomapftb;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** 専用サーバーでは読み込まれない。config 画面の登録だけを行う。 */
@Mod(value = CompassToMapFtb.MODID, dist = Dist.CLIENT)
public class CompassToMapFtbClient {
    public CompassToMapFtbClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
