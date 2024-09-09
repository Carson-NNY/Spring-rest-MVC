package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.events.*;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

// the reason for using BeerServiceJPA is to use JPA, which is a Java Persistence API, a Java specification for accessing, persisting, and managing data between Java objects / classes and a relational database.
@Slf4j  // a Lombok annotation used to automatically generate a logger instance in your class using the Simple Logging Facade for Java (SLF4J) framework.
@Primary // mark it as primary bean
@Service
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService { // the reason for this implementation is to use JPA
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper; // we use beerMapper in conjection wit BeerRepository to manage it.
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 25;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    //Cache eviction refers to the process of removing items from a cache to  reflect changes in the cache or make space for new data.
    private void clearCache(UUID beerId) {
        cacheManager.getCache("beerCache").evict(beerId);
        cacheManager.getCache("beerListCache").clear();
    }

    @Cacheable(cacheNames = "beerListCache")
    @Override
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {

        log.info("List Beer - in service");

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        Page<Beer> beerPage;

        if(StringUtils.hasText(beerName) && beerStyle == null) {
            beerPage = listBeersByName(beerName,pageRequest);
        }else if(!StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = listBeersByStyle(beerStyle,pageRequest);
        }else if(StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = listBeersByNameAndStyle(beerName, beerStyle,pageRequest);
        } else{
            beerPage = beerRepository.findAll(pageRequest);
        }

        if(showInventory !=null && !showInventory) {
            beerPage.forEach(beer -> {
                beer.setQuantityOnHand(null);
            });
        }

        // convert Page<Beer> to Page<BeerDTO>
        return beerPage.map(beerMapper::beerToBeerDto);
    }

    public PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageNumber;
        int queryPageSize;

        if (pageNumber !=null && pageNumber > 0) {
            queryPageNumber = pageNumber - 1; // JPA uses 0-based pagination
        } else {
            queryPageNumber = DEFAULT_PAGE;
        }

        if (pageSize == null) {
            queryPageSize = DEFAULT_PAGE_SIZE;
        } else {
            if(pageSize > 1000) {  // limit the max page size
                queryPageSize = 1000;
            }else{
                queryPageSize = pageSize;
            }
        }

        // we want to Sort comes from springframework-data-domain
        Sort sort = Sort.by(Sort.Order.asc("beerName"));

        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }

    private Page<Beer> listBeersByNameAndStyle(String beerName, BeerStyle beerStyle, Pageable pageable) {
        return  beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle, pageable);
    }

    private Page<Beer> listBeersByStyle(BeerStyle beerStyle, Pageable pageable) {
            return beerRepository.findAllByBeerStyle(beerStyle, pageable);
    }

    public Page<Beer> listBeersByName(String beerName, Pageable pageable) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%", pageable); // % 会直接作用到SQL
    }

    @Cacheable(cacheNames = "beerCache", key = "#id")
    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        log.info("Get beer By Id - in service");
        return Optional.ofNullable(beerMapper.beerToBeerDto(beerRepository.findById(id)
                .orElse(null)));
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beer) {
        // need to clear the cache so that next time we call listBeers we will get latest data
        if(cacheManager.getCache("beerListCache") != null) {
            cacheManager.getCache("beerListCache").clear();
        }

        val savedBeer = beerRepository.save(beerMapper.beerDtoToBeer(beer));

        // set up an application event
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        applicationEventPublisher.publishEvent(new BeerCreatedEvent(savedBeer, authentication));

        // how it works is that we are saving the beer to the repository, and then we are converting the beer to a beerDTO and returning it.
        return beerMapper.beerToBeerDto(savedBeer);
    }



    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beer) {
        clearCache(beerId);

        //we need AtomicReference to be able to set the value of the atomic reference inside the lambda expression
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId).ifPresentOrElse(beer1 -> {
            beer1.setBeerName(beer.getBeerName());
            beer1.setBeerStyle(beer.getBeerStyle());
            beer1.setPrice(beer.getPrice());
            beer1.setUpc(beer.getUpc());
            beer1.setQuantityOnHand(beer.getQuantityOnHand());
            beer1.setVersion(beer.getVersion());

            val savedBeer = beerRepository.save(beer1);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            applicationEventPublisher.publishEvent(new BeerUpdatedEvent( savedBeer, authentication));

            atomicReference.set(Optional.of(beerMapper.beerToBeerDto(savedBeer)));
        }, () ->{ // if the beer is not found, we set the atomic reference to empty
            atomicReference.set(Optional.empty());
        });
        return atomicReference.get();
    }

    @Override
    public Boolean deleteById(UUID beerId) {
        //Cache eviction refers to the process of removing items from a cache to make space for new data.
        clearCache(beerId);

        if(beerRepository.existsById(beerId)){
            val authentication = SecurityContextHolder.getContext().getAuthentication();
            applicationEventPublisher.publishEvent(new BeerDeletedEvent(Beer.builder().id(beerId).build(), authentication));

            beerRepository.deleteById(beerId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<BeerDTO> patchBeerById(UUID beerId, BeerDTO beer) {
        clearCache(beerId);

        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            if (StringUtils.hasText(beer.getBeerName())){
                foundBeer.setBeerName(beer.getBeerName());
            }
            if (beer.getBeerStyle() != null){
                foundBeer.setBeerStyle(beer.getBeerStyle());
            }
            if (StringUtils.hasText(beer.getUpc())){
                foundBeer.setUpc(beer.getUpc());
            }
            if (beer.getPrice() != null){
                foundBeer.setPrice(beer.getPrice());
            }
            if (beer.getQuantityOnHand() != null){
                foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            }

            val savedBeer = beerRepository.save(foundBeer);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            applicationEventPublisher.publishEvent(new BeerPatchedEvent( savedBeer, authentication));

            atomicReference.set(Optional.of(beerMapper
                    .beerToBeerDto(savedBeer)));
        }, () -> {
            atomicReference.set(Optional.empty());
        });

        return atomicReference.get();
    }
}
