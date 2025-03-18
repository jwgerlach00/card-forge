package forge;

import forge.deck.Deck;
import forge.deck.io.DeckSerializer;
import forge.util.MyRandom;
import forge.view.Main;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class SimulateController {

    SimulateService simulateService;

    public SimulateController(SimulateService simulateService) {
        this.simulateService = simulateService;
    }

    @GetMapping("/randomNum")
    public CompletableFuture<ResponseEntity<String>> randomNum() {
        return simulateService.getRandom()
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/simulate")
    public CompletableFuture<ResponseEntity<String>> simulate(
//    public String simulate(
            @RequestParam("numGames") int numGames,
            @RequestParam("numRoundsPerGame") int numRoundsPerGame,
            @RequestParam("deck1Metadata") List<String> deck1Metadata,
            @RequestParam("deck1Main") List<String> deck1Main,
            @RequestParam("deck2Metadata") List<String> deck2Metadata,
            @RequestParam("deck2Main") List<String> deck2Main) {

        System.out.println(deck1Main);

        Map<String, List<String>> deck1Sections = Map.of("metadata", deck1Metadata, "Main", deck1Main);
        Map<String, List<String>> deck2Sections = Map.of("metadata", deck2Metadata, "Main", deck2Main);

//        Deck deck1 = DeckSerializer.fromSections(deck1Sections);
//        System.out.println(deck1.getMain());

//        return "ok";

        return simulateService.simulateGame(numGames, numRoundsPerGame, deck1Sections, deck2Sections)
                .thenApply(ResponseEntity::ok);
    }

}