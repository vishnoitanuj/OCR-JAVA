package com.ocr.multicolumntextsegmentationforocr.services;

import com.ocr.multicolumntextsegmentationforocr.utils.Constants;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.bytedeco.leptonica.global.lept.*;

@Service
public class OCRService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final TessBaseAPI tessBaseAPI;
    private final FileContentService fileContentService;
    private final ExecutorService executorService;
    private StringBuilder parsedOut;
    private Map<Integer, String> ocrMap;

    public OCRService(@Qualifier("tesseract") TessBaseAPI tessBaseAPI, FileContentService fileContentService,@Qualifier("fixedThreadPool") ExecutorService executorService) {
        this.tessBaseAPI = tessBaseAPI;
        this.fileContentService = fileContentService;
        this.executorService = executorService;
    }

    public String doOCR(MultipartFile file) throws Exception {
        Map<Integer, byte[]> documentMap;
        ocrMap = new HashMap<>();
        StringBuilder parsedOut = new StringBuilder();
        try {
            documentMap = fileContentService.serveFile(file);
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
        CountDownLatch latch = new CountDownLatch(documentMap.size());
        for(Map.Entry<Integer, byte[]> imageMap : documentMap.entrySet()){
            executorService.submit(new OCRThread2(imageMap.getValue(), imageMap.getKey(), tessBaseAPI, latch));
            logger.info("Page put to OCR thread");
        }
        latch.await();
        for(String string:ocrMap.values())
            parsedOut.append(string).append("\n");
        return parsedOut.toString();
    }

    private String getParsedString(ByteBuffer imgBB, PIX image) throws Exception {
        if(image == null)
            throw new Exception("Not an image");
        String parsedOut;
        try{
            Future threadResult = executorService.submit(new OCRThread(image, tessBaseAPI));
            parsedOut = (String) threadResult.get();
            if(parsedOut.length()==0){
                logger.error("Unable to find Text");
                throw new Exception("No text Found");
            }
            logger.info("OCR Operation executed Successfully");
        } catch (Exception e){
            throw new Exception("Unknown Exception Occurred");
        } finally {
            pixDestroy(image);
            imgBB.clear();
        }
        return parsedOut;
    }

    class OCRThread2 implements Runnable{
        private byte[] imageBytes;
        private int pageNum;
        private TessBaseAPI tessBaseAPI;
        private CountDownLatch latch;

        public OCRThread2(byte[] imageBytes, int pageNum, TessBaseAPI tessBaseAPI, CountDownLatch latch){
            this.imageBytes = imageBytes;
            this.pageNum = pageNum;
            this.latch = latch;
            this.tessBaseAPI = tessBaseAPI;
        }

        @Override
        public void run(){
            ByteBuffer imgBB = ByteBuffer.wrap(imageBytes);
            PIX image = pixReadMem(imgBB, imageBytes.length);
            if(image == null)
                parsedOut.append("\n");
            tessBaseAPI.SetImage(image);
            tessBaseAPI.SetSourceResolution(Constants.IMAGE_DPI);
            BytePointer outText = tessBaseAPI.GetUTF8Text();
            ocrMap.put(pageNum, outText.getString());
            latch.countDown();
        }
    }

    class OCRThread implements Callable {

        private PIX image;
        private TessBaseAPI tessBaseAPI;

        public OCRThread(PIX image, TessBaseAPI tessBaseAPI){
            this.image = image;
            this.tessBaseAPI = tessBaseAPI;
        }


        @Override
        public String call() {
            tessBaseAPI.SetImage(image);
            tessBaseAPI.SetSourceResolution(Constants.IMAGE_DPI);
            BytePointer outText = tessBaseAPI.GetUTF8Text();
            pixDestroy(image);
            return outText.getString();
        }
    }
}
