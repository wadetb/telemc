package com.wadeb.telemc.client;

import java.util.List;

import net.minecraft.client.gui.Selectable;

public interface TeleMCScreenInterface {
    public String getNarratorText();
    public List<Selectable> getSelectables();
}
