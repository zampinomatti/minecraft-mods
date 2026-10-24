package com.example.explosionproof;

import net.fabricmc.api.ClientModInitializer;

/**
 * Entrypoint client. Questo mod non ha bisogno di alcuna logica
 * client-only (nessun rendering, HUD, keybind...): il calcolo della
 * resistenza alle esplosioni avviene identicamente su client e server
 * tramite il mixin su BlockBehaviour. La classe esiste solo per rispettare
 * la struttura standard "split client/server" raccomandata da Loom per i
 * mod 26.1+.
 */
public class ExplosionProofDecorClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Nessuna inizializzazione client-only necessaria per questo mod.
	}
}
