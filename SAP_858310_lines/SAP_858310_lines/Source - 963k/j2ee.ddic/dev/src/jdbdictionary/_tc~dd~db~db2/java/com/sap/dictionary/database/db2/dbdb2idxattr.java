package com.sap.dictionary.database.db2;

/**
 * @author d022204
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class DbDb2IdxAttr {

	private String  bufferPool;
	private String  pieceSize;
	private Boolean clustering;
	private Boolean defer;
	private Boolean close;
	private Boolean define;
	private Boolean copy;
	private Boolean partitioned;
	
	private DbDb2PartAttr partAttr;
	
	public DbDb2IdxAttr () {
	}
	
	public DbDb2PartAttr setPart () {
		
		partAttr = new DbDb2PartAttr();
		
		return partAttr;
	}
	
	public void setBufferPool ( String bufferPool ) {
		
		if ( bufferPool != null ) {
			this.bufferPool = bufferPool.toUpperCase();
		}
	}
	
	public void setPieceSize ( String pieceSize ) {
		
		if ( pieceSize != null ) {
			this.pieceSize = pieceSize.toUpperCase();
		}
	}
	
	public void setClustering ( String clustering ) {
		
		if ( clustering != null ) {
			
			if ( clustering.equalsIgnoreCase("NO") ) {
				
				this.clustering = new Boolean(false);
			} else {
				
				this.clustering = new Boolean(true);
			}
		}
	}
	
	public void setDefer ( String defer ) {
		
		if ( defer != null ) {
			
			if ( defer.equalsIgnoreCase("NO") ) {
				
				this.defer = new Boolean(false);
			} else {
				
				this.defer = new Boolean(true);
			}
		}
	}
	
	public void setClose ( String close ) {
		
		if ( close != null ) {
			
			if ( close.equalsIgnoreCase("NO") ) {
				
				this.close = new Boolean(false);
			} else {
				
				this.close = new Boolean(true);
			}
		}
	}
	
	public void setDefine ( String define ) {
		
		if ( define != null ) {
			
			if ( define.equalsIgnoreCase("YES") ) {
				
				this.define = new Boolean(true);
			} else {
				
				this.define = new Boolean(false);
			}
		}
	}
	
	public void setCopy ( String copy ) {
		
		if ( copy != null ) {
			
			if ( copy.equalsIgnoreCase("YES") ) {
				
				this.copy = new Boolean(true);
			} else {
				
				this.copy = new Boolean(false);
			}
		}
	}
	
	public void setPartitioned ( String partitioned ) {
		
		if ( partitioned != null ) {
			
			if ( partitioned.equalsIgnoreCase("YES") ) {
				
				this.partitioned = new Boolean(true);
			} else {
				
				this.partitioned = new Boolean(false);
			}
		}
	}
	
	public DbDb2PartAttr getPart () {
		
		return partAttr;
	}
	
	public String getBufferPool () {
		
		if ( bufferPool == null ) {
			
			return DbDb2Parameters.DEFAULT_BP_4K;
		}
				
		return bufferPool;
	}
	
	public String getPieceSize () {
		
		if ( pieceSize == null ) {
			
			return DbDb2Parameters.DEFAULT_PIECESIZE;
		} else {
			
			return pieceSize;
		}
	}
	
	public String getClustering () {
		
		if ( clustering == null ) {
			
			return DbDb2Parameters.DEFAULT_CLUSTERING;
		}
				
		if ( clustering.booleanValue() ) {
			
			return "YES";
		} else {
			
			return "NO";
		}
	}
	
	public String getDefer () {
		
		if ( defer == null ) {
			
			return DbDb2Parameters.DEFAULT_DEFER;
		}
				
		if ( defer.booleanValue() ) {
			
			return "YES";
		} else {
			
			return "NO";
		}
	}
	
	public String getClose () {
		
		if ( close == null ) {
			
			return DbDb2Parameters.DEFAULT_CLOSE;
		}
				
		if ( close.booleanValue() ) {
			
			return "YES";
		} else {
			
			return "NO";
		}
	}
	
	public String getDefine () {
		
		if ( define == null ) {
			
			return DbDb2Parameters.DEFAULT_DEFINE;
		}
				
		if ( define.booleanValue() ) {
			
			return "YES";
		} else {
			
			return "NO";
		}
	}
	
	public String getCopy () {
		
		if ( copy == null ) {
			
			return DbDb2Parameters.DEFAULT_COPY;
		}
				
		if ( copy.booleanValue() ) {
			
			return "YES";
		} else {
			
			return "NO";
		}
	}
	
	public String getPartitioned () {
		
		if ( partitioned == null) {
			
			return null;
		}
		
		if ( partitioned.booleanValue() ) {
			
			return "PARTITIONED";
		}
		
		return null;
	}
}
