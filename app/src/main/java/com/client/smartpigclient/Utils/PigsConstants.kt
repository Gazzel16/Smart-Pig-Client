package com.client.smartpigclient.Utils

object PigConstants {

    val GENDER_OPTIONS = listOf("Male", "Female")

    val ORIGIN_OPTIONS = listOf(
        "Born on Farm",
        "Purchased from Market",
        "Transferred from Another Farm",
        "Gifted"
    )

    val IS_ALIVE_OPTIONS = listOf("Yes", "No")

    val HEALTH_OPTIONS = listOf(
        "Healthy",
        "Sick",
        "Injured",
        "Recovering",
        "Needs Checkup",
        "Vaccinated"
    )

    val ILLNESS_OPTIONS = listOf(
        "None",
        "Swine Flu",
        "Foot and Mouth Disease",
        "Skin Infection",
        "Respiratory Infection",
        "Other"
    )

    val VACCINE_OPTIONS = listOf(
        "None",
        "Swine Fever Vaccine",
        "Foot and Mouth Disease Vaccine",
        "PRRS Vaccine",
        "Classical Swine Fever Vaccine",
        "Other"
    )

    val PIG_FEEDS = listOf(
        "None",
        "Starter Feed",
        "Pre-Starter Feed",
        "Grower Feed",
        "Finisher Feed",
        "Sow Feed",
        "Boar Feed",
        "Lactation Feed",
        "Gestation Feed",
        "Creep Feed",
        "Pellets",
        "Mash",
        "Crumbles",
        "Corn",
        "Rice Bran (Darak)",
        "Copra Meal",
        "Soybean Meal",
        "Fish Meal",
        "Commercial Feed",
        "Custom Mix",
        "Other"
    )

    val PIG_TYPE = listOf(
        "Grower / Finisher",
        "Piglets",
        "Sows ( Adult Breeding Pigs )",
        "Other"
    )

}