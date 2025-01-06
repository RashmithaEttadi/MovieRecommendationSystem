package com.rashmitha.AdvancedDBProject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="movies")
public class Movies {

    @Field("_id")
    private ObjectId Id;
    private String title;
    private String fullplot;
    @Field("directors")
    private List<String> directors;
    private int year;
    @Field("genres")
    private List<String> genre;


}
