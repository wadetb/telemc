package com.wadeb.telemc.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.ScreenNarrator;

import java.util.List;

import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.wadeb.telemc.client.TeleMCScreenInterface;

@Mixin(Screen.class)
public class TeleMCScreenMixin implements TeleMCScreenInterface {

    @Shadow
    private ScreenNarrator narrator;

    @Shadow
    private List<Selectable> selectables;

    public String getNarratorText() {
        // this.narrator.buildNarrations(Screen::addScreenNarrations);
        return this.narrator.buildNarratorText(true);
    }

    public List<Selectable> getSelectables() {
        return this.selectables;
    }
}
