package com.anudados.anudados_oficial

import com.example.anudados_oficial.R

data class NudoInfo(
    val nombre: String,
    val imagenResId: Int,
    val descripcion: String,
    val usos: List<String>,
    val dificultad: String
)

object NudosRepository {
    val nudos = listOf(
        NudoInfo(
            nombre = "Nudo Ahorcado",
            imagenResId = R.drawable.escota,
            descripcion = "El nudo ahorcado es un nudo corredizo que se aprieta cuando se tira del extremo. Es uno de los nudos más conocidos.",
            usos = listOf(
                "Tradicionalmente usado para ejecuciones",
                "Usos decorativos",
                "Construcción de hamacas",
                "Actividades recreativas"
            ),
            dificultad = "Intermedia"
        ),
        NudoInfo(
            nombre = "Nudo Calabrote",
            imagenResId = R.drawable.ballestrinque,
            descripcion = "El nudo calabrote es un nudo utilizado principalmente para unir dos cuerdas de gran diámetro. Es muy resistente a la tensión.",
            usos = listOf(
                "Unión de cuerdas gruesas",
                "Trabajos marítimos",
                "Remolque de embarcaciones",
                "Arrastre de cargas pesadas"
            ),
            dificultad = "Difícil"
        ),
        NudoInfo(
            nombre = "Nudo Cote",
            imagenResId = R.drawable.as_de_guia,
            descripcion = "El nudo cote es un nudo simple y versátil que sirve para iniciar muchos otros nudos. Es fácil de hacer y deshacer.",
            usos = listOf(
                "Base para otros nudos",
                "Asegurar una cuerda a un poste",
                "Amarrar temporalmente objetos",
                "Comenzar un tejido o trenzado"
            ),
            dificultad = "Fácil"
        ),
        NudoInfo(
            nombre = "Nudo Empaquetador",
            imagenResId = R.drawable.pescador,
            descripcion = "El nudo empaquetador se utiliza para asegurar paquetes, cajas o bultos. Permite tensar bien la cuerda y es fácil de desatar.",
            usos = listOf(
                "Embalaje de paquetes",
                "Asegurar cargas",
                "Fijación de objetos para transporte",
                "Atado de objetos voluminosos"
            ),
            dificultad = "Intermedia"
        ),
        NudoInfo(
            nombre = "Nudo Llano",
            imagenResId = R.drawable.margarita,
            descripcion = "El nudo llano es uno de los nudos más básicos y comunes. Sirve para unir dos cuerdas del mismo grosor.",
            usos = listOf(
                "Unir cuerdas de igual grosor",
                "Primeros auxilios",
                "Construcción básica",
                "Actividades diarias"
            ),
            dificultad = "Fácil"
        ),
        NudoInfo(
            nombre = "Nudo Llano Doble",
            imagenResId = R.drawable.escota,
            descripcion = "El nudo llano doble es una variante del nudo llano normal pero con una vuelta adicional que lo hace más seguro y resistente.",
            usos = listOf(
                "Unión segura de cuerdas",
                "Aplicaciones donde se requiere mayor resistencia",
                "Escalada y montañismo",
                "Náutica"
            ),
            dificultad = "Fácil"
        ),
        NudoInfo(
            nombre = "Nudo Margarita",
            imagenResId = R.drawable.margarita,
            descripcion = "El nudo margarita es un nudo decorativo y funcional que permite acortar una cuerda o eliminar secciones dañadas sin cortarla.",
            usos = listOf(
                "Acortar una cuerda sin cortarla",
                "Evitar secciones dañadas",
                "Decoración",
                "Ajuste de tensión en cuerdas"
            ),
            dificultad = "Intermedia"
        ),
        NudoInfo(
            nombre = "Nudo Mariposa",
            imagenResId = R.drawable.as_de_guia,
            descripcion = "El nudo mariposa crea un lazo fijo en medio de una cuerda. Es muy útil cuando se necesita un punto de anclaje seguro.",
            usos = listOf(
                "Escalada y montañismo",
                "Crear un punto de anclaje en medio de una cuerda",
                "Rescate",
                "Aislar secciones dañadas de una cuerda"
            ),
            dificultad = "Intermedia"
        ),
        NudoInfo(
            nombre = "Nudo Pescador",
            imagenResId = R.drawable.pescador,
            descripcion = "El nudo pescador es un nudo de unión muy resistente utilizado tradicionalmente por pescadores para unir líneas de pesca.",
            usos = listOf(
                "Pesca",
                "Unir líneas de pesca",
                "Unir cuerdas finas",
                "Joyería y artesanía"
            ),
            dificultad = "Intermedia"
        ),
        NudoInfo(
            nombre = "Nudo Pescador Doble",
            imagenResId = R.drawable.pescador,
            descripcion = "El nudo pescador doble es una versión mejorada del nudo pescador, con mayor resistencia y seguridad.",
            usos = listOf(
                "Pesca profesional",
                "Unión de cuerdas sometidas a tensión",
                "Escalada",
                "Aplicaciones que requieren máxima seguridad"
            ),
            dificultad = "Intermedia"
        ),
        NudoInfo(
            nombre = "Nudo Zarpa de Gato",
            imagenResId = R.drawable.ballestrinque,
            descripcion = "El nudo zarpa de gato es un nudo de amarre que permite asegurar una cuerda a un objeto redondo como un poste o un árbol.",
            usos = listOf(
                "Fijar cuerdas a postes",
                "Amarre de barcos",
                "Montaje de tiendas de campaña",
                "Construcciones temporales"
            ),
            dificultad = "Intermedia"
        ),
        NudoInfo(
            nombre = "Nudo de Doble Lazo",
            imagenResId = R.drawable.as_de_guia,
            descripcion = "El nudo de doble lazo crea dos lazos fijos que pueden soportar cargas en direcciones opuestas.",
            usos = listOf(
                "Rescate",
                "Aplicaciones donde se necesitan dos puntos de anclaje",
                "Transporte de personas o animales",
                "Elevación de objetos"
            ),
            dificultad = "Difícil"
        ),
        NudoInfo(
            nombre = "Nudo de Ocho",
            imagenResId = R.drawable.as_de_guia,
            descripcion = "El nudo de ocho es un nudo de tope muy utilizado en escalada y navegación. Es fácil de reconocer y deshacer incluso después de soportar una carga.",
            usos = listOf(
                "Escalada y montañismo",
                "Navegación",
                "Evitar que una cuerda se deslice por un orificio",
                "Base para otros nudos"
            ),
            dificultad = "Fácil"
        )
    )
} 