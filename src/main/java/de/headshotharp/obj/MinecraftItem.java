package de.headshotharp.obj;

public class MinecraftItem extends Item {
	short durability;

	public MinecraftItem(int id, String name, short durability) {
		super(id, name);
		this.durability = durability;
	}

	public MinecraftItem(int id, String name) {
		super(id, name);
		durability = 0;
	}

	public short getDurability() {
		return durability;
	}

	public void setDurability(short durability) {
		this.durability = durability;
	}
}
