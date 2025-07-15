package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.ContactsModel
import com.alternadv.vedhelper.model.ContactsRawModel

object ContactsSource {
    private const val PATH = "/widget/contacts"

    suspend fun get(): List<ContactsModel>? {
        val response = DataSource.getRaw<ContactsRawModel>("${DataSource.BASE_URL}${PATH}?json=1")
        return response?.data
    }
}