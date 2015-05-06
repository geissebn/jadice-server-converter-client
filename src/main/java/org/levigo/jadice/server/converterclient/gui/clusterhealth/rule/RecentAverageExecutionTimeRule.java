package org.levigo.jadice.server.converterclient.gui.clusterhealth.rule;

import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;

import javax.management.JMException;
import javax.management.MBeanServerConnection;

import org.levigo.jadice.server.converterclient.gui.clusterhealth.HealthStatus;
import org.levigo.jadice.server.converterclient.gui.clusterhealth.JmxHelper;

public class RecentAverageExecutionTimeRule implements NumericRule<Long> {

  private final LongProperty limit;

  public RecentAverageExecutionTimeRule(long limit) {
    this.limit = new SimpleLongProperty(limit);
  }

  @Override
  public String getDescription() {
    return "Recent average execution time";
  }
  
  @Override
  public Property<Number> limitProperty() {
    return limit;
  }
  
  @Override
  public Long getLimit() {
    return limit.get();
  }
  
  @Override
  public void setLimit(Long value) {
    limit.set(value);
  }

  @Override
  public EvaluationResult<Long> evaluate(MBeanServerConnection mbsc) {
    try {
      final long execTime = JmxHelper.getRecentAverageExecutionTime(mbsc);
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
    return other instanceof RecentAverageExecutionTimeRule && ((RecentAverageExecutionTimeRule) other).getLimit().equals(this.getLimit());
  }
}
