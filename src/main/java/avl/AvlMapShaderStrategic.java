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
public final class AvlMapShaderStrategic extends AvlMapShaderBase {

	private int turnNumber;
	private String[] strategicParams;
	private HexRef startHex;
	private Set<HexRef> occupiedByEnemy;
	private Set<HexRef> inEnemyZOC;
	
	public AvlMapShaderStrategic() {
		super();
	}
	
	@Override
	public void reset(TerrainMapShader mapShader, Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(mapShader, map, grid, terrainMap, params, piece);
		strategicParams = params.split(";");
	}

	@Override
	public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
		if (piece == null) {
			return;
		}
		turnNumber = getTurnNumber();
		filterHostilePieceGroups(strategicParams[0]);
		occupiedByEnemy = getHexesOccupiedByEnemy();
		inEnemyZOC = getHexesInEnemyZOC();
		startHex = grid.getHexPos(piece.getPosition());
		double mp = Double.parseDouble(piece.getProperty("MP").toString());
		mapShader.addHex(selectedHexes, startHex, create(null, true, mp, false, null));
	}

	@Override
	public boolean getCrossCost(HexRef fromHex, HexRef toHex,
			TerrainMapShaderCost currentCost) {
		//read all terrain info
		//String tf = getHexTerrainName(fromHex); - not needed for strategic
		String tt = getHexTerrainName(toHex);
		String et = getEdgeTerrainName(fromHex, toHex);
		String lt = getLineTerrainName(fromHex, toHex);		
		//strategic can't leave hexes in ZOC
		if (inEnemyZOC.contains(fromHex)) {
			return result(fromHex, false, 0.0, false, null);
		}
		//strategic can't leave road
		if (isAny(lt, ROAD, ROADANDRAIL) == false) {
			return result(fromHex, false, 0.0, false, null);
		}
		//strategic can't cross impassable
		if (et.equals(IMPASSABLE)) {
			return result(fromHex, false, 0.0, false, null);
		} else
		//strategic can't cross pocket before turn 5
		if (et.equals(STALINGRADPOCKET)) {
			if (turnNumber < 5) {
				return result(fromHex, false, 0.0, false, null);
			}
		}
		//strategic can't enter water
		if (tt.equals(WATER)) {
			return result(fromHex, false, 0.0, false, null);
		}
		//strategic can't enter enemy occupied hexes
		if (occupiedByEnemy.contains(toHex)) {
			return result(fromHex, false, 0.0, false, null);
		}
		//strategic can't enter hexes in enemy ZOC
		if (inEnemyZOC.contains(toHex)) {
			return result(fromHex, false, 0.0, false, null);
		}
		//OK
		return result(fromHex, true, currentCost.getPointsLeft()-0.5, false, null);
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
