package com.boot3.myrestapi.lectures;

import com.boot3.myrestapi.common.errors.ErrorsResource;
import com.boot3.myrestapi.lectures.dto.LectureReqDto;
import com.boot3.myrestapi.lectures.dto.LectureResDto;
import com.boot3.myrestapi.lectures.dto.LectureResource;
import com.boot3.myrestapi.lectures.validator.LectureValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {

    private final LectureRepository lectureRepository;
    private final ModelMapper modelMapper;
    private final LectureValidator lectureValidator;

    //constructor injection
//    public LectureController(LectureRepository lectureRepository) {
//        this.lectureRepository = lectureRepository;
//    }

    @GetMapping
    public ResponseEntity queryLectures(Pageable pageable) {
        Page<Lecture> lecturePage = this.lectureRepository.findAll(pageable);
        return ResponseEntity.ok(lecturePage);
    }

    @PostMapping
    public ResponseEntity<?> createLecture(@RequestBody @Valid LectureReqDto lectureReqDto, Errors errors) {
        if(errors.hasErrors()) {
            return getErrors(errors);
        }

        //biz 로직에 대한 입력항목 체크
        lectureValidator.validate(lectureReqDto, errors);
        if(errors.hasErrors()) {
            return getErrors(errors);
        }

        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);
        //free, offline 필드 초기화
        lecture.update();
        Lecture addedLecture = lectureRepository.save(lecture);
        //Entity => ResDto
        LectureResDto lectureResDto = modelMapper.map(addedLecture, LectureResDto.class);

        WebMvcLinkBuilder selfLinkBuilder =
                linkTo(LectureController.class).slash(addedLecture.getId());
        URI createUri = selfLinkBuilder.toUri();

        LectureResource lectureResource = new LectureResource(lectureResDto);
        lectureResource.add(linkTo(LectureController.class).withRel("query-lectures"));
        //lectureResource.add(selfLinkBuilder.withSelfRel());
        lectureResource.add(selfLinkBuilder.withRel("update-lecture"));

        return ResponseEntity.created(createUri).body(lectureResource);
    }

    private static ResponseEntity<ErrorsResource> getErrors(Errors errors) {

        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}
