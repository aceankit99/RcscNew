package com.flowserve.system606.view;

import java.io.InputStream;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named
@RequestScoped
public class PobInput implements Serializable {

    private static final long serialVersionUID = 4451468645782448976L;

    private StreamedContent file;

    public PobInput() {
        InputStream stream = PobInput.class.getResourceAsStream("/resources/excel_input_templates/Alonso_PIQ_POCI_IMPORT_TEMPLATE_v1.xlsx");
        file = new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "poci_input_template.xlsx");
    }

    public StreamedContent getFile() {
        return file;
    }
}
