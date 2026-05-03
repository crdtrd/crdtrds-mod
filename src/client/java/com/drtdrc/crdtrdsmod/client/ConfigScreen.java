package com.drtdrc.crdtrdsmod.client;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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

        entries.add(new ToggleEntry("Enchanting Encore",
                "Extended enchantingencore table range, bookshelf bias from chiseled bookshelves, enhanced protection, and water depth strider boost",
                () -> cfg.enchantingEncore, v -> cfg.enchantingEncore = v));
        entries.add(new ToggleEntry("Flexible Portals",
                "Breaking an end portal frame block removes connected portal blocks",
                () -> cfg.flexiblePortals, v -> cfg.flexiblePortals = v));
        entries.add(new ToggleEntry("Go AFK",
                "Use /afk to go AFK and keep your chunks loaded while disconnected",
                () -> cfg.goAfk, v -> cfg.goAfk = v));
        entries.add(new ToggleEntry("Tick Warp Sleep",
                "Accelerates the game tick rate while players are sleeping for faster nights",
                () -> cfg.tickWarpSleep, v -> cfg.tickWarpSleep = v));
        entries.add(new ToggleEntry("Mineable Trials",
                "Makes vault and trial spawner blocks mineable with the correct tool",
                () -> cfg.mineableTrials, v -> cfg.mineableTrials = v));
        entries.add(new ToggleEntry("Mineable Bedrock",
                "Makes bedrock breakable with a very high hardness value",
                () -> cfg.mineableBedrock, v -> cfg.mineableBedrock = v));
        entries.add(new ToggleEntry("Cocktails",
                "Blend multiple potions together in a crafting table to combine their effects",
                () -> cfg.cocktails, v -> cfg.cocktails = v));
        entries.add(new ToggleEntry("Cheaper Anvils",
                "Reduces anvil repair cost scaling from +4 to +2 per level",
                () -> cfg.cheaperAnvils, v -> cfg.cheaperAnvils = v));
        entries.add(new ToggleEntry("Mineable Spawners",
                "Spawner blocks can be picked up with Silk Touch",
                () -> cfg.mineableSpawners, v -> cfg.mineableSpawners = v));
        entries.add(new ToggleEntry("Spawn Egg Drops",
                "Mobs have a small chance to drop their spawn egg on death",
                () -> cfg.spawnEggDrops, v -> cfg.spawnEggDrops = v));
        entries.add(new ToggleEntry("Delimited Anvils",
                "Removes the anvil level 40 cost limit",
                () -> cfg.delimitedAnvils, v -> cfg.delimitedAnvils = v));
        entries.add(new ToggleEntry("Give Me Recipes",
                "Automatically unlocks all crafting recipes when joining a world",
                () -> cfg.giveMeRecipes, v -> cfg.giveMeRecipes = v));
        entries.add(new ToggleEntry("CurseStone",
                "Allows the grindstone to remove curse enchantments from items",
                () -> cfg.curseStone, v -> cfg.curseStone = v));
        entries.add(new ToggleEntry("Compostable Flesh",
                "Makes rotten flesh compostable in the composter",
                () -> cfg.compostableFlesh, v -> cfg.compostableFlesh = v));

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
            }).bounds(x, y, btnW, btnH)
                    .tooltip(Tooltip.create(Component.literal(entry.description())))
                    .build();

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
        graphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        graphics.centeredText(this.font, Component.literal("Changes require a game restart to take effect"), this.width / 2, 25, 0xAAAAAA);
    }

    private record ToggleEntry(String label, String description, Supplier<Boolean> getter, Consumer<Boolean> setter) {
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
