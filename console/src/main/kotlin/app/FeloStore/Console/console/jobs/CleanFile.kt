// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only

package app.FeloStore.Console.console.jobs

import app.FeloStore.Console.console.data.File as FileDao
import app.FeloStore.Console.console.data.Files
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.IOException

/**
 * Removes the file with the given ID if it's marked deleted
 */
fun cleanFile(fileId: Int) {
    val dbFile = transaction {
        FileDao.find { Files.id eq fileId and Files.deleted }.singleOrNull()
    } ?: return
    val diskFile = File(dbFile.localPath)

    if (!diskFile.exists() || diskFile.delete()) {
        transaction { dbFile.delete() }
    } else {
        throw IOException("file cleaning failed")
    }
}
