package com.student.learncraft;

import java.util.ArrayList;
import java.util.List;

public class PPTContent {
    private String fileName;
    private List<SlideContent> slides;

    public PPTContent() {
        this.slides = new ArrayList<>();
    }

    public PPTContent(String fileName) {
        this.fileName = fileName;
        this.slides = new ArrayList<>();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<SlideContent> getSlides() {
        return slides;
    }

    public void setSlides(List<SlideContent> slides) {
        this.slides = slides;
    }

    public void addSlide(SlideContent slide) {
        this.slides.add(slide);
    }

    // Get all text content from all slides
    public List<String> getAllTextContent() {
        List<String> allText = new ArrayList<>();
        for (SlideContent slide : slides) {
            if (slide.getTitle() != null && !slide.getTitle().isEmpty()) {
                allText.add(slide.getTitle());
            }
            allText.addAll(slide.getContentPoints());
        }
        return allText;
    }

    // Inner class for slide content
    public static class SlideContent {
        private String title;
        private List<String> contentPoints;

        public SlideContent() {
            this.contentPoints = new ArrayList<>();
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getContentPoints() {
            return contentPoints;
        }

        public void setContentPoints(List<String> contentPoints) {
            this.contentPoints = contentPoints;
        }

        public void addContentPoint(String point) {
            this.contentPoints.add(point);
        }
    }
}