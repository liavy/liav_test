package com.sap.engine.services.ts.inboundtx;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.SystemException;

import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.tlog.InvalidRMKeyException;
import com.sap.engine.services.ts.tlog.RMNameAlreadyInUseException;
import com.sap.engine.services.ts.tlog.RMPropsExtension;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class RMIDsMapper {

	private static final Location LOCATION = Location.getLocation(RMIDsMapper.class);

	private boolean isInitialized = false;
	private Map<RMPropsExtension, Integer> inboundRMs =
		new HashMap<RMPropsExtension, Integer>(10);

	private Map<Integer, Integer> outboundToInboundRMsMap =
		new HashMap<Integer, Integer>(10);

	private final TLog inboundTLog;

	public RMIDsMapper(TLog inboundTLog) {
		this.inboundTLog = inboundTLog;
	}

	/**
	 * Map the outbound RM ID to the appropriate inbound RM ID
	 * 
	 * @param outboundRMID
	 *            the outbound RM ID
	 * @return inbound RM ID corresponding to the outbound RM ID
	 * @throws SystemException if we cannot do the mapping
	 */
	public int mapOutboundRMIDToInboundRMID(int outboundRMID) throws SystemException {
		if (null==inboundTLog) {
			return outboundRMID;
		}

		Integer res = outboundToInboundRMsMap.get(outboundRMID);

		if (res!=null) {
			return res.intValue();

		} else {
			TLog oTLog = TransactionServiceFrame.getTLog();
			if (null == oTLog) {
				// TODO is it exception case ?
				return outboundRMID; // there is no logging
			}

			synchronized (this) {
				res = outboundToInboundRMsMap.get(outboundRMID);
				if (res!=null) {
					return res.intValue();
				}

				int id = 0;
				try {
					// ensure that inboundRMs is initialized 
					if (!isInitialized) {
						int[] rmIDs = inboundTLog.getAllUsedRMIDs();
						for (int rmID : rmIDs) {
							RMPropsExtension p = inboundTLog.getRMProperties(rmID);
							inboundRMs.put(p, rmID);
						}
						isInitialized = true;
					}

					RMPropsExtension oProps = oTLog.getRMProperties(outboundRMID);
					Integer inID = inboundRMs.get(oProps);
					if (null == inID) {
						id = inboundTLog.registerNewRM(oProps);
						inboundRMs.put(oProps, id);

					} else {
						id = inID.intValue();
					}
					outboundToInboundRMsMap.put(outboundRMID, id);

				} catch (TLogIOException e) {
					String msg = "Cannot register RM in the TLog";
					traceAndRethrowSystemException(e, msg);
				} catch (InvalidRMKeyException e) {
					String msg = "Cannot register RM in the TLog";
					traceAndRethrowSystemException(e, msg);
				} catch (RMNameAlreadyInUseException e) {
					String msg = "Cannot register RM in the TLog";
					traceAndRethrowSystemException(e, msg);
				}

				return id;
			}
		}
	}

	private void traceAndRethrowSystemException(Exception e, String msg)
			throws SystemException {
		SimpleLogger.traceThrowable(Severity.ERROR, LOCATION,e,"ASJ.trans.000112", msg);
		SystemException sysEx = new SystemException(msg);
		sysEx.initCause(e);
		throw sysEx;
	}
}
