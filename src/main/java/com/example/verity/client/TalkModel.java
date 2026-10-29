package com.example.verity.client;

import com.example.verity.VerityMod;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

/** Говорящий Verity: форма шара, текстура меняется на "рот открыт", пока игрок говорит. */
public final class TalkModel {
    public static final EntityModelLayer LAYER =
        new EntityModelLayer(new Identifier(VerityMod.MOD_ID, "talk"), "main");

    public static TexturedModelData createModelData() {
        return SmileModel.createModelData();
    }

    private TalkModel() {}
}
