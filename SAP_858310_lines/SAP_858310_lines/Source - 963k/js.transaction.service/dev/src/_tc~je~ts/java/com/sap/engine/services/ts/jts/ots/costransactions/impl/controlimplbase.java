/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ts.jts.ots.CosTransactions.impl;

import java.util.Hashtable;

/**
 * Description :
 * ~~~~~~~~~~~~~
 * @author : Iliyan Nenov, ilian.nenov@sap.com
 * @version 1.0
 */
public abstract class ControlImplBase extends org.omg.CORBA.DynamicImplementation implements org.omg.CosTransactions.Control, org.omg.CosTransactions.Coordinator, org.omg.CosTransactions.Terminator, org.omg.CosTransactions.RecoveryCoordinator {

  static final long serialVersionUID = 8962050946834242035L;

  public ControlImplBase() {
    super();
  }

  private static final String _type_ids[] = {"IDL:omg.org/CosTransactions/Control:1.0", "IDL:omg.org/CosTransactions/Coordinator:1.0", "IDL:omg.org/CosTransactions/RecoveryCoordinator:1.0"};

  public String[] _ids() {
    return (String[]) _type_ids.clone();
  }

  private static Hashtable _methods = new Hashtable();

  static {
    _methods.put("get_terminator", new java.lang.Integer(0));
    _methods.put("get_coordinator", new java.lang.Integer(1));
    _methods.put("get_status", new java.lang.Integer(2));
    _methods.put("get_parent_status", new java.lang.Integer(3));
    _methods.put("get_top_level_status", new java.lang.Integer(4));
    _methods.put("is_same_transaction", new java.lang.Integer(5));
    _methods.put("is_related_transaction", new java.lang.Integer(6));
    _methods.put("is_ancestor_transaction", new java.lang.Integer(7));
    _methods.put("is_descendant_transaction", new java.lang.Integer(8));
    _methods.put("is_top_level_transaction", new java.lang.Integer(9));
    _methods.put("hash_transaction", new java.lang.Integer(10));
    _methods.put("hash_top_level_tran", new java.lang.Integer(11));
    _methods.put("register_resource", new java.lang.Integer(12));
    _methods.put("register_synchronization", new java.lang.Integer(13));
    _methods.put("register_subtran_aware", new java.lang.Integer(14));
    _methods.put("rollback_only", new java.lang.Integer(15));
    _methods.put("get_transaction_name", new java.lang.Integer(16));
    _methods.put("create_subtransaction", new java.lang.Integer(17));
    _methods.put("get_txcontext", new java.lang.Integer(18));
    _methods.put("replay_completion", new java.lang.Integer(19));
  }

  public void invoke(org.omg.CORBA.ServerRequest r) {
    switch (((java.lang.Integer) _methods.get(r.operation())).intValue()) {
      // _ControlImplBase
      case 0: // org.omg.CosTransactions.Control.get_terminator
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          org.omg.CosTransactions.Terminator ___result;
          try {
            ___result = this.get_terminator();
          } catch (org.omg.CosTransactions.Unavailable e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.UnavailableHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.TerminatorHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      case 1: // org.omg.CosTransactions.Control.get_coordinator
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          org.omg.CosTransactions.Coordinator ___result;
          try {
            ___result = this.get_coordinator();
          } catch (org.omg.CosTransactions.Unavailable e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.UnavailableHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.CoordinatorHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      // _CoordinatorImplBase
      case 2: // org.omg.CosTransactions.Coordinator.get_status
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          org.omg.CosTransactions.Status ___result;
          ___result = this.get_status();
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.StatusHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      case 3: // org.omg.CosTransactions.Coordinator.get_parent_status
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          org.omg.CosTransactions.Status ___result;
          ___result = this.get_parent_status();
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.StatusHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      case 4: // org.omg.CosTransactions.Coordinator.get_top_level_status
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          org.omg.CosTransactions.Status ___result;
          ___result = this.get_top_level_status();
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.StatusHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      case 5: // org.omg.CosTransactions.Coordinator.is_same_transaction
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          org.omg.CORBA.Any _tc = _orb().create_any();
          _tc.type(org.omg.CosTransactions.CoordinatorHelper.type());
          _list.add_value("tc", _tc, org.omg.CORBA.ARG_IN.value);
          r.arguments(_list);
          org.omg.CosTransactions.Coordinator tc;
          tc = org.omg.CosTransactions.CoordinatorHelper.extract(_tc);
          boolean ___result;
          ___result = this.is_same_transaction(tc);
          org.omg.CORBA.Any __result = _orb().create_any();
          __result.insert_boolean(___result);
          r.set_result(__result);
        }

        break;
      }
      case 6: // org.omg.CosTransactions.Coordinator.is_related_transaction
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          org.omg.CORBA.Any _tc = _orb().create_any();
          _tc.type(org.omg.CosTransactions.CoordinatorHelper.type());
          _list.add_value("tc", _tc, org.omg.CORBA.ARG_IN.value);
          r.arguments(_list);
          org.omg.CosTransactions.Coordinator tc;
          tc = org.omg.CosTransactions.CoordinatorHelper.extract(_tc);
          boolean ___result;
          ___result = this.is_related_transaction(tc);
          org.omg.CORBA.Any __result = _orb().create_any();
          __result.insert_boolean(___result);
          r.set_result(__result);
        }

        break;
      }
      case 7: // org.omg.CosTransactions.Coordinator.is_ancestor_transaction
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          org.omg.CORBA.Any _tc = _orb().create_any();
          _tc.type(org.omg.CosTransactions.CoordinatorHelper.type());
          _list.add_value("tc", _tc, org.omg.CORBA.ARG_IN.value);
          r.arguments(_list);
          org.omg.CosTransactions.Coordinator tc;
          tc = org.omg.CosTransactions.CoordinatorHelper.extract(_tc);
          boolean ___result;
          ___result = this.is_ancestor_transaction(tc);
          org.omg.CORBA.Any __result = _orb().create_any();
          __result.insert_boolean(___result);
          r.set_result(__result);
        }

        break;
      }
      case 8: // org.omg.CosTransactions.Coordinator.is_descendant_transaction
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          org.omg.CORBA.Any _tc = _orb().create_any();
          _tc.type(org.omg.CosTransactions.CoordinatorHelper.type());
          _list.add_value("tc", _tc, org.omg.CORBA.ARG_IN.value);
          r.arguments(_list);
          org.omg.CosTransactions.Coordinator tc;
          tc = org.omg.CosTransactions.CoordinatorHelper.extract(_tc);
          boolean ___result;
          ___result = this.is_descendant_transaction(tc);
          org.omg.CORBA.Any __result = _orb().create_any();
          __result.insert_boolean(___result);
          r.set_result(__result);
        }

        break;
      }
      case 9: // org.omg.CosTransactions.Coordinator.is_top_level_transaction
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          boolean ___result;
          ___result = this.is_top_level_transaction();
          org.omg.CORBA.Any __result = _orb().create_any();
          __result.insert_boolean(___result);
          r.set_result(__result);
        }

        break;
      }
      case 10: // org.omg.CosTransactions.Coordinator.hash_transaction
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          int ___result;
          ___result = this.hash_transaction();
          org.omg.CORBA.Any __result = _orb().create_any();
          __result.insert_ulong(___result);
          r.set_result(__result);
        }

        break;
      }
      case 11: // org.omg.CosTransactions.Coordinator.hash_top_level_tran
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          int ___result;
          ___result = this.hash_top_level_tran();
          org.omg.CORBA.Any __result = _orb().create_any();
          __result.insert_ulong(___result);
          r.set_result(__result);
        }

        break;
      }
      case 12: // org.omg.CosTransactions.Coordinator.register_resource
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          org.omg.CORBA.Any _rr = _orb().create_any();
          _rr.type(org.omg.CosTransactions.ResourceHelper.type());
          _list.add_value("rr", _rr, org.omg.CORBA.ARG_IN.value);
          r.arguments(_list);
          org.omg.CosTransactions.Resource rr;
          rr = org.omg.CosTransactions.ResourceHelper.extract(_rr);
          org.omg.CosTransactions.RecoveryCoordinator ___result;
          try {
            ___result = this.register_resource(rr);
          } catch (org.omg.CosTransactions.Inactive e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.InactiveHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.RecoveryCoordinatorHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      case 13: // org.omg.CosTransactions.Coordinator.register_synchronization
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          org.omg.CORBA.Any _sync = _orb().create_any();
          _sync.type(org.omg.CosTransactions.SynchronizationHelper.type());
          _list.add_value("sync", _sync, org.omg.CORBA.ARG_IN.value);
          r.arguments(_list);
          org.omg.CosTransactions.Synchronization sync;
          sync = org.omg.CosTransactions.SynchronizationHelper.extract(_sync);
          try {
            this.register_synchronization(sync);
          } catch (org.omg.CosTransactions.Inactive e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.InactiveHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          } catch (org.omg.CosTransactions.SynchronizationUnavailable e1) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.SynchronizationUnavailableHelper.insert(_except, e1);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __return = _orb().create_any();
          __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
          r.set_result(__return);
        }

        break;
      }
      case 14: // org.omg.CosTransactions.Coordinator.register_subtran_aware
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          org.omg.CORBA.Any _rr = _orb().create_any();
          _rr.type(org.omg.CosTransactions.SubtransactionAwareResourceHelper.type());
          _list.add_value("rr", _rr, org.omg.CORBA.ARG_IN.value);
          r.arguments(_list);
          org.omg.CosTransactions.SubtransactionAwareResource rr;
          rr = org.omg.CosTransactions.SubtransactionAwareResourceHelper.extract(_rr);
          try {
            this.register_subtran_aware(rr);
          } catch (org.omg.CosTransactions.Inactive e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.InactiveHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          } catch (org.omg.CosTransactions.NotSubtransaction e1) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.NotSubtransactionHelper.insert(_except, e1);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __return = _orb().create_any();
          __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
          r.set_result(__return);
        }

        break;
      }
      case 15: // org.omg.CosTransactions.Coordinator.rollback_only
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          try {
            this.rollback_only();
          } catch (org.omg.CosTransactions.Inactive e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.InactiveHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __return = _orb().create_any();
          __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
          r.set_result(__return);
        }

        break;
      }
      case 16: // org.omg.CosTransactions.Coordinator.get_transaction_name
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          String ___result;
          ___result = this.get_transaction_name();
          org.omg.CORBA.Any __result = _orb().create_any();
          __result.insert_string(___result);
          r.set_result(__result);
        }

        break;
      }
      case 17: // org.omg.CosTransactions.Coordinator.create_subtransaction
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          org.omg.CosTransactions.Control ___result;
          try {
            ___result = this.create_subtransaction();
          } catch (org.omg.CosTransactions.SubtransactionsUnavailable e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.SubtransactionsUnavailableHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          } catch (org.omg.CosTransactions.Inactive e1) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.InactiveHelper.insert(_except, e1);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.ControlHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      case 18: // org.omg.CosTransactions.Coordinator.get_txcontext
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          org.omg.CosTransactions.PropagationContext ___result;
          try {
            ___result = this.get_txcontext();
          } catch (org.omg.CosTransactions.Unavailable e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.UnavailableHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.PropagationContextHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      //_RecoveryCoordinatorImplBase
      case 19: // org.omg.CosTransactions.RecoveryCoordinator.replay_completion
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          org.omg.CORBA.Any _rr = _orb().create_any();
          _rr.type(org.omg.CosTransactions.ResourceHelper.type());
          _list.add_value("rr", _rr, org.omg.CORBA.ARG_IN.value);
          r.arguments(_list);
          org.omg.CosTransactions.Resource rr;
          rr = org.omg.CosTransactions.ResourceHelper.extract(_rr);
          org.omg.CosTransactions.Status ___result;
          try {
            ___result = this.replay_completion(rr);
          } catch (org.omg.CosTransactions.NotPrepared e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.NotPreparedHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.StatusHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      default: {
        throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
      }
    }
  }

}

