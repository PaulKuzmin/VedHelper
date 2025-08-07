package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class OisResultModel(
    @param:Json(name = "ois_text")
    val oisText: String?,

    @param:Json(name = "ois_list")
    val oisList: List<OisModel>?,

    @param:Json(name = "ois_description")
    val oisDescription: Map<String, String>?
)

data class OisModel(
    //Рег. номер
    val regnom: String?,

    //Наименование
    @param:Json(name = "g31_12")
    val g3112: String?,

    //Описание
    val note: String?,

    //Документ об охраноспособности ОИС
    val document: String?,

    //Сведения о правообладателе
    val name: String?,

    //Наименование товаров
    val namet: String?,

    //Доверенный лица правообладателя
    val agent: String?,

    //Класс товаров по МКТУ
    val mktu: String?,

    //Срок внесения в реестр
    val dateend: String?,

    //Примечание
    val comm: String?,

    //Номер и дата писем ФТС России в тамож. органы
    val letter: String?,

    //Коды товаров по ТНВЭД
    @param:Json(name = "g33")
    val g33: String?,

    //Имя изображения
    val image: String?
)
