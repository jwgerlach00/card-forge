package forge;

import forge.view.Main;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SimulateController {

    @PostMapping("/simulate")
    public ResponseEntity<String> simulate(
            @RequestParam("numGames") int numGames,
            @RequestParam("numRoundsPerGame") int numRoundsPerGame,
            @RequestParam("deck1Name") String deck1Name,
            @RequestParam("deck2Name") String deck2Name) {

        String[] args = {
                "sim",
                "-n", String.valueOf(numGames),
                "-m", String.valueOf(numRoundsPerGame),
                "-d", deck1Name + ".dck", deck2Name + ".dck"
        };

        String winningDeck = Main.apiMain(args);

        return ResponseEntity.ok(winningDeck);
    }

}