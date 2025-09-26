package com.cinema.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cinema.dto.MovieDto;
import com.cinema.dto.MoviePageResponse;
import com.cinema.entities.Movie;
import com.cinema.exception.FileExistsException;
import com.cinema.exception.MovieNotFoundException;
import com.cinema.repositories.MovieRepository;

@Service
public class MovieServiceImpl implements MovieService {

	private final MovieRepository movieRepository;
	private final FileService fileService;

	public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
		this.movieRepository = movieRepository;
		this.fileService = fileService;
	}

	@Value("${project.poster}")
	private String path;

	@Value("${base.url}")
	private String baseUrl;

	@Override
	public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
		// 0. check for file already exists
		if (Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))) {
			throw new FileExistsException("File already exists! Please enter another file name!");
		}

		String uploadedFileName = fileService.uploadFile(path, file);

		movieDto.setPoster(uploadedFileName);

		Movie movie = new Movie(null, movieDto.getTitle(), movieDto.getDirector(), movieDto.getStudio(),
				movieDto.getMovieCast(), movieDto.getReleaseYear(), movieDto.getPoster());

		Movie SavedMovie = movieRepository.save(movie);

		String posterUrl = baseUrl.substring(1, baseUrl.length() - 1) + "/file/" + uploadedFileName;

		MovieDto response = new MovieDto(SavedMovie.getMovieId(), SavedMovie.getTitle(), SavedMovie.getDirector(),
				SavedMovie.getStudio(), SavedMovie.getMovieCast(), SavedMovie.getReleaseYear(), SavedMovie.getPoster(),
				posterUrl);

		return response;
	}

	@Override
	public MovieDto getMovie(Integer movieId) {
		Movie movie = movieRepository.findById(movieId)
				.orElseThrow(() -> new MovieNotFoundException("Movie Not Found with id= " + movieId));

		String posterUrl = baseUrl.substring(1, baseUrl.length() - 1) + "/file" + movie.getPoster();

		MovieDto response = new MovieDto(movie.getMovieId(), movie.getTitle(), movie.getDirector(), movie.getStudio(),
				movie.getMovieCast(), movie.getReleaseYear(), movie.getPoster(), posterUrl);

		return response;
	}

	@Override
	public List<MovieDto> getAllMovies() {
		List<Movie> movies = movieRepository.findAll();

		List<MovieDto> movieDtos = new ArrayList<>();

		for (Movie movie : movies) {
			String posterUrl = baseUrl.substring(1, baseUrl.length() - 1) + "/file/" + movie.getPoster();
			MovieDto response = new MovieDto(movie.getMovieId(), movie.getTitle(), movie.getDirector(),
					movie.getStudio(), movie.getMovieCast(), movie.getReleaseYear(), movie.getPoster(), posterUrl);
			movieDtos.add(response);
		}
		return movieDtos;
	}

	@Override
	public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
		Movie mv = movieRepository.findById(movieId)
				.orElseThrow(() -> new MovieNotFoundException("Movie Not Found with id= " + movieId));

		String fileName = mv.getPoster();
		if (file != null) {
			Files.deleteIfExists(Paths.get(path + File.separator + fileName));
			fileName = fileService.uploadFile(path, file);
		}

		movieDto.setPoster(fileName);

		Movie movie = new Movie(mv.getMovieId(), movieDto.getTitle(), movieDto.getDirector(), movieDto.getStudio(),
				movieDto.getMovieCast(), movieDto.getReleaseYear(), movieDto.getPoster());

		Movie updatedMovie = movieRepository.save(movie);

		String posterUrl = baseUrl.substring(1, baseUrl.length() - 1) + "/file/" + fileName;

		MovieDto response = new MovieDto(movie.getMovieId(), movie.getTitle(), movie.getDirector(), movie.getStudio(),
				movie.getMovieCast(), movie.getReleaseYear(), movie.getPoster(), posterUrl);

		return response;
	}

	@Override
	public String deleteMovie(Integer movieId) throws IOException {
		Movie mv = movieRepository.findById(movieId)
				.orElseThrow(() -> new MovieNotFoundException("Movie Not Found with id= " + movieId));
		Integer id = mv.getMovieId();

		Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));
		movieRepository.delete(mv);

		return "Movie deleted with id: " + id;
	}

	@Override
	public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Movie> moviePages = movieRepository.findAll(pageable);
		List<Movie> movies = moviePages.getContent();

		List<MovieDto> movieDtos = new ArrayList<>();
		// 2. iterate through the list, generate posterUrl for each movie obj,
		// and map to MovieDto obj
		for (Movie movie : movies) {
			String posterUrl = baseUrl.substring(1, baseUrl.length() - 1) + "/file/" + movie.getPoster();
			MovieDto movieDto = new MovieDto(movie.getMovieId(), movie.getTitle(), movie.getDirector(),
					movie.getStudio(), movie.getMovieCast(), movie.getReleaseYear(), movie.getPoster(), posterUrl);
			movieDtos.add(movieDto);
		}

		return new MoviePageResponse(movieDtos, pageNumber, pageSize, moviePages.getTotalElements(),
				moviePages.getTotalPages(), moviePages.isLast());
	}

	@Override
	public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy,
			String dir) {
		Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		Page<Movie> moviePages = movieRepository.findAll(pageable);
		List<Movie> movies = moviePages.getContent();

		List<MovieDto> movieDtos = new ArrayList<>();
		for (Movie movie : movies) {
			String posterUrl = baseUrl.substring(1, baseUrl.length() - 1) + "/file/" + movie.getPoster();
			MovieDto movieDto = new MovieDto(movie.getMovieId(), movie.getTitle(), movie.getDirector(),
					movie.getStudio(), movie.getMovieCast(), movie.getReleaseYear(), movie.getPoster(), posterUrl);
			movieDtos.add(movieDto);
		}

		return new MoviePageResponse(movieDtos, pageNumber, pageSize, moviePages.getTotalElements(),
				moviePages.getTotalPages(), moviePages.isLast());

	}

}
