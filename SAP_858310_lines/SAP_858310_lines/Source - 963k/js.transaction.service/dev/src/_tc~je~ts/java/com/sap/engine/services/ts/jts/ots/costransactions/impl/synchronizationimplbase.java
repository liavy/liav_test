package com.sap.engine.services.ts.jts.ots.CosTransactions.impl;

import java.util.Hashtable;

public abstract class SynchronizationImplBase extends org.omg.CORBA.DynamicImplementation implements org.omg.CosTransactions.Synchronization {

  static final long serialVersionUID = -114198772084458396L;

  // Constructor
  public SynchronizationImplBase() {
    super();
  }

  // Type strings for this class and its superclases
  private static final String _type_ids[] = {"IDL:omg.org/CosTransactions/Synchronization:1.0", "IDL:omg.org/CosTransactions/TransactionalObject:1.0"};

  public String[] _ids() {
    return (String[]) _type_ids.clone();
  }

  private static Hashtable _methods = new Hashtable();

  static {
    _methods.put("before_completion", new java.lang.Integer(0));
    _methods.put("after_completion", new java.lang.Integer(1));
  }

  // DSI Dispatch call
  public void invoke(org.omg.CORBA.ServerRequest r) {
    switch (((java.lang.Integer) _methods.get(r.operation())).intValue()) {
      case 0: // org.omg.CosTransactions.Synchronization.before_completion
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          this.before_completion();
          org.omg.CORBA.Any __return = _orb().create_any();
          __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
          r.set_result(__return);
        }

        break;
      }
      case 1: // org.omg.CosTransactions.Synchronization.after_completion
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          org.omg.CORBA.Any _status = _orb().create_any();
          _status.type(org.omg.CosTransactions.StatusHelper.type());
          _list.add_value("status", _status, org.omg.CORBA.ARG_IN.value);
          r.arguments(_list);
          org.omg.CosTransactions.Status status;
          status = org.omg.CosTransactions.StatusHelper.extract(_status);
          this.after_completion(status);
          org.omg.CORBA.Any __return = _orb().create_any();
          __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
          r.set_result(__return);
        }

        break;
      }
      default: {
        throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
      }
    }
  }

}

