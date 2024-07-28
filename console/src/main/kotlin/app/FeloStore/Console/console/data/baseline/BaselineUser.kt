// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only
//
// DO NOT MODIFY - DATABASE BASELINE

package app.FeloStore.Console.console.data.baseline

import org.jetbrains.exposed.dao.id.UUIDTable

object BaselineUsers : UUIDTable("users") {
    val githubUserId = long("gh_id").uniqueIndex()
    val email = text("email")
    val publisher = bool("publisher").default(false)
}
