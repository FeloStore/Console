// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only

package app.FeloStore.Console.console.jobs

import app.FeloStore.Console.console.data.Draft as DraftDao
import app.FeloStore.Console.console.data.Update as UpdateDao
import app.FeloStore.Console.console.data.AccessControlList
import app.FeloStore.Console.console.data.App
import app.FeloStore.Console.console.data.Icon
import app.FeloStore.Console.console.data.Listing
import app.FeloStore.Console.console.publish.PublishService
import app.FeloStore.Console.console.storage.FileStorageService
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.java.KoinJavaComponent.inject
import java.util.UUID

/**
 * Publishes the draft with the given ID, making it available for download
 */
fun registerPublishAppJob(draftId: UUID) {
    val storageService: FileStorageService by inject(FileStorageService::class.java)
    val publishService: PublishService by inject(PublishService::class.java)

    val draft = transaction { DraftDao.findById(draftId) } ?: return
    val iconFileId =
        transaction { Icon.findById(draft.iconId)?.fileId } ?: throw IllegalStateException()

    // Publish to the repository
    storageService.loadFile(draft.fileId).use { draftStream ->
        storageService.loadFile(iconFileId).use { iconStream ->
            runBlocking {
                publishService.publishDraft(draftStream, iconStream, draft.shortDescription)
            }
        }
    }

    // Account for publication
    transaction {
        draft.delete()
        val app = App.new(draft.appId) {
            versionCode = draft.versionCode
            versionName = draft.versionName
            fileId = draft.fileId
            reviewIssueGroupId = draft.reviewIssueGroupId
        }
        Listing.new {
            appId = app.id
            locale = "en-US"
            iconId = draft.iconId
            label = draft.label
            shortDescription = draft.shortDescription
        }
        AccessControlList.new {
            this.userId = draft.creatorId
            appId = app.id
            update = true
            editMetadata = true
        }
    }
}

/**
 * Publishes the update with the given ID, making it available for download
 */
fun registerPublishUpdateJob(updateId: UUID) {
    val storageService: FileStorageService by inject(FileStorageService::class.java)
    val publishService: PublishService by inject(PublishService::class.java)

    val update = transaction { UpdateDao.findById(updateId) } ?: return

    // Publish to the repository
    storageService.loadFile(update.fileId).use {
        runBlocking { publishService.publishUpdate(it, update.appId.value) }
    }

    // Account for publication
    val oldAppFileId = transaction {
        App.findById(update.appId)?.run {
            versionCode = update.versionCode
            versionName = update.versionName

            val oldAppFileId = fileId
            fileId = update.fileId

            update.published = true
            updating = false

            oldAppFileId
        }
    }

    // Delete old app file
    if (oldAppFileId != null) {
        storageService.deleteFile(oldAppFileId)
    }
}
