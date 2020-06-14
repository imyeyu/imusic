package net.imyeyu.imusic.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import net.imyeyu.util.YeyuUtils;

/**
 * 播放列表缓存加载器
 * 
 * @author Yeyu
 *
 */
public class CacheLoader extends Service<List<File>> {

	/**
	 * 加载播放列表
	 * 
	 */
	protected Task<List<File>> createTask() {
		return new Task<List<File>>() {
			protected List<File> call() throws Exception {
				Thread.currentThread().setName("Cache Loader");
				
				List<File> list = new ArrayList<>();
				String data = YeyuUtils.file().fileToString(new File("iMusic.cache"), "UTF-8");
				if (!data.trim().equals("")) {
					String[] datas = data.split("\r\n|[\r\n]");
					for (int i = 0; i < datas.length; i++) {
						list.add(new File(datas[i]));
					}
				}
				return list;
			}
		};
	}
	
	/**
	 * 缓存播放列表
	 * 
	 */
	public void setCacheList(List<File> list) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i).getAbsolutePath() + "\r\n");
		}
		YeyuUtils.file().stringToFile(new File("iMusic.cache"), sb.toString());
	}
}