package com.sap.engine.services.webservices.jaxr.infomodel;

import java.util.Collection;
import java.util.Locale;
import java.util.Vector;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;

public class InternationalStringImpl implements InternationalString {
  Vector localizedStrings;

  public InternationalStringImpl() {
    localizedStrings = new Vector();
  }
  
  public InternationalStringImpl(String s) throws JAXRException {
    this();
    setValue(s);
  }
  
  public void addLocalizedString(LocalizedString localizedString) throws JAXRException {
    localizedStrings.addElement(localizedString);
  }
  
  public void addLocalizedStrings(Collection localizedStrings) throws JAXRException {
    this.localizedStrings.addAll(localizedStrings);
  }
  
  public LocalizedString getLocalizedString(Locale locale, String charsetName) throws JAXRException {
    for (int i = 0; i < localizedStrings.size(); i++) {
      LocalizedString string = (LocalizedString) localizedStrings.elementAt(i);
      if ( (locale.toString().equalsIgnoreCase(string.getLocale().toString())) &&
        (charsetName.equalsIgnoreCase(string.getCharsetName())) ) {
        return string;
      }
    }
    return null;
  }
  
  public Collection getLocalizedStrings() throws JAXRException {
    return localizedStrings;
  }
  
  public void removeLocalizedString(LocalizedString localizedString) throws JAXRException {
    localizedStrings.remove(localizedString);
  }
  
  public void removeLocalizedStrings(Collection localizedStrings) throws JAXRException {
    this.localizedStrings.removeAll(localizedStrings);
  }

  public void setValue(String s) throws JAXRException {
    setValue(Locale.getDefault(), s);
  }
  
  public void setValue(Locale l, String s) throws JAXRException {
    LocalizedString locString = getLocalizedString(l, LocalizedString.DEFAULT_CHARSET_NAME);

    if (locString == null) {
      locString = new LocalizedStringImpl();
      locString.setLocale(l);
      locString.setValue(s);
      locString.setCharsetName(LocalizedString.DEFAULT_CHARSET_NAME);
      addLocalizedString(locString);
    } else {
      locString.setValue(s);
    }
  }
  
  public String getValue() throws JAXRException {
    return getValue(Locale.getDefault());
  }
  
  public String getValue(Locale l) throws JAXRException {
    LocalizedString string = getLocalizedString(l, LocalizedString.DEFAULT_CHARSET_NAME);
    if (string != null) {
      return string.getValue();
    } else {
      return null;
    }
  }
}