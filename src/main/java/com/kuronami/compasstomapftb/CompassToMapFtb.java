package com.kuronami.compasstomapftb;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Compass to Map: FTB Chunks.
 *
 * <p>Nature's Compass / Explorer's Compass の検索成功地点を FTB Chunks の waypoint に登録する。
 * 登録も検出もクライアント側だけで完結するので、この本体クラスは config を登録するだけで何もしない
 * （ブロック・アイテム・レシピ・ネットワークのいずれも持たない）。
 */
@Mod(CompassToMapFtb.MODID)
public class CompassToMapFtb {
    public static final String MODID = "compasstomapftb";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompassToMapFtb(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
