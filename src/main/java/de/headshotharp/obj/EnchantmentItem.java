package de.headshotharp.obj;

import de.headshotharp.enchantment.helper.EnchantmentHelper;

public class EnchantmentItem {
	private int id;
	private EnchantmentHelper ench;
	private int level;

	public EnchantmentItem(int id, String name) {
		this.id = id;
		ench = EnchantmentHelper.byName(name);
		level = 0;
	}

	public EnchantmentItem(int id, String name, int level) {
		this.id = id;
		ench = EnchantmentHelper.byName(name);
		this.level = level;
	}

	public int getId() {
		return id;
	}

	public EnchantmentHelper getEnch() {
		return ench;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return id + ": " + getEnch().getNormalName();
	}
}
