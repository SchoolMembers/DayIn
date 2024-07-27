package com.example.dayin.data

import androidx.annotation.ColorRes
import com.example.dayin.R
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private typealias Diary = Calender.Diary

data class Calender(
    val time: LocalDateTime,
    val departure: Diary,
    val destination: Diary,
    @ColorRes val color: Int,
) {
    data class Diary(val city: String, val code: String)
}

fun generateCalenders(): List<Calender> = buildList {
    val currentMonth = YearMonth.now()

    currentMonth.atDay(17).also { date ->
        add(
            Calender(
                date.atTime(14, 0),
                Diary("Lagos", "LOS"),
                Diary("Abuja", "ABV"),
                R.color.blue_800,
            ),
        )
        add(
            Calender(
                date.atTime(21, 30),
                Diary("Enugu", "ENU"),
                Diary("Owerri", "QOW"),
                R.color.red_800,
            ),
        )
    }

    currentMonth.atDay(22).also { date ->
        add(
            Calender(
                date.atTime(13, 20),
                Diary("Ibadan", "IBA"),
                Diary("Benin", "BNI"),
                R.color.brown_700,
            ),
        )
        add(
            Calender(
                date.atTime(17, 40),
                Diary("Sokoto", "SKO"),
                Diary("Ilorin", "ILR"),
                R.color.blue_grey_700,
            ),
        )
    }

    currentMonth.atDay(3).also { date ->
        add(
            Calender(
                date.atTime(20, 0),
                Diary("Makurdi", "MDI"),
                Diary("Calabar", "CBQ"),
                R.color.teal_700,
            ),
        )
    }

    currentMonth.atDay(12).also { date ->
        add(
            Calender(
                date.atTime(18, 15),
                Diary("Kaduna", "KAD"),
                Diary("Jos", "JOS"),
                R.color.cyan_700,
            ),
        )
    }

    currentMonth.plusMonths(1).atDay(13).also { date ->
        add(
            Calender(
                date.atTime(7, 30),
                Diary("Kano", "KAN"),
                Diary("Akure", "AKR"),
                R.color.pink_700,
            ),
        )
        add(
            Calender(
                date.atTime(10, 50),
                Diary("Minna", "MXJ"),
                Diary("Zaria", "ZAR"),
                R.color.green_700,
            ),
        )
    }

    currentMonth.minusMonths(1).atDay(9).also { date ->
        add(
            Calender(
                date.atTime(20, 15),
                Diary("Asaba", "ABB"),
                Diary("Port Harcourt", "PHC"),
                R.color.orange_800,
            ),
        )
    }
}

val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")
