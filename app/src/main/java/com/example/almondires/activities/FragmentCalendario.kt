package com.example.almondires.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.almondires.R
import com.example.almondires.database.DBHelper
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class FragmentCalendario : Fragment(R.layout.fragment_calendario) {

    // Formateador para convertir las fechas de String (DB) a objetos LocalDate
    private val formatter = DateTimeFormatter.ofPattern("d/M/yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = DBHelper(requireContext())
        val currentUserId = requireActivity().intent.getLongExtra("USER_ID", -1)
        
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val btnLlamar = view.findViewById<TextView>(R.id.btnLlamarCalendario)

        val monthTitle = view.findViewById<TextView>(R.id.exFiveMonthText)
        
        // Formato para el título superior (ej: "Enero 2026")
        val titleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))

        // OBTENCIÓN Y CONVERSIÓN DE DATOS
        // Recuperamos las reservas de la base de datos y las convertimos en rangos de LocalDate
        // Esto permite comparar fechas de forma lógica (isBefore, isAfter)
        val reservas = db.obtenerReservasPorUsuario(currentUserId)
        val bookedRanges = reservas.mapNotNull {
            try {
                val start = LocalDate.parse(it.fechaInicio, formatter)
                val end = LocalDate.parse(it.fechaFinal, formatter)
                start to end
            } catch (e: Exception) {
                null
            }
        }



        // CONTENEDOR DE VISTA DE DÍA (DayViewContainer)
        // Gestiona la UI de cada celda individual y sus eventos (clicks)
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView: TextView = view.findViewById(R.id.calendarDayText)
            val background: View = view.findViewById(R.id.calendarDayBackground)
            lateinit var day: CalendarDay // Referencia al día actual de la celda

            init {
                // Evento al pulsar un día
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        val date = day.date
                        // LÓGICA DE NOCHES: Un día está ocupado si es el de entrada o está en medio.
                        // El día de salida (end) se considera libre porque el cliente se va por la mañana.
                        val isOccupied = bookedRanges.any { (start, end) ->
                            (date == start || date.isAfter(start)) && date.isBefore(end)
                        }

                        if (isOccupied) {
                            Toast.makeText(requireContext(), getString(R.string.fecha_ocupada), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.fecha_NoOcupada), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        // ENLACE DE DATOS DEL CALENDARIO (MonthDayBinder)
        // Define cómo se dibuja visualmente cada día según su estado
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    val date = data.date
                    var isStart = false
                    var isEnd = false
                    var isMiddle = false

                    // Comprobamos la posición del día actual dentro de los rangos de reserva
                    for ((start, end) in bookedRanges) {
                        when {
                            date == start -> isStart = true
                            date == end -> isEnd = true
                            date.isAfter(start) && date.isBefore(end) -> isMiddle = true
                        }
                    }

                    // Aplicamos los fondos con radius según el tipo de día en la reserva
                    when {
                        isStart -> {
                            // Inicio de reserva: Borde redondeado a la izquierda
                            container.background.setBackgroundResource(R.drawable.calendar_range_start_bg)
                            container.textView.setTextColor(Color.WHITE)
                        }
                        isEnd -> {
                            // Fin de reserva: Borde redondeado a la derecha
                            container.background.setBackgroundResource(R.drawable.calendar_range_end_bg)
                            container.textView.setTextColor(Color.WHITE)
                        }
                        isMiddle -> {
                            // Noches intermedias: Fondo rectangular (puente continuo)
                            container.background.setBackgroundResource(R.drawable.calendar_range_middle_bg)
                            container.textView.setTextColor(Color.WHITE)
                        }
                        else -> {
                            // Día libre: Sin fondo
                            container.background.background = null
                            container.textView.setTextColor(Color.BLACK)
                        }
                    }
                } else {
                    // Días que pertenecen al mes anterior o siguiente en la vista actual
                    container.textView.setTextColor(Color.LTGRAY)
                    container.background.background = null
                }
            }
        }

        btnLlamar.setOnClickListener {
            val numero = "666666666"
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero"))
            startActivity(intent)
        }

        // Actualiza el nombre del mes y el año en el TextView superior al deslizar
        calendarView.monthScrollListener = { month ->
            val title = month.yearMonth.format(titleFormatter).replaceFirstChar { it.uppercase() }
            monthTitle.text = title
        }


        //CONFIGURACIÓN INICIAL DEL CALENDARIO
        // Establecemos el rango visible y el día de inicio de la semana
        val currentMonth = YearMonth.now() // Año dinámico según el sistema
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) // Semana empieza en Lunes
        
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth) // Centrar en el mes actual
    }
}
