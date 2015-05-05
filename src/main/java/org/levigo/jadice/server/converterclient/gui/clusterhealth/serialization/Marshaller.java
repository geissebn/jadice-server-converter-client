package org.levigo.jadice.server.converterclient.gui.clusterhealth.serialization;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.levigo.jadice.server.converterclient.gui.clusterhealth.rule.Rule;
import org.levigo.jadice.server.converterclient.gui.clusterhealth.rule.ServerRunningRule;
import org.levigo.jadice.server.converterclient.gui.clusterhealth.rule.TotalFailureRateRule;
import org.levigo.jadice.server.converterclient.gui.clusterhealth.serialization.v1.V1Marshaller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.levigo.util.base.Objects;

public abstract class Marshaller {
  
  public static final class MarshallingDTO {
    public List<String> instances;
    public List<Rule<?>> rules;
  }
  
  public static Marshaller getDefault() {
    return new V1Marshaller();
  }
  
  public static Marshaller get(String version) {
    Objects.assertNotNull("version", version);
    switch (version) {
      case "1.0" : 
        return new V1Marshaller();
      
      default :
        throw new IllegalArgumentException("no support for serialization format version " + version);
    }
  }
  
  public static String lookupVersion(String serialized) throws MarshallingException {
    try {
      final JsonParser parser = new JsonFactory().createParser(serialized);
      while (parser.nextToken() != null) {
        if (parser.getCurrentToken().isScalarValue() && parser.getCurrentName() == "version") {
          return parser.getValueAsString();
        }
      }
    } catch (IOException e) {
      throw new MarshallingException("Could not read serialization version", e);
    }
    throw new MarshallingException("No serialization version found");
  }
  
  public abstract String marshall(MarshallingDTO dto) throws MarshallingException;
  
  public String marshall(List<String> instances, List<Rule<?>> rules) throws MarshallingException {
    MarshallingDTO dto = new MarshallingDTO();
    dto.instances = instances;
    dto.rules = rules;
    return marshall(dto);
  };
  
  public abstract MarshallingDTO unmarshall(String s) throws MarshallingException;
}
