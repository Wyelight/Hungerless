package wyelight.hungerless.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends Screen {
    private final Screen previous;

    public ConfigScreen(Screen previous) {
        super(Component.literal("Hungerless Options"));
        this.previous = previous;
    }

    @Override
    protected void init() {
        Config.read();

        this.addRenderableWidget(CycleButton.onOffBuilder(Config.movementRework)
                .create(this.width / 2 - 75, this.height / 6, 150, 20,
                        Component.literal("Enable Movement Rework"), (button, value) -> Config.movementRework = value));

        this.addRenderableWidget(CycleButton.onOffBuilder(Config.autoSwim)
                .create(this.width / 2 - 75, this.height / 6 + 24, 150, 20,
                        Component.literal("Enable Auto Swim"), (button, value) -> Config.autoSwim = value));
        //this.addRenderableWidget(new Button(
                //this.width / 2 - 100, this.height - 27, 200, 20,
                //CommonComponents.GUI_DONE, button -> this.onClose()));
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(previous);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(0);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    private void renderDirtBackground(int i) {

    }

    @Override
    public void removed() {
        Config.save();
    }
}