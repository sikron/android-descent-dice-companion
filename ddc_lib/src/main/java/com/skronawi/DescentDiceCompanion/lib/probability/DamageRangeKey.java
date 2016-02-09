package com.skronawi.DescentDiceCompanion.lib.probability;

public class DamageRangeKey implements Comparable<DamageRangeKey> {

    private Integer damage;
    private Integer range;


    public DamageRangeKey(Integer damage, Integer range) {
        this.damage = damage;
        this.range = range;
    }


    public Integer getDamage() {
        return damage;
    }


    public void setDamage(Integer damage) {
        this.damage = damage;
    }


    public Integer getRange() {
        return range;
    }


    public void setRange(Integer range) {
        this.range = range;
    }

    public String toString() {
        return "(range:" + range + "/damage:" + damage + ")";
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((damage == null) ? 0 : damage.hashCode());
        result = prime * result + ((range == null) ? 0 : range.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DamageRangeKey other = (DamageRangeKey) obj;
        if (damage == null) {
            if (other.damage != null)
                return false;
        } else if (!damage.equals(other.damage))
            return false;
        if (range == null) {
            if (other.range != null)
                return false;
        } else if (!range.equals(other.range))
            return false;
        return true;
    }


    //order by range ascending, damage ascending
    @Override
    public int compareTo(DamageRangeKey other) {

        if (other.getRange() == getRange()) {
            return getDamage().compareTo(other.getDamage());
        } else {
            return getRange().compareTo(other.getRange());
        }
    }


}
