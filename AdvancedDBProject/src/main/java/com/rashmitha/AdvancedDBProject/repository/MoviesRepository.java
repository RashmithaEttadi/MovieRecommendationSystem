package com.rashmitha.AdvancedDBProject.repository;

import com.rashmitha.AdvancedDBProject.model.Movies;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoviesRepository extends MongoRepository<Movies, ObjectId> {

  public List<Movies> findByTitleIgnoreCase(String movieName);

}
