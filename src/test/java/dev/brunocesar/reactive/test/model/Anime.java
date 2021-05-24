package dev.brunocesar.reactive.test.model;

import java.util.Objects;

public class Anime {

    private final String title;
    private final String studio;
    private final int episodes;

    public Anime(String title, String studio, int episodes) {
        this.title = title;
        this.studio = studio;
        this.episodes = episodes;
    }

    public String getTitle() {
        return title;
    }

    public String getStudio() {
        return studio;
    }

    public int getEpisodes() {
        return episodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Anime anime = (Anime) o;
        return episodes == anime.episodes && Objects.equals(title, anime.title) && Objects.equals(studio, anime.studio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, studio, episodes);
    }

    @Override
    public String toString() {
        return "Anime{" +
                "title='" + title + '\'' +
                ", studio='" + studio + '\'' +
                ", episodes=" + episodes +
                '}';
    }

}
