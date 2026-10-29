package com.example.verity.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Настоящий гладкий шар (сетка из четырёхугольников) с лицом спереди.
 * Лицо — плоская проекция квадрата 24x24 пикселя текстуры (с центром в точке 33,33)
 * на переднюю полусферу; задняя половина закрашена цветом из пикселя (100,100).
 * Текстура 128x128.
 */
public final class SphereMesh {
    private static final int LAT = 24;   // сегментов по вертикали
    private static final int LON = 32;   // сегментов по горизонтали (чётное число!)
    private static final float R = 0.75f; // радиус в блоках (диаметр 1.5)
    private static final float TEX = 128f;

    public static void draw(MatrixStack matrices, VertexConsumer vc, int light) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f pos = entry.getPositionMatrix();
        Matrix3f nrm = entry.getNormalMatrix();

        for (int i = 0; i < LAT; i++) {
            double p0 = Math.PI * i / LAT;
            double p1 = Math.PI * (i + 1) / LAT;
            for (int j = 0; j < LON; j++) {
                double t0 = 2.0 * Math.PI * j / LON;
                double t1 = 2.0 * Math.PI * (j + 1) / LON;
                // Перед игрока — это -Z, то есть theta от PI до 2*PI
                boolean front = j >= LON / 2;

                vertex(vc, pos, nrm, p0, t0, front, light);
                vertex(vc, pos, nrm, p0, t1, front, light);
                vertex(vc, pos, nrm, p1, t1, front, light);
                vertex(vc, pos, nrm, p1, t0, front, light);
            }
        }
    }

    private static void vertex(VertexConsumer vc, Matrix4f pos, Matrix3f nrm,
                               double phi, double theta, boolean front, int light) {
        float nx = (float) (Math.sin(phi) * Math.cos(theta));
        float ny = (float) Math.cos(phi);
        float nz = (float) (Math.sin(phi) * Math.sin(theta));

        float u;
        float v;
        if (front) {
            // Вид спереди: правая рука зрителя — это -X
            u = (33.0f - nx * 12.0f) / TEX;
            v = (33.0f - ny * 12.0f) / TEX;
        } else {
            u = 100.0f / TEX;
            v = 100.0f / TEX;
        }

        vc.vertex(pos, nx * R, R + ny * R, nz * R)
            .color(255, 255, 255, 255)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(nrm, nx, ny, nz)
            .next();
    }

    private SphereMesh() {}
}
