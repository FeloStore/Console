// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only

package app.FeloStore.Console.console.storage

import app.FeloStore.Console.console.data.File as FileDao
import app.FeloStore.Console.console.data.Files
import app.FeloStore.Console.console.jobs.cleanFile
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.transactions.transaction
import org.jobrunr.scheduling.BackgroundJob
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.UUID
import kotlin.io.path.createFile

/**
 * An implementation of [FileStorageService] for the local filesystem
 */
class LocalFileStorageService(private val baseDirectory: Path) : FileStorageService {
    override fun saveFile(inputStream: InputStream): EntityID<Int> {
        val fileAttributes = PosixFilePermissions.asFileAttribute(
            setOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
            )
        )
        val path = baseDirectory.resolve(UUID.randomUUID().toString()).createFile(fileAttributes)
        path.toFile().outputStream().use { inputStream.copyTo(it) }

        return transaction { FileDao.new { localPath = path.toString() }.id }
    }

    override fun deleteFile(id: EntityID<Int>) {
        transaction { findFile(id)?.apply { deleted = true } } ?: return

        BackgroundJob.enqueue { cleanFile(id.value) }
    }

    override fun loadFile(id: EntityID<Int>): InputStream {
        val path = findFile(id)?.localPath ?: throw FileNotFoundException()

        return File(path).inputStream()
    }

    private fun findFile(id: EntityID<Int>): FileDao? = transaction {
        FileDao.find { Files.id eq id and not(Files.deleted) }.singleOrNull()
    }
}
