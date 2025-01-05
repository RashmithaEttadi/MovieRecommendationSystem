package com.rashmitha.AdvancedDBProject.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.rashmitha.AdvancedDBProject.Exception.EntityNotFoundException;
import com.rashmitha.AdvancedDBProject.model.Movies;
import com.rashmitha.AdvancedDBProject.repository.MoviesRepository;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.search.SearchOperator.text;
import static com.mongodb.client.model.search.SearchOptions.searchOptions;
import static com.mongodb.client.model.search.SearchPath.fieldPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class MoviesService {
    @Autowired
    private MoviesRepository moviesRepository;
    @Autowired
    private MongoTemplate mongoTemplate;


    private final MongoCollection<Document> collection;
    @Value("${spring.data.mongodb.atlas.search.index}")
    private String index;

    public MoviesService(MongoTemplate mongoTemplate) {

        this.collection = mongoTemplate.getCollection("movies");
    }

    public Movies saveMovies(Movies movie)
    {

        return moviesRepository.save(movie);
    }

    public Movies updateMovieById(ObjectId id, Movies updatedMovie) {
        Movies existingMovie = moviesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movie with ID " + id + " not found."));
        Update update = new Update();
        update.set("title", updatedMovie.getTitle());
        update.set("fullplot", updatedMovie.getFullplot());
        update.set("directors", updatedMovie.getDirectors());
        update.set("year", updatedMovie.getYear());
        update.set("genres", updatedMovie.getGenre());

        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(id)), update, Movies.class);

        return moviesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Movie with ID " + id + " not found."));
    }

    public String deleteMovieById(ObjectId id) {
        if (moviesRepository.existsById(id)) {
            moviesRepository.deleteById(id);
            return "Movie with ID " + id + " has been deleted successfully.";
        } else {
            throw new IllegalArgumentException("Movie with ID " + id + " not found.");
        }
    }
    public List<Movies> findByMoviesName(String movieName)
    {
        return moviesRepository.findByTitleIgnoreCase(movieName);
    }

    public Collection<Document> moviesByKeywords(String keywords, int limit) {

        Bson searchStage = search(text(fieldPath("fullplot"), keywords), searchOptions().index(index));
        Bson projectStage = project(fields(excludeId(), include("title", "year", "fullplot", "imdb.rating")));
        Bson limitStage = limit(limit);
        List<Bson> pipeline = List.of(searchStage, projectStage, limitStage);
        List<Document> docs = collection.aggregate(pipeline).into(new ArrayList<>());
        if (docs.isEmpty()) {
            throw new EntityNotFoundException("moviesByKeywords", keywords);
        }
        return docs;
    }

    public Collection<Document> findByGenreName(String genreName, int limit) {
        Bson matchStage = Aggregates.match(Filters.regex("genres", Pattern.compile(genreName, Pattern.CASE_INSENSITIVE)));

        Bson projectStage = Aggregates.project(
                Projections.fields(
                        Projections.excludeId(),
                        Projections.include("title", "year", "fullplot", "genres", "directors")
                )
        );
        Bson limitStage = Aggregates.limit(limit);

        List<Bson> pipeline = List.of(matchStage, projectStage, limitStage);
        List<Document> docs = collection.aggregate(pipeline).into(new ArrayList<>());

        if (docs.isEmpty()) {
            throw new EntityNotFoundException("Movies with genre " + genreName + " not found.");
        }

        return docs;
    }

    public Collection<Document> findByDirectorName(String directorName, int limit) {

        Bson matchStage = Aggregates.match(Filters.regex("directors", Pattern.compile(directorName, Pattern.CASE_INSENSITIVE)));
        Bson projectStage = Aggregates.project(
                Projections.fields(
                        Projections.excludeId(),
                        Projections.include("title", "year", "fullplot", "directors", "genres")
                )
        );

        Bson limitStage = Aggregates.limit(limit);

        List<Bson> pipeline = List.of(matchStage, projectStage, limitStage);

        List<Document> docs = collection.aggregate(pipeline).into(new ArrayList<>());

        if (docs.isEmpty()) {
            throw new EntityNotFoundException("Movies directed by " + directorName + " not found.");
        }

        return docs;
    }



}
