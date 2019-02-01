/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.User;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;

/**
 *
 * @author shubhamv
 */
@Stateless
public class EmailService {

    @Resource(name = "mail/RevRecMailSession")
    private Session mailSession;
    private final String sender = "Flowserve RCS Administrator";
    private final String fromAddress = "noreply@flowservenotify.enspir.net";
    private boolean production = false;

    @PostConstruct
    public void init() {
        if ("Prod".equals(System.getProperty("rcs_environment"))) {
            production = true;
        }
    }

    @Asynchronous
    public void emailReviewers(ReportingUnit ru) throws NamingException, AddressException {
        if (!production) {
            return;
        }
        List<String> reviewersEmailAddr = new ArrayList();
        //Adding live reviewers emails to recipients list
        for (User u : ru.getReviewers()) {
            if (u.getEmailAddress() != null) {
                reviewersEmailAddr.add(u.getEmailAddress());
            }
        }
        InternetAddress[] recipientsReviewersList = new InternetAddress[reviewersEmailAddr.size()];
        for (int i = 0; i < reviewersEmailAddr.size(); i++) {
            recipientsReviewersList[i] = new InternetAddress(reviewersEmailAddr.get(i));
        }

        Message message = new MimeMessage(mailSession);

        try {
            message.setSubject("IMPORTANT: Monthly JE Report for Review - " + ru.getName());
            message.setFrom(new InternetAddress(fromAddress, sender));
            message.setRecipients(Message.RecipientType.TO, recipientsReviewersList);
            message.setText("IMPORTANT: RCS Action Required - " + ru.getName() + " JE Report has been submitted for your review; please open the link to review your required action.  https://flowserve.enspir.net/rcs");
            Transport.send(message);
            Logger.getLogger(EmailService.class.getName()).log(Level.INFO, "Message Sent SuccessFully..please check your mail");

        } catch (MessagingException ex) {
            Logger.getLogger(EmailService.class.getName()).log(Level.INFO, "Oops! Got an Exception");
            Logger.getLogger(EmailService.class.getName()).log(Level.INFO, ex.toString());

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EmailService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Asynchronous
    public void emailApprovers(ReportingUnit ru) throws NamingException, AddressException {
        if (!production) {
            return;
        }

        List<String> approversEmailAddr = new ArrayList();
        for (User u : ru.getApprovers()) {
            if (u.getEmailAddress() != null) {
                approversEmailAddr.add(u.getEmailAddress());
            }
        }
        InternetAddress[] recipientApprovers = new InternetAddress[approversEmailAddr.size()];
        for (int i = 0; i < approversEmailAddr.size(); i++) {
            recipientApprovers[i] = new InternetAddress(approversEmailAddr.get(i));
        }

        Message message = new MimeMessage(mailSession);

        try {
            message.setSubject("IMPORTANT: Monthly JE Report for Approval - " + ru.getName());
            message.setFrom(new InternetAddress(fromAddress, sender));
            message.setRecipients(Message.RecipientType.TO, recipientApprovers);
            message.setText("IMPORTANT: RCS Action Required - " + ru.getName() + " JE Report has been submitted for your approval; please open the link to review your required action.  https://flowserve.enspir.net/rcs");
            Transport.send(message);
            Logger.getLogger(EmailService.class.getName()).log(Level.INFO, "Message Sent SuccessFully..please check your mail");

        } catch (MessagingException ex) {
            Logger.getLogger(EmailService.class.getName()).log(Level.INFO, "Oops! Got an Exception");
            Logger.getLogger(EmailService.class.getName()).log(Level.INFO, ex.toString());

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EmailService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Asynchronous
    public void emailPreparers(ReportingUnit ru) throws NamingException, AddressException {
        if (!production) {
            return;
        }

        List<String> preparersEmailAddr = new ArrayList();
        for (User u : ru.getPreparers()) {
            if (u.getEmailAddress() != null) {
                preparersEmailAddr.add(u.getEmailAddress());
            }
        }
        InternetAddress[] recipientPreparersEmails = new InternetAddress[preparersEmailAddr.size()];
        for (int i = 0; i < preparersEmailAddr.size(); i++) {
            recipientPreparersEmails[i] = new InternetAddress(preparersEmailAddr.get(i));
        }

        Message message = new MimeMessage(mailSession);

        try {
            message.setSubject("IMPORTANT: Monthly JE Report Rejected - " + ru.getName());
            message.setFrom(new InternetAddress(fromAddress, sender));
            message.setRecipients(Message.RecipientType.TO, recipientPreparersEmails);
            message.setText("IMPORTANT: RCS Action Required - " + ru.getName() + " JE Report has been rejected; please open the link to review your required action. https://flowserve.enspir.net/rcs");
            Transport.send(message);
            Logger.getLogger(EmailService.class.getName()).log(Level.INFO, "Message Sent SuccessFully..please check your mail");

        } catch (MessagingException ex) {
            Logger.getLogger(EmailService.class.getName()).log(Level.INFO, "Oops! Got an Exception");
            Logger.getLogger(EmailService.class.getName()).log(Level.INFO, ex.toString());

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EmailService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
