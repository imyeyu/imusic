package net.imyeyu.imusic.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import net.imyeyu.imusic.bean.InternetCtrl;
import net.imyeyu.util.YeyuUtils;

/**
 * 远程控制核心
 * 
 * @author Yeyu
 *
 */
public class InternetCtrler extends Service<InternetCtrl> {

	private int port;
	private String ip, playNow = "NULL", playNext = "NULL";
	private Socket socket;
	private boolean isShutdown = false;
	private InputStream is;
	private OutputStream os;
	private BufferedReader br;
	private SimpleIntegerProperty icPing;
	
	public InternetCtrler(SimpleIntegerProperty icPing, String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.icPing = icPing;
	}
	
	protected Task<InternetCtrl> createTask() {
		return new Task<InternetCtrl>() {
			protected InternetCtrl call() throws Exception {
				Thread.currentThread().setName("Internet Controller");
				
				socket = new Socket(ip, port);
				icPing.set(0);
				long l = -1; // 延时更新间隔
				String action = "NULL";
				while (!isShutdown) {
					// 发送当前播放和下一首播放
					os = socket.getOutputStream();
					os.write((playNow + "#" + playNext + "\r\n").getBytes("UTF-8"));
					// 接收控制信息
					is = socket.getInputStream();
					br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					action = br.readLine();
					if (action.equals("SHUTDOWN")) break;
					updateValue(InternetCtrl.fromTypeName(action));
					if (!action.equals("NULL")) {
						Thread.sleep(2000);
						updateValue(InternetCtrl.ACTION);
					}
					
					if (l % 8 == 0) icPing.set(YeyuUtils.network().pingHostByCMD(ip)); // 每 4 秒更新一次延时
					l++;
					Thread.sleep(500);
				}
				icPing.set(-1);
				if (action != null && !action.equals("SHUTDOWN")) {
					os = socket.getOutputStream();
					os.write("SHUTDOWN\r\n".getBytes("UTF-8"));
					os.close();
				}
				socket.close();
				return null;
			}
		};
	}
	
	/**
	 * 发送当前播放歌曲和下一首到远程控制中转接口
	 * 
	 * @param playNow  当前播放
	 * @param playNext 下一首播放
	 * @param type     播放模式
	 */
	public void setState(String playNow, String playNext, int type) {
		this.playNow = playNow;
		this.playNext = type == 1 ? playNow : playNext;
	}
	
	public String getIP() {
		return ip;
	}
	
	public boolean isConnect() {
		return icPing.get() != -1;
	}
	
	public void shutdown() {
		this.isShutdown = true;
	}
	
	public void shutdown(boolean shutdown) {
		this.isShutdown = shutdown;
	}
}