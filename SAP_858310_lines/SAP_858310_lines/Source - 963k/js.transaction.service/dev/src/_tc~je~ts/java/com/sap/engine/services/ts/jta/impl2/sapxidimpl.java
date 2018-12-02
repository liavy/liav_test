package com.sap.engine.services.ts.jta.impl2;

import java.util.Arrays;

import javax.transaction.xa.Xid;

import com.sap.engine.services.ts.utils.ByteArrayUtils;
import com.sap.engine.services.ts.utils.TLogVersion;

/**
 * This class represents SAP implementation of Xid interface. Instances from this class will be used for 
 * all distributed transactions. The class will be used also for management of imported transactions. 
 *  
 * @author I024163
 *
 */
public class SAPXidImpl implements Xid {

	/**
	 * Default abandon transaction timeout is 2 days. Abandon transaction timeout is the timeout 
	 * between prepare and commit/rollback of the transaction. After that timeout transaction manager is free to 
	 * forget about it. In this implementation this is the timeout between transaction startup and rollback/commit of 
	 * the transaction. This is not an issue because timeout between transaction startup and prepare is just some minutes. 
	 */
	public static final long defaultTxAbandonTimeout = 172800000; // 2 days 
	
	/**
	 * This is the FormatId for all Xid-s which are used from SAP transaction manager. 
	 */
	public static final int sapFormatId= 0x53415001; //SAP1
		
//	/**
//	 * Id of the cluster where the TM is running. Length of this string is always 3 characters. Examples are LKG, GTP... 
//	 */
//	private static String cid = null;
//	/**
//	 * Id of the node where the transaction manager is running. Usually this is a small int. 
//	 */
//	private static int nodeID = 0;
	/**
	 * Startup time of Transaction manager in milliseconds. This time is used for generation of uniue Xid-s.  
	 */
	private static long tmStartupTime = 0;
	/**
	 * Abandon transaction timeout is the timeout 
	 * between prepare and commit/rollback of the transaction. After that timeout transaction manager is free to 
	 * forget about it. In this implementation this is the timeout between transaction startup and rollback/commit of 
	 * the transaction. This is not an issue because timeout between transaction startup and prepare is just some minutes.
	 */
	private static long txAbandonTimeout = defaultTxAbandonTimeout;
	/**
	 * byte array which keeps cid, nodeID and tmStartupTime. According design doc this is tLogVersion. This byte array is 
	 * created ones in order to skip a lot of runtime invocations of System.arraycopy. 
	 */
	private static byte[] tLogVersion = null;
	/**
	 * byte array which keeps tLogVersion and txabandonTimeout. When abandon timeout is changed this byte array will be 
	 * re-created again. But this is a rare operation and this byte array is used as a prefix of all global transaction ID-s.  
	 */
	private static byte[] gTxPrefix = null;
	/**
	 * byte array which keeps gTxPrefix and zero byte array for transaction names. When transaction name is not set this
	 *  byte array will be used.
	 */
	private static byte[] gTxPrefixWithoutTxName = null;
 
	

	
	/**
	 * This method will be called during start or restart of the transaction manager. 
	 * the method will initialize transaction log version and will restart counters of transactions. 
	 * 
	 * @param newCid - Cid of the cluster on which this transaction manager is running. 
	 * @param newNodeId - Id of the server node on which this TM is running
	 */
	public static void initializeStaticValues (TLogVersion ver){
//	  cid = newCid;
//	  nodeID = newNodeId;	  
//	  tmStartupTime = System.currentTimeMillis();
//	  // no problem with synchronizations because it will not be used in parallel
//	  TLogVersion ver = new TLogVersion(cid.getBytes(), nodeID, tmStartupTime);
	  tLogVersion = ver.getTLogVersion();
	  
	  gTxPrefix = new byte[15+8];
	  System.arraycopy(tLogVersion, 0, gTxPrefix, 0, 15);
	  ByteArrayUtils.addLongInByteArray(SAPXidImpl.txAbandonTimeout, gTxPrefix, 15);
	  
	  
	  gTxPrefixWithoutTxName = new byte[15+8+4];
	  Arrays.fill(gTxPrefixWithoutTxName, (byte)0);//to fill last 4 bytes with 0-s.This method is called ones so this is not an issue
	  System.arraycopy(gTxPrefix, 0, gTxPrefixWithoutTxName, 0, 15+8);
	  
    }
	
//	/**
//	 * @return the cluster ID where the TransactionManager is running. 
//	 */
//	public static String getCid(){
//		return SAPXidImpl.cid;
//	}
//	
//	/**
//	 * @return the node id where the TransactionManager is running
//	 */
//	public static int getNodeID(){
//		return SAPXidImpl.nodeID;
//	}
	
	/**
	 * @return the time in milliseconds when the TransactionManager was started. This time is used for unique generation of 
	 * global transaction ID-s.
	 */
	public static long getTmStartupTime(){
		return SAPXidImpl.tmStartupTime;
	}
	
    /**
     * Used from configuration tools to change transaction abandon timeout without restart of the server, transaction service 
     * or TransactionManager. gTxPrefix and gTxPrefixWithoutTxName and regenerated according new timeout.  
     * 
     * @param newTxAbandonTimeout - new abandon timeout which is set via admiistration tools.	
     */
    public static synchronized void setTxAbandonTimeout(long newTxAbandonTimeout){
    	SAPXidImpl.txAbandonTimeout = newTxAbandonTimeout;

  	  byte[] local_gTxPrefix = new byte[15+8];
	  System.arraycopy(tLogVersion, 0, local_gTxPrefix, 0, 15);
	  ByteArrayUtils.addLongInByteArray(SAPXidImpl.txAbandonTimeout, local_gTxPrefix, 15);
	  gTxPrefix = local_gTxPrefix;
	  
	  byte[] local_gTxPrefixWithoutTxName = new byte[15+8+4];
	  Arrays.fill(gTxPrefix, (byte)0);//to fill last 4 bytes with 0-s.This method is called ones so this is not an issue
	  System.arraycopy(gTxPrefix, 0, local_gTxPrefixWithoutTxName, 0, 15+8);
	  gTxPrefixWithoutTxName = local_gTxPrefixWithoutTxName;
    }
    
	//---------------------------------------------------------------------------------
	
	/**
	 * Global Transaction ID for this Xid
	 */
	private byte[] globalTransactionID = null;
    
	/**
	 * Branch qualifier for this Xid
	 */
	private byte[] branchQualifier = null;
	
	/**
	 * Constructor for SAPXidImpl. This constructor is private and SAPXidImpl instances can be created 
	 * only using  createNewXidWithNewGTxId or createXidForNewResourceManager methods.
	 * 
	 * @param gTxId - global transaction ID for Xid which is created using this constructor.
	 * @param bQualifier - branch qualifier for Xid which is created using this constructor.
	 */
	public SAPXidImpl(byte[] gTxId, byte[] bQualifier){
		this.globalTransactionID = gTxId;
		this.branchQualifier = bQualifier;		
	}
	
	/**
	 * Creates new Xid with new unique global transaction ID using provided parameters. 
	 * This method is used from TransactionManager to create Xid for first ResourceManager.
	 *  
	 * @param transactionClassifierID - the ID for the transaction name which is associated with the transaction for which 
	 * this Xid is generated. Null must be provides if there is no transaction name provided from application.  
	 * @param transactionBirthTime - The time when the transaction was created. It is possible to create JTA 
	 * transaction and later to enlist resources which support 2 phase commit protocol. 
	 * @param txSequenceNumber - the number of the transaction for which this Xid will be generated. This number 
	 * is unique only for current TransactionManager
	 * @param rmID - the ID of the resource manager for which this Xid is generated. 
	 * @return new Xid instance with unique global transaction ID.
	 * @throws IllegalArgumentException if provided transactionNameID is not null and it's length is not 4.
	 */
	public static SAPXidImpl createNewXidWithNewGTxId(byte[] transactionClassifierID, long transactionBirthTime, long txSequenceNumber,int rmID) throws IllegalArgumentException{
		if(transactionClassifierID != null && transactionClassifierID.length != 4){
			throw new IllegalArgumentException("Length of transactionNameId byte array must be 4");
		}
		
		byte[] globalTxID = new byte[44];
		
		if(transactionClassifierID == null){
			System.arraycopy(gTxPrefixWithoutTxName, 0, globalTxID, 0, 27);
		} else {
			System.arraycopy(gTxPrefix, 0, globalTxID, 0, 23);
			System.arraycopy(transactionClassifierID, 0, globalTxID, 23, 4);
		}
		
		ByteArrayUtils.addLongInByteArray(transactionBirthTime, globalTxID, 27);
		ByteArrayUtils.addLongInByteArray(txSequenceNumber, globalTxID, 35);
		globalTxID[43] = (byte)0;//because first branch +iterator is 0
		
		byte[] branchQualifier = new byte[2];
		
		branchQualifier[0] = (byte)(rmID >> 8);
		branchQualifier[1] = (byte)rmID;
		
		return new SAPXidImpl(globalTxID, branchQualifier); 
	}

	/**
	 * Used to generate XIDs for transaction propagation via RMI/IIOP.
	 *   
	 * @param globalTransactionID global transaction id which will be incremented.
	 * @param rmID resource manager id.
	 * @return new XID with contains provided parameters.
	 * 
	 */
	public static SAPXidImpl createNewSimpleXid(byte[] globalTransactionID, int rmID) {

		byte[] branchQualifier = new byte[2];
		
		branchQualifier[0] = (byte)(rmID >> 8);
		branchQualifier[1] = (byte)rmID;
		
		return new SAPXidImpl(globalTransactionID, branchQualifier); 		
	}	
	
	/**
	 * Creates new Xid with same global transaction ID  and provided branch iterator and branch qualifier.  
	 * This method is used from TransactionManager to create Xid for second and next ResourceManagers.
	 *  
	 * @param branchIterator last byte for global transaction ID. Usually this byte is 0 if Resource is shared. 
	 * @param rmID - the ID of the resource manager for which this Xid is generated. 
	 * @return new Xid with global transaction ID derived from this Xid and new branch qualifier. 
	 */
	public Xid createXidForNewResourceManager(byte branchIterator, int rmID){
		
		byte[] globalTxID = new byte[44];
		System.arraycopy(this.globalTransactionID, 0, globalTxID, 0, 43);
		globalTxID[43] = branchIterator; 
		
		byte[] branchQualifier = new byte[2];
		
		branchQualifier[0] = (byte)(rmID >> 8);
		branchQualifier[1] = (byte)rmID;
		
		return new SAPXidImpl(globalTxID, branchQualifier);
	}
	
	/** 
	 * @see javax.transaction.xa.Xid#getBranchQualifier()
	 */
	public byte[] getBranchQualifier() {	
		return branchQualifier;
	}

	/**
	 * @see javax.transaction.xa.Xid#getFormatId()
	 */
	public int getFormatId() {
		return sapFormatId;
	}

	/**
	 * @see javax.transaction.xa.Xid#getGlobalTransactionId()
	 */
	public byte[] getGlobalTransactionId() {
		return globalTransactionID;
	}
	
    /**
     * Generated global transaction ID which is used for RMI/IIOP 
     *  transaction propagation. 
     *   
     * @param transactionClassifierID byte array which represent tx classifier ID
     * @param transactionBirthTime transaction birth time
     * @param txSequenceNumber transaction sequence number 
     * @return byte array with length 44 which ends with 0 byte for branch iterator. 
     */
    public static byte[] createGlobalTxIDWith_0_BranchIterator(byte[] transactionClassifierID, long transactionBirthTime, long txSequenceNumber){
    	if(transactionClassifierID != null && transactionClassifierID.length != 4){
			throw new IllegalArgumentException("Length of transactionNameId byte array must be 4");
		}
		
		byte[] globalTxID = new byte[44];
		
		if(transactionClassifierID == null){
			System.arraycopy(gTxPrefixWithoutTxName, 0, globalTxID, 0, 27);
		} else {
			System.arraycopy(gTxPrefix, 0, globalTxID, 0, 23);
			System.arraycopy(transactionClassifierID, 0, globalTxID, 23, 4);
		}
		
		ByteArrayUtils.addLongInByteArray(transactionBirthTime, globalTxID, 27);
		ByteArrayUtils.addLongInByteArray(txSequenceNumber, globalTxID, 35);
		globalTxID[43] = (byte)0;//because first branch +iterator is 0
		
		return globalTxID;
    	
    } 
	
	public static boolean compareTLogVersions(Xid xid, TLogVersion tlogVersion) {
		
		byte[] globalTransactionId = xid.getGlobalTransactionId();
		if(globalTransactionId == null || globalTransactionId.length<44){
			return false;
		}
		byte[] tLogVersionBytes = tlogVersion.getTLogVersion();// length of this byte array is 15
		
		for(int i=0; i<tLogVersionBytes.length; i++){
			if(tLogVersionBytes[i] != globalTransactionId[i]){
				return false;
			}
		}
		return true;
	}

	
	public static byte[] getTLogVersion() {
		return tLogVersion;
	}	
}
