package net.imyeyu.imusic.util;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 定时器，倒计时，返回音量变化值
 * 
 * @author Yeyu
 *
 */
public class Timer extends Service<Number> {
	
	private boolean isShutdown = false;
	private double[] steps;
	
	public void setTime(int time, double nowVolume, double toVolume) {
		// 非线性分布
		double change = nowVolume - toVolume, step = change / time, half = step * .9;
		steps = new double[time];
		if (0 < change) {
			for (int i = 0; i < steps.length / 2; i++) {
				steps[steps.length - i - 1] = step - half;
				steps[i] = step + half;
				half -= half / 64;
			}
		} else {
			for (int i = 0; i < steps.length / 2; i++) {
				steps[i] = step - half;
				steps[steps.length - i - 1] = step + half;
				half -= half / 64;
			}
		}
		for (int i = 0; i < steps.length; i++) {
			System.out.println(steps[i]);
		}
	}
	
	protected Task<Number> createTask() {
		return new Task<Number>() {
			protected Number call() throws Exception {
				Thread.currentThread().setName("Timing Timer");
				
				int i = 0;
				while (!isShutdown) {
					updateMessage(String.valueOf(i++));
					updateValue(steps[i]);
					isShutdown = i == steps.length - 1;
					Thread.sleep(1000);
				}
				return null;
			}
		};
	}
	
	public void restart() {
		this.isShutdown = false;
		super.restart();
	}
	
	public void shutdown() {
		this.isShutdown = true;
	}
}