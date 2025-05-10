package com.maximumg9.shadow.config;

import com.maximumg9.shadow.Shadow;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public class Config {
    public int worldBorderSize = 150;
    public int roleSlotCount = 25;
    public int overworldEyes = 8;
    public int netherEyes = 8;
    public int netherRoofEyes = 8;
    public Food food = Food.BREAD;
    public int foodAmount = 16;

    public Config(Shadow shadow, Path saveFile) {
        this.roleManager = new RoleManager(shadow, this);
        this.saveFile = saveFile;
    }

    private void readNbt(NbtCompound nbt) {
        this.worldBorderSize = nbt.getInt("worldBorderSize");
        this.roleSlotCount = nbt.getInt("roleSlotCount");
        this.overworldEyes = nbt.getInt("overworldEyes");
        this.netherEyes = nbt.getInt("netherEyes");
        this.netherRoofEyes = nbt.getInt("netherRoofEyes");
        this.food = Food.valueOf(nbt.getString("food"));
        this.foodAmount = nbt.getInt("foodAmount");

        this.roleManager.readNbt(nbt.getCompound("roleManager"));
    }

    private NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("worldBorderSize", this.worldBorderSize);
        nbt.putInt("roleSlotCount", this.roleSlotCount);
        nbt.putInt("overworldEyes", this.overworldEyes);
        nbt.putInt("netherEyes", this.netherEyes);
        nbt.putInt("netherRoofEyes", this.netherRoofEyes);
        nbt.putString("food", this.food.name());
        nbt.putInt("foodAmount", this.foodAmount);

        nbt.put("roleManager", this.roleManager.writeNbt(new NbtCompound()));

        return nbt;
    }

    public Config copy(Shadow shadow) {
        Config newConfig = new Config(shadow, this.saveFile);

        newConfig.readNbt(this.writeNbt(new NbtCompound()));

        return newConfig;
    }

    public void load() throws IOException {
        NbtCompound compound = NbtIo.read(saveFile);
        if(compound == null) throw new FileNotFoundException("Could not find config file");
        this.readNbt(compound);
    }

    public void save() throws IOException {
        NbtIo.writeCompressed(this.writeNbt(new NbtCompound()), saveFile);
    }

    private final Path saveFile;
    public final RoleManager roleManager;
}
