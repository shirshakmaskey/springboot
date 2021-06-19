package com.example.tutorial.service;

import com.example.tutorial.dto.PostRequest;
import com.example.tutorial.dto.PostResponse;
import com.example.tutorial.exception.PostNotFoundException;
import com.example.tutorial.exception.SubredditNotFoundException;
import com.example.tutorial.mapper.PostMapper;
import com.example.tutorial.model.Post;
import com.example.tutorial.model.Subreddit;
import com.example.tutorial.model.User;
import com.example.tutorial.repository.PostRepository;
import com.example.tutorial.repository.SubredditRepository;
import com.example.tutorial.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PostService {
    private final SubredditRepository subredditRepository;
    private final AuthService authService;
    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    public void save(PostRequest postRequest){
        Subreddit subreddit = subredditRepository.findByName(postRequest.getSubredditName()).orElseThrow(()->new SubredditNotFoundException(postRequest.getSubredditName()));
        User currentUser =  authService.getCurrentUser();
        postMapper.map(postRequest,subreddit, currentUser);

    }

    public PostResponse getPost(Long id){
        Post post = postRepository.findById(id).orElseThrow(()-> new PostNotFoundException(id.toString()));
        return postMapper.mapToDto(post);
    }

    public List<PostResponse> getAllPosts(){
        return postRepository.findAll().stream().map(postMapper::mapToDto).collect(Collectors.toList());
    }

    public List<PostResponse> getPostsBySubreddit(Long subredditId){
        Subreddit subreddit = subredditRepository.findById(subredditId).orElseThrow(()->new SubredditNotFoundException(subredditId.toString()));
        List<Post> posts = postRepository.findAllBySubreddit(subreddit);
        return posts.stream().map(postMapper::mapToDto).collect(Collectors.toList());
    }

    public List<PostResponse> getPostsByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(username));
        return postRepository.findByUser(user).stream().map(postMapper::mapToDto).collect(Collectors.toList());
    }
}
