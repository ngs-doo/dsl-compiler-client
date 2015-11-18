package com.dslplatform.plugin;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class Marker {
    IResource resource;

    public Marker(IResource resource){
        this.resource = resource;

    }

    public void reportError(int line, String msg)  {
      try {
           IMarker m = resource.createMarker(IMarker.PROBLEM);
           m.setAttribute(IMarker.LINE_NUMBER, line);
           m.setAttribute(IMarker.MESSAGE, msg);
           m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
           m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
      }
      catch (CoreException e) {
          e.printStackTrace();
      }
    }


}
