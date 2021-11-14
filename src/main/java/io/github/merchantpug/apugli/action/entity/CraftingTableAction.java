package io.github.merchantpug.apugli.action.entity;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.merchantpug.apugli.Apugli;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class CraftingTableAction {
    private static final Text TITLE = new TranslatableText("container.crafting");

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return;

        player.openHandledScreen(
                new SimpleNamedScreenHandlerFactory((i, inventory, _player) ->
                    new CraftingScreenHandler(i, inventory),
                    TITLE
                )
        );

        player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apugli.identifier("crafting_table"),
                new SerializableData(),
                CraftingTableAction::action
        );
    }
}