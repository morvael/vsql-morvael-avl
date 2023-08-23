/*
 * $Id: InvEmbellishment.java 685 2005-08-23 12:46:51Z swampwallaby $
 *
 * Copyright (c) 2005 by Rodney Kinney, Brent Easton
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

package Inventory;

import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;

public class InvEmbellishment extends Embellishment {

  public static final String ID = "invEmb2;";
  public static final String LAYER_NAME = "LAYER_NAME";
  public static final String LAYER_IMAGE = "LAYER_IMAGE";
  public static final String LAYER_LEVEL = "LAYER_LEVEL";
  
  public InvEmbellishment() {
    this(ID + "Activate", null);
  }
  
  public InvEmbellishment(String type, GamePiece inner) {
    
    super(type, inner);
  }

  public void mySetType(String s) {
    if (s.startsWith(ID)) {
      super.mySetType(Embellishment.ID + s.substring(ID.length()));
    }
    else {
      super.mySetType(s);
    }
  }
  
  public String myGetType() {
    return (ID + super.myGetType().substring(Embellishment.ID.length()));
  }
      
  public Object getProperty(Object key) {
    if (LAYER_LEVEL.equals(key)) {
      return value + "";
    }
    else if (LAYER_IMAGE.equals(key)) {
      String name = "";
      if (value > 0) {
        name = imageName[value-1];
      }
      return name;
    }
    else if (LAYER_NAME.equals(key)) {
      String name = "";
      if (value > 0) {
        name = commonName[value - 1];
        if (name.startsWith("+")) {
          name = name.substring(1);
        }
        else if (name.endsWith("+")) {
          name = name.substring(0,name.length()-2);
        }
      }
      return name;
    }
    else {
      return super.getProperty(key);
    }
  }
  
  public String getDescription() {
    if (imageName.length == 0 || imageName[0] == null || imageName[0].length() == 0) {
      return "Inv Layer v"+Inventory.VERSION;
    }
    else {
      return "Inv Layer v"+Inventory.VERSION+" - " + imageName[0];
    }
  }

}
