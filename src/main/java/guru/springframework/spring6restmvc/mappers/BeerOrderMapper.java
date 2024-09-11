package guru.springframework.spring6restmvc.mappers;

import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.entities.BeerOrderLine;
import guru.springframework.spring6restmvc.entities.BeerOrderShipment;
import guru.springframework.spring6restmvc.model.BeerOrderDTO;
import guru.springframework.spring6restmvc.model.BeerOrderLineDTO;
import guru.springframework.spring6restmvc.model.BeerOrderShipmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface BeerOrderMapper {

    BeerOrder beerOrderDtoToBeerOrder(BeerOrderDTO dto);

    BeerOrderDTO beerOrderToBeerOrderDto(BeerOrder beerOrder);

    @Mapping(target = "beerOrder", ignore = true)
    BeerOrderShipment BeerOrderShipmentDtoToBeerOrderShipment(BeerOrderShipmentDTO dto);


    BeerOrderShipmentDTO beerOrderShipmentToBeerOrderShipmentDto(BeerOrderShipment beerOrderShipment);

    BeerOrderLine beerOrderLineDtoToBeerOrderLine(BeerOrderLineDTO dto);


    BeerOrderLineDTO beerOrderLineToBeerOrderLineDto(BeerOrderLine beerOrderLine);




}
