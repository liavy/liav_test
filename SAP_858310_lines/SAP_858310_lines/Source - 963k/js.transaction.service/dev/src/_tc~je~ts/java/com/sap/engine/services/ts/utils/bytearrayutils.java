package com.sap.engine.services.ts.utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.transaction.xa.Xid;

public class ByteArrayUtils {

	/**
	 * Convert byte[] to int[]. The byte[] is considered
	 * little endian
	 * @param bytes the byte[].
	 * @return int[] representation of the byte[]
	 */
	public static int[] bytesToInts(byte[] bytes) {
		int[] ints = new int[bytes.length/4];
		for (int i=0; i<ints.length; ++i) {
			ints[i] = getIntFromByteArray(bytes, i*4);
		}
		return ints;
	}

//	/**
//	 * Convert int[] to byte[]. The byte array is
//	 * little endian
//	 * @param ints the int[]
//	 * @return byte[] representation of the int[]
//	 */
//	public static byte[] intsToBytes(int[] ints) {
//		byte[] bytes = new byte[ints.length*4];
//		for (int i=0; i<ints.length; ++i) {
//			addIntInByteArray(ints[i], bytes, i*4);
//		}
//		return bytes;
//	}

	/**
	 * Converts <i>theInt</i> to bytes and add it's byte representation
	 * in <i>bytes</i> in <i>position</i> position. You must be sure
	 * that there is room for int at the specified position, e.g. 4
	 * bytes at least, or you will get ArrayIndexOutOfBoundsException
	 * runtime exception
	 * @param theInt the int to be added
	 * @param bytes the byte array in which to add <i>theInt</i>
	 * @param position the position at which to add <i>theInt</i>
	 */
	public static void addIntInByteArray(int theInt, byte[] bytes, int position) {
		bytes[position]		=	(byte)( theInt			& 0xFF);
		bytes[position+1]	=	(byte)((theInt >>> 8)	& 0xFF);
		bytes[position+2]	=	(byte)((theInt >>> 16)	& 0xFF);
		bytes[position+3]	=	(byte) (theInt >>> 24);
	}

	/**
	 * Extract int from the specified <i>position</i> in a byte array.
	 * You must be sure that there are enough bytes for the int, e.g. 4,
	 * or you will get ArrayIndexOutOfBoundsException runtime exception
	 * @param bytes the byte array
	 * @param position the position in which the int begins
	 * @return the int representation of the four bytes in <i>bytes</i>
	 * array beginning at <i>position</i> position
	 */
	public static int getIntFromByteArray(byte[] bytes, int position) {
		return ((int)bytes[position]    & 0xff) |
			   ((int)bytes[position+1]  & 0xff) << 8 |
			   ((int)bytes[position+2]  & 0xff) << 16 |
			   ((int)bytes[position+3]  & 0xff) << 24;
	}

	/**
	 * Extract long from the specified <i>position</i> in a byte array.
	 * You must be sure that there are enough bytes for the long, e.g. 8,
	 * or you will get ArrayIndexOutOfBoundsException runtime exception
	 * @param bytes the byte array
	 * @param position the position in which the long begins
	 * @return the long representation of the eight bytes in <i>bytes</i>
	 * array beginning at <i>position</i> position
	 */
	public static long getLongFromByteArray(byte[] bytes, int position) {
		return (((long)getIntFromByteArray(bytes, position)) & 0xffffffffL) |
			   (((long)getIntFromByteArray(bytes, position+4)) << (4*8));
	}

	/**
	 * Converts <i>theLong</i> to bytes and add it's byte representation
	 * in <i>bytes</i> in <i>position</i> position. You must be sure
	 * that there is room for long at the specified position, e.g. 8
	 * bytes at least, or you will get ArrayIndexOutOfBoundsException
	 * runtime exception
	 * @param theLong the long to be added
	 * @param bytes the byte array in which to add <i>theLong</i>
	 * @param position the position at which to add <i>theLong</i>
	 */
	public static void addLongInByteArray(long theLong, byte[] bytes, int position) {
		addIntInByteArray((int)(theLong & 0xffffffffL), bytes, position);
		addIntInByteArray((int)(theLong >>> (4*8)),    bytes, position+4);
	}

	/**
	 * Converts long to its byte array representation.
	 * 
	 * @param arg the argument which will be converted. 
	 * @return byte array which represents the provided long.  
	 */
	public static byte[] convertLongToByteArr(long arg){
		byte[] result = new byte[8];
		addLongInByteArray(arg, result, 0);
		return result;
	}
	
	/**
	 * Converts int to its byte array representation.
	 * 
	 * @param arg the argument which will be converted. 
	 * @return byte array which represents the provided int.  
	 */	
	public static byte[] convertIntToByteArr(int arg){
		byte[] result = new byte[4];
		addIntInByteArray(arg, result, 0);
		return result;
	}	

	/**
	 * Converts byte array to long value.
	 * 
	 * @param arg he byte array which will be converted. 
	 * @return a long value created from provided byte array.
	 * @throws IllegalArgumentException if length of provided byte array is not 8. 
	 */
	public static long convertByteArrToLong(byte[] arg) throws IllegalArgumentException{		
		if(arg == null){
			throw new IllegalArgumentException("Provided argument is null.");
		}
		if(arg.length != 8 ){
			throw new IllegalArgumentException("Provided byte array does not represent a long value because its length is not 8. Its length is " + arg.length + ".");
		}
		
		return getLongFromByteArray(arg, 0);
	}
	
	/**
	 * Converts byte array to int value. 
	 * 
	 * @param arg he byte array which will be converted. 
	 * @return an int value created from provided byte array.
	 * @throws IllegalArgumentException if length of provided byte array is not 4. 
	 */
	public static int convertByteArrToInt(byte[] arg) throws IllegalArgumentException{
		if(arg == null){
			throw new IllegalArgumentException("Provided argument is null.");
		}
		if(arg.length != 4 ){
			throw new IllegalArgumentException("Provided byte array does not represent a int value because its length is not 4. Its length is " + arg.length + ".");
		}
		
		return getIntFromByteArray(arg, 0);
	}

	public static String convertByteArrayToString(byte[] bytes) {
		try {
			return new String(bytes, "UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Internal error.", e);
		}
	}

	public static byte[] convertStringToByteArray(String string) {
		try {
			return string.getBytes("UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Internal error.", e);
		}
	}

	protected final static int formatIdPosition = 0;
	protected final static int globalTxIDLengthPosition = formatIdPosition + 4;
	protected final static int branchQualifierLengthPosition = globalTxIDLengthPosition + 1;
	protected final static int globalTxIDPosition = branchQualifierLengthPosition + 1;
	protected final static int branchQualifierPosition = globalTxIDPosition + 64;

	/**
	 * Convert Xid to byte array in the following format:
	 * formatId - 4 bytes
	 * global TX ID length - 1 byte
	 * branch qualifier length - 1 byte
	 * global TX ID padded with zeroes - 64 bytes
	 * branch qualifier padded with zeroes - 64 bytes
	 * 
	 * @param xid the Xid to be converted
	 * @return the byte[] representation of the Xid
	 */
	public static byte[] convertXidToByteArray(Xid xid) {
		byte[] result = new byte[134];
		addXidInByteArr(xid, result, 0);
		return result;
	}

	/**
	 * Convert byte array to Xid. The byte[] must be in the following format:
	 * formatId - 4 bytes global TX ID length - 1 byte branch qualifier length -
	 * 1 byte global TX ID padded with zeroes - 64 bytes branch qualifier padded
	 * with zeroes - 64 bytes
	 * 
	 * @param xid
	 *            the byte[] to be converted
	 * @param position
	 *            the start position of the Xid in the byte array
	 * @return the Xid instance corresponding to the xid parameter
	 */
	public static Xid getXidFromByteArr(byte[] xid, int position) {
		int formatId				=	getIntFromByteArray(xid, position+formatIdPosition);

		byte globalTxIDLength		=	xid[position+globalTxIDLengthPosition];
		byte branchQualifierLength	=	xid[position+branchQualifierLengthPosition];

		byte[] globalTxID			=	new byte[globalTxIDLength];
		byte[] branchQualifier		=	new byte[branchQualifierLength];

		int start = position+globalTxIDPosition;
		for (int i=0; i<globalTxIDLength; ++i) {
			globalTxID[i] = xid[start+i];
		}

		start = position+branchQualifierPosition;
		for (int i=0; i<branchQualifierLength; ++i) {
			branchQualifier[i] = xid[start+i];
		}

		return new XidImpl(formatId, branchQualifier, globalTxID);
	}

	public static void addXidInByteArr(Xid xid, byte[] arr, int position) {
		Arrays.fill(arr, position, position+134, (byte)0);

		addIntInByteArray(xid.getFormatId(), arr, position+formatIdPosition);

		byte[] globalTXID = xid.getGlobalTransactionId(); 
		byte[] branchQualifier = xid.getBranchQualifier();
		arr[position+globalTxIDLengthPosition] = (byte) globalTXID.length;
		arr[position+branchQualifierLengthPosition] = (byte) branchQualifier.length;

		int start = position+globalTxIDPosition;
		for (int i=0; i<globalTXID.length; ++i) {
			arr[start+i] = globalTXID[i];
		}

		start = position+branchQualifierPosition;
		for (int i=0; i<branchQualifier.length; ++i) {
			arr[start+i] = branchQualifier[i];
		}
	}
}
