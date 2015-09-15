package org.levigo.jadice.server.converterclient.gui.clusterhealth.rule;

import javax.management.JMException;
import javax.management.MBeanServerConnection;

import org.levigo.jadice.server.converterclient.gui.clusterhealth.HealthStatus;
import org.levigo.jadice.server.converterclient.gui.clusterhealth.JmxHelper;

import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;

public class AverageExecutionTimeRule extends NumericRule<Long> {
  
  private final LongProperty limit;

  public AverageExecutionTimeRule(long limit) {
    this.limit = new SimpleLongProperty(limit);
  }

  @Override
  public String getDescription() {
    return "Average execution time";
  }
  
  @Override
  public Property<Number> limitProperty() {
    return limit;
  }
  
  @Override
  public EvaluationResult<Long> evaluate(MBeanServerConnection mbsc) {
    try {
      final long execTime = jmxFunction().apply(mbsc);
      if (execTime <= limit.get()) {
        return new EvaluationResult<Long>(HealthStatus.GOOD, execTime);
      } else {
        return new EvaluationResult<Long>(HealthStatus.ATTENTION, execTime, getDescription() + ": " + execTime);
      }
    } catch (JMException e) {
      return new EvaluationResult<Long>(HealthStatus.FAILURE, -1L, e);
    }
  }
  
  @Override
  public int hashCode() {
    return limit.hashCode();
  }
  
  @Override
  public boolean equals(Object other) {
    return other instanceof AverageExecutionTimeRule && ((AverageExecutionTimeRule) other).getLimit().equals(this.getLimit());
  }
  
  @Override
  public String toString() {
    return String.format("Average Excecution Time of %d ms", getLimit());
  }

  @Override
  protected ExceptionalFunction<MBeanServerConnection, Long, JMException> jmxFunction() {
    return JmxHelper::getAverageExecutionTime;
  }
}
