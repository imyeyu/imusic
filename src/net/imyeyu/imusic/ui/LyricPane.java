package net.imyeyu.imusic.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.gui.AnchorPaneX;
import net.imyeyu.util.gui.BorderX;
import net.imyeyu.util.interfaces.GUIX;

public class LyricPane extends AnchorPane {
	
	private Font lrcFont = Font.font("Microsoft YaHei");
	private Label icon, title, offset;
	private Button lrcSlowX, lrcSlow, lrcSave, lrcFast, lrcFastX, imp0rt, editor;
	private LynicList lrcs;
	private BorderPane header;
	private SimpleIntegerProperty offsetValue = new SimpleIntegerProperty(0);
	
	public LyricPane() {
		// 歌词来源微标（API 或 LOCAL）
		icon = new Label();
		icon.setFont(Font.font("Consolas"));
		icon.setStyle("-fx-background-color: #AAA;-fx-background-radius: 2px");
		icon.setPadding(new Insets(0, 4, 0, 4));
		icon.setTextFill(GUIX.WHITE);
		BorderPane.setAlignment(icon, Pos.CENTER);
		// 标题
		title = new Label(Entrance.getRb().def("noMusicPlaying"));
		title.setPadding(new Insets(0, 0, 0, 4));
		title.setTextFill(Paint.valueOf("#999"));
		BorderPane.setAlignment(title, Pos.CENTER_LEFT);
		// 偏移
		offset = new Label();
		offset.setTextFill(Paint.valueOf("#777"));
		offsetValue.addListener((tmp, o, n) -> {
			String v = 0 < n.intValue() ? "+" + n.intValue() : String.valueOf(n.intValue());
			offset.setText(n.intValue() == 0 ? "" : v + " ms");
		});
		
		header = new BorderPane();
		header.setPadding(new Insets(4, 6, 4, 6));
		header.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).bottom());
		header.setCenter(title);
		header.setRight(offset);
		// 歌词操作
		VBox ctrl = new VBox();
		ctrl.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).right());
		
		lrcSlowX = new Button();
		lrcSlow = new Button();
		lrcSave = new Button();
		lrcFast = new Button();
		lrcFastX = new Button();
		imp0rt = new Button();
		editor = new Button();
		
		String[] icoName = {"lrcSlowX", "lrcSlow", "lrcSave", "lrcFast", "lrcFastX", "import", "editor"};
		Button[] lrcBtns = {lrcSlowX, lrcSlow, lrcSave, lrcFast, lrcFastX, imp0rt, editor};
		Border lrcBtnBorder = new BorderX("#C8C8C8", BorderX.SOLID, 1).bottom();
		for (int i = 0; i < lrcBtns.length; i++) {
			lrcBtns[i].setPrefWidth(26);
			lrcBtns[i].setBorder(lrcBtnBorder);
			YeyuUtils.gui().setBg(lrcBtns[i], Entrance.RES_PATH + icoName[i] + ".png", 16, 5, 5);
			ctrl.getChildren().add(lrcBtns[i]);
		}
		
		// 歌词
		lrcs = new LynicList();
		lrcs.setId("lrcs");
		
		BorderPane main = new BorderPane();
		BorderPane.setMargin(lrcs, new Insets(0, 1, 1, 0));
		main.setTop(header);
		main.setLeft(ctrl);
		main.setCenter(lrcs);

		setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).right());
		AnchorPaneX.def(main);
		AnchorPaneX.def(this);
		getChildren().add(main);
		
		widthProperty().addListener((tmp, o, width) -> {
			lrcFont = 320 < width.doubleValue() ? Font.font("Microsoft YaHei", 15) : Font.font("Microsoft YaHei");
			lrcs.refresh();
		});
	}
	
	private class LynicList extends ListView<String> {
		// 重绘歌词列表单元
		public LynicList() {
			setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
				public ListCell<String> call(ListView<String> param) {
					return new ListCell<String>() {
						
						protected void updateItem(String text, boolean empty) {
							super.updateItem(text, empty);
							if (!empty && text != null) {
								Label lrc = new Label(text);
								lrc.setFont(lrcFont);
								lrc.setWrapText(true);
								lrc.setAlignment(Pos.CENTER);
		                        lrc.setTextAlignment(TextAlignment.CENTER);
		                        lrc.prefWidthProperty().bind(lrcs.widthProperty().subtract(25));
		                        
		                        setGraphic(lrc);
							} else {
								setGraphic(null);
							}
						}
					};
				}
			});
		}
	}

	/**
	 * 设置标题
	 * 
	 * @param title 标题
	 * @param source 歌词来源
	 */
	public void setTitle(String title, String source) {
		this.title.setText(title);
		if (header.getLeft() == null) header.setLeft(icon);
		icon.setText(source);
	}
	
	/**
	 * 歌词进度偏移
	 * 
	 * @param offset 偏移值
	 */
	public void offset(int offset) {
		offsetValue.set(offsetValue.get() + offset);
	}
	
	/**
	 * 重置偏移值
	 * 
	 */
	public void resetOffset() {
		offsetValue.set(0);
	}

	public Button getLrcSlowX() {
		return lrcSlowX;
	}

	public Button getLrcSlow() {
		return lrcSlow;
	}

	public Button getLrcSave() {
		return lrcSave;
	}

	public Button getLrcFast() {
		return lrcFast;
	}

	public Button getLrcFastX() {
		return lrcFastX;
	}
	
	public Button getImport() {
		return imp0rt;
	}
	
	public Button getEditor() {
		return editor;
	}

	public ListView<String> getLrcs() {
		return lrcs;
	}
}