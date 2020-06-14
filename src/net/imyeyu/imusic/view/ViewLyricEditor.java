package net.imyeyu.imusic.view;

import java.io.InputStream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.ui.LyricEditorList;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.gui.BorderX;
import net.imyeyu.util.gui.ToolTipsX;

/**
 * 歌词编辑器视图层
 * 
 * @author Yeyu
 *
 */
public class ViewLyricEditor extends Stage {
	
	private ResourceBundleX rb = Entrance.getRb();

	private Label time;
	private Slider pb;
	private Button toggle, tips;
	private MenuItem imp0rt, imp0rtClipboard, apply, export, close, insertBefore, insertAfter, insertFirst, insertLast, remove;
	private MenuItem cBefore, cAfter, cRemove;
	private ToolTipsX help;
	private LyricEditorList editor;

	public ViewLyricEditor() {
		KeyCodeCombination keyInsertBefore = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN);
		KeyCodeCombination keyInsertAfter = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHIFT_DOWN);
		KeyCodeCombination keyRemove = new KeyCodeCombination(KeyCode.DELETE);
		
		// 菜单
		MenuBar menu = new MenuBar();

		Menu file = new Menu(rb.def("file"));
		imp0rt = new MenuItem(rb.def("lrc.import"), new ImageView(Entrance.RES_PATH + "import.png"));
		imp0rtClipboard = new MenuItem(rb.def("lrc.importCB"));
		imp0rtClipboard.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
		apply = new MenuItem(rb.def("lrc.apply"), new ImageView(Entrance.RES_PATH + "apply.png"));
		apply.setAccelerator(new KeyCodeCombination(KeyCode.F11));
		export = new MenuItem(rb.def("lrc.export"), new ImageView(Entrance.RES_PATH + "export.png"));
		close = new MenuItem(rb.def("close"), new ImageView(Entrance.RES_PATH + "close.png"));
		close.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN));
		file.getItems().addAll(imp0rt, imp0rtClipboard, new SeparatorMenuItem(), apply, export, new SeparatorMenuItem(), close);

		Menu lyric = new Menu(rb.def("lyric"));
		insertBefore = new MenuItem(rb.def("lrc.insertBefore"), new ImageView(Entrance.RES_PATH + "insert_before.png"));
		insertBefore.setAccelerator(keyInsertBefore);
		insertAfter = new MenuItem(rb.def("lrc.insertAfter"), new ImageView(Entrance.RES_PATH + "insert_after.png"));
		insertAfter.setAccelerator(keyInsertAfter);
		insertFirst = new MenuItem(rb.def("lrc.insertFirst"));
		insertLast = new MenuItem(rb.def("lrc.insertLast"));
		remove = new MenuItem(rb.def("removeSelected"), new ImageView(Entrance.RES_PATH + "delete.png"));
		remove.setAccelerator(keyRemove);
		lyric.getItems().addAll(insertBefore, insertAfter, insertFirst, insertLast, new SeparatorMenuItem(), remove);

		menu.getMenus().addAll(file, lyric);

		// 播放控制
		toggle = new Button();
		toggle.setPrefSize(28, 26);
		toggle.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).right());
		YeyuUtils.gui().setBg(toggle, Entrance.RES_PATH + "play.png", 16, 6, 6);
		pb = new Slider();
		pb.setPadding(new Insets(0, 6, 0, 6));
		
		time = new Label("00:00.000");
		time.setPadding(new Insets(0, 4, 0, 0));
		InputStream fontStream = getClass().getResourceAsStream(Entrance.RES_PATH + "DIGITAL.ttf");
		time.setFont(Font.loadFont(fontStream, 20));
		help = new ToolTipsX(rb.def("lyricEditorTips"));
		tips = new Button();
		tips.setPrefSize(28, 26);
		tips.setTooltip(help);
		tips.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).left());
		YeyuUtils.gui().setBg(tips, Entrance.RES_PATH + "help.png", 16, 6, 5);
		HBox timeAndTips = new HBox();
		timeAndTips.setAlignment(Pos.CENTER);
		timeAndTips.getChildren().addAll(time, tips);

		BorderPane ctrl = new BorderPane();
		ctrl.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).bottom());
		ctrl.setLeft(toggle);
		ctrl.setCenter(pb);
		ctrl.setRight(timeAndTips);

		GridPane header = new GridPane();
		ColumnConstraints col = new ColumnConstraints();
		col.setPercentWidth(100);
		header.getColumnConstraints().add(col);
		header.addColumn(0, menu, ctrl);

		// 编辑区
		cBefore = new MenuItem(rb.def("lrc.insertBefore"), new ImageView(Entrance.RES_PATH + "insert_before.png"));
		cBefore.setAccelerator(keyInsertBefore);
		cAfter = new MenuItem(rb.def("lrc.insertAfter"), new ImageView(Entrance.RES_PATH + "insert_after.png"));
		cAfter.setAccelerator(keyInsertAfter);
		cRemove = new MenuItem(rb.def("remove"), new ImageView(Entrance.RES_PATH + "delete.png"));
		cRemove.setAccelerator(keyRemove);
		
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.getItems().addAll(cBefore, cAfter, new SeparatorMenuItem(), cRemove);
		editor = new LyricEditorList(contextMenu);
		editor.setContextMenu(contextMenu);

		BorderPane main = new BorderPane();
		main.setTop(header);
		main.setCenter(editor);
		main.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).top());

		Scene scene = new Scene(main);
		scene.getStylesheets().add(this.getClass().getResource(Entrance.RES_PATH + "style.css").toExternalForm());
		getIcons().add(new Image(Entrance.RES_PATH + "lrcEditor.png"));
		setMinWidth(375);
		setMinHeight(520);
		setWidth(375);
		setHeight(520);
		setScene(scene);
		initModality(Modality.APPLICATION_MODAL);
	}
	
	public LyricEditorList getEditor() {
		return editor;
	}
	
	public Label getTime() {
		return time;
	}
	
	public Button getToggle() {
		return toggle;
	}
	
	public Button getTips() {
		return tips;
	}

	public Slider getPb() {
		return pb;
	}

	public MenuItem getImp0rt() {
		return imp0rt;
	}
	
	public MenuItem getImp0rtClipboard() {
		return imp0rtClipboard;
	}

	public MenuItem getApply() {
		return apply;
	}

	public MenuItem getExport() {
		return export;
	}

	public MenuItem getClose() {
		return close;
	}

	public MenuItem getcBefore() {
		return cBefore;
	}

	public MenuItem getInsertBefore() {
		return insertBefore;
	}

	public MenuItem getcAfter() {
		return cAfter;
	}

	public MenuItem getInsertAfter() {
		return insertAfter;
	}

	public MenuItem getInsertFirst() {
		return insertFirst;
	}

	public MenuItem getInsertLast() {
		return insertLast;
	}

	public MenuItem getcRemove() {
		return cRemove;
	}

	public MenuItem getRemove() {
		return remove;
	}
}