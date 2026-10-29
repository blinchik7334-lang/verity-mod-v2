package com.example.verity.client;

import com.example.verity.VerityMod;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

/**
 * 3D-смайлик: шар диаметром 24 пикселя (1.5 блока), собранный из трёх пересекающихся боксов
 * (получается скруглённая форма). Лицо — на передней грани бокса "front".
 * Текстура 128x128. Низ шара стоит на земле.
 */
public final class SmileModel {
    public static final EntityModelLayer LAYER =
        new EntityModelLayer(new Identifier(VerityMod.MOD_ID, "smile"), "main");

    public static TexturedModelData createModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        // Центр шара по высоте y=12 (земля — y=24)
        // front: 18x18x24 — его передняя грань (18x18) несёт лицо, UV (0,0)
        root.addChild("front", ModelPartBuilder.create().uv(0, 0)
            .cuboid(-9f, 3f, -12f, 18f, 18f, 24f), ModelTransform.NONE);
        // tall: 18x24x18, UV (0,42)
        root.addChild("tall", ModelPartBuilder.create().uv(0, 42)
            .cuboid(-9f, 0f, -9f, 18f, 24f, 18f), ModelTransform.NONE);
        // wide: 24x18x18, UV (0,84)
        root.addChild("wide", ModelPartBuilder.create().uv(0, 84)
            .cuboid(-12f, 3f, -9f, 24f, 18f, 18f), ModelTransform.NONE);

        return TexturedModelData.of(data, 128, 128);
    }

    private SmileModel() {}
}
