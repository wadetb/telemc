package com.wadeb.telemc.client.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wadeb.telemc.client.TeleMCGetDataInterface;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClickableWidget.class)
public class TeleMCClickableWidgetMixin implements TeleMCGetDataInterface {

    @Shadow private int x;
    @Shadow private int y;
    @Shadow private int width;
    @Shadow private int height;
    @Shadow private Text message;
    @Shadow private boolean active;
    @Shadow private boolean visible;
    @Shadow private boolean focused;
    @Shadow private boolean hovered;
    // @Shadow private Tooltip tooltip;

    @Inject(at = @At("TAIL"), method = "<init>(IIIILnet/minecraft/text/Text;)V")
    public void ClickableWidget(int x, int y, int width, int height, Text message, CallbackInfo info) {
        // // Screen screen = MinecraftClient.getInstance().currentScreen;
        // // Get the class name as a string
        // System.out.println("ClickableWidget: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", message=" + message.getString());
    }

    public Map<String,Object> getTeleMCData() {
        Map<String,Object> data = new HashMap<String,Object>();
        data.put("class", this.getClass().getName());
        data.put("x", this.x);
        data.put("y", this.y);
        data.put("width", this.width);
        data.put("height", this.height);
        data.put("message", this.message.getString());
        data.put("active", this.active);
        data.put("visible", this.visible);
        data.put("focused", this.focused);
        data.put("hovered", this.hovered);
        // if (this.tooltip != null) {
        //     StringBuilder tooltipBuilder = new StringBuilder();
        //     List<OrderedText> lines =  this.tooltip.getLines(MinecraftClient.getInstance());
        //     for (OrderedText line : lines) {
        //         line.accept((j, style, codePoint) -> {
        //             tooltipBuilder.append(Character.toChars(codePoint));
        //             return true;
        //         });
        //     }
        //     data.put("tooltip", tooltipBuilder.toString());
        // }
        return data;
    }
}
