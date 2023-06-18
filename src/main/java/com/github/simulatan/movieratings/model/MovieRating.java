package com.github.simulatan.movieratings.model;

import java.util.Objects;

public class MovieRating {
	private final String title;
	private final int year;
	private final double rating;

	public MovieRating(String title, int year, double rating) {
		if (title == null) throw new IllegalArgumentException("Title is null!");
		this.title = title;
		if (year < 1888) throw new IllegalArgumentException("Year is below 1888");
		this.year = year;
		if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating out of range");
		this.rating = rating;
	}

	public String getTitle() {
		return title;
	}

	public int getYear() {
		return year;
	}

	public double getRating() {
		return rating;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MovieRating that = (MovieRating) o;
		return year == that.year && Objects.equals(title, that.title);
	}

	@Override
	public int hashCode() {
		return Objects.hash(title, year);
	}
}
