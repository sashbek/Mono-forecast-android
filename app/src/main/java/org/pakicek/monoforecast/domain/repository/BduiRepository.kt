package org.pakicek.monoforecast.domain.repository

import org.pakicek.monoforecast.domain.model.bdui.BduiPage

interface BduiRepository {
    suspend fun getPage(path: String): Result<BduiPage>
    suspend fun putPage(path: String, page: BduiPage): Result<Unit>
    suspend fun deletePage(path: String): Result<Unit>
}