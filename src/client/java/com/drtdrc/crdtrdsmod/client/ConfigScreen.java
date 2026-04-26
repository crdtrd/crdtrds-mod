package com.drtdrc.crdtrdsmod.client;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final List<ToggleEntry> entries = new ArrayList<>();

    public ConfigScreen(Screen parent) {
        super(Component.literal("crdtrd's mod - Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        entries.clear();
        ModConfig cfg = ModConfig.get();

        entries.add(new ToggleEntry("Enchanting Encore", () -> cfg.enchantingEncore, v -> cfg.enchantingEncore = v));
        entries.add(new ToggleEntry("Flexible Portals", () -> cfg.flexiblePortals, v -> cfg.flexiblePortals = v));
        entries.add(new ToggleEntry("Go AFK", () -> cfg.goAfk, v -> cfg.goAfk = v));
        entries.add(new ToggleEntry("Tick Warp Sleep", () -> cfg.tickWarpSleep, v -> cfg.tickWarpSleep = v));
        entries.add(new ToggleEntry("Mineable Trials", () -> cfg.mineableTrials, v -> cfg.mineableTrials = v));
        entries.add(new ToggleEntry("Mineable Bedrock", () -> cfg.mineableBedrock, v -> cfg.mineableBedrock = v));
        entries.add(new ToggleEntry("Cocktails", () -> cfg.cocktails, v -> cfg.cocktails = v));
        entries.add(new ToggleEntry("Cheaper Anvils", () -> cfg.cheaperAnvils, v -> cfg.cheaperAnvils = v));
        entries.add(new ToggleEntry("Mineable Spawners", () -> cfg.mineableSpawners, v -> cfg.mineableSpawners = v));
        entries.add(new ToggleEntry("Spawn Egg Drops", () -> cfg.spawnEggDrops, v -> cfg.spawnEggDrops = v));
        entries.add(new ToggleEntry("Delimited Anvils", () -> cfg.delimitedAnvils, v -> cfg.delimitedAnvils = v));
        entries.add(new ToggleEntry("Give Me Recipes", () -> cfg.giveMeRecipes, v -> cfg.giveMeRecipes = v));

        int cols = 2;
        int btnW = 150;
        int btnH = 20;
        int gapX = 10;
        int gapY = 4;
        int totalW = cols * btnW + (cols - 1) * gapX;
        int startX = (this.width - totalW) / 2;
        int startY = 40;

        for (int i = 0; i < entries.size(); i++) {
            ToggleEntry entry = entries.get(i);
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (btnW + gapX);
            int y = startY + row * (btnH + gapY);

            Button btn = Button.builder(entry.displayText(), b -> {
                entry.toggle();
                b.setMessage(entry.displayText());
            }).bounds(x, y, btnW, btnH).build();

            this.addRenderableWidget(btn);
        }

        int doneY = startY + ((entries.size() + cols - 1) / cols) * (btnH + gapY) + 16;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds((this.width - 200) / 2, doneY, 200, 20).build());
    }

    @Override
    public void onClose() {
        ModConfig.save();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    private record ToggleEntry(String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        Component displayText() {
            boolean on = getter.get();
            return Component.literal(label + ": ").append(
                    on ? Component.literal("ON").withColor(0x55FF55)
                            : Component.literal("OFF").withColor(0xFF5555)
            );
        }

        void toggle() {
            setter.accept(!getter.get());
        }
    }
}
