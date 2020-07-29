package com.ocr.multicolumntextsegmentationforocr.controller;

import com.ocr.multicolumntextsegmentationforocr.api.OCRApi;
import com.ocr.multicolumntextsegmentationforocr.services.OCRService;
import com.ocr.multicolumntextsegmentationforocr.utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping(value = Constants.SPRING_REST_BASE_PATH)
public class OCRApiController implements OCRApi {

    private final OCRService ocrService;

    public OCRApiController(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    @PostMapping(value = "extract")
    public ResponseEntity extract(@RequestParam("file")MultipartFile file){
        String ocr;
        try{
            ocr = ocrService.doOCR(file);
        }
        catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(ocr, HttpStatus.OK);
    }
}
