package Utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import Models.Reclamation;
import Models.Reponse;

public class EmailService {
    // Mailtrap credentials - using SMTP credentials instead of API token
    private static final String USERNAME = "58758cbf58c694";  // Replace with your SMTP username
    private static final String PASSWORD = "310f4b9ed48247";  // Replace with your SMTP password
    private static final String HOST = "sandbox.smtp.mailtrap.io";
    private static final int PORT = 2525;

    private static Properties getEmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", HOST);
        props.put("mail.debug", "true");
        return props;
    }

    public static boolean sendResponseNotification(Reclamation reclamation, Reponse reponse, String recipientEmail) {
        try {
            // Create session with authentication
            Session session = Session.getInstance(getEmailProperties(), new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            
            // Set the "From" header field
            message.setFrom(new InternetAddress("support@coachini.com"));
            
            // Set the "To" header field
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            
            // Set the "Subject" header field
            message.setSubject("Réponse à votre réclamation #" + reclamation.getIdReclamation());

            // Create HTML content
            String htmlContent = createEmailTemplate(reclamation, reponse);

            // Set content directly
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Send message
            Transport.send(message);

            System.out.println("Email notification sent successfully to: " + recipientEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String createEmailTemplate(Reclamation reclamation, Reponse reponse) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #F58400; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; padding: 20px; }
                    .reclamation-details { background-color: #fff; padding: 15px; margin: 15px 0; border-left: 4px solid #F58400; border-radius: 4px; }
                    .response { background-color: #fff; padding: 15px; margin: 15px 0; border-left: 4px solid #28a745; border-radius: 4px; }
                    h2, h3 { margin-top: 0; color: #444; }
                    .header h2 { color: white; margin: 0; }
                    p { margin: 10px 0; }
                    strong { color: #555; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>Réponse à votre réclamation</h2>
                    </div>
                    <div class="content">
                        <p>Cher(e) client(e),</p>
                        <p>Nous avons traité votre réclamation et voici notre réponse :</p>
                        
                        <div class="reclamation-details">
                            <h3>Votre réclamation :</h3>
                            <p><strong>Type :</strong> %s</p>
                            <p><strong>Description :</strong> %s</p>
                            <p><strong>Date :</strong> %s</p>
                        </div>
                        
                        <div class="response">
                            <h3>Notre réponse :</h3>
                            <p>%s</p>
                            <p><strong>Date de réponse :</strong> %s</p>
                        </div>
                        
                        <p>Nous vous remercions de votre confiance et restons à votre disposition pour toute information complémentaire.</p>
                    </div>
                    <div class="footer">
                        <p>Cordialement,<br><strong>L'équipe Coachini</strong></p>
                        <p style="color: #999; font-size: 11px;">Cet email est généré automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                reclamation.getType(),
                reclamation.getDescription(),
                new java.text.SimpleDateFormat("dd/MM/yyyy").format(reclamation.getDate()),
                reponse.getContenu(),
                new java.text.SimpleDateFormat("dd/MM/yyyy").format(reponse.getDate_reponse())
            );
    }
} 