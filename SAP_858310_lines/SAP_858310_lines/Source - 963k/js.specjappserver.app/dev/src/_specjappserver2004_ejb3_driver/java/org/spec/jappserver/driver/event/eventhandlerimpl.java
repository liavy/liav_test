package org.spec.jappserver.driver.event;

import java.util.ArrayList;
import java.util.List;

public class EventHandlerImpl
      implements EventHandler
{
   private static final String DRIVER_EVENT_SYSTEM_PARAM_NAME = "driverEventClasses";
   private List<EventHandler> eventHandlers = new ArrayList<EventHandler>();
   
   public EventHandlerImpl()
   {
      String eventClassesNamesStr = System.getProperty(DRIVER_EVENT_SYSTEM_PARAM_NAME);
      if (eventClassesNamesStr == null)
      {
         return;
      }

      String[] eventClassesNames = eventClassesNamesStr.split(",");
      for (String str : eventClassesNames)
      {
         Class<EventHandler> eventHandlerClass;
         try
         {
            eventHandlerClass = (Class<EventHandler>)Class.forName(str);
            eventHandlers.add(eventHandlerClass.newInstance());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   public void rampUpStart ()
   {
      for (EventHandler eventHandler : eventHandlers)
      {
         eventHandler.rampUpStart();
      }
   }

   public void rampUpEnd ()
   {
      for (EventHandler eventHandler : eventHandlers)
      {
         eventHandler.rampUpEnd();
      }
   }

   public void steadyStateStart ()
   {
      for (EventHandler eventHandler : eventHandlers)
      {
         eventHandler.steadyStateStart();
      }
   }

   public void steadyStateEnd ()
   {
      for (EventHandler eventHandler : eventHandlers)
      {
         eventHandler.steadyStateEnd();
      }
   }

   public void rampDownStart ()
   {
      for (EventHandler eventHandler : eventHandlers)
      {
         eventHandler.rampDownStart();
      }
   }

   public void rampDownEnd ()
   {
      for (EventHandler eventHandler : eventHandlers)
      {
         eventHandler.rampDownEnd();
      }
   }
}
