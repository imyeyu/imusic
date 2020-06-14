package net.imyeyu.imusic.ui;

import javax.swing.SwingUtilities;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.core.Spectrum;
import net.imyeyu.util.Config;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.gui.AnchorPaneX;
import net.imyeyu.util.gui.BorderX;
import sun.awt.AWTAutoShutdown;

/**
 * 频谱面板
 * 
 * @author Yeyu
 *
 */
public class SpecPane extends BorderPane {
	
	private Config config = Entrance.getConfig();
	private ResourceBundleX rb = Entrance.getRb();

	private Label title;
	private Slider boost, spacing, speed, band;
	private Button specColor;
	private BorderX border = new BorderX("#C8C8C8", BorderX.SOLID, 1);
	private Spectrum spectrum;
	private SwingNode specNode;

	public SpecPane() {
		title = new Label(rb.def("noMusicPlaying"));
		title.setPadding(new Insets(4, 6, 0, 6));
		title.setTextFill(Paint.valueOf("#999"));
		title.setAlignment(Pos.CENTER);
		AnchorPaneX.exBottom(title);

		spectrum = new Spectrum();
		specNode = new SwingNode();
		AnchorPaneX.def(specNode);
		SwingUtilities.invokeLater(() -> specNode.setContent(spectrum));

		AnchorPane center = new AnchorPane();
		AnchorPaneX.def(center);
		center.getChildren().addAll(specNode, title);
		
		GridPane boostAndSpacing = new GridPane();

		Label labelBoost = new Label(rb.def("spec.boost"));
		boost = new Slider(10, 50, config.getDouble("specBoost"));
		boost.setBorder(border.right());
		boost.setPadding(new Insets(6, 4, 6, 0));
		BorderPane boostPane = new BorderPane();
		BorderPane.setAlignment(labelBoost, Pos.CENTER);
		boostPane.setLeft(labelBoost);
		boostPane.setCenter(boost);

		Label labelSpacing = new Label(rb.def("spec.spacing"));
		labelSpacing.setPadding(new Insets(0, 0, 0, 6));
		spacing = new Slider(1, 6, config.getDouble("specSpacing"));
		spacing.setBorder(border.right());
		spacing.setPadding(new Insets(6, 4, 6, 0));
		BorderPane spacingPane = new BorderPane();
		BorderPane.setAlignment(labelSpacing, Pos.CENTER);
		spacingPane.setLeft(labelSpacing);
		spacingPane.setCenter(spacing);
		ColumnConstraints col = new ColumnConstraints();
		col.setPercentWidth(50);
		boostAndSpacing.getColumnConstraints().addAll(col, col);
		boostAndSpacing.add(boostPane, 0, 0);
		boostAndSpacing.add(spacingPane, 1, 0);
		
		HBox speedPane = new HBox();
		Label labelSpeed = new Label(rb.def("spec.speed"));
		labelSpeed.setPadding(new Insets(0, 0, 0, 6));
		speed = new Slider(1, 3, config.getDouble("specSpeed"));
		speed.setPrefWidth(32);
		speed.setMajorTickUnit(4);
		speed.setSnapToTicks(true);
		speedPane.setAlignment(Pos.CENTER_LEFT);
		speedPane.getChildren().addAll(labelSpeed, speed);
		
		BorderPane bsPane = new BorderPane();
		bsPane.setPadding(new Insets(0, 4, 0, 4));
		bsPane.setBorder(border.top());
		bsPane.setCenter(boostAndSpacing);
		bsPane.setRight(speedPane);

		specColor = new Button(rb.def("spec.color"));
		specColor.setBorder(border.topRight());
		specColor.setStyle("-fx-background-insets: 0;");

		BorderPane bandPane = new BorderPane();
		Label labelBand = new Label(rb.def("spec.band"));
		band = new Slider(16, 128, config.getDouble("specBand"));
		BorderPane.setAlignment(labelBand, Pos.CENTER);
		bandPane.setBorder(border.top());
		bandPane.setPadding(new Insets(0, 4, 0, 4));
		bandPane.setLeft(labelBand);
		bandPane.setCenter(band);

		BorderPane bottomBottom = new BorderPane();
		bottomBottom.setLeft(specColor);
		bottomBottom.setCenter(bandPane);

		VBox bottom = new VBox();
		bottom.getChildren().addAll(bsPane, bottomBottom);

		AnchorPaneX.def(this);
		setCenter(center);
		setBottom(bottom);

		setBorder(border.right());
	}

	public void setSize(double width, double height) {
		setWidth(width);
		setHeight(height);
		spectrum.setSize((int) width - 1, (int) height - 51);
	}

	public void shutdown() {
		AWTAutoShutdown.getInstance().run();
		if (spectrum != null) spectrum.shutdown();
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}

	public Slider getBoost() {
		return boost;
	}
	
	public Slider getSpacing() {
		return spacing;
	}

	public Slider getSpeed() {
		return speed;
	}

	public Slider getBand() {
		return band;
	}
	
	public Button getSpecColor() {
		return specColor;
	}
	
	public Spectrum getSpectrum() {
		return spectrum;
	}

	public SwingNode getSpecNode() {
		return specNode;
	}
}