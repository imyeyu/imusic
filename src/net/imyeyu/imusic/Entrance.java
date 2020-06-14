package net.imyeyu.imusic;

import java.io.File;

import javafx.application.Application;
import net.imyeyu.imusic.ctrl.Main;
import net.imyeyu.util.Config;
import net.imyeyu.util.Configer;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.YeyuUtils;

public class Entrance {

	public static final String RES_PATH = "/net/imyeyu/imusic/res/";
	private static Config config;
	private static ResourceBundleX rb;

	public static void main(String[] args) {
		try {
			// 禁止 DPI 缩放
			System.setProperty("prism.allowhidpi", "false");
			
			// 依赖文件
			File file = new File("iMusic.cache");
			if (!file.exists()) YeyuUtils.file().stringToFile(file, "");
			(new File("lrcs")).mkdirs();
			
			// 配置文件
			config = new Configer(RES_PATH.substring(1) + "iMusic.ini").get();
			
			// 语言
			rb = new ResourceBundleX("lang/language", config.getString("language"));
			
			// 启动
			Application.launch(Main.class, args);
		} catch (Exception e) {
			YeyuUtils.gui().exception(e);
		}
	}
	
	public static ResourceBundleX getRb() {
		return rb;
	}
	
	public static Config getConfig() {
		return config;
	}
	
	public static void setConfig(Config config) {
		Entrance.config = config;
	}
}