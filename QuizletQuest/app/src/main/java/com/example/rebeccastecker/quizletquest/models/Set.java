package com.example.rebeccastecker.quizletquest.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by rebeccastecker on 4/11/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Set {
    @JsonProperty("created_by")
    public String username;
    public Long id;
    public String title;
    public List<Term> terms;
}
