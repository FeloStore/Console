// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only

package app.FeloStore.Console.console.jobs

import app.FeloStore.Console.console.data.Edit as EditDao
import app.FeloStore.Console.console.data.App
import app.FeloStore.Console.console.data.Listing
import app.FeloStore.Console.console.data.Listings
import app.FeloStore.Console.console.publish.PublishService
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.java.KoinJavaComponent.inject
import java.util.UUID

/**
 * Publishes the app metadata changes in the edit with the given ID
 */
fun publishEdit(editId: UUID) {
    val publishService: PublishService by inject(PublishService::class.java)

    val edit = transaction { EditDao.findById(editId) } ?: return

    // Publish to the repository
    runBlocking { publishService.publishEdit(edit.appId.value, edit.shortDescription) }

    // Account for publication
    transaction {
        App.findById(edit.appId)?.run { updating = false }
        Listing
            .find { Listings.appId eq edit.appId and (Listings.locale eq "en-US") }
            .singleOrNull()
            ?.run {
                if (edit.shortDescription != null) {
                    shortDescription = edit.shortDescription!!
                }
            }

        edit.published = true
    }
}
