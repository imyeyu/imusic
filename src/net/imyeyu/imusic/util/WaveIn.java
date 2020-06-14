package net.imyeyu.imusic.util;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.TargetDataLine;

import net.imyeyu.imusic.Entrance;

/**
 * 捕获音频输出设备波形数据
 * 
 * @author Yeyu
 *
 */
public class WaveIn {

	private InputStream is;
	private DataLine.Info dli;
	private TargetDataLine tdl;
	private AudioInputStream ais;
	
	public boolean open() {
		try {
			// 需要一个音频文件来主动打开输出设备
			String wave = Entrance.RES_PATH.substring(1) + "wave.wav";
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(wave);
			is = new BufferedInputStream(is);
			ais = AudioSystem.getAudioInputStream(is);
			AudioFormat af = ais.getFormat();
			dli = new DataLine.Info(TargetDataLine.class, af);
			tdl = (TargetDataLine) AudioSystem.getLine(dli);
			tdl.open(af, FFT.FFT_N << 1);
			// JavaFX 可能无法强制终止这个线程，当程序关闭输出设备时代表程序退出
			// System.exit(0) 强制 JVM 退出
			tdl.addLineListener(new LineListener() {
			    public void update(LineEvent event) {
			        if (event.getType() == LineEvent.Type.STOP) {
			            event.getLine().close();
			            System.exit(0);
			        }
			    }
			});
			return true;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void shutdown() {
		try {
			tdl.drain();
			tdl.stop();
			tdl.close();
			tdl = null;

			ais.close();
			ais = null;
			
			is.close();
			is = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		tdl.close();
	}

	public void start() {
		Thread.currentThread().setName("Catch Wave");
		tdl.start();
	}

	public void stop() {
		tdl.stop();
	}

	public int read(byte[] b, int len) {
		return tdl.read(b, 0, len);
	}
}