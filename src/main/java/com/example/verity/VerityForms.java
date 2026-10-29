package com.example.verity;

import java.util.List;

/** Список id всех форм (нужен и серверу, и клиенту). Добавляя новую модель — впиши её id сюда. */
public final class VerityForms {
    public static final String DEFAULT = "smile";
    public static final List<String> IDS = List.of(
        "smile",
        "evil",
        "talk"
        // , "my"   <- сюда добавляй id новых моделей
    );

    private VerityForms() {}
}
