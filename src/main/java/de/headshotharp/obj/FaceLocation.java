package de.headshotharp.obj;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public enum FaceLocation {
	NORTH(new Vector(0, 0, -1), BlockFace.NORTH), SOUTH(new Vector(0, 0, 1), BlockFace.SOUTH), EAST(new Vector(1, 0, 0),
			BlockFace.EAST), WEST(new Vector(-1, 0, 0),
					BlockFace.WEST), UP(new Vector(0, 1, 0), BlockFace.UP), DOWN(new Vector(0, -1, 0), BlockFace.DOWN);

	private BlockFace face;
	private Vector v;

	FaceLocation(Vector v, BlockFace face) {
		this.v = v;
		this.face = face;
	}

	public BlockFace getBlockFace() {
		return face;
	}

	public static Location getLocation(Location loc, FaceLocation face) {
		return loc.add(face.v);
	}

	public static Location getLocation(Location loc, BlockFace face) {
		return getLocation(loc, getFaceLocationBy(face));
	}

	public static FaceLocation getFaceLocationBy(BlockFace bf) {
		for (FaceLocation fl : values()) {
			if (fl.face == bf)
				return fl;
		}
		return null;
	}

}
