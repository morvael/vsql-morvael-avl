package terrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import VASSAL.build.module.Map;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.counters.PropertiesPieceFilter;
import VASSAL.counters.Stack;

/**
 * 
 * @author morvael
 */
public abstract class TerrainMapShaderRule {
	
	public static final String NULL_TERRAIN = "NULL_TERRAIN";
	public static final String NULL_EDGE = "NULL_EDGE";
	public static final String NULL_LINE = "NULL_LINE";
	public static final String NULL_ATTRIBUTE = "NULL_ATTRIBUTE";
	
	protected TerrainMapShader mapShader;
	protected Map map;
	protected TerrainHexGrid grid;
	protected TerrainMap terrainMap;
	protected String params;
	protected GamePiece piece;
	
	protected HexRef from;
	protected boolean mayContinue;
	protected double pointsLeft;
	protected boolean traceOnly;
	protected String flag;	
	
	public TerrainMapShaderRule() {
	}
	
	public void reset(TerrainMapShader mapShader, Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		this.mapShader = mapShader;
		this.map = map;
		this.grid = grid;
		this.terrainMap = terrainMap;
		this.params = params;
		this.piece = piece;
	}
	
	public abstract void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes);	
	public abstract boolean getCrossCost(HexRef fromHex, HexRef toHex, TerrainMapShaderCost currentCost);
	public abstract void stop();
	public abstract HexRef getBestPathDestination();	
	public abstract TerrainMapShaderCost getBestCost(HashMap<String, TerrainMapShaderCost> costs);
	
	public final int compareWith(TerrainMapShaderCost o2) {
		int result = TerrainMapShaderCost.compareBoolean(mayContinue, o2.isMayContinue());
		if (result == 0) {
			result = TerrainMapShaderCost.compareBoolean(traceOnly, o2.isTraceOnly());
			if (result == 0) {
				result = -Double.compare(pointsLeft, o2.getPointsLeft());
			}
		}
		return result;
	}
	
	protected HexTerrain getHexTerrain(HexRef hex) {
		TerrainHex h = terrainMap.getHexTerrain(hex);
	    return h == null ? null : h.getTerrain();		
	}

	protected EdgeTerrain getEdgeTerrain(HexRef fromHex, HexRef toHex) {
	    TerrainEdge e = terrainMap.getEdgeTerrain(fromHex, toHex);
	    return e == null ? null : e.getTerrain();		
	}

	protected LineTerrain getLineTerrain(HexRef fromHex, HexRef toHex) {
		TerrainLine l = terrainMap.getLineTerrain(fromHex, toHex);
	    return l == null ? null : l.getTerrain();		
	}
	
	protected AttributeTerrain getAttributeTerrain(HexRef hex, String name) {
		TerrainAttribute a = terrainMap.getAttributeTerrain(new AttrRef(hex, name));
		return a == null ? null : a.getTerrain();
	}
	
	public static ArrayList<GamePiece> filterPieces(Map m, String s) {
		ArrayList<GamePiece> result = new ArrayList<GamePiece>();
		PieceFilter filter = PropertiesPieceFilter.parse(s);
		GamePiece[] p = m.getPieces();
		Stack st;
		GamePiece gp;
		for (int i = 0; i < p.length; ++i) {
			if (p[i] instanceof Stack) {
				st = (Stack)p[i];
				for (int j=0; j<st.getPieceCount(); j++) {
					gp = st.getPieceAt(j);
					if (filter.accept(gp)) {
						result.add(Decorator.getOutermost(gp));
					}
				}
			} else {			
				if (filter.accept(p[i])) {
					result.add(Decorator.getOutermost(p[i]));
				}
			}
		}
		return result;
	}
	
	protected void addPiece(HashMap<HexRef, ArrayList<GamePiece>> pieces, GamePiece gp) {
		HexRef hr = grid.getHexPos(gp.getPosition());
		ArrayList<GamePiece> list;
		if (pieces.containsKey(hr)) {
			list = pieces.get(hr);			
		} else {
			list = new ArrayList<GamePiece>();
			pieces.put(hr, list);
		}
		list.add(gp);
	}
	
	private static String buildQuery(String ... checks) {
		StringBuilder query = new StringBuilder();		
		for (String check : checks) {
			query.append(check);
			query.append("&&");
		}
		if (query.length() > 0) {
			query.setLength(query.length()-2);
		}
		return query.toString();
	}
	
	public HashMap<HexRef, ArrayList<GamePiece>> filterPieceGroups(String ... checks) {
		HashMap<HexRef, ArrayList<GamePiece>> result = new HashMap<HexRef, ArrayList<GamePiece>>();
		filterPieceGroups(result, checks);
		return result;
	}
	
	public void filterPieceGroups(HashMap<HexRef, ArrayList<GamePiece>> map, String ... checks) {
		map.clear();
		for (GamePiece gp : filterPieces(this.map, buildQuery(checks))) {
			addPiece(map, gp);
		}		
	}

	public static ArrayList<GamePiece> filterPieces(Map m, String ... checks) {
		ArrayList<GamePiece> result = new ArrayList<GamePiece>();
		filterPieces(m, result, checks);
		return result;
	}

	public static ArrayList<GamePiece> filterPieces(Map m, ArrayList<GamePiece> list, String ... checks) {
		list.clear();
		for (GamePiece gp : filterPieces(m, buildQuery(checks))) {
			list.add(gp);
		}
		return list;
	}
	
	protected String getHexTerrainName(HexRef hex) {
		HexTerrain h = getHexTerrain(hex);
		return h == null ? TerrainMapShaderRule.NULL_TERRAIN : h.getTerrainName();
	}
	
	protected String getEdgeTerrainName(HexRef fromHex, HexRef toHex) {
		EdgeTerrain e = getEdgeTerrain(fromHex, toHex);
		return e == null ? TerrainMapShaderRule.NULL_EDGE : e.getTerrainName();
	}

	protected String getLineTerrainName(HexRef fromHex, HexRef toHex) {
		LineTerrain l = getLineTerrain(fromHex, toHex);
		return l == null ? TerrainMapShaderRule.NULL_LINE : l.getTerrainName();
	}
	
	protected String getAttributeTerrainValue(HexRef hex, String name) {
		TerrainAttribute a = terrainMap.getAttributeTerrain(new AttrRef(hex, name));
		return a == null ? TerrainMapShaderRule.NULL_ATTRIBUTE : a.getValue();
	}
	
	public static boolean isAny(String c, String ... elements) {
		for (int i=0; i<elements.length; i++) {
			if (c.equals(elements[i])) {
				return true;
			}
		}
		return false;
	}
	
	protected Set<HexRef> findHexesWithTag(String name, String value) {
		Set<HexRef> result = new HashSet<HexRef>();
		Iterator<TerrainAttribute> it = terrainMap.getAllAttributeTerrain();
		TerrainAttribute ta;
		while (it.hasNext()) {
			ta = it.next();
			if ((name.equals(ta.getName())) && (value.equals(ta.getValue()))) {
				result.add(new HexRef(ta.getColumn(), ta.getRow(), grid));
			}
		}
		return result;
	}
	
	protected String getMarkerValue(GamePiece gp, String marker) {
		Object result = gp.getProperty(marker);
		return result != null ? result.toString() : "NULL_MARKER_VALUE";
	}
	
	protected boolean isMarkerValueAny(GamePiece gp, String marker, String ... values) {
		String v = getMarkerValue(gp, marker);
		for (String value : values) {
			if (value.equals(v)) {
				return true;
			}
		}
		return false;
	}	
	
	protected boolean result(TerrainMapShaderCost c) {
		this.from = c.getFrom();
		this.mayContinue = c.isMayContinue();
		this.pointsLeft = c.getPointsLeft();
		this.traceOnly = c.isTraceOnly();
		this.flag = c.getFlag();
		return mayContinue && pointsLeft >= 0.0;
	}
	
	protected boolean result(HexRef from, boolean mayContinue, double newPoolValue, boolean traceOnly, String flag) {
		this.from = from;
		this.mayContinue = mayContinue;
		this.pointsLeft = newPoolValue;
		this.traceOnly = traceOnly;
		this.flag = flag;
		return mayContinue && pointsLeft >= 0.0;
	}
	
	protected TerrainMapShaderCost create(HexRef from, boolean mayContinue, double newPoolValue, boolean traceOnly, String flag) {
		result(from, mayContinue, newPoolValue, traceOnly, flag);
		return new TerrainMapShaderCost(this);
	}
	
	public HexRef getFrom() {
		return from;
	}
	
	public boolean isMayContinue() {
		return mayContinue;
	}
	
	public double getPointsLeft() {
		return pointsLeft;
	}
	
	public boolean isTraceOnly() {
		return traceOnly;
	}
	
	public String getFlag() {
		return flag;
	}	
	
	public Map getMap() {
		return map;
	}

}
