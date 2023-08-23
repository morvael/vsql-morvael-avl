package avl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import terrain.HexRef;
import terrain.TerrainMapShaderRule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.counters.GamePiece;

/**
 * 
 * @author morvael
 */
public abstract class AvlMapShaderBase extends TerrainMapShaderRule {

	protected static final String WATER = "Water";
	protected static final String ROUGH = "Rough";
	protected static final String TOWN = "Town";
	protected static final String CITY = "City";

	protected static final String IMPASSABLE = "Impassable";
	protected static final String RIVER = "River";
	protected static final String MINORRIVER = "MinorRiver";
	protected static final String STALINGRADPOCKET = "StalingradPocket";

	protected static final String RAILROAD = "Railroad";
	protected static final String ROAD = "Road";
	protected static final String ROADANDRAIL = "RoadAndRail";
	
	private Board turnBoard;
	private GamePiece turnPiece;
	private HashMap<HexRef, ArrayList<GamePiece>> friendlyPieceGroups;
	private HashMap<HexRef, ArrayList<GamePiece>> hostilePieceGroups;
	
	protected AvlMapShaderBase() {
		super();
		Map turnMap = Map.getMapById("Turn");
		for (GamePiece gp : turnMap.getPieces()) {
			if (gp.getName().equals("marker-gameturn")) {
				turnPiece = gp;
				break;
			}
		}
		turnBoard = turnMap.getBoardByName("Turn");		
	}
	
	protected void filterFriendlyPieceGroups(String side) {
		if (friendlyPieceGroups == null) {
			friendlyPieceGroups = new HashMap<HexRef, ArrayList<GamePiece>>();
		}
		filterPieceGroups(friendlyPieceGroups, "CurrentMap=Main&&CurrentBoard=Map&&Side=" + side);
	}
	
	protected void filterHostilePieceGroups(String side) {
		if (hostilePieceGroups == null) {
			hostilePieceGroups = new HashMap<HexRef, ArrayList<GamePiece>>();
		}
		filterPieceGroups(hostilePieceGroups, "CurrentMap=Main&&CurrentBoard=Map&&Side=" + side);
	}
	
	protected Set<HexRef> getHexesOccupiedByFriend() {
		return new HashSet<HexRef>(friendlyPieceGroups.keySet());
	}
	
	protected Set<HexRef> getHexesOccupiedByEnemy() {
		return new HashSet<HexRef>(hostilePieceGroups.keySet());
	}
	
	protected Set<HexRef> getHexesInEnemyZOC() {
		Set<HexRef> result = new HashSet<HexRef>();
		HexRef hex;
		for (HexRef hr : hostilePieceGroups.keySet()) {
			hex = null;
			for (GamePiece gp : hostilePieceGroups.get(hr)) {
				if (isMarkerValueAny(gp, "Type", "Unit")) {
					hex = hr;					
					break;
				}
			}
			if (hex != null) {
				HexRef[] neighbours = mapShader.getAdjacentHexes(hex);
				for (int i=0; i<6; i++) {
					if (neighbours[i] == null) break;
					if (getEdgeTerrainName(hex, neighbours[i]).equals(IMPASSABLE) == false) {
						result.add(neighbours[i]);
					}
				}
			}
		}
		return result;
	}
	
	protected int getTurnNumber() {
		if ((turnBoard != null) && (turnPiece != null)) {
			return Integer.parseInt(turnBoard.locationName(
					turnPiece.getPosition()).split(":")[0]);
		} else {
			return 0;
		}
	}
	
	
}
