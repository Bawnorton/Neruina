package com.bawnorton.neruina;

import com.bawnorton.neruina.config.ConfigManager;
import com.bawnorton.neruina.handler.PersitanceHandler;
import com.bawnorton.neruina.report.AutoReportHandler;
import com.bawnorton.neruina.handler.MessageHandler;
import com.bawnorton.neruina.handler.NeruinaTickHandler;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Function;

public class Neruina {
    public static final String MOD_ID = "neruina";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final NeruinaTickHandler TICK_HANDLER = new NeruinaTickHandler();
    public static final MessageHandler MESSAGE_HANDLER = new MessageHandler();
    public static final AutoReportHandler AUTO_REPORT_HANDLER = new AutoReportHandler();
    public static final Function<MinecraftServer, PersitanceHandler> PERSISTANCE_HANDLER = PersitanceHandler::getServerState;

    public static void init() {
        ConfigManager.loadConfig();
    }
}