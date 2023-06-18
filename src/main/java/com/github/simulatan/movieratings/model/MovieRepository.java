package com.github.simulatan.movieratings.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;

public class MovieRepository {
	private static final MovieRepository instance = new MovieRepository();
	public static MovieRepository getInstance() {
		return instance;
	}

	private final Connection connection;

	public MovieRepository() {
		try {
			this.connection = DriverManager.getConnection("jdbc:derby:db");
			this.connection.setAutoCommit(true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		this.movieRatings.addListener((ListChangeListener<MovieRating>) change -> {
			movieCount.setValue(change.getList().size());
			averageRating.setValue(change.getList().stream().mapToDouble(MovieRating::getRating).average().orElse(-1));
			minYear.setValue(change.getList().stream().mapToInt(MovieRating::getYear).min().orElse(1888));
			maxYear.setValue(change.getList().stream().mapToInt(MovieRating::getYear).max().orElse(LocalDate.now().getYear()));
		});

		try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT title, release_year, rating FROM movie"); ResultSet rs = preparedStatement.executeQuery()) {
			while (rs.next()) {
				movieRatings.add(new MovieRating(rs.getString(1), rs.getInt(2), rs.getDouble(3)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private final ObservableList<MovieRating> movieRatings = FXCollections.observableList(new LinkedList<>());

	public ObservableList<MovieRating> getMovieRatings() {
		return FXCollections.unmodifiableObservableList(movieRatings);
	}

	private final SimpleIntegerProperty movieCount = new SimpleIntegerProperty(0);
	private final SimpleDoubleProperty averageRating = new SimpleDoubleProperty(-1);
	private final SimpleIntegerProperty minYear = new SimpleIntegerProperty(1888);
	private final SimpleIntegerProperty maxYear = new SimpleIntegerProperty(LocalDate.now().getYear());

	public SimpleIntegerProperty movieCountProperty() {
		return movieCount;
	}

	public SimpleDoubleProperty averageRatingProperty() {
		return averageRating;
	}

	public SimpleIntegerProperty minYearProperty() {
		return minYear;
	}

	public SimpleIntegerProperty maxYearProperty() {
		return maxYear;
	}

	public void addMovieRating(String title, int year, double rating) throws SQLException {
		MovieRating movieRating = new MovieRating(title, year, rating);

		try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO movie (title, release_year, rating) VALUES (?, ?, ?)")) {
			preparedStatement.setString(1, title);
			preparedStatement.setInt(2, year);
			preparedStatement.setDouble(3, rating);

			int count = preparedStatement.executeUpdate();
			if (count == 0) throw new IllegalStateException("Nothing inserted");
		}

		movieRatings.add(movieRating);
	}

	public void removeMovieRating(MovieRating movieRating) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM movie WHERE title = ? AND release_year = ? AND rating = ?")) {
			preparedStatement.setString(1, movieRating.getTitle());
			preparedStatement.setInt(2, movieRating.getYear());
			preparedStatement.setDouble(3, movieRating.getRating());

			int count = preparedStatement.executeUpdate();
			if (count == 0) throw new IllegalStateException("Nothing deleted");
		}

		movieRatings.remove(movieRating);
	}
}
