/*
 * Created on Jun 28, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.sl.util.dbaccess.api;
import java.io.IOException;
import java.sql.SQLException;
import com.sap.sl.util.jarsl.api.JarSLIF;
/**
 * @author d000706
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface DBTaskIF
{
  public static final int IMPORTMODE_DEFAULT = 0;
  public static final int IMPORTMODE_NODELETE = 1;
  public static final int IMPORTMODE_NOUPDATE = 2;
  public static final int IMPORTMODE_INSERTONLY = 3;
  public static final int IMPORTMODE_DELETEONLY = 4;

  /**
   * set the log file name
   * @param fileName write the log to this file
   */
  public void useLogger (String fileName);

  /**
   * Export tables from database to file by reading the selection descriptions and processing these selections.
   * @param selectFileNames  array of files containing selection descriptions.
   * @param jarsl            archive where the table entries will be exported to.
   */
  public void dbExport(String[] selectFileNames, JarSLIF jarsl) throws IOException, XMLException, SQLException, ClassNotFoundException;
  
  /**
   * Export tables from database to file by reading the selection description and processing these selections.
   * @param selectFileName   file containing selection description.
   * @param jarsl            archive where the table entries will be exported to.
   */
  public void dbExport(String selectFileName, JarSLIF jarsl) throws IOException, XMLException, SQLException, ClassNotFoundException;

  /**
   * import tables from the file into the database
   * @param archivename data file containing the entries which shall be imported
   */
  public void dbImport(String datafilename) throws IOException, XMLException, SQLException, ClassNotFoundException;
   
  /**
   * import tables from the file into the database
   * @param archivename data file containing the entries which shall be imported
   */
  public void display(String datafilename) throws IOException;

  /**
   * set the import mode
   * @param importmode: The 'importmode' defines the import behavior
   * importmode = IMPORTMODE_DEFAULT   : (default) First perform DELETEs with the specified where clase, then INSERTS (and UPDATEs if the INSERTs fail which is possible if the where clase specifies non-key fields).
   * importmode = IMPORTMODE_NODELETE  : Perform no DELETEs, onlY INSERT/UPDATE.
   * importmode = IMPORTMODE_NOUPDATE  : Perform only DELETES and INSERTs.
   * importmode = IMPORTMODE_INSERTONLY: PERFORM only INSERTs, neither DELETEs nor UPDATEs.
   */
   public void setImportMode (int importmode);
   
  /**
   * Specify a directory for temporary results.
   */
   public void setTempdir(String dir);
 
   /**
    * print the import mode to the log file
    */
   public void printImportMode ();
}