package com.maximumg9.shadow.config;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.RoleManager;

public class Config {
    public int worldBorderSize = 150;
    public int roleSlotCount = 25;
    public int overworldEyes = 8;
    public int netherEyes = 8;
    public int netherRoofEyes = 8;
    public Food food = Food.BREAD;
    public int foodAmount = 16;

    public Config(Shadow shadow) {
        this.roleManager = new RoleManager(shadow,this);
    }

    public RoleManager roleManager;
}
