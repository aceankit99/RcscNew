/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.service.DataExtractionService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.poi.util.IOUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author shubhamv
 */
@Named
@ViewScoped
public class PobOutput implements Serializable {

    @Inject
    private DataExtractionService dataExtractionService;
    private static Logger logger = Logger.getLogger("com.flowserve.system606");
    public static final String PREFIX = "msaccess";
    public static final String SUFFIX = ".accdb";

    private StreamedContent file;
    private InputStream inputStream;

    public StreamedContent getFile() throws Exception {

        inputStream = PobOutput.class.getResourceAsStream("/resources/excel_input_templates/POB_Output.accdb");

        File accessFile = stream2file(inputStream);
        String fileName = accessFile.getAbsolutePath();

        dataExtractionService.generateOutputDB(fileName);

        InputStream inputStreamFromOutputStream = new FileInputStream(accessFile);
        file = new DefaultStreamedContent(inputStreamFromOutputStream, "accdb", "POB_Output.accdb");
        return file;
    }

    public File stream2file(InputStream in) throws IOException {
        final File tempFile = File.createTempFile(PREFIX, SUFFIX);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }

}
