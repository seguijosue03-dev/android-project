package com.student.learncraft;

import android.content.Context;
import android.net.Uri;
import org.apache.poi.xslf.usermodel.*;
import java.io.InputStream;
import java.util.List;

public class PPTReader {

    private Context context;

    public PPTReader(Context context) {
        this.context = context;
    }

    /**
     * Reads a PowerPoint file and extracts all text content
     * @param uri The URI of the PPT file
     * @return PPTContent object with all slides and text
     */
    public PPTContent readPPT(Uri uri, String fileName) throws Exception {
        PPTContent pptContent = new PPTContent(fileName);

        try {
            // Open the PPT file
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            XMLSlideShow ppt = new XMLSlideShow(inputStream);

            // Get all slides
            List<XSLFSlide> slides = ppt.getSlides();

            // Process each slide
            for (XSLFSlide slide : slides) {
                PPTContent.SlideContent slideContent = new PPTContent.SlideContent();

                // Get all shapes (text boxes) in the slide
                List<XSLFShape> shapes = slide.getShapes();

                boolean titleFound = false;

                for (XSLFShape shape : shapes) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        String text = textShape.getText();

                        // Skip empty text
                        if (text == null || text.trim().isEmpty()) {
                            continue;
                        }

                        text = text.trim();

                        // First significant text is usually the title
                        if (!titleFound && text.length() > 3) {
                            slideContent.setTitle(text);
                            titleFound = true;
                        } else {
                            // Split by newlines and add as bullet points
                            String[] lines = text.split("\n");
                            for (String line : lines) {
                                line = line.trim();
                                if (!line.isEmpty() && line.length() > 3) {
                                    // Remove bullet point symbols
                                    line = line.replaceAll("^[•·∙◦▪▫-]\\s*", "");
                                    slideContent.addContentPoint(line);
                                }
                            }
                        }
                    }
                }

                // Only add slide if it has content
                if (slideContent.getTitle() != null || !slideContent.getContentPoints().isEmpty()) {
                    pptContent.addSlide(slideContent);
                }
            }

            ppt.close();
            inputStream.close();

        } catch (Exception e) {
            throw new Exception("Error reading PPT file: " + e.getMessage());
        }

        return pptContent;
    }

    /**
     * Validates if the file is a valid PowerPoint file
     */
    public boolean isValidPPT(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            XMLSlideShow ppt = new XMLSlideShow(inputStream);
            boolean valid = ppt.getSlides().size() > 0;
            ppt.close();
            inputStream.close();
            return valid;
        } catch (Exception e) {
            return false;
        }
    }
}