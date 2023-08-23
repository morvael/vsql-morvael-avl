package terrain;

import VASSAL.build.AbstractToolbarItem;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.KeyStroke;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.MapShader;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.ZonedGrid;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import java.util.List;

/**
 * 
 * @author morvael
 */
public class TerrainMapShader extends MapShader {

  public static final String RULES_CLASS = "rules_class";
  public static final String RULES_PARAMS = "rules_params";
  protected String rulesClass = "";
  protected String rulesParams = "";
  private Board gridBoard;
  private TerrainHexGrid grid;
  private TerrainMapShaderRule rules = null;
  private HashMap<HexRef, HexRef[]> adjacentHexes = new HashMap<HexRef, HexRef[]>();
  private Area lastArea = null;
  private TerrainMapAreaOfEffect decorator = null;

  public TerrainMapShader() {
    super();
  }

  public String[] getAttributeNames() {
    return new String[]{NAME, ALWAYS_ON, STARTS_ON, BUTTON_TEXT, TOOLTIP,
      ICON, HOT_KEY, BOARDS, BOARD_LIST, TYPE, DRAW_OVER, PATTERN,
      COLOR, IMAGE, OPACITY, BORDER, BORDER_COLOR, BORDER_WIDTH,
      BORDER_OPACITY, RULES_CLASS, RULES_PARAMS
    };
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[]{String.class, Boolean.class, Boolean.class,
      String.class, String.class, AbstractToolbarItem.IconConfig.class, KeyStroke.class,
      BoardPrompt.class, String[].class, TypePrompt.class,
      Boolean.class, PatternPrompt.class, Color.class, Image.class,
      Integer.class, Boolean.class, Color.class, Integer.class,
      Integer.class, String.class, String.class
    };
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"Name:  ", "Shading Always On?  ",
      "Shading Starts turned on?  ", "Button text:  ",
      "Tooltip Text:  ", "Button Icon:  ", "Hotkey:  ",
      "All boards in map get Shaded?  ", "Board List:  ", "Type:  ",
      "Draw Shade on top of Counters?  ", "Shade Pattern:  ",
      "Color:  ", "Image:  ", "Opacity(%)", "Border?  ",
      "Border Color:  ", "Border Width:  ", "Border opacity(%)",
      "Rules class:  ", "Rules params:  "
    };
  }

  public void setAttribute(String key, Object value) {
    if (RULES_CLASS.equals(key)) {
      rulesClass = (String) value;
    } else if (RULES_PARAMS.equals(key)) {
      rulesParams = (String) value;
    } else {
      super.setAttribute(key, value);
    }
  }

  public String getAttributeValueString(String key) {
    if (RULES_CLASS.equals(key)) {
      return rulesClass + "";
    } else if (RULES_PARAMS.equals(key)) {
      return rulesParams + "";
    } else {
      return super.getAttributeValueString(key);
    }
  }

  public static String getConfigureTypeName() {
    return "Terrain Map Shading";
  }

  @Override
  protected void toggleShading() {
    super.toggleShading();
    decorator = null;
    lastArea = null;
  }

  protected Area getShadeShape(Map map) {
    if (lastArea == null) {
      if (type.equals(FG_TYPE)) {
        lastArea = new Area();
      } else {
        lastArea = new Area(getBoardClip());
      }
      lastArea.add(getArea(decorator != null ? Decorator.getOutermost(decorator) : null));
    }
    return lastArea;
  }

  @SuppressWarnings("unchecked")
  public TerrainMapShaderRule getRules() {
    if (rules != null) {
      return rules;
    } else {
      try {
        Class<TerrainMapShaderRule> rc = (Class<TerrainMapShaderRule>) Class.forName(rulesClass);
        rules = rc.getConstructor().newInstance();
        try {
          findGrid();
          rules.reset(this, map, grid, TerrainDefinitions.getInstance().getTerrainMap(grid), "", null);
        } catch (Exception e) {

        }
        return rules;
      } catch (Exception e) {
        return null;
      }
    }
  }

  public boolean addHex(
          HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes,
          HexRef hex, TerrainMapShaderCost cost) {
    HashMap<String, TerrainMapShaderCost> hf;
    if (selectedHexes.containsKey(hex)) {
      hf = selectedHexes.get(hex);
    } else {
      hf = new HashMap<String, TerrainMapShaderCost>();
      selectedHexes.put(hex, hf);
    }
    TerrainMapShaderCost oldCost = hf.get(cost.getFlag());
    if ((oldCost == null) || (cost.compareTo(oldCost) < 0)) {
      hf.put(cost.getFlag(), cost);
      return true;
    } else {
      return false;
    }
  }

  private boolean addHex(
          HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes,
          HexRef hex, TerrainMapShaderRule rule) {
    HashMap<String, TerrainMapShaderCost> hf;
    if (selectedHexes.containsKey(hex)) {
      hf = selectedHexes.get(hex);
    } else {
      hf = new HashMap<String, TerrainMapShaderCost>();
      selectedHexes.put(hex, hf);
    }
    TerrainMapShaderCost oldCost = hf.get(rule.getFlag());
    if ((oldCost == null) || (rule.compareWith(oldCost) < 0)) {
      hf.put(rule.getFlag(), new TerrainMapShaderCost(rule));
      return true;
    } else {
      return false;
    }
  }

  public Board getBoard() {
    if (gridBoard == null) {
      findGrid();
    }
    return gridBoard;
  }

  public TerrainHexGrid getGrid() {
    if (grid == null) {
      findGrid();
    }
    return grid;
  }

  private void findGrid() {
    gridBoard = null;
    grid = null;
    for (Board b : map.getBoards()) {
      if (b.getGrid() instanceof TerrainHexGrid) {
        gridBoard = b;
        grid = (TerrainHexGrid) b.getGrid();
        break;
      } else if (b.getGrid() instanceof ZonedGrid) {
        ZonedGrid zg = (ZonedGrid) b.getGrid();
        List<TerrainHexGrid> l = zg.getComponentsOf(TerrainHexGrid.class);
        if (l.size() > 0) {
          gridBoard = b;
          grid = l.get(0);
          break;
        }

      }
    }
  }

  public HashMap<HexRef, TerrainMapShaderCost> getHexes(TerrainMapShaderRule r, GamePiece piece) {
    HashMap<HexRef, TerrainMapShaderCost> result = new HashMap<HexRef, TerrainMapShaderCost>();
    findGrid();
    if (grid != null) {
      //adjacentHexes.clear();
      r.reset(this, map, grid, TerrainDefinitions.getInstance().getTerrainMap(grid), rulesParams, piece);
      HexRef hdest = r.getBestPathDestination();
      HashSet<HexRef> hexesToCheck = new HashSet<HexRef>();
      HashSet<HexRef> hexesToCheckNextTime = new HashSet<HexRef>();
      HashSet<HexRef> hexesToCheckSwitch;
      HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes = new HashMap<HexRef, HashMap<String, TerrainMapShaderCost>>();
      r.start(selectedHexes);
      if ((hdest == null) || (selectedHexes.containsKey(hdest) == false)) {
        for (HexRef hr : selectedHexes.keySet()) {
          hexesToCheck.add(hr);
        }
        boolean breakSearch = false;
        while (hexesToCheck.size() > 0) {
          for (HexRef hex : hexesToCheck) {
            HexRef[] neighbours = getAdjacentHexes(hex);
            for (TerrainMapShaderCost pc : selectedHexes.get(hex).values()) {
              for (int i = 0; i < 6; i++) {
                if (neighbours[i] == null) {
                  break;
                }
                if (neighbours[i].equals(pc.getFrom())) {
                  continue;
                }
                if (r.getCrossCost(hex, neighbours[i], pc)) {
                  if (addHex(selectedHexes, neighbours[i], r)) {
                    if ((hdest != null) && (neighbours[i].equals(hdest))) {
                      breakSearch = true;
                      break;
                    }
                    if (r.getPointsLeft() > 0.0d) {
                      hexesToCheckNextTime.add(neighbours[i]);
                    }
                  }
                }

              }
              if (breakSearch) {
                break;
              }
            }
            if (breakSearch) {
              break;
            }
          }
          if (breakSearch) {
            break;
          }
          hexesToCheckSwitch = hexesToCheckNextTime;
          hexesToCheckNextTime = hexesToCheck;
          hexesToCheck = hexesToCheckSwitch;
          hexesToCheckNextTime.clear();
        }
      }
      r.stop();
      TerrainMapShaderCost cost;
      if (hdest == null) {
        for (HexRef hr : selectedHexes.keySet()) {
          cost = r.getBestCost(selectedHexes.get(hr));
          if ((cost != null) && (cost.isTraceOnly() == false)) {
            result.put(hr, cost);
          }
        }
      } else {
        if (selectedHexes.containsKey(hdest)) {
          TerrainMapShaderCost startCost = r.getBestCost(selectedHexes.get(hdest));
          if (startCost != null) {
            String startFlag = startCost.getFlag();
            HexRef hr = hdest;
            while (hr != null) {
              cost = selectedHexes.get(hr).get(startFlag);
              if ((cost != null) && (cost.isTraceOnly() == false)) {
                result.put(hr, cost);
              }
              if (cost != null) {
                hr = cost.getFrom();
              } else {
                hr = null;
              }
            }
          }
        }
      }
    }
    return result;
  }

  public Area getArea(GamePiece piece) {
    Area shape = new Area();
    for (HexRef hr : getHexes(getRules(), piece).keySet()) {
      final Point c = grid.getHexCenter(hr.getColumn(), hr.getRow());
      shape.add(grid.getSingleHex(c.x, c.y));
    }
    if (gridBoard != null) {
      Rectangle r = gridBoard.bounds();
      shape = new Area(AffineTransform.getTranslateInstance(r.x, r.y).createTransformedShape(shape));
    }
    return shape;
  }

  public void setDecorator(TerrainMapAreaOfEffect decorator) {
    if (this.decorator != decorator) {
      this.decorator = decorator;
      lastArea = null;
      shadingVisible = decorator != null;
      map.repaint();
    }
  }

  public TerrainMapAreaOfEffect getDecorator() {
    return decorator;
  }

  public HexRef[] getAdjacentHexes(HexRef hex) {
    if (adjacentHexes.containsKey(hex) == false) {
      HexRef[] result = grid.getAdjacentHexes(hex);
      adjacentHexes.put(hex, result);
      return result;
    } else {
      return adjacentHexes.get(hex);
    }
  }
}
