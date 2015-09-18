package org.levigo.jadice.server.converterclient.gui.clusterhealth;

import java.util.stream.Collectors;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import eu.hansolo.enzo.canvasled.Led;
import eu.hansolo.enzo.canvasled.LedBuilder;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class StatusControl extends AnchorPane {

  private final Led green = buildLed(Color.LAWNGREEN);
  
  private final Led yellow = buildLed(Color.GOLD);
  
  private final Led red = buildLed(Color.RED);
  
  private final Tooltip messagesTooltip = new Tooltip();
  
  private final Text instanceName = new Text();
  
  private final ClusterInstance instance;
  
  private final ClusterHealthPaneController controller;
  
  private final HBox middleBox = new HBox();
  
  private final HBox innerBox = new HBox();
  
  private final Button removeBttn = GlyphsDude.createIconButton(FontAwesomeIcon.TIMES_CIRCLE);
  
  public StatusControl(ClusterInstance instance, ClusterHealthPaneController controller) {
    this.instance = instance;
    this.controller = controller;
    middleBox.getChildren().add(innerBox);
    innerBox.getChildren().add(red);
    innerBox.getChildren().add(yellow);
    innerBox.getChildren().add(green);
    innerBox.setMinWidth(USE_PREF_SIZE);
    final LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.BLACK), new Stop(1, Color.DIMGRAY));
    innerBox.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(15), null)));
    innerBox.setPadding(new Insets(2, 5, 2, 5));
    middleBox.getChildren().add(instanceName);
    middleBox.setMaxHeight(USE_PREF_SIZE);
    HBox.setMargin(innerBox, new Insets(2, 0, 2, 3));
    HBox.setMargin(instanceName, new Insets(0, 5, 0, 10));
    middleBox.setAlignment(Pos.CENTER_LEFT);
    
    middleBox.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(9), new Insets(-2))));
    getChildren().add(middleBox);
    AnchorPane.setLeftAnchor(middleBox, 0.0);
    AnchorPane.setRightAnchor(middleBox, 0.0);
    AnchorPane.setTopAnchor(middleBox, 0.0);
    AnchorPane.setBottomAnchor(middleBox, 0.0);
    
    getChildren().add(removeBttn);
    AnchorPane.setTopAnchor(removeBttn, -2.0);
    AnchorPane.setRightAnchor(removeBttn, -2.0);
    
    instanceName.setStyle("-fx-font-weight: bold;");
    initBindings();
    
    configureRemoveButton();
  }

  private void configureRemoveButton() {
    removeBttn.setOpacity(0.0);
    
    final int removeBttnInvisibleAngle = -60;
    
    final RotateTransition rotateIn = new RotateTransition(Duration.millis(150), removeBttn);
    rotateIn.setFromAngle(removeBttnInvisibleAngle);
    rotateIn.setToAngle(0);
    
    final FadeTransition fadeIn = new FadeTransition(Duration.millis(100), removeBttn);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);
    
    final Animation mouseEnterAnimation = new ParallelTransition(rotateIn, fadeIn);

    final RotateTransition rotateOut = new RotateTransition(Duration.millis(150), removeBttn);
    rotateOut.setFromAngle(0);
    rotateOut.setToAngle(removeBttnInvisibleAngle);
    
    final FadeTransition fadeOut = new FadeTransition(Duration.millis(100), removeBttn);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    
    final Animation mouseExitedAnimation = new ParallelTransition(rotateOut, fadeOut);
    
    setOnMouseEntered(evt -> {
      mouseExitedAnimation.stop();
      mouseEnterAnimation.playFromStart();
    });
    
    setOnMouseExited(evt -> {
      mouseEnterAnimation.stop();
      mouseExitedAnimation.playFromStart();
    });
    removeBttn.setOnAction(evt -> controller.removeClusterInstance(this));
    removeBttn.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
  }
  
  public ClusterInstance getClusterInstance() {
    return instance;
  }
  
  private void initBindings() {
    green.onProperty().bind(instance.healthProperty().isEqualTo(HealthStatus.GOOD));
    yellow.onProperty().bind(instance.healthProperty().isEqualTo(HealthStatus.ATTENTION));
    red.onProperty().bind(instance.healthProperty().isEqualTo(HealthStatus.FAILURE));
    instanceName.textProperty().bind(instance.serverNameProperty());
    instance.messagesProperty().addListener((ListChangeListener.Change<? extends String>  c) -> {
      if (instance.messagesProperty().get().isEmpty()) {
        Tooltip.uninstall(innerBox, messagesTooltip);
      } else {
        Tooltip.install(innerBox, messagesTooltip);
        messagesTooltip.setText(instance.messagesProperty().stream().collect(Collectors.joining("\n")));
      }
    });
  }

  private static Led buildLed(Color color) {
    return LedBuilder.create()//
    .ledColor(color)//
    .frameVisible(false)//
    .minSize(30, 30)//
    .build();
  }


}
