package com.sap.engine.services.webservices.server.deploy.preprocess;

import java.util.Map;

import com.sap.lib.javalang.annotation.impl.AnnotationNamedMember;
import com.sap.lib.javalang.annotation.impl.AnnotationRecordImpl;
import com.sap.lib.javalang.element.impl.ClassInfoImpl;

public class AnnotationTools {
    
    /**
     * 
     * @param annRecord
     * @param attrName
     * @return value of annotation attribute if exists, otherwise empty string
     */
    public static String getAnnotationAttribute(AnnotationRecordImpl annRecord, String attrName)
    {
        Map memberMap = annRecord.getNamedMembersMap();
        String annotationValue = "";
        
        if (memberMap.containsKey(attrName)) {
            annotationValue = ((AnnotationNamedMember)memberMap.get(attrName)).getStringValue();
        }
        return annotationValue;
    }
    
    /**
     * 
     * @param annRecord
     * @return fully qualified class 
     */
    public static String getAnnotationClassName(AnnotationRecordImpl annRecord) {
        ClassInfoImpl elementInfo = (ClassInfoImpl)annRecord.getOwner();
        return elementInfo.getName();
    }
}
