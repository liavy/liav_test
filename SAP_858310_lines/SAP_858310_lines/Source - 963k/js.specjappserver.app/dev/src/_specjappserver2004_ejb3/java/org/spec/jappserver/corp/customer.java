/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2005/12/22  Bernhard Riedhofer, SAP Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.corp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.spec.jappserver.common.Address;
import org.spec.jappserver.common.CustomerInfo;
import org.spec.jappserver.corp.helper.CustomerDataBean;

/**
 * EJB3 equivalent for CustomerEnt
 */
@Entity
@Table(name = "C_CUSTOMER")
public class Customer
      implements Serializable
{
   private static final long serialVersionUID = 1L;

   @TableGenerator(
         name="customer",
         table="U_SEQUENCES",
         pkColumnName="S_ID",
         valueColumnName="S_NEXTNUM",
         pkColumnValue="customer",
         initialValue=1000000000,
         allocationSize=10000)

   // persistent
   @Id @Column(name = "C_ID")
   @GeneratedValue(strategy=GenerationType.TABLE, generator="customer")
   protected int customerId;

   @Basic @Column(name = "C_SINCE")
   protected Date since;

   @Basic @Column(name = "C_CREDIT")
   protected String credit;

   @Basic @Column(name = "C_FIRST")
   protected String firstName;

   @Basic @Column(name = "C_LAST")
   protected String lastName;

   @Basic @Column(name = "C_CONTACT")
   protected String contact;

   @Basic @Column(name = "C_STREET1")
   protected String street1;

   @Basic @Column(name = "C_STREET2")
   protected String street2;

   @Basic @Column(name = "C_CITY")
   protected String city;

   @Basic @Column(name = "C_STATE")
   protected String state;

   @Basic @Column(name = "C_COUNTRY")
   protected String country;

   @Basic @Column(name = "C_ZIP")
   protected String zip;

   @Basic @Column(name = "C_PHONE")
   protected String phone;

   @Basic @Column(name = "C_BALANCE")
   protected BigDecimal balance;

   @Basic @Column(name = "C_CREDIT_LIMIT")
   protected BigDecimal creditLimit;

   @Basic @Column(name = "C_YTD_PAYMENT")
   protected BigDecimal ytdPayment;

   @Version @Column(name = "C_VERSION")
   private int version;

   // relationship
   @OneToMany(mappedBy = "customer")
   protected Set<CustomerInventory> customerInventory;

   /**
    * Default constructor
    */
   public Customer ()
   {
   }

   /**
    * Constructor
    *
    * @param info CustomerInfo object
    * @see org.spec.jappserver.common.CustomerInfo
    */
   public Customer (CustomerInfo info)
   {
      setCustomerId(info.customerId.intValue());
      setSince(info.since);
      setBalance(info.balance.setScale(2, BigDecimal.ROUND_UP));
      setCredit(info.credit);
      setCreditLimit(info.creditLimit.setScale(2, BigDecimal.ROUND_UP));
      setYtdPayment(info.YtdPayment.setScale(2, BigDecimal.ROUND_UP));
   }

   /**
    * Constructor
    *
    * @param customerId Identifier
    */
   public Customer (int customerId)
   {
      setCustomerId(customerId);
   }

   /**
    * Checks the credit.
    *
    * @param amount Amount
    * @return true if the amount exceeds the credit limit, otherwise false
    */
   public boolean hasSufficientCredit (BigDecimal amount)
   {
      if (getCredit().equals("BC"))
      {
         return false;
      }

      return getCreditLimit().compareTo(amount) >= 0;
   }

   /**
    * Gets the address.
    *
    * @return Address object including all address attributes.
    * @see org.spec.jappserver.common.Address
    */
   public Address getAddress ()
   {
      return new Address(getStreet1(), getStreet2(), getCity(),
            getState(), getCountry(), getZip(), getPhone());
   }

   /**
    * Sets the address.
    *
    * @param address Address object including all address attributes.
    * @see org.spec.jappserver.common.Address
    */
   public void setAddress (Address address)
   {
      setStreet1(address.street1);
      setStreet2(address.street2);
      setCity(address.city);
      setState(address.state);
      setCountry(address.country);
      setZip(address.zip);
      setPhone(address.phone);
   }

   /**
    * Gets a customer object from the type CustomerDataBean.
    *
    * @return CustomerDataBean object.
    * @see org.spec.jappserver.corp.helper.CustomerDataBean
    */
   public CustomerDataBean getDataBean ()
   {
      CustomerDataBean custData = new CustomerDataBean();
      custData.setAccountID(getCustomerId());
      custData.setAccountCreated(getSince());
      custData.setBalance(getBalance());
      custData.setCreditLimit(getCreditLimit());
      custData.setCreditRating(getCredit());
      custData.setLastName(getLastName());
      custData.setFirstName(getFirstName());
      return custData;
   }

   /**
    * Persistent field.
    *
    * @return balance value
    */
   public BigDecimal getBalance ()
   {
      return balance;
   }

   /**
    * Persistent field.
    *
    * @return city value
    */
   public String getCity ()
   {
      return city;
   }

   /**
    * Persistent field.
    *
    * @return contact value
    */
   public String getContact ()
   {
      return contact;
   }

   /**
    * Persistent field.
    *
    * @return country value
    */
   public String getCountry ()
   {
      return country;
   }

   /**
    * Persistent field.
    *
    * @return credit value
    */
   public String getCredit ()
   {
      return credit;
   }

   /**
    * Persistent field.
    *
    * @return creditLimit value
    */
   public BigDecimal getCreditLimit ()
   {
      return creditLimit;
   }

   /**
    * Persistent field.
    *
    * @return customerId value
    */
   public int getCustomerId ()
   {
      return customerId;
   }

   /**
    * Persistent field.
    *
    * @return firstName value
    */
   public String getFirstName ()
   {
      return firstName;
   }

   /**
    * Persistent field.
    *
    * @return lastName value
    */
   public String getLastName ()
   {
      return lastName;
   }

   /**
    * Persistent field.
    *
    * @return phone value
    */
   public String getPhone ()
   {
      return phone;
   }

   /**
    * Persistent field.
    *
    * @return since value
    */
   public Date getSince ()
   {
      return since;
   }

   /**
    * Persistent field.
    *
    * @return state value
    */
   public String getState ()
   {
      return state;
   }

   /**
    * Persistent field.
    *
    * @return street1 value
    */
   public String getStreet1 ()
   {
      return street1;
   }

   /**
    * Persistent field.
    *
    * @return street2 value
    */
   public String getStreet2 ()
   {
      return street2;
   }

   /**
    * Persistent field.
    *
    * @return ytdPayment value
    */
   public BigDecimal getYtdPayment ()
   {
      return ytdPayment;
   }

   /**
    * Persistent field.
    *
    * @return zip value
    */
   public String getZip ()
   {
      return zip;
   }

   /**
    * Persistent field.
    * Version column.
    *
    * @return version
    */
   public int getVersion()
   {
      return version;
   }

   /**
    * Persistent field.
    *
    * @param decimal balance value
    */
   public void setBalance (BigDecimal decimal)
   {
      balance = decimal;
   }

   /**
    * Persistent field.
    *
    * @param string city value
    */
   public void setCity (String string)
   {
      city = string;
   }

   /**
    * Persistent field.
    *
    * @param string contact value
    */
   public void setContact (String string)
   {
      contact = string;
   }

   /**
    * Persistent field.
    *
    * @param string country value
    */
   public void setCountry (String string)
   {
      country = string;
   }

   /**
    * Persistent field.
    *
    * @param string credit value
    */
   public void setCredit (String string)
   {
      credit = string;
   }

   /**
    * Persistent field.
    *
    * @param decimal creditLimit value
    */
   public void setCreditLimit (BigDecimal decimal)
   {
      creditLimit = decimal;
   }

   /**
    * Persistent field.
    *
    * @param i customerId value
    */
   public void setCustomerId (int i)
   {
      customerId = i;
   }

   /**
    * Persistent field.
    *
    * @param string firstName value
    */
   public void setFirstName (String string)
   {
      firstName = string;
   }

   /**
    * Persistent field.
    *
    * @param string lastName value
    */
   public void setLastName (String string)
   {
      lastName = string;
   }

   /**
    * Persistent field.
    *
    * @param string phone value
    */
   public void setPhone (String string)
   {
      phone = string;
   }

   /**
    * Persistent field.
    *
    * @param date since value
    */
   public void setSince (Date date)
   {
      since = date;
   }

   /**
    * Persistent field.
    *
    * @param string state value
    */
   public void setState (String string)
   {
      state = string;
   }

   /**
    * Persistent field.
    *
    * @param string street1 value
    */
   public void setStreet1 (String string)
   {
      street1 = string;
   }

   /**
    * Persistent field.
    *
    * @param string street2 value
    */
   public void setStreet2 (String string)
   {
      street2 = string;
   }

   /**
    * Persistent field.
    *
    * @param decimal ytdPayment value
    */
   public void setYtdPayment (BigDecimal decimal)
   {
      ytdPayment = decimal;
   }

   /**
    * Persistent field.
    *
    * @param string zip value
    */
   public void setZip (String string)
   {
      zip = string;
   }

   /**
    * Persistent field.
    * Version column.
    *
    * @param int version
    */
   public void setVersion(int version)
   {
      this.version = version;
   }

   /**
    * Relationship.
    *
    * @return Set of customerinventories
    */
   public Set<CustomerInventory> getCustomerInventory ()
   {
      return customerInventory;
   }

   /**
    * Relationship.
    *
    * @param customer Set of customerinventories
    */
   public void setCustomerInventory (Set<CustomerInventory> custInv)
   {
      customerInventory = custInv;
   }
}
