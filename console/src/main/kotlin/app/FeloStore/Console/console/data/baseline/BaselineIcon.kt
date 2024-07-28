// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only
//
// DO NOT MODIFY - DATABASE BASELINE

package app.FeloStore.Console.console.data.baseline

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object BaselineIcons : IntIdTable("icons") {
    val hash = text("hash")
    val fileId = reference("file_id", BaselineFiles, ReferenceOption.NO_ACTION)
}
