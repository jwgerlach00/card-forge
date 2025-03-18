package forge;

import forge.util.MyRandom;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class SimulateService {

    public SimulateService() {
        Simulation.initialize();
        System.out.println("initialization complete");
    }

    @Async("simulationExecutor")
    public CompletableFuture<String> getRandom() {
        for (int i : MyRandom.splitIntoRandomGroups(65, 2)) {
            System.out.println(i);
        }
        return CompletableFuture.completedFuture(String.valueOf(MyRandom.splitIntoRandomGroups(65, 2)[0]));
    }

    @Async("simulationExecutor")  // Run this method in a separate thread
    public CompletableFuture<String> simulateGame(int numGames, int numRounds, Map<String, List<String>> deck1Sections, Map<String, List<String>> deck2Sections) {
        // Call the library without modifying it
        String result = String.valueOf(Simulation.simulate(0, numRounds, deck1Sections, deck2Sections));
        return CompletableFuture.completedFuture(result);
    }

}
