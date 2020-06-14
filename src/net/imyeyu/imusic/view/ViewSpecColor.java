package net.imyeyu.imusic.view;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.SpecPreset;
import net.imyeyu.imusic.util.SpecPresets;
import net.imyeyu.util.Config;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.gui.AnchorPaneX;

/**
 * 频谱颜色设置视图层
 * 
 * @author Yeyu
 *
 */
public class ViewSpecColor extends Stage {
	
	private Config config = Entrance.getConfig();
	private ResourceBundleX rb = Entrance.getRb();
	
	private Stage stage = new Stage();
	private Button close;
	private ColorPicker bg, specTop, specMiddle, specBottom, peak;
	private ComboBox<SpecPreset> preset;

	public ViewSpecColor() {
		// 嵌套舞台去除边框的同时不显示在任务栏
		initStyle(StageStyle.UTILITY);
		setOpacity(0);
		show();
        
		close = new Button();
		close.setMinSize(16, 16);
		close.setPrefSize(16, 16);
		YeyuUtils.gui().setBgTp(close, Entrance.RES_PATH + "close.png", 16, 0, 0);
		AnchorPaneX.def(close, 6, 6, null, null);
		
		Label labelPreset = new Label(rb.def("spec.preset"));
		Label labelBg = new Label(rb.def("spec.bg"));
		Label labelSpec = new Label(rb.def("spec.spec"));
		Label labelPeak = new Label(rb.def("spec.peak"));
		
		preset = new ComboBox<>();
		preset.setConverter(new StringConverter<SpecPreset>() {
			public String toString(SpecPreset preset) {
				return preset.getName();
			}
			
			public SpecPreset fromString(String string) {
				return null;
			}
		});
		preset.setPrefWidth(100);
		preset.getItems().addAll(SpecPresets.get());
		preset.getSelectionModel().select(config.getInt("specID"));
		
		// 已选预设
		SpecPreset specPreset = preset.getValue();
		
		// 背景
		bg = new ColorPicker();
		bg.setValue(specPreset.getBg());
		// 频谱峰值
		Label labelTop = new Label(rb.def("spec.top"));
		labelTop.setTextFill(Color.GRAY);
		specTop = new ColorPicker();
		specTop.setValue(specPreset.getSpecTop());
		HBox boxTop = new HBox();
		boxTop.setSpacing(6);
		boxTop.setAlignment(Pos.CENTER);
		boxTop.getChildren().addAll(specTop, labelTop);
		// 频谱中间
		Label labelMiddle = new Label(rb.def("spec.middle"));
		labelMiddle.setTextFill(Color.GRAY);
		specMiddle = new ColorPicker();
		specMiddle.setValue(specPreset.getSpecMiddle());
		HBox boxMiddle = new HBox();
		boxMiddle.setSpacing(6);
		boxMiddle.setAlignment(Pos.CENTER);
		boxMiddle.getChildren().addAll(specMiddle, labelMiddle);
		// 频谱底部
		Label labelBottom = new Label(rb.def("spec.bottom"));
		labelBottom.setTextFill(Color.GRAY);
		specBottom = new ColorPicker();
		specBottom.setValue(specPreset.getSpecBottom());
		HBox boxBottom = new HBox();
		boxBottom.setSpacing(6);
		boxBottom.setAlignment(Pos.CENTER);
		boxBottom.getChildren().addAll(specBottom, labelBottom);
		
		VBox spec = new VBox();
		spec.setSpacing(8);
		spec.getChildren().addAll(boxTop, boxMiddle, boxBottom);
		// 顶块
		peak = new ColorPicker();
		peak.setValue(specPreset.getPeak());
		
		ColumnConstraints col = new ColumnConstraints();
		col.setHalignment(HPos.RIGHT);
		
		GridPane settings = new GridPane();
		AnchorPaneX.def(settings, 12, null, 12, 10);
		settings.setVgap(8);
		settings.getColumnConstraints().add(col);
		settings.addColumn(0, labelPreset, labelBg, labelSpec, labelPeak);
		settings.addColumn(1, preset, bg, spec, peak);
		
		DropShadow dropshadow = new DropShadow();
		dropshadow.setRadius(6);
		dropshadow.setOffsetX(0);
		dropshadow.setOffsetY(0);
		dropshadow.setSpread(.05);
		dropshadow.setColor(Color.valueOf("#333E"));
		
		AnchorPane main = new AnchorPane();
		AnchorPaneX.def(main, 5);
		main.setEffect(dropshadow);
		main.setStyle("-fx-background-color: #F4F4F4;-fx-background-radius: 2px");
		main.getChildren().addAll(settings, close);

		AnchorPane mainBox = new AnchorPane();
		mainBox.setBackground(Background.EMPTY);
		mainBox.getChildren().add(main);

		Scene scene = new Scene(mainBox);
		scene.setFill(null);
		scene.getStylesheets().add(this.getClass().getResource(Entrance.RES_PATH + "style.css").toExternalForm());
		stage.setTitle(rb.def("spec.title"));
		stage.setScene(scene);
		stage.setWidth(240);
		stage.setHeight(220);
        stage.initOwner(this);
		stage.setResizable(false);
		stage.setAlwaysOnTop(true);
		stage.initStyle(StageStyle.TRANSPARENT);
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public Button getClose() {
		return close;
	}
	
	public ColorPicker getBg() {
		return bg;
	}

	public ColorPicker getSpecTop() {
		return specTop;
	}

	public ColorPicker getSpecMiddle() {
		return specMiddle;
	}

	public ColorPicker getSpecBottom() {
		return specBottom;
	}

	public ColorPicker getPeak() {
		return peak;
	}
	
	public ComboBox<SpecPreset> getPreset() {
		return preset;
	}
}