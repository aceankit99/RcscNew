/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.servlet;

import com.flowserve.system606.model.ContractAttachment;
import com.flowserve.system606.service.CalculationVersionService;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author constacloud
 */
@WebServlet(name = "pdfViewer", urlPatterns = "/pdfViewer")
public class PdfViewer extends HttpServlet {

    private static final long serialVersionUID = 122L;
    private Logger logger = Logger.getLogger("com.flowserve.ecm");

    @Inject
    private CalculationVersionService calculationVersionService;

    public PdfViewer() {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String certId = request.getParameter("certId");
        String ocr = request.getParameter("ocr");

        boolean isOCR = false;
        if ("true".equals(ocr)) {
            isOCR = true;
        }
        ContractAttachment contractAttachment = calculationVersionService.findContractAttachment(new Long(certId));
        byte[] pdf = contractAttachment.getAttachment();
        ServletOutputStream op = response.getOutputStream();

        if (pdf != null) {
            response.setContentType(contractAttachment.getContentType());
            int length = pdf.length;
            response.setContentLength(length);
            String imageFilename = contractAttachment.getDescription();
            response.setHeader("Content-disposition", "inline; filename=\"" + imageFilename + "\"");
            byte[] bbuf = new byte[1024];
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(pdf));
            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                op.write(bbuf, 0, length);
            }
            in.close();
        } else {
            op.write("<html><body><div><b>PDF image not available</b></div></body></html>".getBytes());
        }

        op.flush();
        op.close();
    }
}
