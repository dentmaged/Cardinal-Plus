package in.twizmwaz.cardinal;

import in.twizmwaz.cardinal.cycle.Cycle;
import in.twizmwaz.cardinal.cycle.CycleTimer;
import in.twizmwaz.cardinal.event.CycleCompleteEvent;
import in.twizmwaz.cardinal.match.Match;
import in.twizmwaz.cardinal.match.MatchState;
import in.twizmwaz.cardinal.rotation.Rotation;
import in.twizmwaz.cardinal.rotation.exception.RotationLoadException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class GameHandler {

    private static GameHandler handler;
    private Rotation rotation;
    private World matchWorld;
    private Match match;
    private Cycle cycle;
    private CycleTimer cycleTimer;

    public GameHandler() throws RotationLoadException {
        handler = this;
        rotation = new Rotation();
        cycle = new Cycle(rotation.getNext(), UUID.randomUUID(), this);
        cycleAndMakeMatch();
    }

    public static GameHandler getGameHandler() {
        return handler;
    }

    public void cycleAndMakeMatch() {
        rotation.move();
        World oldMatchWorld = matchWorld;
        cycle.run();
        if (match != null) match.unregisterModules();
        this.match = new Match(this, cycle.getUuid(), cycle.getMap());
        this.match.registerModules();
        Bukkit.getLogger().info("[CardinalPGM] " + this.match.getModules().size() + " modules loaded.");
        Bukkit.getServer().getPluginManager().callEvent(new CycleCompleteEvent(match));
        cycle = new Cycle(rotation.getNext(), UUID.randomUUID(), this);
        Bukkit.unloadWorld(oldMatchWorld, true);
    }

    public Rotation getRotation() {
        return rotation;
    }

    public World getMatchWorld() {
        return matchWorld;
    }

    public void setMatchWorld(World world) {
        this.matchWorld = world;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Cycle getCycle() {
        return cycle;
    }

    public CycleTimer getCycleTimer() {
        return cycleTimer;
    }

    public JavaPlugin getPlugin() {
        return Cardinal.getInstance();
    }

    public boolean startCycleTimer(int seconds) {
        if (this.getMatch().getState() != MatchState.PLAYING) {
            this.cycleTimer = new CycleTimer(this.getCycle(), seconds);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.getPlugin(), cycleTimer);
            return true;
        } else return false;
    }
}
