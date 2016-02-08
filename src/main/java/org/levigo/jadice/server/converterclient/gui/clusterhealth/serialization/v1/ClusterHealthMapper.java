package org.levigo.jadice.server.converterclient.gui.clusterhealth.serialization.v1;

import java.lang.reflect.Constructor;

import org.levigo.jadice.server.converterclient.gui.clusterhealth.rule.ImmutableBooleanRule;
import org.levigo.jadice.server.converterclient.gui.clusterhealth.rule.AbstractNumericRule;
import org.levigo.jadice.server.converterclient.gui.clusterhealth.serialization.Marshaller.ClusterHealthDTO;
import org.levigo.jadice.server.converterclient.gui.clusterhealth.serialization.MarshallingException;

public class ClusterHealthMapper {
  
  public ClusterHealth map(ClusterHealthDTO dto) throws MarshallingException {
    final ClusterHealth result = new ClusterHealth();
    
    result.instances = dto.instances;
    
    result.autoUpdateEnabled = dto.autoUpdateEnabled.get();
    result.autoUpdateInterval = dto.autoUpdateInterval.get();
    
    for (org.levigo.jadice.server.converterclient.gui.clusterhealth.rule.Rule<?> rule : dto.rules) {
      if (rule instanceof AbstractNumericRule<?>) {
        Rule<Number> r = new Rule<>();
        r.limit = ((AbstractNumericRule<?>) rule).getLimit();
        r.enabled = rule.isEnabled();
        r.implementation = rule.getClass().getName();
        result.rules.add(r);
      } else if (rule instanceof ImmutableBooleanRule) {
        Rule<Boolean> r = new Rule<>();
        r.enabled = rule.isEnabled();
        r.implementation = rule.getClass().getName();
        result.rules.add(r);
      } else {
        throw new MarshallingException("No support for rule of type " + rule.getClass());
      }
    }
    return result;
  }
  
  public ClusterHealthDTO unmap(ClusterHealth ch) throws MarshallingException {
    ClusterHealthDTO result = new ClusterHealthDTO();
    result.instances.addAll(ch.instances);
    result.autoUpdateEnabled.set(ch.autoUpdateEnabled);
    result.autoUpdateInterval.set(ch.autoUpdateInterval);
    
    for (Rule<?> rule : ch.rules) {
      try {
        final Class<?> clazz = Class.forName(rule.implementation);
        if (AbstractNumericRule.class.isAssignableFrom(clazz)) {
          final AbstractNumericRule<?> r = unmarshallNumericRule(rule, clazz);
          result.rules.add(r);
        } else if (ImmutableBooleanRule.class.isAssignableFrom(clazz)) {
          final ImmutableBooleanRule r = unmarshallImmutableBooleanRule(rule, clazz);
          result.rules.add(r);
        } else {
          throw new MarshallingException("No support for rule of type " + rule.implementation);
        }
      } catch (ReflectiveOperationException | SecurityException e) {
        throw new MarshallingException("Could not unmarshall rule of type " + rule.implementation, e);
      }
    }

    return result;
  }

  private ImmutableBooleanRule unmarshallImmutableBooleanRule(Rule<?> rule, final Class<?> clazz) throws ReflectiveOperationException, MarshallingException {
    Constructor<?> constr = null;
    for (Constructor<?> c : clazz.getConstructors()) {
      if (c.getParameterCount() != 1) {
        continue;
      }
      constr = c;
      break;
    }
    if (constr == null) {
      throw new MarshallingException("No matching constructor found for type " + rule.implementation);
    }
    return (ImmutableBooleanRule) constr.newInstance(rule.enabled);
  }

  private AbstractNumericRule<?> unmarshallNumericRule(Rule<?> rule, final Class<?> clazz) throws ReflectiveOperationException, MarshallingException {
    Constructor<?> constr = null;
    for (Constructor<?> c : clazz.getConstructors()) {
      if (c.getParameterCount() != 2) {
        continue;
      }
      constr = c;
      break;
    }
    if (constr == null) {
      throw new MarshallingException("No matching constructor found for type " + rule.implementation);
    }
    final AbstractNumericRule<?> r = (AbstractNumericRule<?>) constr.newInstance(castValue(constr.getParameterTypes()[0], rule.limit), rule.enabled);
    return r;
  }
  
  private static Number castValue(Class<?> target, Object o) throws MarshallingException {
    if (!Number.class.isAssignableFrom(o.getClass())) {
      throw new MarshallingException(o + " ("+ o.getClass() +") is not a number");
    }
    Number nmbr = (Number) o;
    if (target == Float.TYPE) {
      return nmbr.floatValue();
    } else if (target == Long.TYPE) {
      return nmbr.longValue();
    } else if (target == Double.TYPE) {
      return nmbr.doubleValue();
    } else {
      throw new MarshallingException("No numeric conversion for type " + target.getSimpleName() + " available");
    }
  }
}
