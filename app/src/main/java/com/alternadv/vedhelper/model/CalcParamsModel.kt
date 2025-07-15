package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class CalcParamsModel(
    @param:Json(name = "tnved_code")
    val tnvedCode: String?,

    val countries: List<CountryModel>?,

    @param:Json(name = "calc_params")
    val calcParams: Map<String, CalcParams>?,

    @param:Json(name = "calc_special")
    val calcSpecial: List<CalcSpecial>?,

    @param:Json(name = "calc_info")
    val calcInfo: CalcInfo?,

    val chosen: Chosen?
)

data class CountryModel(
    val code: String?,
    val name: String?
)

data class CalcSpecial(
    val id: String?,
    val name: String?,
    val type: String?,

    @param:Json(name = "type_name")
    val typeName: String?
)

data class Chosen(
    val code: String?,
    val direction: String?,
    val country: String?,

    @param:Json(name = "param_cost")
    val paramCost: Boolean?,

    val json: Boolean?,
    val specials: Map<String, String>?,

    val addons: Map<String, Double>?
)

data class CalcParams(
    val code: String?,
    val name: String?,
    val dimension: String?,
    val description: String?
)

data class CalcInfo(
    val name: String?,

    @param:Json(name = "import_tax")
    val importTax: CalcRateModel?,

    @param:Json(name = "export_tax")
    val exportTax: CalcRateModel?,

    val vat: CalcRateModel?,
    val excise: CalcRateModel?,
    val special: CalcRateModel?,
    //ensuring
    val documents: CalcDocumentsModel?
)

data class CalcDocumentsModel(
    val restrictions: CalcDocumentModel?,
    val license: CalcDocumentModel?,
    val certificates: CalcDocumentModel?,
    val others: CalcDocumentModel?,
)

data class CalcDocumentModel(
    val name: String?,
    val data: List<CalcDocumentDataModel>?
)

data class CalcDocumentDataModel(
    val order: String?,
    val authority: String?,

    @param:Json(name = "authority_license")
    val authorityLicense: String?,

    val document: String?,
    val direction: String?,
    val country: String?,
    val description: String?
)

data class CalcRateModel(
    val name: String?,
    val data: List<CalcRateDataModel>?
)

data class CalcRateDataModel(
    val name: String?,
    val description: String?,
    val order: String?,
    val rate: String?,

    @param:Json(name = "rate_type")
    val rateType: CalcRateTypeModel? = null,

    @param:Json(name = "rate_name")
    val rateName: String?,

    @param:Json(name = "rate_plus")
    val ratePlus: String?,

    @param:Json(name = "rate_plus_type")
    val ratePlusType: CalcRateTypeModel? = null,

    @param:Json(name = "rate_plus_name")
    val ratePlusName: String?,

    @param:Json(name = "rate_alt")
    val rateAlt: String?,

    @param:Json(name = "rate_alt_type")
    val rateAltType: CalcRateTypeModel? = null,

    @param:Json(name = "rate_alt_name")
    val rateAltName: String?,

    val minimum: String?,
    val maximum: String?,

    @param:Json(name = "min_max_rate")
    val minMaxRate: String?,

    val limit: String?,

    @param:Json(name = "rate_string")
    val rateString: String?,

    val country: String?
)

data class CalcRateTypeModel(
    val kod: String?,
    val name: String?,
    val `val` : String?,
    val kolval: String?,
    val vised: String?,
    val viskol: String?,
    val edizm: String?,
    val koled: String?,
    val nameosn: String?,

    @param:Json(name = "name_krat")
    val nameKrat: String?,

    @param:Json(name = "name_poln")
    val namePoln: String?,

    val brutto: String?,
    val znak: String?,
    val idx: String?,
    val `class`: String?,
    val description: String?
)