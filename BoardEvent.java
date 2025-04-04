import java.util.*;

public class BoardEvent implements Event {
    public final Passenger p;
    public final Train t;
    public final Station s;

    public BoardEvent(Passenger p, Train t, Station s) {
        this.p = p;
        this.t = t;
        this.s = s;
    }

    public boolean equals(Object o) {
        if (o instanceof BoardEvent e) {
            return p.equals(e.p) && t.equals(e.t) && s.equals(e.s);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(p, t, s);
    }

    public String toString() {
        return "Passenger " + p + " boards " + t + " at " + s;
    }

    public List<String> toStringList() {
        return List.of(p.toString(), t.toString(), s.toString());
    }

    public void replayAndCheck(MBTA mbta) {
        if(!(mbta.getTCurrStation(t).equals(s) && mbta.getPCurrStation(p).equals(s))) {
            throw new RuntimeException("Passenger and train must currently be at the same station");
        }
        if(!mbta.NextTrainToBoard(p, s).equals(t)) {
            throw new RuntimeException("Passenger and train must be going to the same station");
        }
        mbta.boardPassenger(t, p, s);
    }
}
