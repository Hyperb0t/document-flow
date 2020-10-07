package ru.itis.emailsender;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;


public class EmailSender {
    private final static String QUEUE_NAME = "email_send_queue";

    private final static String RECEIVER_EMAIL = "zehggoirrrka@dropmail.me";

    public static void sendEmailWithAttachments(String host, String port,
                                                final String userName, final String password, String toAddress,
                                                String subject, String message, String[] attachFiles)
            throws AddressException, MessagingException {
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.user", userName);
        properties.put("mail.password", password);

        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        Session session = Session.getInstance(properties, auth);

        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // adds attachments
        if (attachFiles != null && attachFiles.length > 0) {
            for (String filePath : attachFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();

                try {
                    attachPart.attachFile(filePath);
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }

                multipart.addBodyPart(attachPart);
            }
        }

        // sets the multi-part as e-mail's content
        msg.setContent(multipart);

        // sends the e-mail
        Transport.send(msg);

    }

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicConsume(QUEUE_NAME, false, (consumerTag, message) -> {
                File f = new File("tempSend.pdf");
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(message.getBody());
                    fos.flush();
                    System.out.println("received file");
                    String[] attachFiles = {"tempSend.pdf"};
                    sendEmailWithAttachments("smtp.gmail.com", "587", "app.hyperbot@gmail.com",
                            "cbhma8hq", RECEIVER_EMAIL, "generated PDF", "here is your pdf file",
                            attachFiles
                            );
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                    System.out.println("send it to " + RECEIVER_EMAIL);
                    System.out.println();
                } catch (Exception e) {
                    System.out.println("couldn't send email");
                    throw new IllegalStateException(e);
                }
            }, consumerTag -> {
            });
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }
}
