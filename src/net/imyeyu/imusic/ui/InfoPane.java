package net.imyeyu.imusic.ui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.Music;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.gui.AnchorPaneX;

public class InfoPane extends GridPane {
	
	private ResourceBundleX rb = Entrance.getRb();
	
	private Label name, singer, album, year, size, encode, status;

	public InfoPane() {
		Label labelName = new Label(rb.def("labelName"));
		Label labelSinger = new Label(rb.def("labelSinger"));
		Label labelAlbum = new Label(rb.def("labelAlbum"));
		Label labelYear = new Label(rb.def("labelYear"));
		Label labelSize = new Label(rb.def("labelFileSize"));
		Label labelEncode = new Label(rb.def("labelEncode"));
		Label labelListStatus = new Label(rb.def("labelListStatus"));

		name = new Label();
		singer = new Label();
		album = new Label();
		year = new Label();
		size = new Label();
		encode = new Label();
		status = new Label();

		Paint gray = Paint.valueOf("#777");
		Label[] labels = {labelName, labelSinger, labelAlbum, labelYear, labelSize, labelEncode, labelListStatus};
		Label[] values = {name, singer, album, year, size, encode, status};
		for (int i = 0; i < labels.length; i++) {
			labels[i].setTextFill(gray);
			add(labels[i], 0, i);
			GridPane.setHalignment(labels[i], HPos.RIGHT);
			
			add(values[i], 1, i);
		}
		
		ColumnConstraints col = new ColumnConstraints();
		col.setMinWidth(64);
		getColumnConstraints().add(col);
		
		setVgap(8);
		setPadding(new Insets(8, 0, 8, 8));
		setAlignment(Pos.CENTER_LEFT);
		AnchorPaneX.exBottom(this);
	}
	
	public void set(Music music) {
		name.setText(music.getName());
		singer.setText(music.getSinger());
		album.setText(music.getAlbum());
		year.setText(music.getYear());
		size.setText(String.format("%.3f", music.getSize() / 1024f / 1024f) + " MB");
		encode.setText(music.getEncode());
	}
	
	public void setListStatus(int playingId, int listSize) {
		status.setText((playingId + 1) + " / " + listSize);
	}
	
	public void setListStatus(int playingId, int next, int listSize) {
		status.setText((playingId + 1) + " > " + (next + 1) + " / " + listSize);
	}
}