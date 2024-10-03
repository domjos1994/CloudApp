/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.caldav.model

import java.util.Date

data class Todo(
    var uid: String,
    var created: Date?,
    var lastModified: Date?,
    var timestamp: Date?,
    var summary: String,
    var start: Date?,
    var end: Date?,
    var status: String,
    var completed: Int,
    var priority: Int,
    var location: String,
    var url: String,
    var categories: String,
    var path: String
)