package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvc.model.BeerOrderDTO;
import guru.springframework.spring6restmvc.model.BeerOrderUpdateDTO;
import guru.springframework.spring6restmvc.services.BeerOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BeerOrderController {
  public static final String BEER_ORDER_PATH = "/api/v1/beerOrder";
  public static final String BEER_ORDER_PATH_ID = BEER_ORDER_PATH + "/{beerOrderId}";

  private final BeerOrderService beerOrderService;

  @GetMapping(BEER_ORDER_PATH_ID)
  public BeerOrderDTO getBeerOrderById(@PathVariable("beerOrderId") UUID beerOrderId) {
    return beerOrderService
        .getBeerOrderById(beerOrderId)
        .orElseThrow(NotFoundException::new);
  }

  @GetMapping(BEER_ORDER_PATH)
  public Page<BeerOrderDTO> listOrders(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                       @RequestParam(value = "pageSize", required = false) Integer pageSize) {
    return beerOrderService.listOrders(pageNumber, pageSize);
  }


  @PostMapping(BEER_ORDER_PATH)
  public ResponseEntity<Void> createOrder(@RequestBody BeerOrderCreateDTO beerOrderCreateDTO) {
    BeerOrder savedOrder =  beerOrderService.saveNewBeerOrder(beerOrderCreateDTO);

    return ResponseEntity.created(URI.create(BEER_ORDER_PATH + "/" + savedOrder.getId().toString())).build();
  }

  @PutMapping(BEER_ORDER_PATH_ID)
  public ResponseEntity updateBeerOrderById(@PathVariable("beerOrderId") UUID beerOrderId,
                                            @Validated @RequestBody BeerOrderUpdateDTO beerOrderUpdateDTO) {
    return ResponseEntity.ok(beerOrderService.updateBeerOrderById(beerOrderId, beerOrderUpdateDTO));
  }


  @DeleteMapping(BEER_ORDER_PATH_ID)
  public ResponseEntity<Void> deleteById(@PathVariable("beerOrderId") UUID beerOrderId) {
    beerOrderService.deleteById(beerOrderId);
    return ResponseEntity.noContent().build();
  }


}
