package com.sap.engine.services.webservices.espbase.client.dynamic.util;

public class InternalCache {
  
  private static final int DEFAULT_CAPACITY = 10;
  
  private InternalCacheUnit firstUnit;
  private InternalCacheUnit lastUnit;
  private int unitsCapacity;
  private int unitsCount;
  private int creationUnitIndex;
  
  public InternalCache() {
    unitsCapacity = DEFAULT_CAPACITY;
    unitsCount = 0;
    creationUnitIndex = 0;
  }
  
  public synchronized int getSize() {
    return(unitsCount);
  }
  
  public synchronized void clear() {
    firstUnit = null;
    lastUnit = null;
    unitsCount = 0;
    creationUnitIndex = 0;
  }
  
  public synchronized Object[] getKeys() {
    Object[] keys = new Object[unitsCount]; 
    InternalCacheUnit unit = firstUnit;
    int counter = 0;
    while(unit != null) {
      keys[counter++] = unit.getKey();
      unit = unit.getNextUnit();
    }
    return(keys);
  }
  
  public synchronized Object[] getValues() {
    Object[] values = new Object[unitsCount]; 
    InternalCacheUnit unit = firstUnit;
    int counter = 0;
    while(unit != null) {
      values[counter++] = unit.getValue();
      unit = unit.getNextUnit();
    }
    return(values);
  }
  
  public synchronized void put(Object key, Object value) {
    if(unitsCapacity != 0) {
      if(firstUnit == null) {
        put_TheFirstTime(key, value);
      } else {
        InternalCacheUnit unit = findUnit(key);
        if(unit != null) {
          unit.setValue(value);
        } else if(unitsCount == unitsCapacity) {
          put_OverflowCapacity(key, value);
        } else {
          put_CreateNewUnit(key, value);
        }
      }
    }
  }
  
  public synchronized void removeKey(Object key) {
    InternalCacheUnit unit = findUnit(key);
    if(unit != null) {
      removeUnit(unit);
    }
  }
  
  public synchronized void removeValue(Object value) {
    InternalCacheUnit unit = firstUnit;
    while(unit != null) {
      if(unit.getValue().equals(value)) {
        removeUnit(unit);
      }
      unit = unit.getNextUnit();
    }
  }
  
  private void removeUnit(InternalCacheUnit unit) {
    unitsCount--;
    if(unitsCount == 0) {
      removeTheOnlyUnit();
    } else if(unit == firstUnit) {
      removeFirstUnit();
    } else if(unit == lastUnit) {
      removeLastUnit();
    } else {
      removeIntermediateUnit(unit);
    }
  }
  
  private void removeTheOnlyUnit() {
    firstUnit = null;
    lastUnit = null;
  }
  
  private void removeIntermediateUnit(InternalCacheUnit unit) {
    InternalCacheUnit previousUnit = unit.getPreviousUnit();
    InternalCacheUnit nextUnit = unit.getNextUnit();
    previousUnit.setNextUnit(nextUnit);
    nextUnit.setPreviousUnit(previousUnit);
  }
  
  private void removeFirstUnit() {
    InternalCacheUnit secondUnit = firstUnit.getNextUnit();
    secondUnit.setPreviousUnit(null);
    firstUnit = secondUnit;
  }
  
  private void removeLastUnit() {
    InternalCacheUnit beforeLastUnit = lastUnit.getPreviousUnit();
    beforeLastUnit.setNextUnit(null);
    lastUnit = beforeLastUnit; 
  }
  
  public synchronized Object get(Object key) {
    InternalCacheUnit unit = findUnit(key);
    if(unit != null) {
      unit.use();
      return(unit.getValue());
    }
    return(null);
  }
  
  public synchronized int getCapacity() {
    return(unitsCapacity);
  }
  
  public synchronized void setCapacity(int unitsCapacity) {
    if(unitsCapacity < 0) {
      throw new IllegalArgumentException("The capacity should be non negative number!");
    }
    this.unitsCapacity = unitsCapacity;
    if(unitsCapacity == 0) {
      clear();
    } else if(unitsCount > unitsCapacity) {
      removeTheTheMostUnusedUnits();
    }
  }
  
  private void removeTheTheMostUnusedUnits() {
    int theMostUnusedUnitsCount = unitsCount - unitsCapacity; 
    for(int i = 0; i < theMostUnusedUnitsCount; i++) {
      InternalCacheUnit theMostUnusedUnit = findTheMostUnusedUnit();
      removeUnit(theMostUnusedUnit);
    }
  }
  
  private InternalCacheUnit findTheMostUnusedUnit() {
    InternalCacheUnit unit = firstUnit;
    InternalCacheUnit theMostUnusedUnit = unit;
    double theLessUseFrequency = unit.getUseFrequency(creationUnitIndex);
    if(theLessUseFrequency == 0.0) {
      return(theMostUnusedUnit);
    }
    while((unit = unit.getNextUnit()) != null) {
      double useFrequency = unit.getUseFrequency(creationUnitIndex);
      if(useFrequency < theLessUseFrequency) {
        theLessUseFrequency = useFrequency;
        theMostUnusedUnit = unit;
      }
    }
    return(theMostUnusedUnit);
  }
  
  private void put_TheFirstTime(Object key, Object value) {
    firstUnit = createUnit(key, value, null, null);
    put_InitLastUnit(firstUnit);
  }
  
  private void put_OverflowCapacity(Object key, Object value) {
    if(unitsCapacity == 1) {
      initUnit(firstUnit, key, value, null, null, creationUnitIndex);
    } else {
      InternalCacheUnit theMostUnusedUnit = findTheMostUnusedUnit();
      removeUnit(theMostUnusedUnit);
      initUnit(theMostUnusedUnit, key, value, lastUnit, null, ++creationUnitIndex);
      theMostUnusedUnit.clearUses();
      put_NewUnit(theMostUnusedUnit);
    }
  }
  
  private void put_CreateNewUnit(Object key, Object value) {
    InternalCacheUnit newUnit = createUnit(key, value, lastUnit, null);
    put_NewUnit(newUnit);
  }
  
  private void put_NewUnit(InternalCacheUnit newUnit) {
    lastUnit.setNextUnit(newUnit);
    put_InitLastUnit(newUnit);
  }
  
  private void put_InitLastUnit(InternalCacheUnit unit) {
    lastUnit = unit;
    unitsCount++;
  }
  
  private InternalCacheUnit createUnit(Object key, Object value, InternalCacheUnit previousUnit, InternalCacheUnit nextUnit) {
    InternalCacheUnit unit = new InternalCacheUnit();
    initUnit(unit, key, value, previousUnit, nextUnit, ++creationUnitIndex);
    return(unit);
  }

  private void initUnit(InternalCacheUnit unit, Object key, Object value, InternalCacheUnit previousUnit, InternalCacheUnit nextUnit, int creationIndex) {
    unit.setKey(key);
    unit.setValue(value);
    unit.setPreviousUnit(previousUnit);
    unit.setNextUnit(nextUnit);
    unit.setCreationIndex(creationIndex);
  }
  
  private InternalCacheUnit findUnit(Object key) {
    InternalCacheUnit unit = firstUnit;
    while(unit != null) {
      if(unit.getKey().equals(key)) {
        return(unit);
      }
      unit = unit.getNextUnit();
    }
    return(null);
  }
}
