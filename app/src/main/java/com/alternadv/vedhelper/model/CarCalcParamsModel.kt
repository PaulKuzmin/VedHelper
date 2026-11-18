package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

/**
 * Ответ widget/calcauto/params/{vehicle}.
 *
 * Форма строится по данным сервера: набор полей и допустимые значения приходят из calc_params,
 * поэтому новый параметр на сервере не требует обновления приложения.
 */
data class CarCalcParamsModel(
    @param:Json(name = "calc_params")
    val calcParams: List<CarCalcParam>,

    @param:Json(name = "calc_engines")
    val calcEngines: List<CarCalcEngine>,

    @param:Json(name = "calc_engine")
    val calcEngine: String?
)

/** Одно поле формы. Если [choices] не пуст — это выпадающий список, иначе числовое/текстовое поле. */
data class CarCalcParam(
    val name: String,
    val dimension: String,
    val code: String,
    val choices: List<CarCalcChoice>? = null
)

/** Допустимое значение поля-списка (например state: new/used, kind: motor/motor_sea/sail). */
data class CarCalcChoice(
    val id: String,
    val name: String
)

data class CarCalcEngine(
    val id: String,
    val name: String
)

/** Список видов ТС: GET widget/calcauto/vehicles — чтобы приложение не хардкодило типы. */
data class CarCalcVehiclesModel(
    val vehicles: List<CarCalcVehicle>
)

data class CarCalcVehicle(
    val id: String,
    val name: String
)
