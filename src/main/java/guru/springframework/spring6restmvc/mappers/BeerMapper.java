package guru.springframework.spring6restmvc.mappers;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.entities.BeerAudit;
import guru.springframework.spring6restmvc.model.BeerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


// 我们创建完这个Mapper interface后点开 Maven -> Lifecycle -> clean ,
// then compile -> then we have target package -> generated-sources里可以找到 mapstruct帮助我们
// generated implementation for later injection use!

@Mapper
public interface BeerMapper {
// the reason we need this interface is that we need to convert between Beer and BeerDTO
    @Mapping(target = "categories", ignore = true) // telling MapStruct to: ignore the categories field when converting between Beer and BeerDTO
    @Mapping(target = "beerOrderLines", ignore = true)
    Beer beerDtoToBeer(BeerDTO dto);

    BeerDTO beerToBeerDto(Beer beer);


    //represents an audit entity that tracks changes made to Beer objects. This can include events like the creation,
    // modification, or deletion of Beer records, as well as information about who made the changes, when the changes were made, and what was changed.
    @Mapping(target = "createdDateAudit", ignore = true)
    @Mapping(target = "auditId", ignore = true)
    @Mapping(target = "auditEventType", ignore = true)
    @Mapping(target = "principalName", ignore = true)
    BeerAudit beerToBeerAudit(Beer beer);
}
