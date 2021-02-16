package br.com.alura.forum.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alura.forum.controller.form.TopicForm;
import br.com.alura.forum.controller.form.TopicFormUpdate;
import br.com.alura.forum.dto.TopicDto;
import br.com.alura.forum.dto.TopicDtoDetailed;
import br.com.alura.forum.modelo.Topic;
import br.com.alura.forum.repository.CourseRepository;
import br.com.alura.forum.repository.TopicRepository;

@RestController
@RequestMapping("/topics")
public class TopicsController {

	@Autowired
	private TopicRepository topicRepository;
	
	@Autowired
	private CourseRepository courseRepository;
	
	@GetMapping
	@Cacheable(value = "topicsList")	
	public Page<TopicDto> listTopics(@RequestParam(required = false) String courseName, 
			Pageable pagination) {
		
		if(courseName == null) {
			Page<Topic> topicsFromDatabase = topicRepository.findAll(pagination);
			return TopicDto.convertToDto(topicsFromDatabase);	
		}else {
			Page<Topic> topicsFromDatabase = topicRepository.findByCourse_Name(courseName, pagination);
			return TopicDto.convertToDto(topicsFromDatabase);	
		}
	}
	
	@PostMapping
	@Transactional
	@CacheEvict(value = "topicsList", allEntries = true)
	public ResponseEntity<TopicDto> addTopic(@RequestBody @Valid TopicForm formTopic, UriComponentsBuilder uriBuilder) {
		System.out.println(formTopic.getTitle());
		System.out.println(formTopic.getMessage());
		Topic topic = formTopic.convertToTopic(courseRepository);
		topicRepository.save(topic);
		
		URI uri = uriBuilder.path("/topics/{id}").buildAndExpand(topic.getId()).toUri();
		return ResponseEntity.created(uri).body(new TopicDto(topic));
		// You should adapt this code to @Put and @Delete as well. All should return 404 if Id id not found in database.
		
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<TopicDtoDetailed> detailsTopic(@PathVariable("id") Long topicId) {
		Optional<Topic> topicFromDatabase = topicRepository.findById(topicId);
		if(topicFromDatabase.isPresent()) {
			return ResponseEntity.ok(new TopicDtoDetailed(topicFromDatabase.get()));	
		}


		return ResponseEntity.notFound().build();
		
	}
	
	@PutMapping("/{id}")
	@Transactional
	@CacheEvict(value = "topicsList", allEntries = true)
	public ResponseEntity<TopicDto> updateTopic(@PathVariable Long id, @RequestBody @Valid TopicFormUpdate topicFormUpdate) {
		
		Topic topic = topicFormUpdate.update(id, topicRepository);
		
		return ResponseEntity.ok(new TopicDto(topic));
		
	}
	
	@DeleteMapping("/{id}")
	@Transactional
	@CacheEvict(value = "topicsList", allEntries = true)
	public ResponseEntity<?> deleteTopic(@PathVariable Long id) {
		topicRepository.deleteById(id);
		return ResponseEntity.ok().build();
		
	}
}
