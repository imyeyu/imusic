package net.imyeyu.imusic.bean;

import net.imyeyu.imusic.Entrance;
import net.imyeyu.util.ResourceBundleX;

/**
 * 远程控制
 * 
 * @author Yeyu
 *
 */
public enum InternetCtrl {

	PLAY_PREV("PLAY_PREV"),     // 上一首
	PLAY_TOGGLE("PLAY_TOGGLE"), // 切换播放状态
	PLAY_NEXT("PLAY_NEXT"),     // 下一首
	RANDOM("RANDOM"),           // 随机列表
	/**
	 * 用于还原远程控制图标样式，在触发远程控制 2 秒后执行，返回此枚举类型到视图层，不再另开线程
	 * 
	 */
	ACTION("ACTION");

	private String typeName;
	
	private InternetCtrl(String typeName) {
        this.typeName = typeName;
    }
	
	public static InternetCtrl fromTypeName(String typeName) {
        for (InternetCtrl type : InternetCtrl.values()) {
            if (type.getTypeName().equals(typeName)) {
                return type;
            }
        }
        return null;
    }
	
	public static String toString(InternetCtrl internetCtrl) {
		ResourceBundleX rb = Entrance.getRb();
		switch (internetCtrl) {
			case PLAY_PREV:   return rb.def("playPrev");
			case PLAY_TOGGLE: return rb.def("togglePlayState");
			case PLAY_NEXT:   return rb.def("playNext");
			case RANDOM:      return rb.def("random");
			default:          return "";
		}
	}
	
	public String getTypeName() {
        return this.typeName;
    }
}
