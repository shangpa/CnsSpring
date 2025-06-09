package com.example.springjwt.api.vision;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class GcpVisionClient {

    public List<String> detectLabels(MultipartFile imageFile) {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            ByteString imgBytes = ByteString.copyFrom(imageFile.getBytes());

            Image image = Image.newBuilder().setContent(imgBytes).build();
            Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();

            List<AnnotateImageResponse> responses = vision.batchAnnotateImages(List.of(request)).getResponsesList();

            List<String> results = new ArrayList<>();
            for (AnnotateImageResponse res : responses) {
                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    results.add(annotation.getDescription().toLowerCase()); // 소문자로 통일
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("GCP Vision API 호출 실패: " + e.getMessage(), e);
        }
    }
}