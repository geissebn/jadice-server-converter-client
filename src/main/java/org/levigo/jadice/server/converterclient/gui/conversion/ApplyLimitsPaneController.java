package org.levigo.jadice.server.converterclient.gui.conversion;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.URL;
import java.util.EnumSet;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import org.apache.log4j.Logger;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import com.levigo.jadice.server.Limit;
import com.levigo.jadice.server.NodeCountLimit;
import com.levigo.jadice.server.PageCountLimit;
import com.levigo.jadice.server.StreamCountLimit;
import com.levigo.jadice.server.StreamSizeLimit;
import com.levigo.jadice.server.TimeLimit;

public class ApplyLimitsPaneController implements Initializable {
  
  private static final Logger LOGGER = Logger.getLogger(ApplyLimitsPaneController.class);
  
  @FXML
  private CheckBox timeLimitCB;
  
  @FXML
  private TextField timeLimitValue;
  
  @FXML
  private ChoiceBox<TimeUnit> timeLimitUnit;
  
  @FXML
  private CheckBox streamSizeLimitCB;
  
  @FXML
  private TextField streamSizeLimitValue;
  
  @FXML
  private CheckBox streamCountLimitCB;
  
  @FXML
  private TextField streamCountLimitValue;
  
  @FXML
  private CheckBox nodeCountLimitCB;
  
  @FXML
  private TextField nodeCountLimitValue;
  
  @FXML
  private CheckBox pageCountLimitCB;

  @FXML
  private TextField pageCountLimitValue;
  
  private final ObservableList<Limit> effectiveLimits = FXCollections.observableArrayList();
  
  private final ValidationSupport validationSupport = new ValidationSupport();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    timeLimitUnit.itemsProperty().getValue().addAll(EnumSet.of(MILLISECONDS, SECONDS, MINUTES));
    timeLimitUnit.setValue(SECONDS);
    timeLimitUnit.setConverter(new StringConverter<TimeUnit>() {
      @Override
      public String toString(TimeUnit object) {
        return object.toString().toLowerCase();
      }
      
      @Override
      public TimeUnit fromString(String string) {
        return TimeUnit.valueOf(string.toUpperCase());
      }
    });
    
    validationSupport.setErrorDecorationEnabled(true);
    registerValidator(timeLimitValue, timeLimitCB, new LongValidator());
    registerValidator(streamSizeLimitValue, streamSizeLimitCB, new LongValidator());
    registerValidator(streamCountLimitValue, streamCountLimitCB, new IntegerValidator());
    registerValidator(nodeCountLimitValue, nodeCountLimitCB, new IntegerValidator());
    registerValidator(pageCountLimitValue, pageCountLimitCB, new IntegerValidator());
  }
  
  private void registerValidator(TextField field, CheckBox cb, DisableableNumberValidator<String> validator) {
    cb.selectedProperty().addListener((observable, oldValue, newValue) -> {
      ValidationSupport.setRequired(field, newValue);
      
      // "Change" the text in order to force a revalidation
      final String tmp = field.getText();
      field.setText(null);
      field.setText(tmp);
    });
    validator.enabledProperty().bind(cb.selectedProperty());
    validationSupport.registerValidator(field, cb.isSelected(), validator);
  }
  
  public ObservableList<Limit> getLimits() {
    buildLimits();
    return effectiveLimits;
  }
  
  private void buildLimits() {
    effectiveLimits.clear();
    final ValidationResult result = validationSupport.getValidationResult();
    if (validationSupport.isInvalid() || (result != null && !result.getWarnings().isEmpty())) {
      LOGGER.warn("User Input is not valid. Do not apply limits at all");
      return;
    }
    
    if (timeLimitCB.isSelected()) {
      LOGGER.debug("Apply Time Limit: " + timeLimitValue.getText() + " " + timeLimitUnit.getValue());
      effectiveLimits.add(new TimeLimit(parseLong(timeLimitValue.getText()), timeLimitUnit.getValue()));
    }
    if (streamSizeLimitCB.isSelected()) {
      LOGGER.debug("Apply Stream Size Limit: " + streamSizeLimitValue.getText());
      effectiveLimits.add(new StreamSizeLimit(parseLong(streamSizeLimitValue.getText())));
    }
    if (streamCountLimitCB.isSelected()) {
      LOGGER.debug("Apply Stream Count Limit: " + streamCountLimitValue.getText());
      effectiveLimits.add(new StreamCountLimit(parseInt(streamCountLimitValue.getText())));
    }
    if (nodeCountLimitCB.isSelected()) {
      LOGGER.debug("Apply Node Count Limit: " + nodeCountLimitValue.getText());
      effectiveLimits.add(new NodeCountLimit(parseInt(nodeCountLimitValue.getText())));
    }
    if (pageCountLimitCB.isSelected()) {
      LOGGER.debug("Apply Page Count Limit: " + pageCountLimitValue.getText());
      effectiveLimits.add(new PageCountLimit(parseInt(pageCountLimitValue.getText())));
    }
  }
}
