package com.sap.engine.services.ts.jts.ots.CosTransactions.impl;

import java.util.Hashtable;

public abstract class ResourceImplBase extends org.omg.CORBA.DynamicImplementation implements org.omg.CosTransactions.Resource {

  static final long serialVersionUID = 2966668962326320577L;

  // Constructor
  public ResourceImplBase() {
    super();
  }

  // Type strings for this class and its superclases
  private static final String _type_ids[] = {"IDL:omg.org/CosTransactions/Resource:1.0"};

  public String[] _ids() {
    return (String[]) _type_ids.clone();
  }

  private static Hashtable _methods = new Hashtable();

  static {
    _methods.put("prepare", new java.lang.Integer(0));
    _methods.put("rollback", new java.lang.Integer(1));
    _methods.put("commit", new java.lang.Integer(2));
    _methods.put("commit_one_phase", new java.lang.Integer(3));
    _methods.put("forget", new java.lang.Integer(4));
  }

  // DSI Dispatch call
  public void invoke(org.omg.CORBA.ServerRequest r) {
    switch (((java.lang.Integer) _methods.get(r.operation())).intValue()) {
      case 0: // org.omg.CosTransactions.Resource.prepare
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          org.omg.CosTransactions.Vote ___result;
          try {
            ___result = this.prepare();
          } catch (org.omg.CosTransactions.HeuristicMixed e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.HeuristicMixedHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          } catch (org.omg.CosTransactions.HeuristicHazard e1) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.HeuristicHazardHelper.insert(_except, e1);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __result = _orb().create_any();
          org.omg.CosTransactions.VoteHelper.insert(__result, ___result);
          r.set_result(__result);
        }

        break;
      }
      case 1: // org.omg.CosTransactions.Resource.rollback
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          try {
            this.rollback();
          } catch (org.omg.CosTransactions.HeuristicCommit e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.HeuristicCommitHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          } catch (org.omg.CosTransactions.HeuristicMixed e1) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.HeuristicMixedHelper.insert(_except, e1);
            r.set_exception(_except);
            return;
          } catch (org.omg.CosTransactions.HeuristicHazard e2) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.HeuristicHazardHelper.insert(_except, e2);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __return = _orb().create_any();
          __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
          r.set_result(__return);
        }

        break;
      }
      case 2: // org.omg.CosTransactions.Resource.commit
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          try {
            this.commit();
          } catch (org.omg.CosTransactions.NotPrepared e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.NotPreparedHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          } catch (org.omg.CosTransactions.HeuristicRollback e1) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.HeuristicRollbackHelper.insert(_except, e1);
            r.set_exception(_except);
            return;
          } catch (org.omg.CosTransactions.HeuristicMixed e2) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.HeuristicMixedHelper.insert(_except, e2);
            r.set_exception(_except);
            return;
          } catch (org.omg.CosTransactions.HeuristicHazard e3) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.HeuristicHazardHelper.insert(_except, e3);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __return = _orb().create_any();
          __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
          r.set_result(__return);
        }

        break;
      }
      case 3: // org.omg.CosTransactions.Resource.commit_one_phase
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          try {
            this.commit_one_phase();
          } catch (org.omg.CosTransactions.HeuristicHazard e0) {
            org.omg.CORBA.Any _except = _orb().create_any();
            org.omg.CosTransactions.HeuristicHazardHelper.insert(_except, e0);
            r.set_exception(_except);
            return;
          }
          org.omg.CORBA.Any __return = _orb().create_any();
          __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
          r.set_result(__return);
        }

        break;
      }
      case 4: // org.omg.CosTransactions.Resource.forget
      {
        {
          org.omg.CORBA.NVList _list = _orb().create_list(0);
          r.arguments(_list);
          this.forget();
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

