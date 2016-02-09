package com.skronawi.DescentDiceCompanion.app.icons;


import com.skronawi.DescentDiceCompanion.R;

public enum Icon {

    SWORD(R.string.icon_sword_name, R.drawable.icon_attack_sword),
    AXE(R.string.icon_axe_name, R.drawable.icon_attack_axe),
    BOW(R.string.icon_bow_name, R.drawable.icon_attack_bow),
    STAFF(R.string.icon_staff_name, R.drawable.icon_attack_staff),
    HAMMER(R.string.icon_hammer_name, R.drawable.icon_attack_hammer),
    DAGGER(R.string.icon_dagger_name, R.drawable.icon_attack_dagger),
    RUNE(R.string.icon_rune_name, R.drawable.icon_attack_rune),
    SLING(R.string.icon_sling_name, R.drawable.icon_attack_sling),
    SPEAR(R.string.icon_spear_name, R.drawable.icon_attack_spear),
    CROSSBOW(R.string.icon_crossbow_name, R.drawable.icon_attack_crossbow),

    PLATE(R.string.icon_plate_name, R.drawable.icon_defense_plate),
    SHIELD(R.string.icon_shield_name, R.drawable.icon_defense_shield),
    CLOAK(R.string.icon_cloak_name, R.drawable.icon_defense_cloak),
    GAUNTLETS(R.string.icon_gauntlets_name, R.drawable.icon_defense_gauntlets),
    HELMET(R.string.icon_helmet_name, R.drawable.icon_defense_helmet),
    RING(R.string.icon_ring_name, R.drawable.icon_defense_ring),
    BOOTS(R.string.icon_boots_name, R.drawable.icon_defense_boots),

    DIE(R.string.icon_die_name, R.drawable.icon_die);

    private final int nameResourceId;
    private final int imageResourceId;

    Icon(int nameResourceId, int imageResourceId) {
        this.nameResourceId = nameResourceId;
        this.imageResourceId = imageResourceId;
    }

    public int getNameResourceId() {
        return nameResourceId;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}
