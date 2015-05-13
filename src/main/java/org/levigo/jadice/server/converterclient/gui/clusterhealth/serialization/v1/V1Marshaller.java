package org.levigo.jadice.server.converterclient.gui.clusterhealth.serialization.v1;

import java.io.IOException;
import java.nio.charset.Charset;

import org.levigo.jadice.server.converterclient.gui.clusterhealth.serialization.Marshaller;
import org.levigo.jadice.server.converterclient.gui.clusterhealth.serialization.MarshallingException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class V1Marshaller extends Marshaller {
  
  private final ClusterHealthMapper mapper = new ClusterHealthMapper();
  
  private final ObjectMapper objectMapper = new ObjectMapper();
  
  @Override
  public String marshallPrettyPrint(ClusterHealthDTO dto) throws MarshallingException {
    final ClusterHealth ch = mapper.map(dto);
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ch);
    } catch (JsonProcessingException jpe) {
      throw new MarshallingException("Could not marshall", jpe);
    }
  }
  
  @Override
  public String marshall(ClusterHealthDTO dto) throws MarshallingException {
    final ClusterHealth ch = mapper.map(dto);
    try {
      return objectMapper.writeValueAsString(ch);
    } catch (JsonProcessingException jpe) {
      throw new MarshallingException("Could not marshall", jpe);
    }
  }
  
  public ClusterHealthDTO unmarshall(String s) throws MarshallingException {
    try {
      final ClusterHealth ch = objectMapper.readValue(s.getBytes(Charset.forName("UTF-8")), ClusterHealth.class);
      return mapper.unmap(ch);
    } catch (IOException e) {
      throw new MarshallingException("Could not unmarshall", e);
    }
  }



  
}
