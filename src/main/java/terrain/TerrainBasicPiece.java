/*
 * $Id: TerrainBasicPiece.java 3639 2008-05-23 11:24:55Z swampwallaby $
 *
 * Copyright (c) 2000-2008 by Brent Easton
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package terrain;

import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.counters.BasicPiece;

/**
 * A subclass of BasicPiece that exposes the current terrain as a property
 */
// FIXME: Merge this into BasicPiece
public class TerrainBasicPiece extends BasicPiece {

  public static final String CURRENT_TERRAIN = "currentTerrain";
  
  public TerrainBasicPiece() {
    super();
  }

  public TerrainBasicPiece(String type) {
    super(type);
  }
  
  public Object getProperty(Object key) {
    TerrainHexGrid thg = null;
    
    if (getMap() != null) {
      Board b = getMap().findBoard(getPosition());
      if (b != null) {
        MapGrid grid = b.getGrid();
        if (grid instanceof TerrainHexGrid) {
          thg = (TerrainHexGrid) grid;
        }
      }
    }
    
    if (CURRENT_TERRAIN.equals(key)) {
      return thg == null ? "" : thg.getTerrainName(getPosition());             
    }
    
    if (thg != null && key instanceof String) {
      String value = thg.getProperty((String) key, getPosition());
      if (value != null) {
        return value;
      }
    }
    return super.getProperty(key);    
  }  
  
  public Object getLocalizedProperty(Object key) {
    TerrainHexGrid thg = null;
    
    if (getMap() != null) {
      Board b = getMap().findBoard(getPosition());
      if (b != null) {
        MapGrid grid = b.getGrid();
        if (grid instanceof TerrainHexGrid) {
          thg = (TerrainHexGrid) grid;
        }
      }
    }
    
    if (CURRENT_TERRAIN.equals(key)) {
      return thg == null ? "" : thg.getTerrainName(getPosition());             
    }
    
    if (thg != null && key instanceof String) {
      String value = thg.getProperty((String) key, getPosition());
      if (value != null) {
        return value;
      }
    }
    return super.getLocalizedProperty(key);   
  }
}
