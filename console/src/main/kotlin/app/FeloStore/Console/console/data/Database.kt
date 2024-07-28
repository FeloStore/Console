// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only

package app.FeloStore.Console.console.data

import app.FeloStore.Console.console.Config
import app.FeloStore.Console.console.data.baseline.BaselineAccessControlLists
import app.FeloStore.Console.console.data.baseline.BaselineApps
import app.FeloStore.Console.console.data.baseline.BaselineDrafts
import app.FeloStore.Console.console.data.baseline.BaselineFiles
import app.FeloStore.Console.console.data.baseline.BaselineIcons
import app.FeloStore.Console.console.data.baseline.BaselineRejectionReasons
import app.FeloStore.Console.console.data.baseline.BaselineReviewIssues
import app.FeloStore.Console.console.data.baseline.BaselineReviewers
import app.FeloStore.Console.console.data.baseline.BaselineReviews
import app.FeloStore.Console.console.data.baseline.BaselineSessions
import app.FeloStore.Console.console.data.baseline.BaselineUpdates
import app.FeloStore.Console.console.data.baseline.BaselineUsers
import app.FeloStore.Console.console.data.baseline.BaselineWhitelistedGitHubUsers
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

fun Application.configureDatabase(): DataSource {
    val config: Config by inject()

    val dataSource = SQLiteDataSource().apply {
        url = "jdbc:sqlite:${config.application.databasePath}?journal_mode=wal"

        setEnforceForeignKeys(true)
    }
    Database.connect(dataSource, setupConnection = {
        it.createStatement().executeUpdate("PRAGMA trusted_schema = OFF")
    })

    transaction {
        SchemaUtils.create(
            BaselineAccessControlLists,
            BaselineApps,
            BaselineDrafts,
            BaselineFiles,
            BaselineIcons,
            BaselineRejectionReasons,
            BaselineReviewers,
            BaselineReviewIssues,
            BaselineReviews,
            BaselineSessions,
            BaselineUpdates,
            BaselineUsers,
            BaselineWhitelistedGitHubUsers,
        )

        if (environment.developmentMode) {
            // Create a default superuser
            val debugUserGitHubId = (System.getenv("DEBUG_USER_GITHUB_ID")
                ?: throw Exception("DEBUG_USER_GITHUB_ID not specified in environment")).toLong()
            val userId = Users.insertIgnoreAndGetId {
                it[githubUserId] = debugUserGitHubId
                it[email] = System.getenv("DEBUG_USER_EMAIL")
                it[publisher] = true
            } ?: User.find { Users.githubUserId eq debugUserGitHubId }.singleOrNull()!!.id

            Reviewers.insertIgnore {
                it[this.userId] = userId
                it[email] = System.getenv("DEBUG_USER_REVIEWER_EMAIL")
            }
            WhitelistedGitHubUsers.insertIgnore { it[id] = debugUserGitHubId }
        }
    }

    Flyway
        .configure()
        .dataSource(dataSource)
        .baselineOnMigrate(true)
        .mixed(true)
        .validateMigrationNaming(true)
        .load()
        .migrate()

    return dataSource
}
