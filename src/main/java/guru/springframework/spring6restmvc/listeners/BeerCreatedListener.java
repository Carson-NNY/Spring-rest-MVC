package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.events.BeerCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BeerCreatedListener {

    @EventListener
    public void listen(BeerCreatedEvent event) {
        System.out.println("I heard a beer is created");
        System.out.println(event.getBeer().getId());


    }
}
