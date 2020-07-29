package com.ocr.multicolumntextsegmentationforocr.utils;

public interface Constants {

    String englishLanguage = "eng";
    String SPRING_REST_BASE_PATH = "/ocr";
    int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 2;
    int IMAGE_DPI = 300;
    String IMAGE_FORMAT_NAME = "png";
}
