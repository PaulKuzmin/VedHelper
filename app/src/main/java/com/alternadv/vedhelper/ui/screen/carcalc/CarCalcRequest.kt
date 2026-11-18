package com.alternadv.vedhelper.ui.screen.carcalc

import com.alternadv.vedhelper.model.CarCalcParam
import com.alternadv.vedhelper.model.CarCalcResultModel

/**
 * Сборка query-параметров запроса к API.
 *
 * Вынесено из ViewModel отдельной чистой функцией, чтобы покрывать тестами без Android.
 *
 * Правила:
 *  • ничего не конвертируем — ни валюту, ни мощность: сервер делает это сам
 *    (`currency` + `cost`, `power` + `power_edizm`);
 *  • значения передаём ровно так, как ввёл пользователь (строкой), включая дробные;
 *  • `json=1` добавляет DataSource.buildUrl, здесь его нет.
 */
fun CarCalcState.toParamMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    map["vehicle"] = vehicle

    if (costInput.isNotEmpty()) map["cost"] = costInput
    map["currency"] = currency

    if (!engine.isNullOrEmpty()) map["engine"] = engine

    // Мощность — только если поле есть в форме текущего вида ТС: у гольф-кара и др. поля нет,
    // а значение, введённое для прошлого вида, не должно уходить на сервер.
    if (powerInput.isNotEmpty() && hasPowerField()) {
        map["power"] = powerInput
        map["power_edizm"] = powerUnit
    }

    // Евро-5/6, международные перевозки — только для тягача (как чекбокс на сайте).
    if (intl && supportsIntl()) map["intl"] = "1"

    // Только поля текущей формы: после смены двигателя (бензин -> электро) поле объёма исчезает,
    // а введённое раньше значение не должно уходить на сервер и менять код ТН ВЭД.
    val visible = calcParams.map { it.code }.toSet()
    paramValues.forEach { (key, value) ->
        if (value.isNotEmpty() && key in visible) map[key] = value
    }

    map["month"] = month.toString()
    map["year"] = year.toString()
    return map
}

/** В текущей форме (calc_params) есть поле мощности. */
fun CarCalcState.hasPowerField(): Boolean = calcParams.any { it.code == "power" }

/**
 * Флаг intl (тягач Евро-5/6 для международных перевозок). Сервер не отдаёт его в calc_params,
 * а сайт рисует чекбокс по виду ТС (calcauto_params.twig: calc_type == 'tractor') — повторяем.
 */
fun CarCalcState.supportsIntl(): Boolean = vehicle == INTL_VEHICLE

const val INTL_VEHICLE = "tractor"

/**
 * Дробные разрешены (1.6 литра, 1.75 тонны): запятая становится точкой,
 * вторая и последующие точки отбрасываются вместе с остатком («1.6.3» → «1.6»).
 */
fun normalizeNumber(value: String): String {
    val replaced = value.replace(',', '.').filter { it.isDigit() || it == '.' }
    val parts = replaced.split('.')
    return if (parts.size <= 1) replaced else parts[0] + "." + parts[1]
}

/**
 * Состояние по умолчанию для поля `state`: выводится из возраста, как в ТКС
 * (у эталона параметра state нет — ветку ТН ВЭД он выбирает по возрасту).
 */
fun defaultState(year: Int, currentYear: Int): String =
    if (currentYear - year < 3) "new" else "used"

/**
 * Проставляет значения по умолчанию для полей-списков (choices).
 *
 * Нужно, чтобы выбранное на экране совпадало с тем, что уходит в запрос: раньше «Состояние»
 * показывало «б/у», а на сервер уходило пусто, и он сам решал по возрасту.
 *
 * @param touched коды полей, которые пользователь менял руками — их не перезаписываем
 */
fun seedChoiceDefaults(
    params: List<CarCalcParam>,
    current: Map<String, String>,
    year: Int,
    currentYear: Int,
    touched: Set<String> = emptySet()
): Map<String, String> {
    val result = current.toMutableMap()

    params.forEach { param ->
        val choices = param.choices.orEmpty()
        if (choices.isEmpty() || param.code in touched) return@forEach

        val value = result[param.code]
        if (!value.isNullOrEmpty() && choices.any { it.id == value }) return@forEach

        result[param.code] = if (param.code == "state") {
            defaultState(year, currentYear)
        } else {
            choices.first().id
        }
    }

    return result
}

/**
 * Ошибка расчёта для диалога или null, если есть что показать.
 *
 * Результат показываем, если посчитан хотя бы один блок: раньше ошибка в F (физлицо)
 * прятала за диалогом готовый расчёт на юрлицо.
 */
fun calcErrorText(result: CarCalcResultModel?): String? {
    if (result == null) return "Сервер не вернул результат расчёта"
    val f = result.calculation.f
    val u = result.calculation.u
    if (u?.success == true || f?.success == true) return null
    return formatCalcErrors(
        fMessages = f?.messages.orEmpty().map { it.message },
        uMessages = u?.messages.orEmpty().map { it.message }
    )
}

/**
 * Текст ошибок расчёта для диалога.
 *
 * Заголовок добавляется только если в соответствующем блоке есть сообщения: раньше при
 * отсутствии блока `F` (грузовик, автобус, спецтехника) пользователь видел пустой заголовок
 * «Ошибки при расчете на физ.лицо:».
 */
fun formatCalcErrors(fMessages: List<String>, uMessages: List<String>): String {
    val blocks = mutableListOf<String>()

    if (fMessages.isNotEmpty()) {
        blocks += buildString {
            append("Ошибки при расчете на физ.лицо:")
            fMessages.forEach { append("\r\n").append(it) }
        }
    }
    if (uMessages.isNotEmpty()) {
        blocks += buildString {
            append("Ошибки при расчете на юр.лицо:")
            uMessages.forEach { append("\r\n").append(it) }
        }
    }

    return if (blocks.isEmpty()) "Неизвестная ошибка при расчете" else blocks.joinToString("\r\n\r\n")
}
