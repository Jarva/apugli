package io.github.merchantpug.apugli;

import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.merchantpug.apugli.registry.*;
import io.github.merchantpug.apugli.registry.action.ApugliBlockActions;
import io.github.merchantpug.apugli.registry.action.ApugliEntityActions;
import io.github.merchantpug.apugli.registry.condition.ApugliBiEntityConditions;
import io.github.merchantpug.apugli.registry.condition.ApugliBlockConditions;
import io.github.merchantpug.apugli.registry.condition.ApugliDamageConditions;
import io.github.merchantpug.apugli.registry.condition.ApugliEntityConditions;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Apugli implements ModInitializer {
	public static final String MODID = "apugli";
	public static final Logger LOGGER = LogManager.getLogger(Apugli.class);

	@Override
	public void onInitialize() {
		LOGGER.info("Apugli has initialized. Powering up your powered up game.");
		ApugliBlockActions.register();
		ApugliEntityActions.register();
		ApugliBiEntityConditions.register();
		ApugliBlockConditions.register();
		ApugliDamageConditions.register();
		ApugliEntityConditions.register();
		ApugliPowerFactories.register();

		NamespaceAlias.addAlias("ope", MODID);
	}


	public static Identifier identifier(String path) {
		return new Identifier(MODID, path);
	}
}
