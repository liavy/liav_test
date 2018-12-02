package com.sap.security.core.admin;

import java.util.Vector;
import java.util.Enumeration;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

import com.sap.security.api.ISearchResult;

public class ListBean {

    /* session objects
        int totalItems - size of search result
        int currentPage - current page number
        int totalPages  - total page number
        int totalOptions - all page number list
        Integer[] itemPerPageOptions
        int currentItemPerPage
    */

    /* actions
        - to change item per page
        totalpages change, totalOptions change, reset current page to 1
        - to navigate among pages
        currentpage change
    */

    /* export
        - currentPage, totalPages, totalOptions
    */

    /* instantiate trace object*/
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/ListBean.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private final static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    // page navigation
    public final static String objsPerPage = "objsPerPage";
    public final static String objsTotal = "objsTotal";
    public final static String totalItems = "totalItems";
    public final static String totalPages = "totalPages";
    public final static String currentPage = "currentPage";
    public final static String currentItemPerPage = "currentItemPerPage";
    public final static String itemPerPageOptions = "itemPerPageOptions";
    public final static String multiPages = "multiPages";
    public final static String requestPage = "requestPage";
    public final static String displayFrom = "displayFrom";
    public final static String displayTo = "displayTo";

    public static final String beanId = "list";
    public static final String objId = "objId";
    public static final String selectedObjId = "selectedObjId";
    public static final String slctObjId = "slctObjId";

    public static final int defaultItemPerPage = 10;

    private Vector objs = new Vector();
    private Vector selectedObjs = new Vector();
    private Vector _objsOnCurrentPage = null;
    private int _totalItems = 0;
    private int _totalPages = 1;
    private int _currentPage = 1;
    private int _currentItemPerPage = defaultItemPerPage;
    private Integer[] _pageOptions ;
    private int _beginIdx, _endIdx;

    public ListBean(IAccessToLogic proxy, Vector listObj) {
        if ( null != listObj ) {
            if ( !listObj.isEmpty() ) {
                setAll(listObj);
                init(proxy);
            }
        }
    } // ListBean(HttpServletRequest, Vector)

    public ListBean(IAccessToLogic proxy, Enumeration listObj) {
        if ( null != listObj ) {
            if ( listObj.hasMoreElements() ) {
                setAll(listObj);
                init(proxy);
            }
        }
    } // ListBean(HttpServletRequest, Enumeration)

    public ListBean(IAccessToLogic proxy, Object[] listObj) {
        if ( null != listObj ) {
            if ( listObj.length > 0 ) {
                setAll(listObj);
                init(proxy);
            }
        }
    } // ListBean(HttpServletRequest, Object[])

    public ListBean(IAccessToLogic proxy, ISearchResult result) {
        if ( null != result ) {
            if ( result.size() > 0 ) {
                setAll(result);
                init(proxy);
            }
        }
    } // ListBean(HttpServletRequest, ISearchResult)

    public ListBean(Vector listObj) {
        if ( null != listObj ) {
            if ( !listObj.isEmpty() ) {
                setAll(listObj);
                init();
            }
        }
    } // ListBean(Vector listObj)

    public ListBean(Enumeration listObj ) {
        if ( null != listObj ) {
            if ( listObj.hasMoreElements() ) {
                setAll(listObj);
                init();
            }
        }
    } // ListBean(Enumeration)

    public ListBean(Object[] listObj) {
        if ( null != listObj ) {
            if ( listObj.length > 0 ) {
                setAll(listObj);
                init();
            }
        }
    } // ListBean(Object[])

    public ListBean(ISearchResult result) {
        if ( null != result ) {
            if ( result.size() > 0 ) {
                setAll(result);
                init();
            }
        }
    } // ListBean(ISearchResult)

    public void setAll(Vector listObj) {
        objs = listObj;
        _totalItems = objs.size();
        trace.debugT("set All", "totalITems", new Integer[]{new Integer(_totalItems)});
    } // setAll(Vector)

    public void setAll(Enumeration listObj) {
        while ( listObj.hasMoreElements() ) {
            objs.add(listObj.nextElement());
        }
        _totalItems = objs.size();
        trace.debugT("set All", "totalITems", new Integer[]{new Integer(_totalItems)});
    } // setAll(Enumeration)

    public void setAll(Object[] listObj) {
        trace.entering("ListBean", new Integer[]{new Integer(listObj.length)});
        for (int i=0; i<listObj.length; i++ ) {
            objs.add(listObj[i]);
        }
        _totalItems = objs.size();
        trace.debugT("set All", "totalITems", new Integer[]{new Integer(_totalItems)});
    } // setAll(Object[])

    public void setAll(ISearchResult result ) {
        trace.entering("ListBean", new Integer[]{new Integer(result.size())});
        while(result.hasNext()) {
            objs.add(result.next());
        }
        _totalItems = objs.size();
        trace.debugT("set All", "totalITems", new Integer[]{new Integer(_totalItems)});
    } // setAll(ISearchResult)

    public void init(IAccessToLogic proxy) {
        setCurrentItemPerPage(proxy);
        setCurrentPage(proxy);
        doListPagingHandling();
        setSelected(proxy);
    } // init(HttpServletRequest)

    private void init() {
        doListPagingHandling();
    } // init()

    public void setCurrentItemPerPage(IAccessToLogic proxy) {
        if ( null != util.checkEmpty(proxy.getRequestParameter(currentItemPerPage)) ) {
            trace.debugT("setCurrentItemPerPage", proxy.getRequestParameter(currentItemPerPage));
            _currentItemPerPage = Integer.parseInt((proxy.getRequestParameter(currentItemPerPage)).trim());
        }
    } // setCurrentItemPerPage(HttpServletRequest)

    public void setCurrentPage(IAccessToLogic proxy) {
        if ( null != util.checkEmpty(proxy.getRequestParameter(requestPage)) ) {
            trace.debugT("setCurrentPage", proxy.getRequestParameter(requestPage));
            _currentPage = Integer.parseInt((proxy.getRequestParameter(requestPage)).trim());
        }
    } // setCurrentPage(HttpServletRequest)

    public void setCurrentItemPerPage(int size) {
        _currentItemPerPage = size;
    } // setCurrentItemPerPage(int)

    public void setCurrentPage(int page) {
        _currentPage = page;
    } // setCurrentPage(int)

    public void doListPagingHandling() {
        String methodName = "doListPagingHandling";
        int items = _totalItems;
        int item = _currentItemPerPage;
        int page = _currentPage;
        int pages = 1;
        int itemOnLastPage = 0;
        int beginIdx, endIdx;

        java.util.Vector options = new java.util.Vector();
        options.add(new Integer(defaultItemPerPage));
        boolean extractable = true;

        for ( int i=2; extractable; i++) {
            if (   ( items/(i*defaultItemPerPage) >= 1 )
                || ((i*defaultItemPerPage)-items%(i*defaultItemPerPage)) < defaultItemPerPage ) {
                options.add(new Integer(i*defaultItemPerPage));
            } else {
                extractable = false;
            }
        }
        _pageOptions = new Integer[options.size()];
        _pageOptions = (Integer[]) options.toArray(_pageOptions);

        if ( (items%item) == 0 ) {
            pages = items/item;
        } else {
            pages = items/item + 1;
            itemOnLastPage = items%item;
        }
        _totalPages = pages;

        if ( page > pages ) {
            page = 1;
            _currentPage = page;
        }

        if ( 1 != pages ) {
        	beginIdx = (page -1)*item;
            endIdx = beginIdx + item;
            if ( page == pages && itemOnLastPage>0 ) {
                endIdx = beginIdx + itemOnLastPage;
            }
        } else {
            beginIdx = 0;
            endIdx = _totalItems;
        }
        _beginIdx = beginIdx;
        _endIdx = endIdx;

        trace.debugT(methodName, "currentPage is:", new Integer[]{new Integer(_currentPage)});
        trace.debugT(methodName, "totalPages are/is", new Integer[]{new Integer(_totalPages)});
        trace.debugT(methodName, "itemPerPageOptions", _pageOptions);
        trace.debugT(methodName, "itemPerPage", new Integer[]{new Integer(_currentItemPerPage)});
        trace.debugT(methodName, "from", new Integer[]{new Integer(_beginIdx)});
        trace.debugT(methodName, "to", new Integer[]{new Integer(_endIdx)});

        addObjsToPage();
    } // doListPagingHandling()

    private void addObjsToPage() {
        _objsOnCurrentPage = new Vector();
        for ( int i=_beginIdx; i<_endIdx; i++) {
            _objsOnCurrentPage.add(objs.get(i));
        }
    } // addObjsToPage()

    public void setSelected(String id) {
        if ( null != id ) {
            setSelected((Object[]) _objsOnCurrentPage.toArray(new Object[_objsOnCurrentPage.size()]),
                        new String[] {id});
        }
    } // setSelected(String)

    public void setSelected(IAccessToLogic proxy) {
        setSelected((Object[]) _objsOnCurrentPage.toArray(new Object[_objsOnCurrentPage.size()]),
                    getSelectedObjIds(proxy));
    } // setSelected(HttpServletRequest)

    public static String[] getSelectedObjIds(IAccessToLogic proxy) {
        if ( null != util.checkEmpty(proxy.getRequestParameter(selectedObjId)) ) {
            String[] selected = (String[]) proxy.getRequestParameterValues(selectedObjId);
            return selected;
        } else {
            return null;
        }
    } // getSelectedObjIds(IAccessToLogic proxy)

    public void setSelected(Object[] all, String[] selected) {
        selectedObjs = new Vector();
        int size = all.length;
        if ( null != selected ) {
            for ( int i=0; i<selected.length; i++) {
                int id = Integer.parseInt(selected[i]);
                if (id<size)
                selectedObjs.add(all[id]);
            }
        }
    } // setSelected

    public Vector getAllObjs() {
        if ( _totalItems > 0 )
            return objs;
        else
            return null;
    } // getAllObjs()

    public Object[] getAllObjsArray() {
        Vector vec = this.objs;
        return this.vectorToArray(vec);
    } // getAllObjsArray()

    public boolean isSelectedObj(String id) {
        Object[] array = new Object[_objsOnCurrentPage.size()];
        array = (Object[]) _objsOnCurrentPage.toArray(array);
        Object obj = array[Integer.parseInt(id)];
        return selectedObjs.contains(obj);
    } // isSelectedObj(String id)

    public boolean isSelectedObj(int id) {
        Object[] array = new Object[_objsOnCurrentPage.size()];
        array = (Object[]) _objsOnCurrentPage.toArray(array);
        Object obj = array[id];
        return selectedObjs.contains(obj);
    } // isSelectedObj(int id)

    public Vector getSelectedObjs() {
        return selectedObjs;
    } // getSelectedObjs

    public Object[] getSelectedObjsArray() {
        Vector vec = selectedObjs;
        return vectorToArray(vec);
    } // getSelectedObjsArray

    public Object getSelectedObj() {
        return selectedObjs.get(0);
    } // getSelectedObj

    public int getSelectedObjIndex() {
        return objs.indexOf(getSelectedObj());
    } // getSelectedObjIndex

    /*
    public void removeSelectedObj(String id) {
        Object[] array = new Object[_objsOnCurrentPage.size()];
        array = (Object[]) _objsOnCurrentPage.toArray(array);
        Object obj = array[Integer.parseInt(id)];
        selectedObjs.remove(obj);
    } // removeSelectedUid
    */

    public boolean removeSelectedObjs( ) {
        for (int i=0; i<selectedObjs.size(); i++) {
            objs.remove(selectedObjs.get(i));
        }
        return updateList();
    } // removeSelectedObjs

    public boolean removeObj(Object oldObj) {
        Object[] oldObjs = new Object[]{oldObj};
        return removeObjs(oldObjs);
    } // removeObj(Object oldObj)

    public boolean removeObjs(Object[] oldObjs) {
        for (int i=0; i<oldObjs.length; i++) {
            objs.remove(oldObjs[i]);
        }
        return updateList();
    } // removeObjs(Object[] oldObjs)

    private boolean updateList() {
        if ( !objs.isEmpty() ) {
			setTotalItems(objs.size());
			setCurrentPage(1);        	
            doListPagingHandling();
            return true;
        } else {
            return false;
        }
    } // updateList

    public void updateSelectedObj(Object newObj) {
        int index = getSelectedObjIndex();
        trace.debugT("updateSelectedObj", "selected index", new Integer[]{new Integer(index)});
        objs.removeElementAt(index);
        objs.insertElementAt(newObj, index);
        addObjsToPage();
    } // updateSelectedObj

    public void updateSelectedObjs(Object[] newObjs) {
        int index;
        for ( int i=0; i<newObjs.length; i++) {
            index = objs.indexOf(selectedObjs.get(i));
            objs.removeElementAt(index);
            objs.insertElementAt(newObjs[i], index);
        }
        addObjsToPage();
    } // updateSelectedObjs

    public void updateObj(Object oldObj, Object newObj) {
        Object[] oldObjs = new Object[]{oldObj};
        Object[] newObjs = new Object[]{newObj};
        updateObjs(oldObjs, newObjs);
    } // updateObj

    public void updateObjs(Object[] oldObjs, Object[] newObjs) {
        int index;
        for ( int i=0; i<oldObjs.length; i++) {
            index = objs.indexOf(oldObjs[i]);
            objs.removeElementAt(index);
            objs.insertElementAt(newObjs[i], index);
        }
        addObjsToPage();
    } // updateObj

    private void setTotalItems(int size) {
        _totalItems = size;
        trace.debugT("setTotalItems", "totalItems", new Integer[]{new Integer(_totalItems)});
    } // setTotalItems

    public int getTotalItems() {
        return _totalItems;
    } // getTotalItems

    public int getCurrentPage() {
        return _currentPage;
    } // getCurrentPage

    public int getTotalPages() {
        return _totalPages;
    } // getTotalPages

    public Vector getObjsOnCurrentPage() {
        return _objsOnCurrentPage;
    } // getObjsOnCurrentPage

    public Object[] getObjsArrayOnCurrentPage() {
    	Object[] array = new Object[_objsOnCurrentPage.size()];
    	array = _objsOnCurrentPage.toArray(array);
    	return array;
    } // getObjsArrayOnCurrentPage

    public Integer[] getItemPerPageOptions() {
        return _pageOptions;
    } // getItemPerPageOptions

    public int getCurrentItemPerPage() {
        return _currentItemPerPage;
    } // getCurrentItemPerPage

    private Object[] vectorToArray(Vector vec) {
        if ( null != vec && !vec.isEmpty() ) {
            Object[] array = new Object[vec.size()];
            array = vec.toArray(array);
            return array;
        } else {
            return null;
        }
    } // vectorToArray
}
