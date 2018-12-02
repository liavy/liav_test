﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Mon May 29 10:14:50 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.sapcontrol.client.types;

/**
 * Schema complexType Java representation.
 * Represents type {urn:SAPControl}WorkProcess
 */
public  class WorkProcess implements java.io.Serializable {

  // Element field for element {}No
  private int _f_No;
  /**
   * Set method for element {}No
   */
  public void setNo(int _No) {
    this._f_No = _No;
  }
  /**
   * Get method for element {}No
   */
  public int getNo() {
    return this._f_No;
  }

  // Element field for element {}Typ
  private java.lang.String _f_Typ;
  /**
   * Set method for element {}Typ
   */
  public void setTyp(java.lang.String _Typ) {
    this._f_Typ = _Typ;
  }
  /**
   * Get method for element {}Typ
   */
  public java.lang.String getTyp() {
    return this._f_Typ;
  }

  // Element field for element {}Pid
  private int _f_Pid;
  /**
   * Set method for element {}Pid
   */
  public void setPid(int _Pid) {
    this._f_Pid = _Pid;
  }
  /**
   * Get method for element {}Pid
   */
  public int getPid() {
    return this._f_Pid;
  }

  // Element field for element {}Status
  private java.lang.String _f_Status;
  /**
   * Set method for element {}Status
   */
  public void setStatus(java.lang.String _Status) {
    this._f_Status = _Status;
  }
  /**
   * Get method for element {}Status
   */
  public java.lang.String getStatus() {
    return this._f_Status;
  }

  // Element field for element {}Reason
  private java.lang.String _f_Reason;
  /**
   * Set method for element {}Reason
   */
  public void setReason(java.lang.String _Reason) {
    this._f_Reason = _Reason;
  }
  /**
   * Get method for element {}Reason
   */
  public java.lang.String getReason() {
    return this._f_Reason;
  }

  // Element field for element {}Start
  private java.lang.String _f_Start;
  /**
   * Set method for element {}Start
   */
  public void setStart(java.lang.String _Start) {
    this._f_Start = _Start;
  }
  /**
   * Get method for element {}Start
   */
  public java.lang.String getStart() {
    return this._f_Start;
  }

  // Element field for element {}Err
  private java.lang.String _f_Err;
  /**
   * Set method for element {}Err
   */
  public void setErr(java.lang.String _Err) {
    this._f_Err = _Err;
  }
  /**
   * Get method for element {}Err
   */
  public java.lang.String getErr() {
    return this._f_Err;
  }

  // Element field for element {}Sem
  private java.lang.String _f_Sem;
  /**
   * Set method for element {}Sem
   */
  public void setSem(java.lang.String _Sem) {
    this._f_Sem = _Sem;
  }
  /**
   * Get method for element {}Sem
   */
  public java.lang.String getSem() {
    return this._f_Sem;
  }

  // Element field for element {}Cpu
  private java.lang.String _f_Cpu;
  /**
   * Set method for element {}Cpu
   */
  public void setCpu(java.lang.String _Cpu) {
    this._f_Cpu = _Cpu;
  }
  /**
   * Get method for element {}Cpu
   */
  public java.lang.String getCpu() {
    return this._f_Cpu;
  }

  // Element field for element {}Time
  private java.lang.String _f_Time;
  /**
   * Set method for element {}Time
   */
  public void setTime(java.lang.String _Time) {
    this._f_Time = _Time;
  }
  /**
   * Get method for element {}Time
   */
  public java.lang.String getTime() {
    return this._f_Time;
  }

  // Element field for element {}Program
  private java.lang.String _f_Program;
  /**
   * Set method for element {}Program
   */
  public void setProgram(java.lang.String _Program) {
    this._f_Program = _Program;
  }
  /**
   * Get method for element {}Program
   */
  public java.lang.String getProgram() {
    return this._f_Program;
  }

  // Element field for element {}Client
  private java.lang.String _f_Client;
  /**
   * Set method for element {}Client
   */
  public void setClient(java.lang.String _Client) {
    this._f_Client = _Client;
  }
  /**
   * Get method for element {}Client
   */
  public java.lang.String getClient() {
    return this._f_Client;
  }

  // Element field for element {}User
  private java.lang.String _f_User;
  /**
   * Set method for element {}User
   */
  public void setUser(java.lang.String _User) {
    this._f_User = _User;
  }
  /**
   * Get method for element {}User
   */
  public java.lang.String getUser() {
    return this._f_User;
  }

  // Element field for element {}Action
  private java.lang.String _f_Action;
  /**
   * Set method for element {}Action
   */
  public void setAction(java.lang.String _Action) {
    this._f_Action = _Action;
  }
  /**
   * Get method for element {}Action
   */
  public java.lang.String getAction() {
    return this._f_Action;
  }

  // Element field for element {}Table
  private java.lang.String _f_Table;
  /**
   * Set method for element {}Table
   */
  public void setTable(java.lang.String _Table) {
    this._f_Table = _Table;
  }
  /**
   * Get method for element {}Table
   */
  public java.lang.String getTable() {
    return this._f_Table;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof WorkProcess)) return false;
    WorkProcess typed = (WorkProcess) object;
    if (this._f_No != typed._f_No) return false;
    if (this._f_Typ != null) {
      if (typed._f_Typ == null) return false;
      if (!this._f_Typ.equals(typed._f_Typ)) return false;
    } else {
      if (typed._f_Typ != null) return false;
    }
    if (this._f_Pid != typed._f_Pid) return false;
    if (this._f_Status != null) {
      if (typed._f_Status == null) return false;
      if (!this._f_Status.equals(typed._f_Status)) return false;
    } else {
      if (typed._f_Status != null) return false;
    }
    if (this._f_Reason != null) {
      if (typed._f_Reason == null) return false;
      if (!this._f_Reason.equals(typed._f_Reason)) return false;
    } else {
      if (typed._f_Reason != null) return false;
    }
    if (this._f_Start != null) {
      if (typed._f_Start == null) return false;
      if (!this._f_Start.equals(typed._f_Start)) return false;
    } else {
      if (typed._f_Start != null) return false;
    }
    if (this._f_Err != null) {
      if (typed._f_Err == null) return false;
      if (!this._f_Err.equals(typed._f_Err)) return false;
    } else {
      if (typed._f_Err != null) return false;
    }
    if (this._f_Sem != null) {
      if (typed._f_Sem == null) return false;
      if (!this._f_Sem.equals(typed._f_Sem)) return false;
    } else {
      if (typed._f_Sem != null) return false;
    }
    if (this._f_Cpu != null) {
      if (typed._f_Cpu == null) return false;
      if (!this._f_Cpu.equals(typed._f_Cpu)) return false;
    } else {
      if (typed._f_Cpu != null) return false;
    }
    if (this._f_Time != null) {
      if (typed._f_Time == null) return false;
      if (!this._f_Time.equals(typed._f_Time)) return false;
    } else {
      if (typed._f_Time != null) return false;
    }
    if (this._f_Program != null) {
      if (typed._f_Program == null) return false;
      if (!this._f_Program.equals(typed._f_Program)) return false;
    } else {
      if (typed._f_Program != null) return false;
    }
    if (this._f_Client != null) {
      if (typed._f_Client == null) return false;
      if (!this._f_Client.equals(typed._f_Client)) return false;
    } else {
      if (typed._f_Client != null) return false;
    }
    if (this._f_User != null) {
      if (typed._f_User == null) return false;
      if (!this._f_User.equals(typed._f_User)) return false;
    } else {
      if (typed._f_User != null) return false;
    }
    if (this._f_Action != null) {
      if (typed._f_Action == null) return false;
      if (!this._f_Action.equals(typed._f_Action)) return false;
    } else {
      if (typed._f_Action != null) return false;
    }
    if (this._f_Table != null) {
      if (typed._f_Table == null) return false;
      if (!this._f_Table.equals(typed._f_Table)) return false;
    } else {
      if (typed._f_Table != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    result+= (int) this._f_No;
    if (this._f_Typ != null) {
      result+= this._f_Typ.hashCode();
    }
    result+= (int) this._f_Pid;
    if (this._f_Status != null) {
      result+= this._f_Status.hashCode();
    }
    if (this._f_Reason != null) {
      result+= this._f_Reason.hashCode();
    }
    if (this._f_Start != null) {
      result+= this._f_Start.hashCode();
    }
    if (this._f_Err != null) {
      result+= this._f_Err.hashCode();
    }
    if (this._f_Sem != null) {
      result+= this._f_Sem.hashCode();
    }
    if (this._f_Cpu != null) {
      result+= this._f_Cpu.hashCode();
    }
    if (this._f_Time != null) {
      result+= this._f_Time.hashCode();
    }
    if (this._f_Program != null) {
      result+= this._f_Program.hashCode();
    }
    if (this._f_Client != null) {
      result+= this._f_Client.hashCode();
    }
    if (this._f_User != null) {
      result+= this._f_User.hashCode();
    }
    if (this._f_Action != null) {
      result+= this._f_Action.hashCode();
    }
    if (this._f_Table != null) {
      result+= this._f_Table.hashCode();
    }
    return result;
  }
}