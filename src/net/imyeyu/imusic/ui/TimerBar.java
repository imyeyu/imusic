package net.imyeyu.imusic.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import net.imyeyu.util.gui.AnchorPaneX;
import net.imyeyu.util.gui.BorderX;

/**
 * 定时器进度条，显示剩余时间在中间
 * 
 * @author Yeyu
 *
 */
public class TimerBar extends AnchorPane {
	
	private Label time;
	private ProgressBar pb;

	public TimerBar() {
		pb = new ProgressBar();
		pb.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).def());
		pb.getStyleClass().add("timer-bar");
		AnchorPaneX.def(pb);
		
		time = new Label("00:00");
		time.setFont(Font.font("Consolas"));
		time.setAlignment(Pos.CENTER);
		AnchorPaneX.def(time);
		
		setPrefSize(320, 22);
		getChildren().addAll(pb, time);
	}
	
	public void setValue(double pb, long second) {
		this.pb.setProgress(pb);
		time.setText(second2time(second));
	}
	
	public void reset(long second) {
		pb.setProgress(-1);
		time.setText(second2time(second));
	}

	/**
	 * 秒数转 小时:分钟:秒钟，如 3:59:46
	 * 
	 * @param second
	 * @return
	 */
	private String second2time(long second) {
		String h = Math.abs(second / 3600) != 0 ? Math.abs(second / 3600) + ":" : "";
		return h + String.format("%02d", Math.abs(second % 3600 / 60)) + ":" + String.format("%02d", second % 60);
	}
}