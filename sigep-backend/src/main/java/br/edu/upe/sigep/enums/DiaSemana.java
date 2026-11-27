package br.edu.upe.sigep.enums;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum DiaSemana {
    DOMINGO,
    SEGUNDA,
    TERCA,
    QUARTA,
    QUINTA,
    SEXTA,
    SABADO;

    public static DiaSemana fromLocalDate(LocalDate data) {
        DayOfWeek dayOfWeek = data.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SUNDAY
                ? DOMINGO
                : values()[dayOfWeek.getValue()];
    }
}