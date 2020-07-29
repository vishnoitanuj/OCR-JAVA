package com.ocr.multicolumntextsegmentationforocr.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "extract", consumes = "file", produces = "extracted Response")
public interface OCRApi {

    default ResponseEntity<String> extract(@ApiParam(value = "File to OCR", required = true, format = "pdf/image") @RequestParam("file")MultipartFile file){
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
