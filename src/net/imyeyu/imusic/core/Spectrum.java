package net.imyeyu.imusic.core;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.SpecPreset;
import net.imyeyu.imusic.util.FFT;
import net.imyeyu.imusic.util.SpecPresets;
import net.imyeyu.imusic.util.WaveIn;
import net.imyeyu.util.Config;

/**
 * 频谱绘制核心
 * 
 * @author Yeyu
 *
 */
public class Spectrum extends JComponent implements Runnable {
	
	private Config config = Entrance.getConfig();

	private static final int Y0 = 1 << ((FFT.FFT_N_LOG + 3) << 1);
	private static final int maxColums = 128;
	private static final long serialVersionUID = 1L;
	private static final double logY0 = Math.log10(Y0);

	// band 频谱数量，width 容器宽度，height 容器高度，speed 频谱速度，specing 频谱间距
	private int band, width = 280, height = 160, deltax, speed, spacing;
	private int[] xplot, lastPeak, lastY;
	private long lastTimeMillis;
	private float boost;
	
	// bgColor 背景颜色，peakColor 顶块颜色
	private Color bgColor, peakColor;
	// 频谱渐变色
	private Color specTop, specMiddle, specBottom;
	
	private Graphics g;
	private BufferedImage specImg, barImg;
	
	private boolean isShutdown = false;

	public Spectrum() {
		boost = config.getInt("specBoost") / 10f;
		spacing = config.getInt("specSpacing");
		speed = config.getInt("specSpeed");
		band = config.getInt("specBand");
		
		SpecPreset preset = SpecPresets.get()[config.getInt("specID")];
		bgColor = new Color(SpecPresets.toHexColor(preset.getBg()));
		specTop = new Color(SpecPresets.toHexColor(preset.getSpecTop()));
		specMiddle = new Color(SpecPresets.toHexColor(preset.getSpecMiddle()));
		specBottom = new Color(SpecPresets.toHexColor(preset.getSpecBottom()));
		peakColor = new Color(SpecPresets.toHexColor(preset.getPeak()));
		
		lastTimeMillis = System.currentTimeMillis();
		setDoubleBuffered(true);
		xplot = new int[maxColums + 1];
		lastY = new int[maxColums];
		lastPeak = new int[maxColums];
		
		initGraphics();
		
		(new Thread(this)).start();
	}

	public void setColor() {
		// 绘制背景
		g.setColor(bgColor);
		g.fillRect(0, 0, width, height);
		Graphics2D g = (Graphics2D) barImg.getGraphics();
		GradientPaint gp1 = new GradientPaint(0, 0, specTop, deltax - 1, height / 2, specMiddle);
		g.setPaint(gp1);
		g.fillRect(0, 0, deltax - 1, height / 2);
		GradientPaint gp2 = new GradientPaint(0, height / 2, specMiddle, deltax - 1, height, specBottom);
		g.setPaint(gp2);
		g.fillRect(0, height / 2, deltax - 1, height * 2);

		gp1 = gp2 = null;
	}

	/**
	 * 非线性分割 0 - 16kHz
	 * 
	 */
	private void setPlot() {
		deltax = (width - band + 1) / band + 2;
		for (int i = 0; i <= band; i++) {
			xplot[i] = (i > 0 && xplot[i] <= xplot[i - 1]) ? xplot[i - 1] + 1 : xplot[i];
		}
	}

	/**
	 * 绘制频谱到组件
	 * 
	 * @param amp amp[0..FFT.FFT_N/2-1]
	 */
	private void drawHistogram(float[] amp) {
		g.clearRect(0, 0, width, height);
		// 绘制背景
		g.setColor(bgColor);
		g.fillRect(0, 0, width, height);

		g.setColor(peakColor);
		long t = System.currentTimeMillis();
		// 频率峰值下降速度
		int speed = (int) (t - lastTimeMillis) / 30;
		lastTimeMillis = t;
		int i = 0, x = 0, y, xi, peaki, w = deltax - 1;
		float maxAmp;
		for (; i != band; i++, x += deltax + spacing - 1) {
			// 捕获当前频率最大值
			maxAmp = 0;
			xi = xplot[i];
			y = xplot[i + 1];
			for (; xi < y; xi++) {
				if (maxAmp < amp[xi]) {
					maxAmp = amp[xi];
				}
			}

			// 增幅
			y = (maxAmp > Y0) ? (int) (((Math.log10(maxAmp) - logY0) * 20) * boost) : 0;

			// 频率下降速度
			lastY[i] -= speed << this.speed;
			if (y < lastY[i]) {
				y = y < 0 ? 0 : lastY[i];
			}
			lastY[i] = y;

			if (lastPeak[i] <= y) {
				lastPeak[i] = y;
			} else {
				// 顶块下降
				peaki = lastPeak[i] - speed;
				peaki = peaki < 0 ? 0 : peaki;
				lastPeak[i] = peaki;
				peaki = height - peaki;
				// 绘制顶块
				g.drawLine(x, peaki, x + w - 1, peaki);
			}

			y = height - y;
			// 绘制频谱
			g.drawImage(barImg, x, y, x + w, height + 1, 0, y, w, height, null);
		}
		repaint(0, 0, width, height);
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(specImg, 0, 0, null);
	}

	public void run() {
		Thread.currentThread().setName("Spectrum Core");
		
		WaveIn waveIn = new WaveIn();
		waveIn.open();
		waveIn.start();

		FFT fft = new FFT();
		byte[] b = new byte[FFT.FFT_N << 1];
		float realIO[] = new float[FFT.FFT_N];
		int i, j;

		while (!isShutdown) {
			waveIn.read(b, FFT.FFT_N << 1);
			for (i = 0, j = 0; i != FFT.FFT_N; i++, j += 2) {
				realIO[i] = ((b[j + 1] << 6) | (b[j] & 0xff));
			}
			fft.calculate(realIO);
			drawHistogram(realIO);
		}
		waveIn.shutdown();
	}
	
	/**
	 * 初始化绘制器
	 * 
	 */
	private void initGraphics() {
		width = width < 1 ? 1 : width;
		height = height < 1 ? 1 : height;
		specImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		g = specImg.getGraphics();
		if (g == null) return;
		setPlot();
		barImg = new BufferedImage(deltax - 1, height, BufferedImage.TYPE_3BYTE_BGR);
		setColor();
	}
	
	public void shutdown() {
		this.isShutdown = true;
	}
	
	public void setWidth(int width) {
		this.width = width;
		initGraphics();
	}
	
	public void setHeight(int height) {
		this.height = height;
		initGraphics();
	}
	
	public void setBand(int band) {
		this.band = band;
		initGraphics();
	}
	
	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}

	public void setBoost(int boost) {
		this.boost = boost / 10f;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setBgColor(int hexColor) {
		bgColor = new Color(hexColor);
		initGraphics();
	}
	
	public void setSpecTop(int hexColor) {
		specTop = new Color(hexColor);
		setColor();
	}
	
	public void setSpecMiddle(int hexColor) {
		specMiddle = new Color(hexColor);
		setColor();
	}
	
	public void setSpecBottom(int hexColor) {
		specBottom = new Color(hexColor);
		setColor();
	}
	
	public void setPeak(int hexColor) {
		peakColor = new Color(hexColor);
		setColor();
	}
}