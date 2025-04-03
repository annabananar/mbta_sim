public abstract class Entity {

    private final String name;

    private MBTA mbta;
    private Log log;

    protected Entity(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object o) {
        if (o instanceof Entity e) {
          return e.getClass().equals(getClass()) && name.equals(e.name);
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public void initializeSim(MBTA mbta, Log log) {
        this.mbta = mbta;
        this.log = log;
    }
}
