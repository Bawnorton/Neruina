package com.bawnorton.neruina;

import com.bawnorton.neruina.blacklist.BlacklistHandler;
import com.bawnorton.neruina.handler.MessageHandler;
import com.bawnorton.neruina.handler.PersitanceHandler;
import com.bawnorton.neruina.handler.TickHandler;
import com.bawnorton.neruina.report.AutoReportHandler;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neruina {
	public static final String MOD_ID = "neruina";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Neruina INSTANCE = new Neruina();

	private final TickHandler tickHandler;
	private final MessageHandler messageHandler;
	private final AutoReportHandler autoReportHandler;
	private final BlacklistHandler blacklistHandler;

	public Neruina() {
		this.tickHandler = new TickHandler();
		this.messageHandler = new MessageHandler();
		this.autoReportHandler = new AutoReportHandler();
		this.blacklistHandler = new BlacklistHandler();
	}

	public static void init() {
	}

	public static Neruina getInstance() {
		return INSTANCE;
	}

	public TickHandler getTickHandler() {
		return tickHandler;
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public AutoReportHandler getAutoReportHandler() {
		return autoReportHandler;
	}

	public BlacklistHandler getBlacklistHandler() {
		return blacklistHandler;
	}

	public void updateServerState(MinecraftServer server) {
		PersitanceHandler.updateServerState(server);
	}
}
