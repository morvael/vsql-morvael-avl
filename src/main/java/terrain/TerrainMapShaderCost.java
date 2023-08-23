package terrain;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 
 * @author morvael
 */
public final class TerrainMapShaderCost implements Comparable<TerrainMapShaderCost> {

	private HexRef from;
	private boolean mayContinue;
	private double pointsLeft;
	private boolean traceOnly;
	private String flag;
	
	public TerrainMapShaderCost(TerrainMapShaderRule r) {
		override(r);
	}
	
	public void override(TerrainMapShaderRule r) {
		this.from = r.getFrom();
		this.mayContinue = r.isMayContinue();
		this.pointsLeft = r.getPointsLeft();
		this.traceOnly = r.isTraceOnly();
		this.flag = r.getFlag();
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
	
	public int compareTo(TerrainMapShaderCost o) {
		int result = compareBoolean(mayContinue, o.isMayContinue());
		if (result == 0) {
			result = compareBoolean(traceOnly, o.isTraceOnly());
			if (result == 0) {
				result = -Double.compare(pointsLeft, o.getPointsLeft());
			}
		}
		return result;	
	}
	
	public static int compareBoolean(boolean b1, boolean b2) {
		if (b1 == true) {
			if (b2 == true) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (b2 == true) {
				return 1;
			} else {
				return 0;
			}
			
		}
	}
	
}
