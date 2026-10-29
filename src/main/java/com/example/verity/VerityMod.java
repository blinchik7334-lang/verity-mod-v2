package com.example.verity;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VerityMod implements ModInitializer {
    public static final String MOD_ID = "verity";
    public static final Identifier SYNC = new Identifier(MOD_ID, "sync");
    public static final Identifier TALK = new Identifier(MOD_ID, "talk");

    /** Ник создателя. Читается из config/verity-owner.txt (создаётся при первом запуске). */
    private static volatile String ownerName = "YourNickname";

    /** Серверная карта: игрок -> id формы, в которую он превращён. */
    private static final Map<UUID, String> ACTIVE = new ConcurrentHashMap<>();

    /** Когда последний раз отправляли "говорит" по голосу (чтобы не спамить пакетами). */
    private static final Map<UUID, Long> LAST_VOICE = new ConcurrentHashMap<>();

    private static void loadOwner() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("verity-owner.txt");
        try {
            if (Files.exists(file)) {
                String line = Files.readString(file, StandardCharsets.UTF_8).trim();
                if (!line.isEmpty()) ownerName = line;
            } else {
                Files.writeString(file, ownerName, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("[Verity] Не удалось прочитать config/verity-owner.txt: " + e);
        }
    }

    private static boolean isOwner(ServerCommandSource src) {
        return src.getEntity() instanceof ServerPlayerEntity p
            && p.getGameProfile().getName().equalsIgnoreCase(ownerName);
    }

    @Override
    public void onInitialize() {
        loadOwner();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
            dispatcher.register(CommandManager.literal("verity")
                .requires(VerityMod::isOwner)
                // /verity — включить/выключить форму по умолчанию на себе
                .executes(ctx -> {
                    ServerPlayerEntity self = ctx.getSource().getPlayerOrThrow();
                    toggle(ctx.getSource().getServer(), self, self);
                    return 1;
                })
                // /verity <игрок> — включить/выключить на другом игроке
                .then(CommandManager.argument("target", EntityArgumentType.player())
                    .executes(ctx -> {
                        ServerPlayerEntity owner = ctx.getSource().getPlayerOrThrow();
                        toggle(ctx.getSource().getServer(), owner, EntityArgumentType.getPlayer(ctx, "target"));
                        return 1;
                    }))
                // /verity form <id> [игрок] — выбрать конкретную модель
                .then(CommandManager.literal("form")
                    .then(CommandManager.argument("id", StringArgumentType.word())
                        .suggests((c, b) -> CommandSource.suggestMatching(VerityForms.IDS, b))
                        .executes(ctx -> {
                            ServerPlayerEntity self = ctx.getSource().getPlayerOrThrow();
                            return setForm(ctx.getSource(), self, self, StringArgumentType.getString(ctx, "id"));
                        })
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                            .executes(ctx -> setForm(ctx.getSource(),
                                ctx.getSource().getPlayerOrThrow(),
                                EntityArgumentType.getPlayer(ctx, "target"),
                                StringArgumentType.getString(ctx, "id"))))))));

        // Сообщение в чате -> Verity "говорит" время, пропорциональное длине текста
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            int len = message.getSignedContent().length();
            int ms = Math.max(700, Math.min(5000, len * 80));
            talk(sender, ms);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (Map.Entry<UUID, String> e : ACTIVE.entrySet()) {
                sender.sendPacket(SYNC, pack(e.getKey(), e.getValue()));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUuid();
            LAST_VOICE.remove(id);
            if (ACTIVE.remove(id) != null) broadcast(server, id, null);
        });
    }

    private static int setForm(ServerCommandSource src, ServerPlayerEntity owner, ServerPlayerEntity target, String form) {
        if (!VerityForms.IDS.contains(form)) {
            src.sendError(Text.literal("Неизвестная форма: " + form + ". Доступные: " + String.join(", ", VerityForms.IDS)));
            return 0;
        }
        apply(src.getServer(), owner, target, form);
        return 1;
    }

    private static void toggle(MinecraftServer server, ServerPlayerEntity owner, ServerPlayerEntity target) {
        apply(server, owner, target, ACTIVE.containsKey(target.getUuid()) ? null : VerityForms.DEFAULT);
    }

    /** form == null — вернуть обычный облик. */
    private static void apply(MinecraftServer server, ServerPlayerEntity owner, ServerPlayerEntity target, String form) {
        UUID id = target.getUuid();
        if (form == null) ACTIVE.remove(id); else ACTIVE.put(id, form);
        broadcast(server, id, form);

        boolean on = form != null;
        if (owner == target) {
            owner.sendMessage(Text.literal(on ? "§dТы превратился в Verity (" + form + ")!" : "§7Ты снова в своём облике."), true);
        } else {
            owner.sendMessage(Text.literal((on ? "§dИгрок " : "§7Игрок ") + target.getGameProfile().getName()
                + (on ? " превращён в Verity (" + form + ")." : " снова в своём облике.")), false);
            target.sendMessage(Text.literal(on ? "§dТебя превратили в Verity!" : "§7Ты снова в своём облике."), true);
        }
    }

    /** Вызывается плагином Simple Voice Chat, пока игрок говорит в микрофон. */
    public static void onVoice(ServerPlayerEntity player) {
        long now = System.currentTimeMillis();
        Long last = LAST_VOICE.get(player.getUuid());
        if (last != null && now - last < 150) return;
        LAST_VOICE.put(player.getUuid(), now);
        MinecraftServer server = player.getServer();
        if (server != null) server.execute(() -> talk(player, 350));
    }

    /** Заставить Verity "говорить" ms миллисекунд (только если игрок превращён). */
    public static void talk(ServerPlayerEntity player, int ms) {
        MinecraftServer server = player.getServer();
        if (server == null || !ACTIVE.containsKey(player.getUuid())) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeUuid(player.getUuid());
            buf.writeInt(ms);
            ServerPlayNetworking.send(p, TALK, buf);
        }
    }

    private static void broadcast(MinecraftServer server, UUID id, String form) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, SYNC, pack(id, form));
        }
    }

    private static PacketByteBuf pack(UUID id, String form) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(id);
        buf.writeString(form == null ? "" : form);
        return buf;
    }
}
