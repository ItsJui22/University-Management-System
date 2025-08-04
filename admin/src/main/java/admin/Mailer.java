package admin;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Mailer {

    public static void send(String to, String subject, String body) {
        final String from = "alamsanjida363@gmail.com";  // Must match a verified sender in Mailjet
        final String username = "e817629e8ef12fe014aa3e851c0cd0c1";             // Mailjet API key
        final String password = "25e31811d4fffa3bee3e075f44e2eca2";          // Mailjet secret key

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "in-v3.mailjet.com");
        props.put("mail.smtp.port", "587");  // TLS

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "University Admin"));  // Display name
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("✅ Mailjet Email sent to: " + to);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Email failed.");
        }
    }
}
