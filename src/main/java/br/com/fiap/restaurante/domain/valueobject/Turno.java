package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.enums.DiaSemana;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Um turno de funcionamento num dia da semana. Fechamento antes da abertura quer
 * dizer que o turno termina no dia seguinte: sexta 18:00–02:00 vai até as 2h de
 * sábado.
 */
public record Turno(DiaSemana dia, LocalTime abertura, LocalTime fechamento) implements Comparable<Turno> {

    private static final int MINUTOS_NO_DIA = 24 * 60;
    private static final int MINUTOS_NA_SEMANA = 7 * MINUTOS_NO_DIA;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final Comparator<Turno> ORDEM = Comparator.comparing(Turno::dia).thenComparing(Turno::abertura);

    public Turno {
        if (dia == null || abertura == null || fechamento == null) {
            throw new ValidacaoDeDominioException("O turno precisa de dia, abertura e fechamento.");
        }
        if (abertura.equals(fechamento)) {
            throw new ValidacaoDeDominioException("O turno de " + dia + " abre e fecha no mesmo horário ("
                    + abertura.format(HORA) + "). Para funcionar o dia todo, use 00:00–23:59.");
        }
    }

    public boolean passaDaMeiaNoite() {
        return fechamento.isBefore(abertura);
    }

    /**
     * Cada turno vira um intervalo de minutos contados a partir de segunda 00:00.
     * A semana é tratada como um relógio que dá a volta, para que domingo 22:00–03:00
     * alcance a segunda de madrugada. Turnos que só encostam não se sobrepõem.
     */
    public boolean sobrepoe(Turno outro) {
        return distancia(inicio(), outro.inicio()) < duracao() || distancia(outro.inicio(), inicio()) < outro.duracao();
    }

    private int inicio() {
        return dia.ordinal() * MINUTOS_NO_DIA + abertura.getHour() * 60 + abertura.getMinute();
    }

    private int duracao() {
        int minutos = (fechamento.toSecondOfDay() - abertura.toSecondOfDay()) / 60;
        return passaDaMeiaNoite() ? minutos + MINUTOS_NO_DIA : minutos;
    }

    // Quantos minutos andar no relógio da semana para ir de "de" até "ate"
    private static int distancia(int de, int ate) {
        return Math.floorMod(ate - de, MINUTOS_NA_SEMANA);
    }

    @Override
    public int compareTo(Turno outro) {
        return ORDEM.compare(this, outro);
    }

    @Override
    public String toString() {
        return dia + " " + abertura.format(HORA) + "–" + fechamento.format(HORA);
    }
}
