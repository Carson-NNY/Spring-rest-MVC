package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvc.model.BeerOrderDTO;
import guru.springframework.spring6restmvc.model.BeerOrderUpdateDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface BeerOrderService {

  Page<BeerOrderDTO> listOrders(Integer pageNumber, Integer pageSize);

  Optional<BeerOrderDTO> getBeerOrderById(UUID beerOrderId);


  BeerOrder saveNewBeerOrder(BeerOrderCreateDTO beerOrderCreateDTO);


  BeerOrderDTO updateBeerOrderById(UUID beerOrderId, BeerOrderUpdateDTO beerOrderUpdateDTO);


  void deleteById(UUID beerOrderId);


  Optional<BeerOrderDTO> patchBeerOrderById(UUID beerOrderId, BeerOrderDTO beerOrderDTO);
}
