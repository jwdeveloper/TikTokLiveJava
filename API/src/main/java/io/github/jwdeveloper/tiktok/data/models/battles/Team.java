/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.data.models.battles;

import lombok.Getter;

public abstract class Team {
    /** Value >= 0 when finished otherwise -1 */
    @Getter protected int totalPoints;

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