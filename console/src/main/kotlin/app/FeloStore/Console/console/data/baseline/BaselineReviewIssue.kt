// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only
//
// DO NOT MODIFY - DATABASE BASELINE

package app.FeloStore.Console.console.data.baseline

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object BaselineReviewIssues : IntIdTable("review_issues") {
    val reviewIssueGroupId =
        reference("review_issue_group_id", BaselineReviewIssueGroups, ReferenceOption.CASCADE)
    val rawValue = text("raw_value")

    init {
        uniqueIndex(reviewIssueGroupId, rawValue)
    }
}
