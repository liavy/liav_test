package org.spec.jappserver.driver.event;

public interface EventHandler
{
   public void rampUpStart();
   public void rampUpEnd();
   public void steadyStateStart();
   public void steadyStateEnd();
   public void rampDownStart();
   public void rampDownEnd();
}
