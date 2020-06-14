package net.imyeyu.imusic.ctrl;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Screen;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.Music;
import net.imyeyu.imusic.view.ViewCover;

public class Cover extends ViewCover {
	
	private double ox = 0, oy = 0, cx = 0, cy = 0, scale = 1;

	public Cover(Music music) {
		setTitle(music.getAlbum() + " - " + Entrance.getRb().def("cover"));
		double imgW = Double.valueOf(music.getCover().getWidth());
		double imgH = Double.valueOf(music.getCover().getHeight());
		
		setWidth(imgW + 10);
		setHeight(imgH + 10);
		
		if (imgH < imgW) { // 横向
			scale = imgH / imgW; // 宽高比
			if (1600 < imgW || 820 < imgH) {
				if ((1600 < imgW || 3 < imgW / imgH)) {
					setWidth();
				} else {
					if (imgW < imgH || 960 < imgH) {
						setHeight();
					} else {
						setWidth();
					}
				}
			}
		} else { // 纵向
			scale = imgW / imgH; // 宽高比
			if (1600 < imgW || 820 < imgH) {
				if ((960 < imgH && 1200 < 960 * scale) || 3 < imgH / imgW) {
					setWidth();
				} else {
					if (imgW < imgH || 960 < imgH) {
						setHeight();
					} else {
						setWidth();
					}
				}
			}
		}
		Rectangle2D screen = Screen.getPrimary().getVisualBounds();
		setX(screen.getMaxX() / 2 - getWidth() / 2);
		setY(screen.getMaxY() / 2 - getHeight() / 2);
		
		getImgView().setImage(music.getCover());

		// 点击图像
		getImgBox().setOnMousePressed(event -> {
			getImgBox().setCursor(Cursor.CLOSED_HAND);
			ox = event.getX();
			oy = event.getY();
			cx = event.getScreenX();
			cy = event.getScreenY();
		});
		// 拖动图像
		getImgBox().setOnMouseDragged(event -> {
			setX(event.getScreenX() - ox);
			setY(event.getScreenY() - oy);
		});
		// 释放图像
		getImgBox().setOnMouseReleased(event -> {
			getImgBox().setCursor(Cursor.DEFAULT);
			if (cx == event.getScreenX() && cy == event.getScreenY()) close();
		});
		// 滚轮事件
		getImgBox().addEventFilter(ScrollEvent.SCROLL, event -> {
			if (!(getWidth() + event.getDeltaY() < 16)) {
				if (getImgView().getFitHeight() < getImgView().getFitWidth()) {
					setX(getX() + -event.getDeltaY() / 2);
					setY(getY() + -(event.getDeltaY() / 2 * scale));
					setWidth(getWidth() + event.getDeltaY());
					setHeight(getHeight() + event.getDeltaY() * scale);
					getImgView().setFitWidth(getWidth() - 10);
					getImgView().setFitHeight(getHeight() - 10);
				} else {
					setX(getX() + -(event.getDeltaY() / 2 * scale));
					setY(getY() + -event.getDeltaY() / 2);
					setWidth(getWidth() + event.getDeltaY() * scale);
					setHeight(getHeight() + event.getDeltaY());
					getImgView().setFitWidth(getWidth() - 10);
					getImgView().setFitHeight(getHeight() - 10);
				}
			}
		});
	}
	
	private void setWidth() {
		setWidth(1200 + 10);
		setHeight(1200 * scale + 10);
		getImgView().setFitWidth(getWidth() - 10);
		getImgView().setFitHeight(getHeight() - 10);
	}
	
	private void setHeight() {
		setWidth(960 * scale + 10);
		setHeight(960 + 10);
		getImgView().setFitWidth(getWidth() - 10);
		getImgView().setFitHeight(getHeight() - 10);
	}
}