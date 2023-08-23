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
public final class AvlMapShaderReinforcement extends AvlMapShaderBase {

	private int turnNumber;
	private String[] reinforcementParams;
	private Set<HexRef> occupiedByEnemy;
	private Set<HexRef> inEnemyZOC;

	//This rule class is optimized to use only two cost objects since we know it won't be used in connection with a given game piece (and no track-back)
	private TerrainMapShaderCost costOK = create(null, true, 1.0d, false, null);
	private TerrainMapShaderCost costXX = create(null, false, 0.0d, false, null);
	
	public AvlMapShaderReinforcement() {
		super();
	}
	
	@Override
	public void reset(TerrainMapShader mapShader, Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(mapShader, map, grid, terrainMap, params, piece);
		reinforcementParams = params.split(";");
	}
	
	@Override
	public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
		turnNumber = getTurnNumber();
		filterHostilePieceGroups(reinforcementParams[1]);
		occupiedByEnemy = getHexesOccupiedByEnemy();
		inEnemyZOC = getHexesInEnemyZOC();
		for (HexRef hr : findHexesWithTag(reinforcementParams[0], "true")) {
			if (canEnter(hr, getHexTerrainName(hr))) {
				mapShader.addHex(selectedHexes, hr, costOK);
			}			
		}
	}

	private boolean canEnter(HexRef toHex, String tt) {
		//reinforcements can't enter water
		if (tt.equals(WATER)) {
			return false;
		}
		//reinforcements can't enter enemy occupied hexes
		if (occupiedByEnemy.contains(toHex)) {
			return false;
		}
		//reinforcements can't enter hexes in enemy ZOC
		if (inEnemyZOC.contains(toHex)) {
			return false;
		}
		//OK
		return true;
	}
	
	@Override
	public boolean getCrossCost(HexRef fromHex, HexRef toHex,
			TerrainMapShaderCost currentCost) {
		//read all terrain info
		//String tf = getHexTerrainName(fromHex); - not needed for reinforcements
		String tt = getHexTerrainName(toHex);
		String et = getEdgeTerrainName(fromHex, toHex);
		String lt = getLineTerrainName(fromHex, toHex);
		//reinforcements can't leave rail
		if (isAny(lt, RAILROAD, ROADANDRAIL) == false) {
			return result(costXX);
		}
		//reinforcements can't cross impassable
		if (et.equals(IMPASSABLE)) {
			return result(costXX);
		} else
		//reinforcements can't cross pocket before turn 5
		if (et.equals(STALINGRADPOCKET)) {
			if (turnNumber < 5) {
				return result(costXX);
			}
		}
		//check if reinforcements can enter target hex
		if (canEnter(toHex, tt) == false) {
			return result(costXX);
		}
		//OK
		return result(costOK);
	}

	@Override
	public void stop() {
	}
	
	@Override
	public HexRef getBestPathDestination() {
		return null;
	}

	@Override
	public TerrainMapShaderCost getBestCost(
			HashMap<String, TerrainMapShaderCost> costs) {
		return costs.get(null);
	}
	
}
