package com.example.studentportal.data


object dataNews {
    val newss = mutableListOf<news>(
        news(
            "1",
            "Какая-то важная новость для студентов университета ЮФУ",
            "30 нояб. 2023 г.",
            "news1", // фото для главной страницы
            listOf("Студентам", "Абитуриентам"),
            false,
            "Иван Иванов",
            "Это текст новости, который будет разделен на две части. Первая часть будет выводиться в первом LinearLayout, а вторая часть — во втором. Первая часть будет выводиться в первом LinearLayout, а вторая часть — во втором. Первая часть будет выводиться в первом LinearLayout, а вторая часть — во втором.", // Текст новости
            listOf("news1", "news2", "news3") // список фото на странице с доп инфой
        ),
        news(
            "2",
            "Толмач уволился",
            "1 дек. 2024 г.",
            "news2",
            listOf("Студентам"),
            true,
            "Петр Петров",
            "Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже. Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.Это текст новости о том, что Толмач уволился. Первая часть текста будет здесь, а вторая часть — ниже.",
            listOf("news2", "news3")
        ),
        news(
            "3",
            "Соколов уволился",
            "5 янв. 2025 г.",
            "news3",
            listOf("Сотрудникам"),
            false,
            "Сергей Сергеев",
            "Это текст новости о том, что Соколов уволился. Первая часть текста будет здесь, а вторая часть — ниже.",
            listOf("news3", "news1")
        ),
        news(
            "4",
            "Вернули столовку",
            "17 янв. 2025 г.",
            "news3",
            listOf("Студентам"),
            false,
            "Сергей Сергеев",
            "Это текст новости о том, что Соколов уволился. Первая часть текста будет здесь, а вторая часть — ниже.",
            listOf("news3", "news1")
        ),
        news(
            "5",
            "Всем повысили стипендию",
            "29 янв. 2025 г.",
            "news3",
            listOf("Самоделка"),
            true,
            "Сергей Сергеев",
            "Это текст новости о том, что Соколов уволился. Первая часть текста будет здесь, а вторая часть — ниже.",
            listOf("news3", "news1")
        )
    ).reversed().toMutableList() // Переворачиваем список
}