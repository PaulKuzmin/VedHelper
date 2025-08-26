package com.alternadv.vedhelper.utils

object PowerConverter {
    private const val KW_TO_HP_FACTOR = 1.35962

    /**
     * Конвертирует мощность из киловатт (кВт) в лошадиные силы (л.с.)
     * с округлением вверх по требованиям ФТС РФ.
     *
     * @param kilowatts Мощность в кВт (Double)
     * @return Мощность в л.с. (Int)
     */
    fun kilowattsToHorsepower(kilowatts: Double): Int {
        require(kilowatts >= 0) { "Мощность не может быть отрицательной" }
        return kotlin.math.ceil(kilowatts * KW_TO_HP_FACTOR).toInt()
    }
}