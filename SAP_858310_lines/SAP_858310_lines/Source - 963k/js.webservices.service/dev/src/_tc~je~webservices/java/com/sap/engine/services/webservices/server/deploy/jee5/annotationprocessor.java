package com.sap.engine.services.webservices.server.deploy.jee5;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.lib.javalang.annotation.AnnotationRecord;
import com.sap.lib.javalang.annotation.impl.AnnotationNamedMember;
import com.sap.lib.javalang.tool.ReadResult;
import com.sap.lib.javalang.tool.ReaderFactory;


public class AnnotationProcessor
{
  //TODO Check what is returned by getTypeName() method. 
  public static PropertyType[] getProperties(AnnotationRecord annotation) {
    Set<PropertyType> properties = getProperties_(annotation); 
    return properties.toArray(new PropertyType[properties.size()]);
  }
  
  private static Set<PropertyType> getProperties_(AnnotationRecord annotation)
  {
	if(annotation.getTypeName().equals(AnnotationConstants.XIENABLED_DT))
	{
      return processXIEnabled(annotation);
	}
    if(annotation.getTypeName().equals(AnnotationConstants.AUTHENTICATION_DT))
    {
      return processAuthenticationDT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.BLOCKING_DT_OPERATION))
    {
      return processBlockingDTOperation(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.COMMIT_HANDLING_DT_OPERATION))
    {
      return processCommitHandlingDTOperation(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT))
    {
      return processInterfaceAndOperationNamingDT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION))
    {
      return processInterfaceAndOperationNamingDTOperation(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.MESSAGE_ID_DT))
    {
      return processMessageIDDT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.REL_MESSAGING_NW05_DT_OPERATION))
    {
      return processRelMessagingNW05DTOperation(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION))
    {
      return processRuntimeIntrinsicFunctionsDTOperation(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.SESSION_HANDLING_DT))
    {
      return processSessionHandlingDT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION))
    {
      return processTransactionHandlingNW05DTOperation(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.TRANSPORT_GUARANTEE_DT))
    {
      return processTransportGuaranteeDT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.WS_ADDRESSING_DT))
    {
      return processWSAddressingDT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.WS_ADDRESSING_DT_OPERATION))
    {
      
      return processWSAddressingDTOperation(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.CENTRAL_ADMINISTRATION_RT))
    {
      return processCentralAdministrationRT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION))
    {
      return processTransportGuaranteeRTOperation(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.AUTHENTICATION_RT))
    {
      return processAuthenticationRT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.WS_ADDRESSING_RT))
    {
      return processWSAddressingRT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.REL_MESSAGING_RT))
    {
      return processRelMessagingRT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.MESSAGE_ID_RT))
    {
      return processMessageIDRT(annotation);
    }
 
    if(annotation.getTypeName().equals(AnnotationConstants.SESSION_HANDLING_RT))
    {
      return processSessionHandlingRT(annotation);
    }

    if(annotation.getTypeName().equals(AnnotationConstants.TRANSPORT_BINDING_RT))
    {
      return processTransportBindingRT(annotation);
    }

//    if(annotation.getTypeName().equals(AnnotationConstants.TRANSPORT_BINDING_RT_OPERATION))
//    {
//      return processTransportBindingRTOperation(annotation);
//    }

//    if(annotation.getTypeName().equals(AnnotationConstants.XI_30_RUNTIME_INTEGRATION_RT))
//    {
//      return processXI30RuntimeIntegrationRT(annotation);
//    }

//    if(annotation.getTypeName().equals(AnnotationConstants.ATTACHMENT_HANDLING_RT))
//    {
//      return processAttachmentHandlingRT(annotation);
//    }

//    if(annotation.getTypeName().equals(AnnotationConstants.EXTERNAL_ASSERTION_RT))
//    {
//      return processExternalAssertionRT(annotation);
//    }

    if(annotation.getTypeName().equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT))
    {
      return processTransportGuaranteeRT(annotation);
    }
    
    return new HashSet<PropertyType>();
  }

  private static Set<PropertyType> processXIEnabled(AnnotationRecord annotation)
  {
	Set<PropertyType> result = new HashSet<PropertyType>();
	
	result.add(createProperty(PropertyConstants.XIENABLED_DT_NAMESPACE, PropertyConstants.XIENABLED_DT_PROPERTY_SOAPAPPLICATION, 
			PropertyConstants.XIENABLED_DT_PROPERTY_SOAPAPPLICATION_DEFAULT));

	return result;
  }
  
  private static Set<PropertyType> processAuthenticationDT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL) == null)
    {
    	result.add(createProperty(PropertyConstants.AUTHENTICATION_DT_NAMESPACE, PropertyConstants.AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL, PropertyConstants.AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL_DEFAULT));    	
    }
    else
    {
    	//((AnnotationNamedMember.EnumConstantImpl)annotation.getMember(AnnotationConstants.AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL).getMemberValue()).getEnumerationLiteral()
    	if(((AnnotationNamedMember)annotation.getMember(AnnotationConstants.AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL)).getEnumConstantValue().
    			getEnumerationLiteral().toString().equals(AnnotationConstants.AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL_NONE))
    	{
    		result.add(createProperty(PropertyConstants.AUTHENTICATION_DT_NAMESPACE, PropertyConstants.AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL, 
    				PropertyConstants.AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL_NONE));
    	}
    	if(((AnnotationNamedMember)annotation.getMember(AnnotationConstants.AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL)).getEnumConstantValue().
    			getEnumerationLiteral().toString().equals(AnnotationConstants.AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL_BASIC))
    	{
    		result.add(createProperty(PropertyConstants.AUTHENTICATION_DT_NAMESPACE, PropertyConstants.AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL, 
    				PropertyConstants.AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL_BASIC));
    	}
    	if(((AnnotationNamedMember)annotation.getMember(AnnotationConstants.AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL)).getEnumConstantValue().
    			getEnumerationLiteral().toString().equals(AnnotationConstants.AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL_STRONG))
    	{
    		result.add(createProperty(PropertyConstants.AUTHENTICATION_DT_NAMESPACE, PropertyConstants.AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL, 
    				PropertyConstants.AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL_STRONG));
    	}
    }
    
    return result;
  }

  private static Set<PropertyType> processBlockingDTOperation(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();

//	if(annotation.getMember(AnnotationConstants.BLOCKING_DT_OPERATION_ATTRIBUTE_ENABLEBLOCKING) == null)
//	{
//	      result.add(createProperty(PropertyConstants.BLOCKING_DT_OPERATION_NAMESPACE, PropertyConstants.BLOCKING_DT_OPERATION_PROPERTY_ENABLEBLOCKING, 
//	    		  PropertyConstants.BLOCKING_DT_OPERATION_PROPERTY_ENABLEBLOCKING_DEFAULT));
//	}
//	else
//	{
//	    boolean value = annotation.getMember(AnnotationConstants.BLOCKING_DT_OPERATION_ATTRIBUTE_ENABLEBLOCKING).getBooleanValue();
    
//	    if(value != AnnotationConstants.BLOCKING_DT_OPERATION_ATTRIBUTE_ENABLEBLOCKING_DEFAULT)
//	    {
	      result.add(createProperty(PropertyConstants.BLOCKING_DT_OPERATION_NAMESPACE, PropertyConstants.BLOCKING_DT_OPERATION_PROPERTY_ENABLEBLOCKING, 
	    		  PropertyConstants.BLOCKING_DT_OPERATION_PROPERTY_ENABLEBLOCKING_TRUE));
//	    }
//	}

    return result;
  }
  
  private static Set<PropertyType> processCommitHandlingDTOperation(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.COMMIT_HANDLING_DT_OPERATION_ATTRIBUTE_ENABLECOMMIT) == null)
    {
        result.add(createProperty(PropertyConstants.COMMIT_HANDLING_DT_OPERATION_NAMESPACE, PropertyConstants.COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT, PropertyConstants.COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT_DEFAULT));    	
    }
    else
    {
    	
    	if(((AnnotationNamedMember)annotation.getMember(AnnotationConstants.COMMIT_HANDLING_DT_OPERATION_ATTRIBUTE_ENABLECOMMIT)).getEnumConstantValue().
    			getEnumerationLiteral().toString().equals(AnnotationConstants.COMMIT_HANDLING_DT_OPERATION_ATTRIBUTE_ENABLECOMMIT_DEFAULT))
    	{
            result.add(createProperty(PropertyConstants.COMMIT_HANDLING_DT_OPERATION_NAMESPACE, PropertyConstants.COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT, 
            		PropertyConstants.COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT_TRUE));
    	}
    	else
    	{
            result.add(createProperty(PropertyConstants.COMMIT_HANDLING_DT_OPERATION_NAMESPACE, PropertyConstants.COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT, 
            		PropertyConstants.COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT_FALSE));
    	}
    	
    	
//        if(!value.equals(AnnotationConstants.COMMIT_HANDLING_DT_OPERATION_ATTRIBUTE_ENABLECOMMIT_DEFAULT))
//       {
//          result.add(createProperty(PropertyConstants.COMMIT_HANDLING_DT_OPERATION_NAMESPACE, PropertyConstants.COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT, value));
//        }
    }
    
    return result;
  }

  private static Set<PropertyType> processInterfaceAndOperationNamingDT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_PROGRAM) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_PROGRAM, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_PROGRAM_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_PROGRAM).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_PROGRAM_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_PROGRAM, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SERVICEDEFINITION) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SERVICEDEFINITION, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SERVICEDEFINITION_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SERVICEDEFINITION).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SERVICEDEFINITION_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SERVICEDEFINITION, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SOAPAPPLICATION) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SOAPAPPLICATION, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SOAPAPPLICATION_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SOAPAPPLICATION).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SOAPAPPLICATION_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SOAPAPPLICATION, value));
//        }
    }

    return result;
  }

  private static Set<PropertyType> processInterfaceAndOperationNamingDTOperation(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_INTERNALNAME) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_INTERNALNAME, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_INTERNALNAME_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_INTERNALNAME).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_INTERNALNAME_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_INTERNALNAME, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_MAPPEDNAME) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_MAPPEDNAME, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_MAPPEDNAME_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_MAPPEDNAME).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_MAPPEDNAME_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_MAPPEDNAME, value));
//        }    	
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAME) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAME, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAME_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAME).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAME_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAME, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAMESPACE) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAMESPACE_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAMESPACE).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAMESPACE_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAMESPACE, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAME) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAME, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAME_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAME).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAME_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAME, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAMESPACE) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAMESPACE_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAMESPACE).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAMESPACE_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAMESPACE, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAME) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAME, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAME_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAME).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAME_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAME, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAMESPACE) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAMESPACE_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAMESPACE).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAMESPACE_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAMESPACE, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAME) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAME, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAME_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAME).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAME_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAME, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAMESPACE) == null)
    {
        result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAMESPACE_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAMESPACE).getStringValue();
//        if(!value.equals(AnnotationConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAMESPACE_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE, PropertyConstants.INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAMESPACE, value));
//        }
    }

    return result;
  }

  private static Set<PropertyType> processMessageIDDT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();

    if(annotation.getMember(AnnotationConstants.MESSAGE_ID_DT_ATTRIBUTE_ENABLEMESSAGEID) == null)
    {
        result.add(createProperty(PropertyConstants.MESSAGE_ID_DT_NAMESPACE, PropertyConstants.MESSAGE_ID_DT_PROPERTY_ENABLEMESSAGEID, 
        		PropertyConstants.MESSAGE_ID_DT_PROPERTY_ENABLEMESSAGEID_DEFAULT));
    }
    else
    {
        boolean value = annotation.getMember(AnnotationConstants.MESSAGE_ID_DT_ATTRIBUTE_ENABLEMESSAGEID).getBooleanValue();
//        if(value != AnnotationConstants.MESSAGE_ID_DT_ATTRIBUTE_ENABLEMESSAGEID_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.MESSAGE_ID_DT_NAMESPACE, PropertyConstants.MESSAGE_ID_DT_PROPERTY_ENABLEMESSAGEID, new Boolean(value).toString()));
//        }
    }

    return result;
  }

  private static Set<PropertyType> processRelMessagingNW05DTOperation(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();

    if(annotation.getMember(AnnotationConstants.REL_MESSAGING_NW05_DT_OPERATION_ATTRIBUTE_ENABLEWSRM) == null)
    {
        result.add(createProperty(PropertyConstants.REL_MESSAGING_NW05_DT_OPERATION_NAMESPACE, PropertyConstants.REL_MESSAGING_NW05_DT_OPERATION_PROPERTY_ENABLEWSRM, 
        		PropertyConstants.REL_MESSAGING_NW05_DT_OPERATION_PROPERTY_ENABLEWSRM_DEFAULT));
    }
    else
    {
        boolean value = annotation.getMember(AnnotationConstants.REL_MESSAGING_NW05_DT_OPERATION_ATTRIBUTE_ENABLEWSRM).getBooleanValue();
//        if(value != AnnotationConstants.REL_MESSAGING_NW05_DT_OPERATION_ATTRIBUTE_ENABLEWSRM_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.REL_MESSAGING_NW05_DT_OPERATION_NAMESPACE, PropertyConstants.REL_MESSAGING_NW05_DT_OPERATION_PROPERTY_ENABLEWSRM, new Boolean(value).toString()));
//        }
    }

    return result;
  }

  private static Set<PropertyType> processRuntimeIntrinsicFunctionsDTOperation(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_ATTRIBUTE_MEP) == null)
    {
        result.add(createProperty(PropertyConstants.RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_PROPERTY_MEP, PropertyConstants.RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_PROPERTY_MEP_DEFAULT));    	
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_ATTRIBUTE_MEP).getStringValue();
//        if(!value.equals(AnnotationConstants.RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_ATTRIBUTE_MEP_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_NAMESPACE, PropertyConstants.RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_PROPERTY_MEP, value));
//        }
    }
    
    return result;
  }

  private static Set<PropertyType> processSessionHandlingDT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();

    if(annotation.getMember(AnnotationConstants.SESSION_HANDLING_DT_ATTRIBUTE_ENABLESESSION) == null)
    {
        result.add(createProperty(PropertyConstants.SESSION_HANDLING_DT_NAMESPACE, PropertyConstants.SESSION_HANDLING_DT_PROPERTY_ENABLESESSION, 
        		PropertyConstants.SESSION_HANDLING_DT_PROPERTY_ENABLESESSION_DEFAULT));
    }
    else
    {
        boolean value = annotation.getMember(AnnotationConstants.SESSION_HANDLING_DT_ATTRIBUTE_ENABLESESSION).getBooleanValue();
//        if(value != AnnotationConstants.SESSION_HANDLING_DT_ATTRIBUTE_ENABLESESSION_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.SESSION_HANDLING_DT_NAMESPACE, PropertyConstants.SESSION_HANDLING_DT_PROPERTY_ENABLESESSION, new Boolean(value).toString()));
//        }
    }

    return result;
  }

  private static Set<PropertyType> processTransactionHandlingNW05DTOperation(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();

    if(annotation.getMember(AnnotationConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_ATTRIBUTE_REQUIRED) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_NAMESPACE, PropertyConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_PROPERTY_REQUIRED, 
        		PropertyConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_PROPERTY_REQUIRED_DEFAULT));
    }
    else
    {
        boolean value = annotation.getMember(AnnotationConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_ATTRIBUTE_REQUIRED).getBooleanValue();
//        if(value != AnnotationConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_ATTRIBUTE_REQUIRED_DEFAULT)
//        {
          PropertyType property = new PropertyType();
          property.setNamespace(PropertyConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_NAMESPACE);
          property.setName(PropertyConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_PROPERTY_REQUIRED);
          if(value)
          {
            property.set_value(AnnotationConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_ATTRIBUTE_REQUIRED_YES);
          }
          else
          {
            property.set_value(AnnotationConstants.TRANSACTION_HANDLING_NW05_DT_OPERATION_ATTRIBUTE_REQUIRED_NO);
          }
          
          result.add(property);
//        }
    }

    return result;
  }

  private static Set<PropertyType> processTransportGuaranteeDT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_DT_NAMESPACE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL, PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_DEFAULT));
    }
    else
    {
    	if(((AnnotationNamedMember)annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL)).getEnumConstantValue().
    			getEnumerationLiteral().toString().equals(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_NONE))
    	{
    		result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_DT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL, 
    				PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_NONE));
    	}
    	if(((AnnotationNamedMember)annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL)).getEnumConstantValue().
    			getEnumerationLiteral().toString().equals(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_INTEGRITY))
    	{
    		result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_DT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL, 
    				PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_BOTH));
    	}
    	if(((AnnotationNamedMember)annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL)).getEnumConstantValue().
    			getEnumerationLiteral().toString().equals(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_CONFIDENTIALITY))
    	{
    		result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_DT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL, 
    				PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_BOTH));
    	}
    	if(((AnnotationNamedMember)annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL)).getEnumConstantValue().
    			getEnumerationLiteral().toString().equals(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_BOTH))
    	{
    		result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_DT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL, 
    				PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_BOTH));
    	}
    	if(((AnnotationNamedMember)annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL)).getEnumConstantValue().
    			getEnumerationLiteral().toString().equals(AnnotationConstants.TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_INTEGRITY_AND_CONFIDENTIALITY))
    	{
    		result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_DT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL, 
    				PropertyConstants.TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_INTEGRITY_AND_CONFIDENTIALITY));
    	}
    }

    return result;
  }

  private static Set<PropertyType> processWSAddressingDT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();

    if(annotation.getMember(AnnotationConstants.WS_ADDRESSING_DT_ATTRIBUTE_ENABLED) == null)
    {
        result.add(createProperty(PropertyConstants.WS_ADDRESSING_DT_NAMESPACE, PropertyConstants.WS_ADDRESSING_DT_PROPERTY_ENABLED, 
        		PropertyConstants.WS_ADDRESSING_DT_PROPERTY_ENABLED_DEFAULT));
    }
    else
    {
        boolean value = annotation.getMember(AnnotationConstants.WS_ADDRESSING_DT_ATTRIBUTE_ENABLED).getBooleanValue();
        //if(value != AnnotationConstants.WS_ADDRESSING_DT_ATTRIBUTE_ENABLED_DEFAULT)
        //if(value)
        //{
          result.add(createProperty(PropertyConstants.WS_ADDRESSING_DT_NAMESPACE, PropertyConstants.WS_ADDRESSING_DT_PROPERTY_ENABLED, new Boolean(value).toString()));
        //}
    }

    return result;
  }
  
  private static Set<PropertyType> processWSAddressingDTOperation(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_INPUTACTION) == null)
    {
        result.add(createProperty(PropertyConstants.WS_ADDRESSING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.WS_ADDRESSING_DT_OPERATION_PROPERTY_INPUTACTION, PropertyConstants.WS_ADDRESSING_DT_OPERATION_PROPERTY_INPUTACTION_DEFAULT));    	
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_INPUTACTION).getStringValue();
//        if(!value.equals(AnnotationConstants.WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_INPUTACTION_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.WS_ADDRESSING_DT_OPERATION_NAMESPACE, PropertyConstants.WS_ADDRESSING_DT_OPERATION_PROPERTY_INPUTACTION, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_OUTPUTACTION) == null)
    {
        result.add(createProperty(PropertyConstants.WS_ADDRESSING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.WS_ADDRESSING_DT_OPERATION_PROPERTY_OUTPUTACTION, PropertyConstants.WS_ADDRESSING_DT_OPERATION_PROPERTY_OUTPUTACTION_DEFAULT));
    }
    else
    {
        String value = annotation.getMember(AnnotationConstants.WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_OUTPUTACTION).getStringValue();
//        if(!value.equals(AnnotationConstants.WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_OUTPUTACTION_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.WS_ADDRESSING_DT_OPERATION_NAMESPACE, PropertyConstants.WS_ADDRESSING_DT_OPERATION_PROPERTY_OUTPUTACTION, value));
//        }
    }

    if(annotation.getMember(AnnotationConstants.WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_FAULTACTION) == null)
    {
        result.add(createProperty(PropertyConstants.WS_ADDRESSING_DT_OPERATION_NAMESPACE, 
        		PropertyConstants.WS_ADDRESSING_DT_OPERATION_PROPERTY_FAULTACTION, PropertyConstants.WS_ADDRESSING_DT_OPERATION_PROPERTY_FAULTACTION_DEFAULT));
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_FAULTACTION).getMemberValue();
        if(!arrValue.equals(AnnotationConstants.WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_FAULTACTION_DEFAULT) && arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.WS_ADDRESSING_DT_OPERATION_NAMESPACE, PropertyConstants.WS_ADDRESSING_DT_OPERATION_PROPERTY_FAULTACTION, arrValue[ari].getStringValue()));
          }
        }
    }

    return result;
  }
  
  private static Set<PropertyType> processCentralAdministrationRT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();

    if(annotation.getMember(AnnotationConstants.CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILENAME) == null)
    {
        result.add(createProperty(PropertyConstants.CENTRAL_ADMINISTRATION_RT_NAMESPACE, 
        		PropertyConstants.CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILENAME, PropertyConstants.CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILENAME_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILENAME).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILENAME_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.CENTRAL_ADMINISTRATION_RT_NAMESPACE, PropertyConstants.CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILENAME, stringValue));
//        }
    }

    if(annotation.getMember(AnnotationConstants.CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILEVERSION) == null)
    {
        result.add(createProperty(PropertyConstants.CENTRAL_ADMINISTRATION_RT_NAMESPACE, 
        		PropertyConstants.CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILEVERSION, PropertyConstants.CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILEVERSION_DEFAULT));
    }
    else
    {
        int intValue = annotation.getMember(AnnotationConstants.CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILEVERSION).getIntValue();
//        if(intValue != AnnotationConstants.CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILEVERSION_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.CENTRAL_ADMINISTRATION_RT_NAMESPACE, PropertyConstants.CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILEVERSION, new Integer(intValue).toString()));
//        }
    }

    return result;
  }

  private static Set<PropertyType> processTransportGuaranteeRTOperation(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT[0]));

        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT[1]));
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART).getMemberValue();
        if(!arrValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT) && arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART, arrValue[ari].getStringValue()));
          }
        }    	
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART,
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART_DEFAULT));
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART).getMemberValue();
        if(!arrValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART_DEFAULT) && arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART, arrValue[ari].getStringValue()));
          }
        }    	
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT[0]));

        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT[1]));
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART).getMemberValue();
        if(!arrValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT) && arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART, arrValue[ari].getStringValue()));
          }
        }    	
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART_DEFAULT));
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART).getMemberValue();
        if(!arrValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART_DEFAULT) && arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART, arrValue[ari].getStringValue()));
          }
        }    	
    }

    return result;
  }
  
  private static Set<PropertyType> processAuthenticationRT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_SINGLESIGNON) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_SINGLESIGNON, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_SINGLESIGNON_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_SINGLESIGNON).getBooleanValue();
////        if(booleanValue != AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_SINGLESIGNON_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_SINGLESIGNON, new Boolean(booleanValue).toString()));
////        }
//    }

    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHOD) == null)
    {
        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHOD, 
        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHOD_DEFAULT));
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHOD).getMemberValue();
        if(arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHOD, arrValue[ari].getStringValue()));
          }
        }
    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD) == null)
//    {
//    	result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//    			PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD, 
//    			PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLISSUER) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLISSUER, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLISSUER_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLISSUER).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLISSUER_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLISSUER, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME, 
//        		PropertyConstants.));
//    }
//    else
//    {
//        stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME_DEFAULT))
//        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME, stringValue));
//        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION).getBooleanValue();
////        if(booleanValue != AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION, new Boolean(booleanValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICUSERNAME) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICUSERNAME, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICUSERNAME_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICUSERNAME).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICUSERNAME_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICUSERNAME, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICPASSWORD) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICPASSWORD, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICPASSWORD_DEFAULT));    	
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICPASSWORD).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICPASSWORD_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICPASSWORD, stringValue));
////        }    	
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREVIEW) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREVIEW, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREVIEW_DEFAULT));    	
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREVIEW).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREVIEW_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREVIEW, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREALIAS) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREALIAS, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREALIAS_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREALIAS).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREALIAS_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREALIAS, stringValue));
////        }    	
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE) == null)
//    {
//        result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE, 
//        		PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.AUTHENTICATION_RT_NAMESPACE, PropertyConstants.AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE, stringValue));
////        }
//    }

    return result;
  }

  private static Set<PropertyType> processWSAddressingRT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.WS_ADDRESSING_RT_ATTRIBUTE_WSAPROTOCOL) == null)
    {
        result.add(createProperty(PropertyConstants.WS_ADDRESSING_RT_NAMESPACE, PropertyConstants.WS_ADDRESSING_RT_PROPERTY_WSAPROTOCOL, 
        		PropertyConstants.WS_ADDRESSING_RT_PROPERTY_WSAPROTOCOL_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.WS_ADDRESSING_RT_ATTRIBUTE_WSAPROTOCOL).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.WS_ADDRESSING_RT_ATTRIBUTE_WSAPROTOCOL_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.WS_ADDRESSING_RT_NAMESPACE, PropertyConstants.WS_ADDRESSING_RT_PROPERTY_WSAPROTOCOL, stringValue));
//        }
    }

//    if(annotation.getMember(AnnotationConstants.WS_ADDRESSING_RT_ATTRIBUTE_REFERENCEPARAMETERS) == null)
//    {
//        result.add(createProperty(PropertyConstants.WS_ADDRESSING_RT_NAMESPACE, PropertyConstants.WS_ADDRESSING_RT_PROPERTY_REFERENCEPARAMETERS, 
//        		PropertyConstants.WS_ADDRESSING_RT_PROPERTY_REFERENCEPARAMETERS_DEFAULT));
//    }
//    else
//    {
//    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.WS_ADDRESSING_RT_ATTRIBUTE_REFERENCEPARAMETERS).getMemberValue();
//        if(!arrValue.equals(AnnotationConstants.WS_ADDRESSING_RT_ATTRIBUTE_REFERENCEPARAMETERS_DEFAULT) && arrValue != null && arrValue.length > 0)
//        {
//          for(int ari = 0; ari < arrValue.length; ari ++)
//          {
//            result.add(createProperty(PropertyConstants.WS_ADDRESSING_RT_NAMESPACE, PropertyConstants.WS_ADDRESSING_RT_PROPERTY_REFERENCEPARAMETERS, arrValue[ari].getStringValue()));
//          }
//        }
//    }
    
    return result;
  }

  private static Set<PropertyType> processRelMessagingRT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_RMPROTOCOL) == null)
    {
        result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_RMPROTOCOL, 
        		PropertyConstants.REL_MESSAGING_RT_PROPERTY_RMPROTOCOL_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_RMPROTOCOL).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_RMPROTOCOL_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_RMPROTOCOL, stringValue));
//        }
    }

    if(annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_RETRANSMISSIONINTERVAL) == null)
    {
        result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_RETRANSMISSIONINTERVAL, 
        		PropertyConstants.REL_MESSAGING_RT_PROPERTY_RETRANSMISSIONINTERVAL_DEFAULT));
    }
    else
    {
        int intValue = annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_RETRANSMISSIONINTERVAL).getIntValue();
//        if(intValue != AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_RETRANSMISSIONINTERVAL_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_RETRANSMISSIONINTERVAL, new Integer(intValue).toString()));
//        }
    }
    
    if(annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_INACTIVITYTIMEOUT) == null)
    {
        result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_INACTIVITYTIMEOUT, 
        		PropertyConstants.REL_MESSAGING_RT_PROPERTY_INACTIVITYTIMEOUT_DEFAULT));
    }
    else
    {
        int intValue = annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_INACTIVITYTIMEOUT).getIntValue();
//        if(intValue != AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_INACTIVITYTIMEOUT_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_INACTIVITYTIMEOUT, new Integer(intValue).toString()));
//        }
    }

    if(annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_ACKINTERVAL) == null)
    {
        result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_ACKINTERVAL, 
        		PropertyConstants.REL_MESSAGING_RT_PROPERTY_ACKINTERVAL_DEFAULT));
    }
    else
    {
        int intValue = annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_ACKINTERVAL).getIntValue();
//        if(intValue != AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_ACKINTERVAL_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_ACKINTERVAL, new Integer(intValue).toString()));
//        }
    }

    if(annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_SEQUENCELIFETIME) == null)
    {
        result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_SEQUENCELIFETIME, 
        		PropertyConstants.REL_MESSAGING_RT_PROPERTY_SEQUENCELIFETIME_DEFAULT));
    }
    else
    {
        int intValue = annotation.getMember(AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_SEQUENCELIFETIME).getIntValue();
//        if(intValue != AnnotationConstants.REL_MESSAGING_RT_ATTRIBUTE_SEQUENCELIFETIME_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.REL_MESSAGING_RT_NAMESPACE, PropertyConstants.REL_MESSAGING_RT_PROPERTY_SEQUENCELIFETIME, new Integer(intValue).toString()));
//        }
    }

    return result;
  }

  private static Set<PropertyType> processMessageIDRT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.MESSAGE_ID_RT_ATTRIBUTE_MESSAGEIDPROTOCOL) == null)
    {
        result.add(createProperty(PropertyConstants.MESSAGE_ID_RT_NAMESPACE, PropertyConstants.MESSAGE_ID_RT_PROPERTY_MESSAGEIDPROTOCOL, 
        		PropertyConstants.MESSAGE_ID_RT_PROPERTY_MESSAGEIDPROTOCOL_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.MESSAGE_ID_RT_ATTRIBUTE_MESSAGEIDPROTOCOL).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.MESSAGE_ID_RT_ATTRIBUTE_MESSAGEIDPROTOCOL_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.MESSAGE_ID_RT_NAMESPACE, PropertyConstants.MESSAGE_ID_RT_PROPERTY_MESSAGEIDPROTOCOL, stringValue));
//        }
    }
    
    return result;
  }

  private static Set<PropertyType> processSessionHandlingRT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
    if(annotation.getMember(AnnotationConstants.SESSION_HANDLING_RT_ATTRIBUTE_SESSIONMETHOD) == null)
    {
        result.add(createProperty(PropertyConstants.SESSION_HANDLING_RT_NAMESPACE, PropertyConstants.SESSION_HANDLING_RT_PROPERTY_SESSIONMETHOD, 
        		PropertyConstants.SESSION_HANDLING_RT_PROPERTY_SESSIONMETHOD_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.SESSION_HANDLING_RT_ATTRIBUTE_SESSIONMETHOD).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.SESSION_HANDLING_RT_ATTRIBUTE_SESSIONMETHOD_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.SESSION_HANDLING_RT_NAMESPACE, PropertyConstants.SESSION_HANDLING_RT_PROPERTY_SESSIONMETHOD, stringValue));
//        }
    }
    
    return result;
  }

  private static Set<PropertyType> processTransportBindingRT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLPATH) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLPATH, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLPATH_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLPATH).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLPATH_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLPATH, stringValue));
////        }
//    }
    
//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLPROTOCOL) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLPROTOCOL, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLPROTOCOL_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLPROTOCOL).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLPROTOCOL_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLPROTOCOL, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLHOST) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLHOST, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLHOST_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLHOST).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLHOST_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLHOST, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLPORT) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLPORT, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLPORT_DEFAULT));
//    }
//    else
//    {
//        int intValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLPORT).getIntValue();
////        if(intValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLPORT_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLPORT, new Integer(intValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLCLIENT) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLCLIENT, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLCLIENT_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLCLIENT).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLCLIENT_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLCLIENT, stringValue));
////        }
//    }
    
//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLLANGUAGE) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLLANGUAGE, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLLANGUAGE_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLLANGUAGE).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_URLLANGUAGE_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_URLLANGUAGE, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYHOST) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYHOST, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYHOST_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYHOST).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYHOST_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYHOST, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPORT) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYPORT, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYPORT_DEFAULT));
//    }
//    else
//    {
//        int intValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPORT).getIntValue();
////        if(intValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPORT_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYPORT, new Integer(intValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYUSER) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYUSER, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYUSER_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYUSER).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYUSER_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYUSER, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPASSWORD) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYPASSWORD, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYPASSWORD_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPASSWORD).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPASSWORD_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_PROXYPASSWORD, stringValue));
////        }
//    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_ALTHOST) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_ALTHOST, 
        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_ALTHOST_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_ALTHOST).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_ALTHOST_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_ALTHOST, stringValue));
//        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPORT) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_ALTPORT, 
        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_ALTPORT_DEFAULT));
    }
    else
    {
        int intValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPORT).getIntValue();
//        if(intValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPORT_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_ALTPORT, new Integer(intValue).toString()));
//        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPATH) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_ALTPATH, 
        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_ALTPATH_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPATH).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPATH_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_ALTPATH, stringValue));
//        }
    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPATH) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CALCPATH, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CALCPATH_DEFAULT));
//    }
//    else
//    {
//        int intValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPATH).getIntValue();
////        if(intValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPATH_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CALCPATH, new Integer(intValue).toString()));
////        }    	
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPROTOCOL) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CALCPROTOCOL, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CALCPROTOCOL_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPROTOCOL).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPROTOCOL_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CALCPROTOCOL, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATION) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_DESTINATION, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_DESTINATION_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATION).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATION_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_DESTINATION, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATIONPATH) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_DESTINATIONPATH, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_DESTINATIONPATH_DEFAULT));
//    }
//    else
//    {
//        int intValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATIONPATH).getIntValue();
////        if(intValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATIONPATH_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_DESTINATIONPATH, new Integer(intValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_LOCALCALL) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_LOCALCALL, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_LOCALCALL_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_LOCALCALL).getBooleanValue();
////        if(booleanValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_LOCALCALL_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_LOCALCALL, new Boolean(booleanValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_STYLE) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_STYLE, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_STYLE_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_STYLE).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_STYLE_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_STYLE, stringValue));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_TYPE) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_TYPE, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_TYPE_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_TYPE).getStringValue();
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_TYPE, stringValue));
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_SERVICESESSIONTIMEOUT) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_SERVICESESSIONTIMEOUT, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_SERVICESESSIONTIMEOUT_DEFAULT));
//    }
//    else
//    {
//        int intValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_SERVICESESSIONTIMEOUT).getIntValue();
////        if(intValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_SERVICESESSIONTIMEOUT_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_SERVICESESSIONTIMEOUT, new Integer(intValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CONSUMERMAXWAITTIME) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CONSUMERMAXWAITTIME, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CONSUMERMAXWAITTIME_DEFAULT));
//    }
//    else
//    {
//        int intValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CONSUMERMAXWAITTIME).getIntValue();
////        if(intValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CONSUMERMAXWAITTIME_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CONSUMERMAXWAITTIME, new Integer(intValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_OPTIMIZEDXMLTRANSFER) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_OPTIMIZEDXMLTRANSFER, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_OPTIMIZEDXMLTRANSFER_DEFAULT));    	
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_OPTIMIZEDXMLTRANSFER).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_OPTIMIZEDXMLTRANSFER_DEFAULT))
//        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_OPTIMIZEDXMLTRANSFER, stringValue));
//        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSREQUEST) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_COMPRESSREQUEST, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_COMPRESSREQUEST_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSREQUEST).getBooleanValue();
////        if(booleanValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSREQUEST_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_COMPRESSREQUEST, new Boolean(booleanValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSRESPONSE) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_COMPRESSRESPONSE, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_COMPRESSRESPONSE_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSRESPONSE).getBooleanValue();
////        if(booleanValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSRESPONSE_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_COMPRESSRESPONSE, new Boolean(booleanValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_KEEPALIVESTATUS) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_KEEPALIVESTATUS, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_KEEPALIVESTATUS_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_KEEPALIVESTATUS).getBooleanValue();
////        if(booleanValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_KEEPALIVESTATUS_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_KEEPALIVESTATUS, new Boolean(booleanValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_SOCKETTIMEOUT) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_SOCKETTIMEOUT, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_SOCKETTIMEOUT_DEFAULT));
//    }
//    else
//    {
//        int intValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_SOCKETTIMEOUT).getIntValue();
////        if(intValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_SOCKETTIMEOUT_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_SOCKETTIMEOUT, new Integer(intValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CHUNKEDREQUEST) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CHUNKEDREQUEST, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CHUNKEDREQUEST_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CHUNKEDREQUEST).getBooleanValue();
////        if(booleanValue != AnnotationConstants.TRANSPORT_BINDING_RT_ATTRIBUTE_CHUNKEDREQUEST_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_PROPERTY_CHUNKEDREQUEST, new Boolean(booleanValue).toString()));
////        }
//    }

    return result;
  }

//  private static Set<PropertyType> processTransportBindingRTOperation(AnnotationRecord annotation)
//  {
//    Set<PropertyType> result = new HashSet<PropertyType>();
//    
//    if(annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_OPERATION_ATTRIBUTE_SOAPACTION) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_OPERATION_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_OPERATION_PROPERTY_SOAPACTION, 
//        		PropertyConstants.TRANSPORT_BINDING_RT_OPERATION_PROPERTY_SOAPACTION_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_BINDING_RT_OPERATION_ATTRIBUTE_SOAPACTION).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_BINDING_RT_OPERATION_ATTRIBUTE_SOAPACTION_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_BINDING_RT_OPERATION_NAMESPACE, PropertyConstants.TRANSPORT_BINDING_RT_OPERATION_PROPERTY_SOAPACTION, stringValue));
////        }
//    }
//
//    return result;
//  }

//  private static Set<PropertyType> processXI30RuntimeIntegrationRT(AnnotationRecord annotation)
//  {
//    Set<PropertyType> result = new HashSet<PropertyType>();
//    
//    if(annotation.getMember(AnnotationConstants.XI_30_RUNTIME_INTEGRATION_RT_ATTRIBUTE_RUNTIMEENVIRONMENT) == null)
//    {
//        result.add(createProperty(PropertyConstants.XI_30_RUNTIME_INTEGRATION_RT_NAMESPACE, PropertyConstants.XI_30_RUNTIME_INTEGRATION_RT_PROPERTY_RUNTIMEENVIRONMENT, 
//        		PropertyConstants.XI_30_RUNTIME_INTEGRATION_RT_PROPERTY_RUNTIMEENVIRONMENT_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.XI_30_RUNTIME_INTEGRATION_RT_ATTRIBUTE_RUNTIMEENVIRONMENT).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.XI_30_RUNTIME_INTEGRATION_RT_ATTRIBUTE_RUNTIMEENVIRONMENT_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.XI_30_RUNTIME_INTEGRATION_RT_NAMESPACE, PropertyConstants.XI_30_RUNTIME_INTEGRATION_RT_PROPERTY_RUNTIMEENVIRONMENT, stringValue));
////        }
//    }
//
//    return result;
//  }
      
//  private static Set<PropertyType> processAttachmentHandlingRT(AnnotationRecord annotation)
//  {
//    Set<PropertyType> result = new HashSet<PropertyType>();
//    
//    if(annotation.getMember(AnnotationConstants.ATTACHMENT_HANDLING_RT_ATTRIBUTE_ENABLED) == null)
//    {
//        result.add(createProperty(PropertyConstants.ATTACHMENT_HANDLING_RT_NAMESPACE, PropertyConstants.ATTACHMENT_HANDLING_RT_PROPERTY_ENABLED, 
//        		PropertyConstants.ATTACHMENT_HANDLING_RT_PROPERTY_ENABLED_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.ATTACHMENT_HANDLING_RT_ATTRIBUTE_ENABLED).getBooleanValue();
////        if(booleanValue != AnnotationConstants.ATTACHMENT_HANDLING_RT_ATTRIBUTE_ENABLED_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.ATTACHMENT_HANDLING_RT_NAMESPACE, PropertyConstants.ATTACHMENT_HANDLING_RT_PROPERTY_ENABLED, new Boolean(booleanValue).toString()));
////        }
//    }
//
//    return result;
//  }

//  private static Set<PropertyType> processExternalAssertionRT(AnnotationRecord annotation)
//  {
//    Set<PropertyType> result = new HashSet<PropertyType>();
//    
//    if(annotation.getMember(AnnotationConstants.EXTERNAL_ASSERTION_RT_ATTRIBUTE_ASSERTIONROOTNAME) == null)
//    {
//        result.add(createProperty(PropertyConstants.EXTERNAL_ASSERTION_RT_NAMESPACE, PropertyConstants.EXTERNAL_ASSERTION_RT_PROPERTY_ASSERTIONROOTNAME, 
//        		PropertyConstants.EXTERNAL_ASSERTION_RT_PROPERTY_ASSERTIONROOTNAME_DEFAULT));
//    }
//    else
//    {
//    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.EXTERNAL_ASSERTION_RT_ATTRIBUTE_ASSERTIONROOTNAME).getMemberValue();
//        if(!arrValue.equals(AnnotationConstants.EXTERNAL_ASSERTION_RT_ATTRIBUTE_ASSERTIONROOTNAME_DEFAULT) && arrValue != null && arrValue.length > 0)
//        {
//          for(int ari = 0; ari < arrValue.length; ari ++)
//          {
//            result.add(createProperty(PropertyConstants.EXTERNAL_ASSERTION_RT_NAMESPACE, PropertyConstants.EXTERNAL_ASSERTION_RT_PROPERTY_ASSERTIONROOTNAME, arrValue[ari].getStringValue()));
//          }
//        }
//    }
//
//    return result;
//  }
      
  private static Set<PropertyType> processTransportGuaranteeRT(AnnotationRecord annotation)
  {
    Set<PropertyType> result = new HashSet<PropertyType>();
    
//    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURITYMECHANISM) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SECURITYMECHANISM, 
//        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SECURITYMECHANISM_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURITYMECHANISM).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURITYMECHANISM_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SECURITYMECHANISM, stringValue));
////        }
//    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_TLSTYPE) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_TLSTYPE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_TLSTYPE_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_TLSTYPE).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_TLSTYPE_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_TLSTYPE, stringValue));
//        }
    }
    
//    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSIGNORESSLSERVERCERTS) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSIGNORESSLSERVERCERTS, 
//        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSIGNORESSLSERVERCERTS_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSIGNORESSLSERVERCERTS).getBooleanValue();
////        if(booleanValue != AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSIGNORESSLSERVERCERTS_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSIGNORESSLSERVERCERTS, new Boolean(booleanValue).toString()));
////        }
//    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, 
//        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW, 
//        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW_DEFAULT));
//    	
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW, stringValue));
////        }
//    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURECONVERSATION) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SECURECONVERSATION, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SECURECONVERSATION_DEFAULT));
    }
    else
    {
        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURECONVERSATION).getBooleanValue();
//        if(booleanValue != AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURECONVERSATION_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SECURECONVERSATION, new Boolean(booleanValue).toString()));
//        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURE) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURE_DEFAULT));
    }
    else
    {
        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURE).getBooleanValue();
//        if(booleanValue != AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURE_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURE, new Boolean(booleanValue).toString()));
//        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW, stringValue));
//        }
    }
    
//    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN, 
//        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN_DEFAULT));    	
//    }
//    else
//    {
//    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN).getMemberValue();
//        if(!arrValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN_DEFAULT) && arrValue != null && arrValue.length > 0)
//        {
//          for(int ari = 0; ari < arrValue.length; ari ++)
//          {
//            result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN, arrValue[ari].getStringValue()));
//          }
//        }
//    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART_DEFAULT[0]));    	

        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART_DEFAULT[1]));    	
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART).getMemberValue();
        if(!arrValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART_DEFAULT) && arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART, arrValue[ari].getStringValue()));
          }
        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGENCRYPTION) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTION, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTION_DEFAULT));
    }
    else
    {
        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGENCRYPTION).getBooleanValue();
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTION, new Boolean(booleanValue).toString()));
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART_DEFAULT));
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART).getMemberValue();
        if(!arrValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART_DEFAULT) && arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART, arrValue[ari].getStringValue()));
          }
        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURE) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURE_DEFAULT));
    }
    else
    {
        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURE).getBooleanValue();
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURE, new Boolean(booleanValue).toString()));
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW, stringValue));
//        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS, stringValue));
//        }
    }
    
    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART_DEFAULT[0]));

        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART_DEFAULT[1]));
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART).getMemberValue();
        if(!arrValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART_DEFAULT) && arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART, arrValue[ari].getStringValue()));
          }
        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTION) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTION, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTION_DEFAULT));
    }
    else
    {
        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTION).getBooleanValue();
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTION, new Boolean(booleanValue).toString()));
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW, stringValue));
//        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS, stringValue));
//        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN, stringValue));
//        }
    }
    
    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART_DEFAULT));
    }
    else
    {
    	AnnotationNamedMember[] arrValue = (AnnotationNamedMember[])annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART).getMemberValue();
        if(!arrValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART_DEFAULT) && arrValue != null && arrValue.length > 0)
        {
          for(int ari = 0; ari < arrValue.length; ari ++)
          {
            result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART, arrValue[ari].getStringValue()));
          }
        }
    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGE) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGE, 
//        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGE_DEFAULT));
//    }
//    else
//    {
//        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGE).getBooleanValue();
////        if(booleanValue != AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGE_DEFAULT)
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGE, new Boolean(booleanValue).toString()));
////        }
//    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGEAGE) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGEAGE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGEAGE_DEFAULT));
    }
    else
    {
        int intValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGEAGE).getIntValue();
//        if(intValue != AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGEAGE_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGEAGE, new Integer(intValue).toString()));
//        }
    }

//    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR) == null)
//    {
//        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR, 
//        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR_DEFAULT));
//    }
//    else
//    {
//        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR).getStringValue();
////        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR_DEFAULT))
////        {
//          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR, stringValue));
////        }
//    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCLUDETIMESTAMP) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCLUDETIMESTAMP, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCLUDETIMESTAMP_DEFAULT));
    }
    else
    {
        boolean booleanValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCLUDETIMESTAMP).getBooleanValue();
//        if(booleanValue != AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCLUDETIMESTAMP_DEFAULT)
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_INCLUDETIMESTAMP, new Boolean(booleanValue).toString()));
//        }
    }

    if(annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_ALGORITHMSUITE) == null)
    {
        result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_ALGORITHMSUITE, 
        		PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_ALGORITHMSUITE_DEFAULT));
    }
    else
    {
        String stringValue = annotation.getMember(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_ALGORITHMSUITE).getStringValue();
//        if(!stringValue.equals(AnnotationConstants.TRANSPORT_GUARANTEE_RT_ATTRIBUTE_ALGORITHMSUITE_DEFAULT))
//        {
          result.add(createProperty(PropertyConstants.TRANSPORT_GUARANTEE_RT_NAMESPACE, PropertyConstants.TRANSPORT_GUARANTEE_RT_PROPERTY_ALGORITHMSUITE, stringValue));
//        }
    }

    return result;
  }    
  
  private static PropertyType createProperty(String nameSpace, String name, String value)
  {
    PropertyType property = new PropertyType();

    property.setNamespace(nameSpace);
    property.setName(name);
    property.set_value(value);
    
    return property;
  }

	public static void main(String[] args) throws Exception 
	{
		ReaderFactory rf = new ReaderFactory();		
		ReadResult rr = rf.getReader().read(new File[]{new File("D:/IDE/DEV_Lkg/2007.06.06/runtime-New_configuration/TestOutsideIn/TestOutsideIn.jar")});

		com.sap.lib.javalang.element.ClassInfo ci = rr.getClass("com.xi.xiveri.source_runtime_ws.SrtTestServiceIn710ImplBean");
		com.sap.lib.javalang.element.MethodInfo[] methods = ci.getMethod("op1OneWayReliable");
		for (com.sap.lib.javalang.element.MethodInfo methodInfo : methods)
		{
			for(AnnotationRecord ar: methodInfo.getAnnotations().values())
			{
				AnnotationProcessor.getProperties(ar);
			}
		}

		Map<String, com.sap.lib.javalang.element.ClassInfo> classes = rr.getClasses();
//			for(ClassInfo ci: rr.getClasses().values())
//			{
//				System.out.println(">>Class: "+ci.getName());
//				for(AnnotationRecord ar: ci.getAnnotations().values())
//				{
//					//System.out.println("     Annotation: "+ar.getTypeName());
//					//System.out.println("        Members: "+ar.getMembersLength());
//					for(NamedMember nm: ar.getNamedMembersMap().values())
//					{
//						System.out.println("             "+nm.getName()+" =  "+nm.getMemberValue());						
//					}
//				}
//			}
			
//			System.out.println("Problems: ");
//			for(String s: rr.getProcessingProblems()){
//				System.out.println(s);
//			}
			
	}


  
  
}