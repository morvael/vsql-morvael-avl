package avl;

import java.util.HashMap;

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
public final class AvlMapShaderCommand extends AvlMapShaderBase {

	public AvlMapShaderCommand() {
		super();
	}
	
	@Override
	public void reset(TerrainMapShader mapShader, Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(mapShader, map, grid, terrainMap, params, piece);
	}

	@Override
	public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
		if (piece == null) {
			return;
		}
		double range = Double.parseDouble(piece.getProperty("Range").toString());
		mapShader.addHex(selectedHexes, grid.getHexPos(piece.getPosition()), create(null, true, range, false, null));
	}

	@Override
	public boolean getCrossCost(HexRef fromHex, HexRef toHex,
			TerrainMapShaderCost currentCost) {
		//read all terrain info
		//String tf = getHexTerrainName(fromHex); - not needed for command
		String tt = getHexTerrainName(toHex);
		String et = getEdgeTerrainName(fromHex, toHex);
		String lt = getLineTerrainName(fromHex, toHex);
		//command can't cross impassable
		if (et.equals(IMPASSABLE)) {
			return result(fromHex, false, 0.0, false, null);
		} else
		//command must stop after crossing a river if not on a bridge
		if (et.equals(RIVER)) {
			if (isAny(lt, RAILROAD, ROAD, ROADANDRAIL) == false) {
				return result(fromHex, true, 0.0, false, null);
			}
		}
		//command can't enter water
		if (tt.equals(WATER)) {
			return result(fromHex, false, 0.0, false, null);
		}
		//all checks passed
		return result(fromHex, true, currentCost.getPointsLeft()-1.0, false, null);
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
