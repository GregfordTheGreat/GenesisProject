package de.sereal.apps.genesisproject.util;

public enum TextAnchor
{
    BOTTOM_LEFT(0),
    MIDDLE_LEFT(1),
    TOP_LEFT(2),
    BOTTOM_CENTER(3),
    MIDDLE_CENTER(4),
    TOP_CENTER(5),
    BOTTOM_RIGHT(6),
    MIDDLE_RIGHT(7),
    TOP_RIGHT(8);
    
    final int value;
    
    private TextAnchor(int value) {
        this.value = value;
    }
}
