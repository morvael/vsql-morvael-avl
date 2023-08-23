package avl;

import java.util.HashMap;
import java.util.Set;

import terrain.HexRef;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import terrain.TerrainMapShader;
import terrain.TerrainMapShaderCost;
import VASSAL.build.module.Map;
import VASSAL.counters.GamePiece;

/**
 * 
 * @author morvael
 */
public final class AvlMapShaderSupply extends AvlMapShaderBase {

	private int turnNumber;
	private String[] supplyParams;
	private Set<HexRef> occupiedByFriend;
	private Set<HexRef> occupiedByEnemy;
	private Set<HexRef> inEnemyZOC;
	
	public AvlMapShaderSupply() {
		super();
	}
	
	@Override
	public void reset(TerrainMapShader mapShader, Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(mapShader, map, grid, terrainMap, params, piece);
		supplyParams = params.split(";");
	}
	
	@Override
	public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
		turnNumber = getTurnNumber();
		filterFriendlyPieceGroups(supplyParams[2]);
		occupiedByFriend = getHexesOccupiedByFriend();
		filterHostilePieceGroups(supplyParams[1]);
		occupiedByEnemy = getHexesOccupiedByEnemy();
		inEnemyZOC = getHexesInEnemyZOC();
		for (HexRef hr : findHexesWithTag(supplyParams[0], "true")) {
			if (canEnter(hr, getHexTerrainName(hr))) {
				mapShader.addHex(selectedHexes, hr, create(null, true, 1.0, false, null));
			}			
		}
	}

	private boolean canEnter(HexRef toHex, String tt) {
		//supply can't enter water
		if (tt.equals(WATER)) {
			return false;
		}
		//supply can't enter enemy occupied hexes
		if (occupiedByEnemy.contains(toHex)) {
			return false;
		}
		//supply can't enter hexes in enemy ZOC unless a friendly unit is there
		if ((inEnemyZOC.contains(toHex)) && (occupiedByFriend.contains(toHex) == false)) {
			return false;
		}
		//OK
		return true;
	}
	
	@Override
	public boolean getCrossCost(HexRef fromHex, HexRef toHex,
			TerrainMapShaderCost currentCost) {
		//read all terrain info
		//String tf = getHexTerrainName(fromHex); - not needed for supply
		String tt = getHexTerrainName(toHex);
		String et = getEdgeTerrainName(fromHex, toHex);
		String lt = getLineTerrainName(fromHex, toHex);
		//supply can't cross impassable
		if (et.equals(IMPASSABLE)) {
			return result(fromHex, false, 0.0, false, null);
		} else
		//supply can't cross pocket before turn 5
		if (et.equals(STALINGRADPOCKET)) {
			if (turnNumber < 5) {
				return result(fromHex, false, 0.0, false, null);
			}
		} else
		//supply can't cross river if not on a bridge
		if (et.equals(RIVER)) {
			if (isAny(lt, RAILROAD, ROAD, ROADANDRAIL) == false) {
				return result(fromHex, false, 0.0, false, null);
			}
		}
		//check if supply can enter target hex
		if (canEnter(toHex, tt) == false) {
			return result(fromHex, false, 0.0, false, null);
		}
		//OK
		return result(fromHex, true, 1.0, false, null);
	}

	@Override
	public void stop() {
	}
	
	@Override
	public HexRef getBestPathDestination() {
		if (piece != null) {
			return grid.getHexPos(piece.getPosition());
		} else {
			return null;
		}
	}
	
	@Override
	public TerrainMapShaderCost getBestCost(
			HashMap<String, TerrainMapShaderCost> costs) {
		return costs.get(null);
	}	

}
