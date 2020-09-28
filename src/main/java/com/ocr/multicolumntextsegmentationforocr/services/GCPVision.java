package com.ocr.multicolumntextsegmentationforocr.services;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GCPVision {

    public static void main(String[] args) throws IOException {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

            String filePath = "src\\main\\resources\\handwritten.jpg";
            List<AnnotateImageRequest> requests = new ArrayList<>();

            ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
//            ImageContext imageContext = ImageContext.newBuilder().setLanguageHints(0,"en").build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            requests.add(request);

            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests. After completing all of your requests, call
            // the "close" method on the client to safely clean up any remaining background resources.
//            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder().setE
            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        System.out.format("Error: %s%n", res.getError().getMessage());
                        return;
                    }

                    // For full list of available annotations, see http://g.co/cloud/vision/docs
                    TextAnnotation annotation = res.getFullTextAnnotation();
                    for (Page page : annotation.getPagesList()) {
                        String pageText = "";
                        for (Block block : page.getBlocksList()) {
                            String blockText = "";
                            for (Paragraph para : block.getParagraphsList()) {
                                String paraText = "";
                                for (Word word : para.getWordsList()) {
                                    String wordText = "";
                                    for (Symbol symbol : word.getSymbolsList()) {
                                        wordText = wordText + symbol.getText();
                                        System.out.format(
                                                "Symbol text: %s (confidence: %f)%n",
                                                symbol.getText(), symbol.getConfidence());
                                    }
                                    System.out.format(
                                            "Word text: %s (confidence: %f)%n%n", wordText, word.getConfidence());
                                    paraText = String.format("%s %s", paraText, wordText);
                                }
                                // Output Example using Paragraph:
                                System.out.println("%nParagraph: %n" + paraText);
                                System.out.format("Paragraph Confidence: %f%n", para.getConfidence());
                                blockText = blockText + paraText;
                            }
                            pageText = pageText + blockText;
                        }
                    }
                    System.out.println("%nComplete annotation:");
                    System.out.println(annotation.getText());
                }
            }
        }
    }
}
