package de.headshotharp.obj;

import org.bukkit.enchantments.Enchantment;

public enum EnchantmentHelper {

    // @formatter:off
	PROTECTION_ENVIRONMENTAL(Enchantment.PROTECTION_ENVIRONMENTAL, 4, "Protection", "Schutz", "Erhöht den Schutz von Rüstungen."),
	PROTECTION_FIRE(Enchantment.PROTECTION_FIRE, 4, "Fire Protection", "Feuerschutz", "Erhöht den Schutz gegen Feuer. Reduziert außerdem die Dauer, wie lange man brennt."),
	PROTECTION_FALL(Enchantment.PROTECTION_FALL, 4, "Feather Falling", "Federfall", "Vermindert den Fallschaden."),
	PROTECTION_EXPLOSIONS(Enchantment.PROTECTION_EXPLOSIONS, 4, "Blast Protection", "Explosionsschutz", "Erhöht den Schutz gegen Explosionen. Reduziert außerdem die Stürke der Druckwelle, mit der man zurückgeworfen wird."),
	PROTECTION_PROJECTILE(Enchantment.PROTECTION_PROJECTILE, 4, "Projectile Protection", "Schusssicher", "Erhöht den Schutz gegen Projektile, z. B. Pfeile, Feuerbälle etc."),
	OXYGEN(Enchantment.OXYGEN, 3, "Respiration", "Atmung", "Erhöht die Atemzeit unter Wasser und verzögert den Eintritt von Erstickungsschaden."),
	WATER_WORKER(Enchantment.WATER_WORKER, 1, "Aqua Affinity", "Wasseraffinität", "Erhöht die Abbaugeschwindigkeit unter Wasser."),
	MENDING(Enchantment.MENDING, 1, "Mending", "Reparatur", "Reparatur durch Erfahrungspunkte."),
	THORNS(Enchantment.THORNS, 3, "Thorns", "Dornen", "Schädigt einen Angreifer."),
	DEPTH_STRIDER(Enchantment.DEPTH_STRIDER, 3, "Depth Strider", "Wasserläufer", "Erhöht die Schwimmgeschwindigkeit unter Wasser."),
	FROST_WALKER(Enchantment.FROST_WALKER, 2, "Frost Walker", "Eisläufer", "Verwandelt Wasserblöcke im Umkreis zu brüchigem Eis."),
	DAMAGE_ALL(Enchantment.DAMAGE_ALL, 5, "Sharpness", "Schärfe", "Erhöht Schaden gegen Spieler und alle Arten von Kreaturen."),
	DAMAGE_UNDEAD(Enchantment.DAMAGE_UNDEAD, 5, "Smite", "Bann", "Erhöht Schaden gegen Zombies, Schweinezombies, Skelette, Witherskelette und Wither."),
	DAMAGE_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS, 5, "Bane of Arthropods", "Nemesis der Gliederfüßer", "Erhöht Schaden gegen Spinnen, Höhlenspinnen und Silberfischchen."),
	KNOCKBACK(Enchantment.KNOCKBACK, 2, "Knockback", "Rückstoß", "Wirft Spieler und Kreaturen beim Schlagen ein Stück zurück."),
	FIRE_ASPECT(Enchantment.FIRE_ASPECT, 2, "Fire Aspect", "Verbrennung", "Steckt getroffene Gegner in Brand."),
	LOOT_BONUS_MOBS(Enchantment.LOOT_BONUS_MOBS, 3, "Looting", "Plünderung", "Lässt Kreaturen mehr Gegenstände droppen."),
	DIG_SPEED(Enchantment.DIG_SPEED, 5, "Efficiency", "Effizienz", "Erhöht die Abbaugeschwindigkeit."),
	SILK_TOUCH(Enchantment.SILK_TOUCH, 1, "Silk Touch", "Behutsamkeit", "Lässt Blöcke sich selbst droppen, auch solche, die sonst einen Gegenstand droppen."),
	DURABILITY(Enchantment.DURABILITY, 3, "Unbreaking", "Haltbarkeit", "Verlängert die Haltbarkeit des Werkzeugs."),
	LOOT_BONUS_BLOCKS(Enchantment.LOOT_BONUS_BLOCKS, 3, "Fortune", "Glück", "Erhöht die Drop-Anzahl beim Abbau von Blöcken."),
	ARROW_DAMAGE(Enchantment.ARROW_DAMAGE, 5, "Power", "Stärke", "Erhöht Schaden gegen Spieler und Kreaturen."),
	ARROW_KNOCKBACK(Enchantment.ARROW_KNOCKBACK, 2, "Punch", "Schlag", "Wirft Spieler und Kreaturen ein Stück zurück, wenn sie getroffen werden."),
	ARROW_FIRE(Enchantment.ARROW_FIRE, 1, "Flame", "Flamme", "Setzt getroffene Gegner mithilfe von brennenden Pfeilen in Brand"),
	ARROW_INFINITE(Enchantment.ARROW_INFINITE, 1, "Infinity", "Unendlich", "Ermöglicht es mit dem Bogen unendlich Pfeile zu schießen, solange man mindestens einen Pfeil im Inventar hat."),
	LUCK(Enchantment.LUCK, 3, "Luck of the Sea", "Glück des Meeres", "Verringert die Wahrscheinlichkeit, wertlosen Müll zu angeln"),
	LURE(Enchantment.LURE, 3, "Lure", "Köder", "Erhöht die Anzahl gefangener Fische in gleicher Zeit."),
	BINDING_CURSE(Enchantment.BINDING_CURSE, 1, "Curse of Binding", "Fluch der Bindung", "Verhindert das Herausnehmen des Gegenstandes aus dem Rüstungsslot."),
	SWEEPING_EDGE(Enchantment.SWEEPING_EDGE, 3, "Sweeping", "Schwungkraft", "Der Schwungschlag von Schwertern macht mehr Schaden."),
	VANISHING_CURSE(Enchantment.VANISHING_CURSE, 1, "Curse of Vanishing", "Fluch des Verschwindens", "Lässt Gegenstände beim Tod nicht fallen, sondern verschwinden.");
	// @formatter:on

    private Enchantment ench;
    private String name;
    private String german;
    private String desc;
    private int maxlevel;

    private EnchantmentHelper(Enchantment ench, int maxlevel, String name, String german, String desc) {
        this.ench = ench;
        this.name = name;
        this.german = german;
        this.desc = desc;
        this.maxlevel = maxlevel;
    }

    public Enchantment getEnchantment() {
        return ench;
    }

    public String getTrivialName() {
        return name;
    }

    public int getMaxLevel() {
        return maxlevel;
    }

    public String getGermanTrivialName() {
        return german;
    }

    public String getGermanDescription() {
        return desc;
    }

    public String getName() {
        return ench.getKey().toString();
    }

    public String getEnumName() {
        return name();
    }

    /**
     * return trivial name + max level in roman number format (eg. Efficeiency IV)
     *
     * @return
     */
    public String getNormalName() {
        return getTrivialName() + " " + intToRoman(getMaxLevel());
    }

    public static EnchantmentHelper byName(String name) {
        for (EnchantmentHelper e : values()) {
            if (e.name().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    public static String getMysqlTableCreation(String table) {
        StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ").append(table).append(";\nCREATE TABLE ")
                .append(table)
                .append(" (\n\tid INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n\tbukkitname VARCHAR(100),\n\tmaxlevel INT,\n\tname VARCHAR(100),\n\tgermanname VARCHAR(100),\n\tdescription TEXT,\n\tprice INT DEFAULT 10000\n);\n");
        for (EnchantmentHelper ench : EnchantmentHelper.values()) {
            sql.append("INSERT INTO ").append(table)
                    .append(" (bukkitname, maxlevel, name, germanname, description) VALUES ('")
                    .append(ench.getEnumName()).append("', ").append(ench.getMaxLevel()).append(", '")
                    .append(ench.getTrivialName()).append("', '").append(ench.getGermanTrivialName()).append("', '")
                    .append(ench.getGermanDescription()).append("');\n");
        }
        return sql.toString();
    }

    public static String intToRoman(int value) {
        StringBuilder roman = new StringBuilder();
        if (value == 0) {
            return "0";
        }
        if (value < 0) {
            return "" + value;
        }
        if (value <= 3999) {
            while (value / 1000 >= 1) {
                roman.append("M");
                value = value - 1000;
            }
            if (value / 900 >= 1) {
                roman.append("CM");
                value = value - 900;
            }
            if (value / 500 >= 1) {
                roman.append("D");
                value = value - 500;
            }
            if (value / 400 >= 1) {
                roman.append("CD");
                value = value - 400;
            }
            while (value / 100 >= 1) {
                roman.append("C");
                value = value - 100;
            }
            if (value / 90 >= 1) {
                roman.append("XC");
                value = value - 90;
            }
            if (value / 50 >= 1) {
                roman.append("L");
                value = value - 50;
            }
            if (value / 40 >= 1) {
                roman.append("XL");
                value = value - 40;
            }
            while (value / 10 >= 1) {
                roman.append("X");
                value = value - 10;
            }
            if (value / 9 >= 1) {
                roman.append("IX");
                value = value - 9;
            }
            if (value / 5 >= 1) {
                roman.append("V");
                value = value - 5;
            }
            if (value / 4 >= 1) {
                roman.append("IV");
                value = value - 4;
            }
            while (value >= 1) {
                roman.append("I");
                value = value - 1;
            }
            return roman.toString();
        } else {
            return "" + value;
        }
    }

    public static void main(String... args) {
        System.out.println(getMysqlTableCreation("enchantments"));
    }
}
