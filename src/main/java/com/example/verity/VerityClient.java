package com.example.verity;

import com.example.verity.client.VerityModels;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VerityClient implements ClientModInitializer {
    /** Обычный 2D-скин — используется, например, для рук от первого лица. */
    public static final Identifier TEXTURE = new Identifier(VerityMod.MOD_ID, "textures/entity/verity.png");

    /** Клиентская карта: игрок -> id формы. */
    public static final Map<UUID, String> ACTIVE = new ConcurrentHashMap<>();

    /** Игрок -> момент (мс), до которого он "говорит". */
    private static final Map<UUID, Long> TALK_UNTIL = new ConcurrentHashMap<>();

    public static boolean isTalking(UUID id) {
        Long until = TALK_UNTIL.get(id);
        return until != null && System.currentTimeMillis() < until;
    }

    @Override
    public void onInitializeClient() {
        VerityModels.registerAll();

        ClientPlayNetworking.registerGlobalReceiver(VerityMod.SYNC, (client, handler, buf, sender) -> {
            UUID id = buf.readUuid();
            String form = buf.readString();
            client.execute(() -> {
                if (form.isEmpty()) ACTIVE.remove(id); else ACTIVE.put(id, form);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(VerityMod.TALK, (client, handler, buf, sender) -> {
            UUID id = buf.readUuid();
            int ms = buf.readInt();
            client.execute(() -> TALK_UNTIL.merge(id, System.currentTimeMillis() + ms, Math::max));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ACTIVE.clear();
            TALK_UNTIL.clear();
        });
    }
}
