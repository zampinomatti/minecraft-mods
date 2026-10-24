package com.example.explosionproof;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint comune (server + client) del mod.
 * <p>
 * La modifica vera e propria alla resistenza alle esplosioni non avviene qui:
 * è applicata da {@link com.example.explosionproof.mixin.BlockBehaviourExplosionMixin},
 * che intercetta il calcolo della resistenza a runtime. Questa classe serve
 * solo a registrare il mod e loggare un messaggio di avvio.
 */
public class ExplosionProofDecor implements ModInitializer {

	public static final String MOD_ID = "explosionproof_decor";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Valore di resistenza alle esplosioni usato per i blocchi bersaglio.
	 * 3 600 000 è lo stesso valore usato dal Bedrock: nessuna esplosione
	 * vanilla (TNT, Creeper, Wither, Cristallo dell'End, letto, ancora di
	 * respawn...) ha potenza sufficiente a scalfirlo, quindi il blocco è
	 * a tutti gli effetti immune.
	 */
	public static final float TARGET_RESISTANCE = 3_600_000.0F;

	@Override
	public void onInitialize() {
		LOGGER.info("[Explosion-Proof Decor] Sapling e glass pane sono ora a prova di esplosione.");
	}

	/**
	 * Un blocco è "bersaglio" se il suo id registrato (namespace:path)
	 * corrisponde a un sapling o a un glass pane, di qualunque legno/colore.
	 * Si controlla la stringa dell'id invece di fare instanceof su una
	 * classe specifica: è più robusto rispetto a eventuali differenze nei
	 * nomi delle classi tra versioni/mappings, e copre automaticamente
	 * anche eventuali sapling/pane aggiunti da altre mod tramite datapack.
	 */
	public static boolean isTargetBlockPath(String path) {
		if (path == null) {
			return false;
		}
		return path.equals("sapling")
				|| path.endsWith("_sapling")
				|| path.equals("mangrove_propagule") // il mangrove non usa "_sapling"
				|| path.equals("glass_pane")
				|| path.endsWith("_stained_glass_pane");
	}
}
