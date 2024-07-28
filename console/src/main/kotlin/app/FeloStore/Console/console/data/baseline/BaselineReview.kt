// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only
//
// DO NOT MODIFY - DATABASE BASELINE

package app.FeloStore.Console.console.data.baseline

import org.jetbrains.exposed.dao.id.IntIdTable

object BaselineReviews : IntIdTable("reviews") {
    val approved = bool("approved")
    val additionalNotes = text("additional_notes").nullable()
}
