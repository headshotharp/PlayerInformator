package de.headshotharp.plugin;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class Tresor {
	private boolean enabled = false;
	private Location a = null, b = null;

	public Tresor(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean loadConfig(FileConfiguration cfg) {
		try {
			a = new Location(PlayerInformator.instance().getServer().getWorld(cfg.getString("tresor.world")),
					cfg.getInt("tresor.a.x"), cfg.getInt("tresor.a.y"), cfg.getInt("tresor.a.z"));
			b = new Location(PlayerInformator.instance().getServer().getWorld(cfg.getString("tresor.world")),
					cfg.getInt("tresor.b.x"), cfg.getInt("tresor.b.y"), cfg.getInt("tresor.b.z"));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void disable() {
		enabled = false;
	}

	public boolean isInitialized() {
		return a != null && b != null;
	}

	public Location getA() {
		return a;
	}

	public Location getB() {
		return b;
	}

	public boolean isIn(Location loc) {
		return loc.getWorld().getName().equals(a.getWorld().getName())
				&& isIn(a.getBlockX(), b.getBlockX(), loc.getBlockX())
				&& isIn(a.getBlockY(), b.getBlockY(), loc.getBlockY())
				&& isIn(a.getBlockZ(), b.getBlockZ(), loc.getBlockZ());
	}

	private boolean isIn(int a, int b, int pos) {
		if (Math.min(a, b) <= pos && Math.max(a, b) >= pos)
			return true;
		return false;
	}
}
