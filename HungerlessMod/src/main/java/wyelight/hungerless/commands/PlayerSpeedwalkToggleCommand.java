package wyelight.hungerless.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

import static wyelight.hungerless.Hungerless.EventListener.PlayerSpeedWalk;

public class PlayerSpeedwalkToggleCommand {
    public PlayerSpeedwalkToggleCommand(CommandDispatcher<CommandSourceStack> dispatcher){
            dispatcher.register(Commands.literal("playerspeedwalk").then(Commands.literal("disable")).executes((command) -> {
                return disablePlayerSpeedwalk(command.getSource());
            }));
    }
    private int disablePlayerSpeedwalk(CommandSourceStack sourceStack) throws CommandSyntaxException {
        ServerPlayer player = sourceStack.getPlayer();
        PlayerSpeedWalk(Objects.requireNonNull(player));
        System.out.print("Command togglePlayerSpeedwalk ran");
        return 1;
    }

}
