package com.example.verity.voice;

import com.example.verity.VerityMod;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Интеграция с модом Simple Voice Chat (необязательная).
 * Если игрок говорит в микрофон, Verity открывает рот.
 * Класс загружается только когда мод Simple Voice Chat установлен.
 */
public class VerityVoicePlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return "verity";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection == null) return;
        Object player = connection.getPlayer().getPlayer();
        if (player instanceof ServerPlayerEntity serverPlayer) {
            VerityMod.onVoice(serverPlayer);
        }
    }
}
