package com.example.verity.client;

import com.example.verity.VerityMod;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Реестр 3D-форм. Чтобы добавить новую — смотри метод registerAll(). */
public final class VerityModels {

    /**
     * Одна форма: id, текстура, текстура "говорит" (может быть null), слой модели и масштаб.
     * Если layer == null — рисуется гладкий шар (SphereMesh).
     * Если layer задан — рисуется своя модель из боксов (см. SmileModel как пример).
     */
    public record Form(String id, Identifier texture, Identifier talkTexture, EntityModelLayer layer,
                       Supplier<TexturedModelData> data, float scale) {}

    private static final Map<String, Form> FORMS = new LinkedHashMap<>();
    private static final Map<String, ModelPart> BAKED = new HashMap<>();

    public static void registerAll() {
        // Все три формы — круглый шар (layer = null)
        add(new Form("smile", tex("smile_ball"), null, null, null, 1.0f));
        add(new Form("evil", tex("evil_ball"), null, null, null, 1.0f));
        add(new Form("talk", tex("talk_ball"), tex("talk_ball_open"), null, null, 1.0f));

        // ===== ДОБАВЛЯЙ НОВЫЕ ФОРМЫ ЗДЕСЬ =====
        // Ещё один шар с другим лицом:
        //   add(new Form("my", tex("my_ball"), null, null, null, 1.0f));
        // Своя модель из боксов (по примеру SmileModel):
        //   add(new Form("my", tex("my"), null, MyModel.LAYER, MyModel::createModelData, 1.0f));

        for (Form f : FORMS.values()) {
            if (f.layer() != null) {
                EntityModelLayerRegistry.registerModelLayer(f.layer(), () -> f.data().get());
            }
        }
    }

    private static Identifier tex(String name) {
        return new Identifier(VerityMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    private static void add(Form f) {
        FORMS.put(f.id(), f);
    }

    public static Form get(String id) {
        return FORMS.get(id);
    }

    public static ModelPart root(Form f) {
        return BAKED.computeIfAbsent(f.id(),
            k -> MinecraftClient.getInstance().getEntityModelLoader().getModelPart(f.layer()));
    }

    private VerityModels() {}
}
