package br.com.fiap.restaurante.application.dto;

/**
 * Filtros da listagem de restaurantes. Os dois são opcionais: o nome pelo trecho,
 * o tipo de cozinha pelo valor exato.
 */
public record FiltroDeRestaurantes(String nome, String tipoCozinha) {
}
