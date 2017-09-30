package de.headshotharp.obj;

import java.util.ArrayList;
import java.util.List;

public class ItemShopItem {
	private int id;
	private String name;
	private String mc_item;
	private List<EnchantmentItem> enchantmentItems = new ArrayList<EnchantmentItem>();

	public ItemShopItem(int id, String name, String mc_item) {
		this.id = id;
		this.name = name;
		this.mc_item = mc_item;
	}

	public List<EnchantmentItem> getEnchantmentItems() {
		return enchantmentItems;
	}

	public void setEnchantmentItems(List<EnchantmentItem> enchantmentItems) {
		this.enchantmentItems = enchantmentItems;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getMcItem() {
		return mc_item;
	}
}
