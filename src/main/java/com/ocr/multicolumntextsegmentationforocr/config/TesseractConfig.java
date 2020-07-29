package com.ocr.multicolumntextsegmentationforocr.config;

import com.ocr.multicolumntextsegmentationforocr.utils.Constants;
import org.bytedeco.tesseract.TessBaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;

@Configuration
public class TesseractConfig {

    private TessBaseAPI tessBaseAPI;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Bean("tesseract")
    public TessBaseAPI setTessBaseAPI() {
        tessBaseAPI = new TessBaseAPI();
        File tessData = null;
        try {
              tessData = ResourceUtils.getFile("classpath:tessdata");
        } catch (FileNotFoundException e) {
            logger.error("Unable to locate tessData directory");
        }
        if (tessBaseAPI.Init(tessData.getPath(), Constants.englishLanguage) != 0){
            logger.error("Could not initialize tesseract");
            System.exit(500);
        }
        tessBaseAPI.SetPageSegMode(6);
        return tessBaseAPI;
    }

    @PreDestroy
    public void destroyApi(){
        tessBaseAPI.End();
        tessBaseAPI.Clear();
    }
}
