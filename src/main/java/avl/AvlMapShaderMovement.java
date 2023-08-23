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
public final class AvlMapShaderMovement extends AvlMapShaderBase {

	private int turnNumber;
	private String[] movementParams;
	private HexRef startHex;
	private Set<HexRef> occupiedByEnemy;
	private Set<HexRef> inEnemyZOC;
	
	public AvlMapShaderMovement() {
		super();
	}
	
	@Override
	public void reset(TerrainMapShader mapShader, Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(mapShader, map, grid, terrainMap, params, piece);
		movementParams = params.split(";");
	}

	@Override
	public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
		if (piece == null) {
			return;
		}
		turnNumber = getTurnNumber();
		filterHostilePieceGroups(movementParams[0]);
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
		//String tf = getHexTerrainName(fromHex); - not needed for movement
		String tt = getHexTerrainName(toHex);
		String et = getEdgeTerrainName(fromHex, toHex);
		String lt = getLineTerrainName(fromHex, toHex);
		//basic cost for movement (all terrain hexes cost 1 MP)
		double cost = 1.0;
		//ZOC check
		boolean fromZOC = inEnemyZOC.contains(fromHex);
		boolean toZOC = inEnemyZOC.contains(toHex);
		//bridge check
		boolean bridge = isAny(lt, RAILROAD, ROAD, ROADANDRAIL);
		//leaving enemy ZOC costs 2 MP
		if (fromZOC) {
			cost += 2.0;
		}
		//entering enemy ZOC costs 2 MP
		if (toZOC) {
			cost += 2.0;
		}
		//movement can't enter water
		if (tt.equals(WATER)) {
			return result(fromHex, false, 0.0, false, null);
		}		
		//movement can't enter enemy occupied hex
		if (occupiedByEnemy.contains(toHex)) {
			return result(fromHex, false, 0.0, false, null);
		}
		//movement can't cross impassable
		if (et.equals(IMPASSABLE)) {
			return result(fromHex, false, 0.0, false, null);
		} else
		//movement can't cross pocket before turn 5
		if (et.equals(STALINGRADPOCKET)) {
			if (turnNumber < 5) {
				return result(fromHex, false, 0.0, false, null);
			}
		} else
		//crossing minor river costs 1 MP if not on a bridge 
		if (et.equals(MINORRIVER)) {
			if (bridge == false) {
				cost += 1.0;
			}
		} else
		if (et.equals(RIVER)) {
			//crossing river is impossible if both hexes are in enemy ZOC
			if ((fromZOC) && (toZOC)) {
				return result(fromHex, false, 0.0, false, null);
			}
			if (bridge == false) {
				if (fromHex.equals(startHex)) {
					//crossing river costs all MP if there is no bridge and the unit starts next to the river
					cost = currentCost.getPointsLeft();
				} else {
					//crossing river is impossible if there is no bridge and the unit doesn't start next to the river
					return result(fromHex, false, 0.0, false, null);
				}
			}
		}
		//OK
		return result(fromHex, true, currentCost.getPointsLeft() - cost, false, null);
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
