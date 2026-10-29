package com.example.verity.client;

import com.example.verity.VerityMod;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

/** Злой Verity: та же форма шара, но со своей текстурой (оскал с зубами). */
public final class EvilModel {
    public static final EntityModelLayer LAYER =
        new EntityModelLayer(new Identifier(VerityMod.MOD_ID, "evil"), "main");

    public static TexturedModelData createModelData() {
        // Форма такая же, как у доброго; чтобы сделать другую — замени этот вызов своими боксами.
        return SmileModel.createModelData();
    }

    private EvilModel() {}
}
