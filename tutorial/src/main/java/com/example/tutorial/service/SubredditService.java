package com.example.tutorial.service;

import com.example.tutorial.dto.SubredditDto;
import com.example.tutorial.exception.SpringRedditException;
import com.example.tutorial.mapper.SubredditMapper;
import com.example.tutorial.model.Subreddit;
import com.example.tutorial.repository.SubredditRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class SubredditService {
    private final SubredditRepository subredditRepository;
    private final SubredditMapper subredditMapper;

    @Transactional
    public SubredditDto save(SubredditDto subredditDto){
        Subreddit save = subredditRepository.save(subredditMapper.mapDtoToSubreddit(subredditDto));
        subredditDto.setId(save.getId());
        return subredditDto;
    }

//    private Subreddit mapSubredditDto(SubredditDto subredditDto) {
//        return Subreddit.builder().name(subredditDto.getName()).description(subredditDto.getDescription()).build();
//    }

    @Transactional
    public List<SubredditDto> getAll() {
        return subredditRepository.findAll().stream().map(subredditMapper::mapSubredditDto).collect(Collectors.toList());
    }

    public SubredditDto getSubreddit(Long id){
        Subreddit subreddit = subredditRepository.findById(id).orElseThrow(()->new SpringRedditException("No subreddit found with this id"));
        return subredditMapper.mapSubredditDto(subreddit);
    }

//    private SubredditDto mapToDto(Subreddit subreddit) {
//        return SubredditDto.builder().name(subreddit.getName()).description(subreddit.getDescription()).build();
//    }
}
