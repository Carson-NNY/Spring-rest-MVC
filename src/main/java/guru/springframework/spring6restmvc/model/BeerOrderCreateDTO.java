package guru.springframework.spring6restmvc.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import javax.swing.*;
import java.util.Set;
import java.util.UUID;

//The reason for creating both BeerOrderCreateDTO and BeerOrderDTO is to handle different
// use cases for managing beer orders in your system. Each Data Transfer Object (DTO) serves
// a distinct purpose, improving clarity and reducing ambiguity when dealing with beer orders
@Data
@Builder
public class BeerOrderCreateDTO {

  private String customerRef;

  @NotNull
  private UUID customerId;

  private Set<BeerOrderLineCreateDTO> beerOrderLines;
}