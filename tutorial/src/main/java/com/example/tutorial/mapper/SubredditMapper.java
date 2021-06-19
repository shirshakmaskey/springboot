package com.example.tutorial.mapper;

import com.example.tutorial.dto.SubredditDto;
import com.example.tutorial.model.Post;
import com.example.tutorial.model.Subreddit;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubredditMapper {
    @Mapping(target = "numberOfPosts",expression = "java(mapPosts(subreddit.getPosts()))")
    SubredditDto mapSubredditDto(Subreddit subreddit);

    default Integer mapPosts(List<Post> numberOfPosts){
        return numberOfPosts.size();
    }

    @InheritInverseConfiguration
    @Mapping(target = "posts",ignore = true)
    Subreddit mapDtoToSubreddit(SubredditDto subredditDto);
}
