package com.sap.archtech.archconn.commands;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;

/**
 * The <code>ArchCommandEnum</code> class defines type-safe constants
 * representing Archiving Commands.
 */
public enum ArchCommandEnum
{
  MKCOL("mkcol", MkcolCommand.class),
  PUT("put", PutCommand.class),
  DELETE("delete", DeleteCommand.class),
  GET("get", GetCommand.class),
  INDEXCREATE("indexcreate", IndexcreateCommand.class),
  INDEXDESCRIBE("indexdescribe", IndexdescribeCommand.class),
  LIST("list", ListCommand.class),
  SELECT("select", SelectCommand.class),
  PICK("pick", PickCommand.class),
  HEAD("head", HeadCommand.class),
  MODIFYPATH("modifypath", MPathCommand.class),
  INDEXDROP("indexdrop", IndexdropCommand.class),
  INDEXGET("indexget", IndexgetCommand.class),
  INDEXINSERT("indexinsert", IndexinsertCommand.class),
  CHECK("check", CheckCommand.class),
  INFO("info", InfoCommand.class),
  SESSIONINFO("sessioninfo", SessioninfoCommand.class),
  SYNCHOMEPATH("_sync_home_path", SyncHPCommand.class),
  DELETIONMARK("deletionmark", DeletionMarkCommand.class),
  PACK("pack", PackCommand.class),
  UNPACK("unpack", UnpackCommand.class),
  PACKSTATUS("packstatus", PackStatusCommand.class),
  RESETDELSTATUS("_reset_delstat", ResetDelstatCommand.class),
  INDEXEXISTS("indexexists", IndexexistsCommand.class),
  PROPERTYSET("propertyset", PropertySetCommand.class),
  PROPERTYGET("propertyget", PropertyGetCommand.class),
  LEGALHOLDADD("legalholdadd", LegalHoldAddCommand.class),
  LEGALHOLDGET("legalholdget", LegalHoldGetCommand.class),
  LEGALHOLDREMOVE("legalholdremove", LegalHoldRemoveCommand.class),
  ASSIGNARCHIVESTORE("_assign_archive_stores", AssignASCommand.class),
  DEFINEARCHIVESTORE("_define_archive_stores", DefineASCommand.class),
  DESTROY("destroy", DestroyCommand.class),
  LISTARCHIVEPATHS("_list_archive_paths", ListAPCommand.class),
  LISTASSIGNEDARCHIVEPATHS("_list_assigned_archive_paths", ListAssignedAPCommand.class),
  COLSEARCH("colsearch", ColSearchCommand.class),
  GET_WEBDAV_STORE_META_DATA("get_webdav_store_meta_data", GetWebDavASDataCommand.class),
  ORIGINSEARCH("originsearch", OriginSearchCommand.class),
  ORIGINLIST("originlist", OriginListCommand.class),
  SESSIONINVALIDATE("sessioninvalidate", SessionInvalidateCommand.class);
  
  /**
   * List of all Archiving Commands applicable for qualified Archiving Sessions (Include-List)
   */
  private static final EnumSet<ArchCommandEnum> allowed4QualifiedSession 
    = EnumSet.of(INDEXCREATE, INDEXDESCRIBE, INDEXINSERT, INDEXDROP, PICK, PUT, DELETIONMARK);
  
  /**
   * List of all Archiving Commands NOT applicable for unqualified Archiving Sessions (Exclude-List)
   */
  private static final EnumSet<ArchCommandEnum> notAllowed4UnqualSession 
    = EnumSet.of(PICK, PUT, DELETIONMARK);
  
  private static Map<String, ArchCommandEnum> valueMap = new HashMap<String, ArchCommandEnum>(values().length);
  static
  {
    // fill value map
    for(ArchCommandEnum archCommandEnum : values())
    {
      valueMap.put(archCommandEnum.toString(), archCommandEnum);
    }
  }
  
  private final String name;
  private final Class<? extends AbstractArchCommand> archCommandClass;
  
  /**
   * Hidden constructor.
   * @param name ID string.
   */
  private ArchCommandEnum(String name, Class<? extends AbstractArchCommand> archCommandClass)
  {
    this.name = name;
    this.archCommandClass = archCommandClass;
  }

  Class<? extends AbstractArchCommand> getArchCommandClass()
  {
    return archCommandClass;
  }
  
  public String toString()
  {
    return name;
  }
  
  public static ArchCommandEnum convert(String cmdName) throws UnsupportedCommandException
  {
    if(!valueMap.containsKey(cmdName))
    {
      throw new UnsupportedCommandException(new StringBuilder("Command \"").append(cmdName).append("\" is not a valid archiving command").toString());
    }
    ArchCommandEnum archCommand = valueMap.get(cmdName);
    if(archCommand == null)
    {
      throw new UnsupportedCommandException(new StringBuilder("Command \"").append(cmdName).append("\" is not a valid archiving command").toString());
    }
    return archCommand;
  }
  
  static void checkIsCommandAllowed(boolean isQualifiedSession, ArchCommandEnum cmdEnum) throws UnsupportedCommandException
  {
    if(cmdEnum == null)
    {
      throw new IllegalArgumentException("Missing archiving command!");
    }
    if(isQualifiedSession && !allowed4QualifiedSession.contains(cmdEnum))
    {
      throw new UnsupportedCommandException(new StringBuilder("Command \"").append(cmdEnum.toString()).append("\" is not allowed in a qualified archiving session").toString());
    }
    if(!isQualifiedSession && notAllowed4UnqualSession.contains(cmdEnum))
    {
      throw new UnsupportedCommandException(new StringBuilder("Command \"").append(cmdEnum.toString()).append("\" is not allowed in an unqualified archiving session").toString());
    }
  }
}
