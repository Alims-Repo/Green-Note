package com.alim.greennote.data

import android.graphics.Color
import com.alim.greennote.data.model.ModelTask

object Dummy {
    val taskList = listOf(
        ModelTask(
            id = 1,
            title = "Complete Project Report",
            description = "Finalize and submit the project report by end of the week.",
            dueDateMillis = 1679472000000, // Example timestamp (milliseconds)
            priority = "High",
            category = "Work",
            color = Color.RED, // Using a predefined color constant
            autoArchiveDays = 7
        ),
        ModelTask(
            id = 2,
            title = "Buy Groceries",
            description = "Purchase groceries for the week.",
            dueDateMillis = 1679558400000, // Another timestamp
            priority = "Medium",
            category = "Personal",
            color = Color.YELLOW, // Example color
            autoArchiveDays = 3
        ),
        ModelTask(
            id = 3,
            title = "Plan Weekend Trip",
            description = "Organize itinerary and accommodation for weekend trip.",
            dueDateMillis = 1679644800000, // Another timestamp
            priority = "Low",
            category = "Leisure",
            color = Color.BLUE, // Example color
            autoArchiveDays = 14
        ),
        ModelTask(
            id = 4,
            title = "Finish Coding Assignment",
            description = "Complete the coding assignment and submit it before the deadline.",
            dueDateMillis = 1679731200000, // Another timestamp
            priority = "High",
            category = "School",
            color = Color.GREEN, // Example color
            autoArchiveDays = 7
        )
    )
}