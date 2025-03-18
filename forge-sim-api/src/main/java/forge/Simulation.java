package forge;

import forge.deck.Deck;
import forge.deck.io.DeckSerializer;
import forge.error.ExceptionHandler;
import forge.game.*;
import forge.game.player.RegisteredPlayer;
import forge.gui.GuiBase;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.util.BuildInfo;
import forge.view.TimeLimitedCodeBlock;
import io.sentry.Sentry;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Simulation {

    private static final Logger log = LoggerFactory.getLogger(Simulation.class);

    private Simulation() {
    }

    public static void initialize() {
        Sentry.init(options -> {
            options.setEnableExternalConfiguration(true);
            options.setRelease(BuildInfo.getVersionString());
            options.setEnvironment(System.getProperty("os.name"));
            options.setTag("Java Version", System.getProperty("java.version"));
            options.setShutdownTimeoutMillis(5000);
            // these belong to sentry.properties, but somehow some OS/Zip tool discards it?
            if (options.getDsn() == null || options.getDsn().isEmpty())
                options.setDsn("https://87bc8d329e49441895502737c069067b@sentry.cardforge.org//3");
        }, true);

        // HACK - temporary solution to "Comparison method violates it's general contract!" crash
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        //Turn off the Java 2D system's use of Direct3D to improve rendering speed (particularly when Full Screen)
        System.setProperty("sun.java2d.d3d", "false");

        //setup GUI interface
        GuiBase.setInterface(new GuiDesktop());

        //install our error handler
        ExceptionHandler.registerErrorHandling();

        //Turn on OpenGl acceleration to improve performance
        //System.setProperty("sun.java2d.opengl", "True");

        //setup GUI interface
        GuiBase.setInterface(new GuiDesktop());

        //install our error handler
        ExceptionHandler.registerErrorHandling();

        FModel.initialize(null, null);
    }

    public static Optional<Map<String, Integer>> simulate(int numGames,
                                                          int numMatches,
                                                          Map<String, List<String>> deck1Sections,
                                                          Map<String, List<String>> deck2Sections) {
        initialize();

        GameType type = GameType.Constructed;

        GameRules rules = new GameRules(type);
        rules.setAppliedVariants(EnumSet.of(type));
        rules.setGamesPerMatch(numMatches);

        List<RegisteredPlayer> players = new ArrayList<>();

        int playerId = 0;
        for (Map<String, List<String>> deckSections : List.of(deck1Sections, deck2Sections)) {
            Deck deck = DeckSerializer.fromSections(deckSections);
            RegisteredPlayer rp = new RegisteredPlayer(deck);
            rp.setPlayer(GamePlayerUtil.createAiPlayer("player%s".formatted(playerId), playerId));
            players.add(rp);
            playerId ++;
        }

        Match match = new Match(rules, players, "Test");

        Map<String, Integer> playerWins = new HashMap<>();

        for (RegisteredPlayer player : players) {
            playerWins.put(player.getDeck().getName(), 0);
        }

        int iGame = 0;
        while (!match.isMatchOver()) {
            // Play games until the match ends
            Optional<String> winnerName = simulateSingleMatch(match, iGame, true);
            winnerName.ifPresent(s -> playerWins.put(s, playerWins.get(s) + 1));
            iGame++;
        }

        System.out.flush();
        return Optional.of(playerWins);
    }

    public static Optional<String> simulateSingleMatch(final Match match, int iGame, boolean outputGamelog) {
        final StopWatch sw = new StopWatch();
        sw.start();

        final Game g1 = match.createGame();
        // will run match in the same thread
        try {
            TimeLimitedCodeBlock.runWithTimeout(() -> {
                match.startGame(g1);
                sw.stop();
            }, 120, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("Stopping slow match as draw");
        } catch (Exception | StackOverflowError e) {
            throw new RuntimeException(e);
        } finally {
            if (sw.isStarted()) {
                sw.stop();
            }
            if (!g1.isGameOver()) {
                g1.setGameOver(GameEndReason.Draw);
            }
        }

        List<GameLogEntry> log;
        if (outputGamelog) {
            log = g1.getGameLog().getLogEntries(null);
        } else {
            log = g1.getGameLog().getLogEntries(GameLogEntryType.MATCH_RESULTS);
        }
        Collections.reverse(log);
        for (GameLogEntry l : log) {
            System.out.println(l);
        }

        // If both players life totals to 0 in a single turn, the game should end in a draw
        if (g1.getOutcome().isDraw()) {
            System.out.printf("\nGame Result: Game %d ended in a Draw! Took %d ms.%n", 1 + iGame, sw.getTime());
            return Optional.empty();
        } else {
            System.out.printf("\nGame Result: Game %d ended in %d ms. %s has won!\n%n", 1 + iGame, sw.getTime(), g1.getOutcome().getWinningLobbyPlayer().getName());
            return g1.getOutcome().getWinningPlayer().getDeck().getName().describeConstable();//getWinningLobbyPlayer().getName().describeConstable();
        }
    }

}
