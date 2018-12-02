package com.sap.engine.services.webservices.jaxr.infomodel;

import com.sap.engine.services.webservices.jaxr.JAXRNestedException;

import javax.xml.registry.Connection;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import java.net.PasswordAuthentication;
import java.util.Set;

public class AssociationImpl extends RegistryObjectImpl implements Association {
  private RegistryObject sourceObject;
  private RegistryObject targetObject;
  private Concept associationType;
  private boolean confirmedSource;
  private boolean confirmedTarget;
  
  public AssociationImpl(Connection con) {
    super(con);
    confirmedSource = false;
    confirmedTarget = false;
  }

  public void confirm() throws JAXRException, InvalidRequestException {
    if ((!isExtramural()) || (isConfirmed())) {
      return;
    }
    try {
      if (this.getSubmittingOrganization().getName().getValue().equals(sourceObject.getSubmittingOrganization().getName().getValue())) {
        confirmedSource = true;
        return;
      } else if (this.getSubmittingOrganization().getName().getValue().equals(targetObject.getSubmittingOrganization().getName().getValue())) {
        confirmedTarget = true;
        return;
      }
    } catch (Exception e) {
      throw new JAXRNestedException(e);
    }    
    throw new InvalidRequestException("The user associated with this Association is not owner either of the sourceObject nor the targetObject");
  }
  
  public Concept getAssociationType() throws JAXRException {
    return associationType;
  }

  public RegistryObject getSourceObject() throws JAXRException {
    return sourceObject;
  }

  public RegistryObject getTargetObject() throws JAXRException {
    return targetObject;
  }

  public boolean isConfirmed() throws JAXRException {
    return (isConfirmedBySourceOwner() && isConfirmedByTargetOwner());
  }

  public boolean isConfirmedBySourceOwner() throws JAXRException {
    if (!isExtramural()) {
      return true;
    }
    return confirmedSource;
  }
  
  public boolean isConfirmedByTargetOwner() throws JAXRException {
    if (!isExtramural()) {
      return true;
    }
    return confirmedTarget;
  }

  public boolean isExtramural() throws JAXRException {
    try {
      Set associationCreds = this.getConnection().getCredentials();
      Set sourceCreds = ((RegistryObjectImpl) sourceObject).getConnection().getCredentials();
      Set targetCreds = ((RegistryObjectImpl) targetObject).getConnection().getCredentials();

      String associationUser = "", sourceUser = "", targetUser = "";

      if (associationCreds.size() > 0) {
        PasswordAuthentication auth = (PasswordAuthentication) associationCreds.iterator().next();
        associationUser = auth.getUserName();
      }

      if (sourceCreds.size() > 0) {
        PasswordAuthentication auth = (PasswordAuthentication) sourceCreds.iterator().next();
        sourceUser = auth.getUserName();
      }

      if (targetCreds.size() > 0) {
        PasswordAuthentication auth = (PasswordAuthentication) targetCreds.iterator().next();
        targetUser = auth.getUserName();
      }

      if (!sourceUser.equals(associationUser) || !targetUser.equals(associationUser)) {
        return true;
      } else {
        return false;
      }
    } catch (Exception ex) {
      throw new JAXRNestedException(ex);
    }
  }

  public void setAssociationType(Concept associationType) throws JAXRException {
    this.associationType = associationType;
  }

  public void setSourceObject(RegistryObject srcObject) throws JAXRException {
    if (!(srcObject instanceof Organization)) {
      throw new JAXRException("For a UDDI provider an Association may only be created between Organizations");
    }

    sourceObject = srcObject;
  }
  
  
  public void setTargetObject(RegistryObject targetObject) throws JAXRException {
    if (!(targetObject instanceof Organization)) {
      throw new JAXRException("For a UDDI provider an Association may only be created between Organizations");
    }
    this.targetObject = targetObject;
  }

  public void unConfirm() throws JAXRException, InvalidRequestException {
    if ((!isExtramural()) || (isConfirmed())) {
      return;
    }
    try {
      if (this.getSubmittingOrganization().getName().toString().equals(sourceObject.getSubmittingOrganization().getName().toString())) {
        confirmedSource = true;
        return;
      } else if (this.getSubmittingOrganization().getName().toString().equals(targetObject.getSubmittingOrganization().getName().toString())) {
        confirmedTarget = true;
        return;
      } 
    } catch (Exception e) {
      throw new JAXRNestedException(e);
    } 
    throw new InvalidRequestException("The user associated with this Association is not owner either of the sourceObject nor the targetObject");
  }
}