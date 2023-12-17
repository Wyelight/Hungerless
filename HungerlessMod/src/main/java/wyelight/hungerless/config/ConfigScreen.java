package wyelight.hungerless.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends Screen {
    protected final Screen parent;

    protected Checkbox movementReworkWidget;
    protected Checkbox mobMovementReworkWidget;
    protected Checkbox bonusEffectsWidget;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Hungerless Options"));
        this.parent = parent;
    }

    @Override
    public void removed() {
        Config.save();
    }

    @Override
    public void onClose() {
        Config.movementRework = movementReworkWidget.selected();
        Config.mobMovementRework = mobMovementReworkWidget.selected();
        Config.bonusEffects = bonusEffectsWidget.selected();
        Config.save();
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        Config.read();
        movementReworkWidget = new Checkbox(width / 2 - 100, 27*1, 200, 20, Component.literal("Sprint Replaced By Speed Walk"), Config.movementRework) {

            public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
                super.renderWidget(matrices, mouseX, mouseY, delta);
                /*
                if (isHovered) {
                    ConfigScreen.this.renderTooltip(matrices, Component.literal("makes movement speed faster"), mouseX, mouseY);
                }*/
            }
        };
        mobMovementReworkWidget = new Checkbox(width / 2 - 100, 27*2, 200, 20, Component.literal("Melee Mob Speed Adjustments"), Config.movementRework) {

            public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
                super.renderWidget(matrices, mouseX, mouseY, delta);
                /*
                if (isHovered) {
                    ConfigScreen.this.renderTooltip(matrices, Component.literal("makes movement speed faster"), mouseX, mouseY);
                }*/
            }
        };
        bonusEffectsWidget = new Checkbox(width / 2 - 100, 27*3, 200, 20, Component.literal("Food Bonus Effects"), Config.bonusEffects) {
            //@Override

            public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
                super.renderWidget(matrices, mouseX, mouseY, delta);
            }
        };

        addRenderableWidget(movementReworkWidget);
        addRenderableWidget(mobMovementReworkWidget);
        addRenderableWidget(bonusEffectsWidget);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (buttonWidget) -> {
            onClose();
        }).bounds(width / 2 - 100, height - 27, 200, 20).build());
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, int i, int j, float f) {
        renderBackground(matrixStack);
        drawCenteredString(matrixStack, font, title, width / 2, 5, 16777215);

        super.render(matrixStack, i, j, f);
    }
}