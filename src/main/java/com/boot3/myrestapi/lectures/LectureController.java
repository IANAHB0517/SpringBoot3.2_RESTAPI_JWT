package com.boot3.myrestapi.lectures;

import com.boot3.myrestapi.lectures.dto.LectureReqDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping(value = "/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {

    private final LectureRepository lectureRepository;
    private final ModelMapper modelMapper;

    //constructor injection
//    public LectureController(LectureRepository lectureRepository) {
//        this.lectureRepository = lectureRepository;
//    }

    @PostMapping
    public ResponseEntity<?> createLecture(@RequestBody @Valid LectureReqDto lectureReqDto, Errors errors) {
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);
        Lecture addedLecture = lectureRepository.save(lecture);

        // api/lectures/10 링크를 생성
        WebMvcLinkBuilder selfLinkBuilder =
                WebMvcLinkBuilder.linkTo(LectureController.class).slash(addedLecture.getId());
        URI createUri = selfLinkBuilder.toUri();
        return ResponseEntity.created(createUri).body(addedLecture);
    }
}
