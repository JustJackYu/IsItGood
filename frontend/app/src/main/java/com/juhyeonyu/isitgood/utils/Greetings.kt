package com.juhyeonyu.isitgood.utils

/**
 * Builds a varied, personality-driven home greeting.
 *
 * Roughly half the time it picks a time-of-day greeting, the other half a general one.
 * Templates use a "{name}" placeholder; when no username is known, the name (and its
 * surrounding comma) is gracefully dropped.
 */
object Greetings {

    private val MORNING = listOf(
        "Good morning, {name}",
        "Rise and shine, {name}",
        "Morning, {name} — ready to find something to play?",
        "Top of the morning, {name}",
        "A fresh day, a fresh backlog, {name}",
        "Early start today, {name}?"
    )

    private val AFTERNOON = listOf(
        "Good afternoon, {name}",
        "Afternoon, {name} — taking a break?",
        "Hope your day's going well, {name}",
        "Midday check-in, {name}?",
        "Afternoon, {name}. Anything catch your eye?"
    )

    private val EVENING = listOf(
        "Good evening, {name}",
        "Evening, {name} — winding down?",
        "Long day, {name}? Let's find something good",
        "Evening, {name}. Time to unwind",
        "Hope you had a good one, {name}"
    )

    private val NIGHT = listOf(
        "Still awake, {name}?",
        "Late one tonight, {name}?",
        "Winding down for the night, {name}?",
        "The night is young, {name}",
        "Up late, {name}? Same"
    )

    private val MIDNIGHT = listOf(
        "Burning the midnight oil, {name}?",
        "Past midnight, {name}?",
        "Can't sleep, {name}?",
        "The witching hour, {name}",
        "Night owl mode, {name}?",
        "Still going strong at this hour, {name}?"
    )

    private val DAWN = listOf(
        "Up before the sun, {name}?",
        "Early bird, {name}?",
        "Catching the sunrise, {name}?",
        "Bright and early, {name}",
        "The day's barely begun, {name}",
        "Dawn already, {name}?"
    )

    private val GENERAL = listOf(
        "How are you today, {name}?",
        "Welcome back, {name}",
        "Good to see you, {name}",
        "What are we playing today, {name}?",
        "Ready to find your next favorite, {name}?",
        "Looking for something to play, {name}?",
        "Back for more, {name}?",
        "Let's find something worth your time, {name}",
        "Hope you're doing well, {name}",
        "Nice to see you again, {name}"
    )

    private fun timePool(hour: Int): List<String> = when (hour) {
        in 4..6 -> DAWN
        in 7..11 -> MORNING
        in 12..16 -> AFTERNOON
        in 17..21 -> EVENING
        23, 0, 1, 2 -> MIDNIGHT
        else -> NIGHT // 22 and 3 — the edges between evening/midnight/dawn
    }

    fun pick(name: String?, hour: Int): String {
        // ~50/50 between a general greeting and a time-of-day one.
        val pool = if (Math.random() < 0.5) GENERAL else timePool(hour)
        return applyName(pool.random(), name)
    }

    private fun applyName(template: String, name: String?): String {
        if (!name.isNullOrBlank()) {
            return template.replace("{name}", name.trim())
        }
        // No name: drop the ", {name}" / "{name}, " / "{name}" and tidy up.
        return template
            .replace(", {name}", "")
            .replace("{name}, ", "")
            .replace("{name}", "")
            .replace("  ", " ")
            .trim()
    }
}
