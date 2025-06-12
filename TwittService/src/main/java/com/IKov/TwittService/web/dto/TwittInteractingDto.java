package com.IKov.TwittService.web.dto;

import jnr.ffi.annotations.In;
import lombok.Data;

import java.util.UUID;

@Data
public class TwittInteractingDto {

    private UUID id;
    private Double interactionWeight;

}
