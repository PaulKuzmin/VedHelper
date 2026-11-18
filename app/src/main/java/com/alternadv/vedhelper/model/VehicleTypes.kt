package com.alternadv.vedhelper.model

/**
 * Аварийный список видов ТС (если сервер недоступен).
 *
 * Основной источник — GET widget/calcauto/vehicles: список приходит с сервера, поэтому новый
 * тип ТС появляется в приложении без обновления. Этот список нужен только офлайн.
 */
val VehicleTypes: Map<String, String> = mapOf(
    "car" to "ЛЕГКОВОЙ",
    "cargo" to "ГРУЗОВОЙ",
    "bus" to "АВТОБУС",
    "bike" to "МОТОЦИКЛ/МОПЕД",
    "tractor" to "ТЯГАЧ",
    "crane" to "АВТОКРАН",
    "quadrocicle" to "КВАДРОЦИКЛ",
    "concretemixer" to "БЕТОНОМЕШАЛКА",
    "driving" to "АВТОБУРОВАЯ",
    "evacuator" to "ЭВАКУАТОР",
    "concretepump" to "БЕТОНОНАСОС",
    "snowmobile" to "СНЕГОХОД",
    "caravan" to "АВТОПРИЦЕП",
    "house" to "ДОМ-АВТОПРИЦЕП",
    "waterbike" to "ВОДНЫЙ МОТОЦИКЛ",
    "boat" to "КАТЕР (ЯХТА,ЛОДКА)",
    "dumptruck" to "САМОСВАЛ",
    "golfcar" to "ГОЛЬФ-КАР",
    "pickup" to "ПИКАП"
)
