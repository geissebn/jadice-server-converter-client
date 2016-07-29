package org.levigo.jadice.server.converterclient.configurations;

import com.levigo.jadice.server.Job;
import com.levigo.jadice.server.ghostscript.GhostscriptNode;
import com.levigo.jadice.server.ghostscript.TIFFOutputDevice;
import com.levigo.jadice.server.ghostscript.TIFFOutputDevice.TIFFType;
import com.levigo.jadice.server.nodes.StreamInputNode;
import com.levigo.jadice.server.nodes.StreamOutputNode;

public class GhostscriptTiffConfig implements WorkflowConfiguration {

  public void configureWorkflow(Job job) {
    final GhostscriptNode gs = new GhostscriptNode();
    final TIFFOutputDevice tiffDevice = new TIFFOutputDevice();
    tiffDevice.setType(TIFFType.RGB24);
    tiffDevice.setResolution(300);
    gs.setOutputDevice(tiffDevice);

    job.attach(new StreamInputNode()//
      .appendSuccessor(gs)//
      .appendSuccessor(new StreamOutputNode()));
  }

  public String getDescription() {
    return "Convert to TIFF via GhostScript";
  }

  public String getID() {
    return "x2tiff (gs)";
  }
}
