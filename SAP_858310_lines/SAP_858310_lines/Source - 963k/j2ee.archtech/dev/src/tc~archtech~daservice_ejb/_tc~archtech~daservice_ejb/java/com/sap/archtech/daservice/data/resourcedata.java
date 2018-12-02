package com.sap.archtech.daservice.data;

public class ResourceData {

	private long resId;
	private String resName;
	private String resType;
	private String fpdb; // Finger print from the DB
	private long offset;
	private int packLength;
	private long length;
	private String packName;
	private String isPacked;

	public ResourceData(long resId, String resName, String resType) {
		this.resId = resId;
		this.resName = resName;
		this.resType = resType;
	}

	public ResourceData(long resId, String resName, String resType, long length) {
		this.resId = resId;
		this.resName = resName;
		this.resType = resType;
		this.length = length;
	}

	public ResourceData(long resId, String resName, String resType,
			String fpdb, int packLength, long offset, String packName) {
		this.resId = resId;
		this.resName = resName;
		this.resType = resType;
		this.fpdb = fpdb;
		this.packLength = packLength;
		this.offset = offset;
		this.packName = packName;
	}

	public ResourceData(long resId, String resName, String fpdb,
			int packLength, long offset, String packName, String isPacked) {
		this.resId = resId;
		this.resName = resName;
		this.fpdb = fpdb;
		this.packLength = packLength;
		this.offset = offset;
		this.packName = packName;
		this.isPacked = isPacked;
	}

	public long getResId() {
		return resId;
	}

	public void setResId(long resId) {
		this.resId = resId;
	}

	public void setResName(String resName) {
		this.resName = resName;
	}

	public String getResName() {
		return resName;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public String getResType() {
		return resType;
	}

	public String getFpdb() {
		return fpdb;
	}

	public long getOffset() {
		return offset;
	}

	public int getPackLength() {
		return packLength;
	}

	public void setFpdb(String fpdb) {
		this.fpdb = fpdb;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public void setPackLength(int packLength) {
		this.packLength = packLength;
	}

	public String getPackName() {
		return packName;
	}

	public void setPackName(String packName) {
		this.packName = packName;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getIsPacked() {
		return isPacked;
	}
}
