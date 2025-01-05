package com.rashmitha.AdvancedDBProject.controller;

import com.rashmitha.AdvancedDBProject.Exception.EntityNotFoundException;
import com.rashmitha.AdvancedDBProject.model.Movies;
import com.rashmitha.AdvancedDBProject.service.MoviesService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/movies")
public class MoviesController {

    private final static Logger LOGGER = LoggerFactory.getLogger(MoviesController.class);
    @Autowired
    private MoviesService moviesService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> createMovie(@RequestBody Movies movie)
    {
        try {
            Movies movieName = moviesService.saveMovies(movie);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Movie added successfully with ID: " + movieName.getId());
        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while creating the Movie: " + e.getMessage());
        }

    }
    @DeleteMapping("/{movieId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> deleteMovie(@PathVariable String movieId)
    {

        try {
            ObjectId objectId = new ObjectId(movieId); // Convert String to ObjectId
            String responseMessage = moviesService.deleteMovieById(objectId);
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateMovieById(@PathVariable String id, @RequestBody Movies updatedMovie) {
        try {
            ObjectId objectId = new ObjectId(id); // Convert String to ObjectId
            Movies movie = moviesService.updateMovieById(objectId, updatedMovie);
            return ResponseEntity.ok("Movie with ID " + id + " has been updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{movieName}")
    public List<Movies> GetByMovieName(@PathVariable String movieName)
    {
       return moviesService.findByMoviesName(movieName);
    }

    @GetMapping("genre/{genreName}")
    public Collection<Document> GetByGenreName(@PathVariable String genreName,
                                       @RequestParam(value = "limit", defaultValue = "5") int limit)
    {
        return moviesService.findByGenreName(genreName, limit);
    }

    @GetMapping("director/{directorName}")
    public Collection<Document> GetByDirectorName(@PathVariable String directorName,
                                                  @RequestParam(value = "limit", defaultValue = "5") int limit)
    {
        return moviesService.findByDirectorName(directorName, limit);
    }



    @GetMapping("with/{keywords}")
    Collection<Document> getMoviesWithKeywords(@PathVariable String keywords,
                                               @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return moviesService.moviesByKeywords(keywords, limit);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "MongoDB didn't find any document.")
    public final void handleNotFoundExceptions(EntityNotFoundException e) {
        LOGGER.info("=> Movie not found: {}", e.toString());
    }
}
