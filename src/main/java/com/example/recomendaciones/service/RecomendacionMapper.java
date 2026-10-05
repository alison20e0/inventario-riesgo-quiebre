package com.example.recomendaciones.service;

import com.example.recomendaciones.dto.RecomendacionResponse;
import com.example.recomendaciones.model.Recomendacion;

public interface RecomendacionMapper {

    RecomendacionResponse toResponse(Recomendacion recomendacion);
}
