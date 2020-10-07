package ru.itis.pdffiller.services;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PdfTemplateEngine {

    public File render(File template, Map<String, String> map, PdfFont font, float fontSize, File output) {
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(template), new PdfWriter(output));
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
            for(Map.Entry<String, PdfFormField> e : form.getFormFields().entrySet()) {
                String content = map.get(e.getKey());
                if(content == null) {
                    continue;
                }
                e.getValue().setValue(content, font, fontSize).setReadOnly(true);
            }
            pdfDoc.close();
            return output;
        }catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Float getFontSize(PdfString defaultApperance) {
        Float result = null;
        String arr [] = defaultApperance.getValue().split(" ");
        for(int i = 0 ; i < arr.length; i++) {
            if(arr[i].startsWith("/") && i < arr.length-1) {
                try {
                    result = Float.parseFloat(arr[i+1]);
                    break;
                }catch (Exception ignored){}
            }
        }
        return result;
    }

    public File render(File template, Map<String, String> map, File output) {
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(template), new PdfWriter(output));
            PdfFont font = pdfDoc.getDefaultFont();
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
            for(Map.Entry<String, PdfFormField> e : form.getFormFields().entrySet()) {
                String content = map.get(e.getKey());
                Float fontSize = getFontSize(e.getValue().getDefaultAppearance());
                if(content == null || fontSize == null) {
                    continue;
                }
                e.getValue().setValue(content, font, fontSize).setReadOnly(true);
            }
            pdfDoc.close();
            return output;
        }catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
