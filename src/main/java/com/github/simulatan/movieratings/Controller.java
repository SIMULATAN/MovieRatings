package com.github.simulatan.movieratings;

import com.github.simulatan.movieratings.model.MovieRating;
import com.github.simulatan.movieratings.model.MovieRepository;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class Controller {

	@FXML
	private Slider sldYearFilter;

	@FXML
	private TableColumn<MovieRating, String> tbcTitle;

	@FXML
	private TableColumn<MovieRating, Integer> tbcYear;

	@FXML
	private TableColumn<MovieRating, Double> tbcRating;

	@FXML
	private TableView<MovieRating> tbvMovieRatings;

	@FXML
	private Label txtAverageRating;

	@FXML
	private Label txtMovieCount;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtRating;

	@FXML
	private TextField txtYear;

	private FilteredList<MovieRating> movieRatings;

	@FXML
	void initialize() {
		movieRatings = new FilteredList<>(MovieRepository.getInstance().getMovieRatings());
		SortedList<MovieRating> movieRatingsSorted = new SortedList<>(movieRatings);

		movieRatingsSorted.comparatorProperty().bind(this.tbvMovieRatings.comparatorProperty());

		this.tbvMovieRatings.setItems(movieRatingsSorted);
		this.tbcTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
		this.tbcYear.setCellValueFactory(new PropertyValueFactory<>("year"));
		this.tbcRating.setCellValueFactory(new PropertyValueFactory<>("rating"));

		this.sldYearFilter.valueProperty().addListener((observableValue, number, t1) -> this.refreshFilters());
		this.sldYearFilter.minProperty().bind(MovieRepository.getInstance().minYearProperty());
		this.sldYearFilter.maxProperty().bind(MovieRepository.getInstance().maxYearProperty());

		MovieRepository.getInstance().getMovieRatings().addListener((ListChangeListener<MovieRating>) change -> refreshFilters());

		this.txtMovieCount.textProperty().bind(Bindings.format("Movies: %d", MovieRepository.getInstance().movieCountProperty()));
		this.txtAverageRating.textProperty().bind(Bindings.format("Average Rating: %.2f", MovieRepository.getInstance().averageRatingProperty()));
	}

	@FXML
	void addRating(ActionEvent event) {
		try {
			String title = txtName.getText();
			int year = Integer.parseInt(txtYear.getText());
			double rating = Double.parseDouble(txtRating.getText());

			MovieRepository.getInstance().addMovieRating(title, year, rating);
		} catch (Exception ex) {
			// quick n drity
			error(ex);
		}
	}

	@FXML
	void removeSelected(ActionEvent event) {
		MovieRating selectedItem = tbvMovieRatings.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			error("No item selected.");
			return;
		}

		try {
			MovieRepository.getInstance().removeMovieRating(selectedItem);
		} catch (Exception ex) {
			error(ex);
		}
	}

	private void error(Exception exception) {
		error(exception.getClass().getSimpleName() + ": " + exception.getMessage());
	}

	private void error(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setHeaderText("Error!");
		alert.setContentText(message);
		alert.show();
	}

	private void refreshFilters() {
		Platform.runLater(() -> this.movieRatings.setPredicate(movieRating -> movieRating.getYear() >= sldYearFilter.getValue()));
	}
}
