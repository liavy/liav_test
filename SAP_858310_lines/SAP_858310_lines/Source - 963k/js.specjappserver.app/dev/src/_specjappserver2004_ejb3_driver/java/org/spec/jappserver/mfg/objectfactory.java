
package org.spec.jappserver.mfg;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.spec.jappserver.mfg package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CompleteWorkOrder_QNAME = new QName("http://mfg.jappserver.spec.org/", "completeWorkOrder");
    private final static QName _UpdateWorkOrderResponse_QNAME = new QName("http://mfg.jappserver.spec.org/", "updateWorkOrderResponse");
    private final static QName _ScheduleLargeWorkOrderResponse_QNAME = new QName("http://mfg.jappserver.spec.org/", "scheduleLargeWorkOrderResponse");
    private final static QName _UpdateWorkOrder_QNAME = new QName("http://mfg.jappserver.spec.org/", "updateWorkOrder");
    private final static QName _CompleteWorkOrderResponse_QNAME = new QName("http://mfg.jappserver.spec.org/", "completeWorkOrderResponse");
    private final static QName _ScheduleWorkOrderResponse_QNAME = new QName("http://mfg.jappserver.spec.org/", "scheduleWorkOrderResponse");
    private final static QName _ScheduleWorkOrder_QNAME = new QName("http://mfg.jappserver.spec.org/", "scheduleWorkOrder");
    private final static QName _ScheduleLargeWorkOrder_QNAME = new QName("http://mfg.jappserver.spec.org/", "scheduleLargeWorkOrder");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.spec.jappserver.mfg
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CompleteWorkOrderType }
     * 
     */
    public CompleteWorkOrderType createCompleteWorkOrderType() {
        return new CompleteWorkOrderType();
    }

    /**
     * Create an instance of {@link CompleteWorkOrderResponseType }
     * 
     */
    public CompleteWorkOrderResponseType createCompleteWorkOrderResponseType() {
        return new CompleteWorkOrderResponseType();
    }

    /**
     * Create an instance of {@link ScheduleWorkOrderResponseType }
     * 
     */
    public ScheduleWorkOrderResponseType createScheduleWorkOrderResponseType() {
        return new ScheduleWorkOrderResponseType();
    }

    /**
     * Create an instance of {@link UpdateWorkOrderType }
     * 
     */
    public UpdateWorkOrderType createUpdateWorkOrderType() {
        return new UpdateWorkOrderType();
    }

    /**
     * Create an instance of {@link ScheduleLargeWorkOrderResponseType }
     * 
     */
    public ScheduleLargeWorkOrderResponseType createScheduleLargeWorkOrderResponseType() {
        return new ScheduleLargeWorkOrderResponseType();
    }

    /**
     * Create an instance of {@link ScheduleWorkOrderType }
     * 
     */
    public ScheduleWorkOrderType createScheduleWorkOrderType() {
        return new ScheduleWorkOrderType();
    }

    /**
     * Create an instance of {@link ScheduleLargeWorkOrderType }
     * 
     */
    public ScheduleLargeWorkOrderType createScheduleLargeWorkOrderType() {
        return new ScheduleLargeWorkOrderType();
    }

    /**
     * Create an instance of {@link UpdateWorkOrderResponseType }
     * 
     */
    public UpdateWorkOrderResponseType createUpdateWorkOrderResponseType() {
        return new UpdateWorkOrderResponseType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteWorkOrderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfg.jappserver.spec.org/", name = "completeWorkOrder")
    public JAXBElement<CompleteWorkOrderType> createCompleteWorkOrder(CompleteWorkOrderType value) {
        return new JAXBElement<CompleteWorkOrderType>(_CompleteWorkOrder_QNAME, CompleteWorkOrderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateWorkOrderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfg.jappserver.spec.org/", name = "updateWorkOrderResponse")
    public JAXBElement<UpdateWorkOrderResponseType> createUpdateWorkOrderResponse(UpdateWorkOrderResponseType value) {
        return new JAXBElement<UpdateWorkOrderResponseType>(_UpdateWorkOrderResponse_QNAME, UpdateWorkOrderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ScheduleLargeWorkOrderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfg.jappserver.spec.org/", name = "scheduleLargeWorkOrderResponse")
    public JAXBElement<ScheduleLargeWorkOrderResponseType> createScheduleLargeWorkOrderResponse(ScheduleLargeWorkOrderResponseType value) {
        return new JAXBElement<ScheduleLargeWorkOrderResponseType>(_ScheduleLargeWorkOrderResponse_QNAME, ScheduleLargeWorkOrderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateWorkOrderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfg.jappserver.spec.org/", name = "updateWorkOrder")
    public JAXBElement<UpdateWorkOrderType> createUpdateWorkOrder(UpdateWorkOrderType value) {
        return new JAXBElement<UpdateWorkOrderType>(_UpdateWorkOrder_QNAME, UpdateWorkOrderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompleteWorkOrderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfg.jappserver.spec.org/", name = "completeWorkOrderResponse")
    public JAXBElement<CompleteWorkOrderResponseType> createCompleteWorkOrderResponse(CompleteWorkOrderResponseType value) {
        return new JAXBElement<CompleteWorkOrderResponseType>(_CompleteWorkOrderResponse_QNAME, CompleteWorkOrderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ScheduleWorkOrderResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfg.jappserver.spec.org/", name = "scheduleWorkOrderResponse")
    public JAXBElement<ScheduleWorkOrderResponseType> createScheduleWorkOrderResponse(ScheduleWorkOrderResponseType value) {
        return new JAXBElement<ScheduleWorkOrderResponseType>(_ScheduleWorkOrderResponse_QNAME, ScheduleWorkOrderResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ScheduleWorkOrderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfg.jappserver.spec.org/", name = "scheduleWorkOrder")
    public JAXBElement<ScheduleWorkOrderType> createScheduleWorkOrder(ScheduleWorkOrderType value) {
        return new JAXBElement<ScheduleWorkOrderType>(_ScheduleWorkOrder_QNAME, ScheduleWorkOrderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ScheduleLargeWorkOrderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfg.jappserver.spec.org/", name = "scheduleLargeWorkOrder")
    public JAXBElement<ScheduleLargeWorkOrderType> createScheduleLargeWorkOrder(ScheduleLargeWorkOrderType value) {
        return new JAXBElement<ScheduleLargeWorkOrderType>(_ScheduleLargeWorkOrder_QNAME, ScheduleLargeWorkOrderType.class, null, value);
    }

}
