package com.ocr.multicolumntextsegmentationforocr.services;

import com.ocr.multicolumntextsegmentationforocr.utils.Constants;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@Service
public class FileContentService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ExecutorService fixedThreadPool;
    private Map<Integer, byte[]> documentMap;

    public FileContentService(@Qualifier("fixedThreadPool") ExecutorService fixedThreadPool) {
        this.fixedThreadPool = fixedThreadPool;
        this.documentMap = new HashMap<>();
        ImageIO.scanForPlugins();
    }

    public Map<Integer, byte[]> serveFile(MultipartFile file) throws Exception {
        String fileType = file.getContentType();
        if (fileType.contains("image")) {
            documentMap.put(0, file.getBytes());
            return documentMap;
        } else if (fileType.contains("pdf")) {
            return convertPDFtoImageBytes(file.getBytes());
        } else
            throw new Exception("Unsupported Document Format");
    }

    private Map<Integer, byte[]> convertPDFtoImageBytes(byte[] pdfBytes) throws Exception {
        long startTime = System.currentTimeMillis();
        logger.debug("Parsing pdf to image");
        PDDocument pdf = null;
        try {
            pdf = PDDocument.load(pdfBytes);
            PDFRenderer pdfRenderer = new PDFRenderer(pdf);
            int numberOfPages = pdf.getNumberOfPages();
            CountDownLatch latch = new CountDownLatch(numberOfPages);
            logger.debug("Converting pdf to image with dpi = {}", Constants.IMAGE_DPI);
            for (int pageNum = 0; pageNum < numberOfPages; pageNum++)
                fixedThreadPool.submit(new PageBytes(pageNum, latch, pdfRenderer));
            latch.await();
            long endTime = System.currentTimeMillis();
            logger.debug("Time for conversion of pdf = {}", (endTime - startTime));
        } catch (Exception e) {
            throw new Exception("Unprocessed Entity");
        } finally {
            try {
                if (pdf != null)
                    pdf.close();
            } catch (IOException e) {
                logger.warn("Unable to close pdf");
            }
        }
        return documentMap;
    }

    class PageBytes implements Runnable {
        private CountDownLatch latch;
        private PDFRenderer pdfRenderer;
        private int pageNum;

        public PageBytes(int pageNum, CountDownLatch latch, PDFRenderer pdfRenderer) {
            this.pageNum = pageNum;
            this.latch = latch;
            this.pdfRenderer = pdfRenderer;
        }

        @Override
        public void run() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedImage bImage = null;
            try {
                bImage = pdfRenderer.renderImageWithDPI(pageNum, Constants.IMAGE_DPI, ImageType.RGB);
                ImageIO.write(bImage, Constants.IMAGE_FORMAT_NAME, outputStream);
            } catch (IOException e) {
                logger.error("Error occurred while converting pdf to image");
            } finally {
                Objects.requireNonNull(bImage).flush();
                try {
                    outputStream.close();
                    outputStream.flush();
                } catch (IOException e) {
                    logger.error("Unable to close buffered image");
                    e.printStackTrace();
                }
            }
            logger.info("Page {} conversion completed", pageNum + 1);
            documentMap.put(pageNum, outputStream.toByteArray());
            latch.countDown();
        }
    }
}
