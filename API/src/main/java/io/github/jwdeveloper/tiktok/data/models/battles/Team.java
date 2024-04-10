package io.github.jwdeveloper.tiktok.data.models.battles;

public abstract class Team {
    /**
     * Provides a check for verifying if this team represents a 1v1 Team.
     * @return true if this team is of type {@link Team1v1}, false otherwise.
     */
    public boolean is1v1Team() {
        return this instanceof Team1v1;
    }

    /**
     * Provides a check for verifying if this team represents a 1v1 Team.
     * @return true if this team is of type {@link Team1v1}, false otherwise.
     */
    public boolean is2v2Team() {
        return this instanceof Team2v2;
    }

    /**
     * Convenience method to get this team as a {@link Team1v1}. If this team is of some
     * other type, an {@link IllegalStateException} will result. Hence it is best to use this method
     * after ensuring that this element is of the desired type by calling {@link #is1v1Team()} first.
     *
     * @return this team as a {@link Team1v1}.
     * @throws IllegalStateException if this team is of another type.
     */
    public Team1v1 getAs1v1Team() {
        if (is1v1Team())
            return (Team1v1) this;
        throw new IllegalStateException("Not a 1v1Team: " + this);
    }

    /**
     * Convenience method to get this team as a {@link Team2v2}. If this team is of some
     * other type, an {@link IllegalStateException} will result. Hence it is best to use this method
     * after ensuring that this element is of the desired type by calling {@link #is2v2Team()} first.
     *
     * @return this team as a {@link Team2v2}.
     * @throws IllegalStateException if this team is of another type.
     */
    public Team2v2 getAs2v2Team() {
        if (is2v2Team())
            return (Team2v2) this;
        throw new IllegalStateException("Not a 2v2Team: " + this);
    }
}