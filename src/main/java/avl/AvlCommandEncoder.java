package avl;

import terrain.TerrainMapAreaOfEffect;
import terrain.TerrainMapCounterAreaCommand;
import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;

public class AvlCommandEncoder extends BasicCommandEncoder {

	public Decorator createDecorator(String type, GamePiece inner) {
		if (type.startsWith(TerrainMapCounterAreaCommand.ID)) {
			return new TerrainMapCounterAreaCommand(type, inner);
		} else
		if (type.startsWith(TerrainMapAreaOfEffect.ID)) {
			return new TerrainMapAreaOfEffect(type, inner);
		} else
		if (type.startsWith(AvlSendToLocation.ID)) {
			return new AvlSendToLocation(type, inner);
		} else {
		return super.createDecorator(type, inner);
		}
	}
}
