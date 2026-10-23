package br.com.cursohub.catalog.application.model;

import br.com.cursohub.catalog.domain.CourseLevel;

/** Critérios da consulta; não contém classes técnicas como Pageable ou Specification. */
public record CatalogFilter(String search, String category, CourseLevel level, int page, int size) {}
