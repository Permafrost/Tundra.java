/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Lachlan Dowding
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package permafrost.tundra.net.smtp;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.net.email.EmailSSLSocketFactory;
import com.wm.util.Config;
import com.wm.util.Values;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.EnumHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.StringHelper;
import permafrost.tundra.mime.MIMETypeHelper;
import permafrost.tundra.net.uri.URIHelper;
import permafrost.tundra.server.NameHelper;
import permafrost.tundra.server.PasswordManagerHelper;
import permafrost.tundra.zip.GzipHelper;
import permafrost.tundra.zip.ZipEntryWithData;
import permafrost.tundra.zip.ZipHelper;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Convenience methods for dealing with SMTP and sending emails.
 */
public class SMTPHelper {
    /**
     * The default timeout used when communicating with SMTP servers.
     */
    private static final int DEFAULT_SMTP_TIMEOUT = 60 * 1000;

    /**
     * Disallow instantiation of this class.
     */
    private SMTPHelper() {}

    /**
     * Sends an email message represented by the given IData document via the configured SMTP server.
     *
     * @param document  The IData document representing the email message to send.
     * @return          The response from sending the email message as an IData document.
     */
    public static IData send(IData document) {
        IData response = IDataFactory.create();
        IDataCursor responseCursor = response.getCursor();
        ByteArrayOutputStream sessionLogStream = new ByteArrayOutputStream();
        Transport transport = null;

        try {
            Properties properties = getProperties(document);
            Session session = Session.getInstance(properties);
            Message message = create(session, document);

            session.setDebug(true);
            session.setDebugOut(new PrintStream(sessionLogStream, true, CharsetHelper.DEFAULT_CHARSET_NAME));

            URLName uri = (URLName)properties.get("mail.smtp.uri");

            transport = session.getTransport(uri);
            transport.connect(uri.getHost(), uri.getUsername(), uri.getPassword());
            transport.sendMessage(message, message.getAllRecipients());

            URLName redactedURI = new URLName(uri.getProtocol(), uri.getHost(), uri.getPort(), uri.getFile(), uri.getUsername(), uri.getPassword() == null ? null : "********");
            responseCursor.insertAfter("message.content", stringify(message));
            responseCursor.insertAfter("message.content.type", "message/rfc822");
            responseCursor.insertAfter("transport.uri", redactedURI.toString());
            responseCursor.insertAfter("transport.status", "Mail sent successfully");
        } catch(MessagingException ex) {
            ExceptionHelper.raiseUnchecked(ex);
        } catch(IOException ex) {
            ExceptionHelper.raiseUnchecked(ex);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch(MessagingException ex) {
                    // ignore exceptions when closing
                }
                responseCursor.destroy();
            }
            try {
                responseCursor.insertAfter("transport.log", sessionLogStream.toString(CharsetHelper.DEFAULT_CHARSET_NAME));
            } catch(IOException ex) {
                // ignore exception
            }
        }
        return response;
    }

    /**
     * Creates a MimeMessage from the given Session and IData document objects.
     *
     * @param session   The session used to create the message.
     * @param document  The IData document containing the message attributes.
     * @return          The MimeMessage created from the given session and document.
     */
    private static Message create(Session session, IData document) {
        if (document == null) return null;

        String from = null, subject = null, body = null, contentType = null;
        MimeMessage message = new MimeMessage(session);
        IDataCursor cursor = document.getCursor();

        try {
            // extract message properties from uri if given
            String uri = IDataHelper.get(cursor, "uri", String.class);
            IData parsedURI = URIHelper.parse(uri);
            if (parsedURI != null) {
                IDataCursor parsedURICursor = parsedURI.getCursor();
                try {
                    String scheme = IDataHelper.get(parsedURICursor, "scheme", String.class);
                    if ("mailto".equals(scheme)) {
                        setRecipient(message, Message.RecipientType.TO, IDataHelper.get(parsedURICursor, "body", String.class));
                        IData query = IDataHelper.get(parsedURICursor, "query", IData.class);
                        if (query != null) {
                            IDataCursor queryCursor = query.getCursor();
                            try {
                                setRecipient(message, Message.RecipientType.TO, IDataHelper.get(queryCursor, "to", String.class));
                                setRecipient(message, Message.RecipientType.CC, IDataHelper.get(queryCursor, "cc", String.class));
                                setRecipient(message, Message.RecipientType.BCC, IDataHelper.get(queryCursor, "bcc", String.class));
                                from = IDataHelper.get(queryCursor, "from", String.class);
                                subject = IDataHelper.get(queryCursor, "subject", String.class);
                                body = IDataHelper.get(queryCursor, "body", String.class);
                                contentType = IDataHelper.first(queryCursor, String.class, "content.type", "content-type");
                            } finally {
                                queryCursor.destroy();
                            }
                        }
                    } else {
                        throw new URISyntaxException(uri, "Email cannot be sent because mailto: scheme was not specified", 0);
                    }
                } finally {
                    parsedURICursor.destroy();
                }
            }

            String[] to = IDataHelper.get(cursor, "to", String[].class);
            String[] cc = IDataHelper.get(cursor, "cc", String[].class);
            String[] bcc = IDataHelper.get(cursor, "bcc", String[].class);
            from = IDataHelper.getOrDefault(cursor, "from", String.class, from);
            subject = IDataHelper.getOrDefault(cursor, "subject", String.class, subject == null ? "" : subject);
            body = IDataHelper.getOrDefault(cursor, "body", String.class, body == null ? "" : body);
            contentType = IDataHelper.getOrDefault(cursor, "content.type", String.class, contentType == null ? "text/plain" : contentType);
            IData headers = IDataHelper.get(cursor, "headers", IData.class);
            IData[] attachments = IDataHelper.get(cursor, "attachments", IData[].class);

            setRecipients(message, Message.RecipientType.TO, to);
            setRecipients(message, Message.RecipientType.CC, cc);
            setRecipients(message, Message.RecipientType.BCC, bcc);
            setFrom(message, from);
            setSubject(message, subject);

            if (attachments == null || attachments.length == 0) {
                setBody(message, body, contentType);
            } else {
                // build a multipart message
                Multipart multipart = new MimeMultipart();
                setBody(multipart, body, contentType);
                setAttachments(multipart, attachments);
                setMultipart(message, multipart);
            }

            setHeaders(message, headers);

            message.setSentDate(new Date());

            Address[] recipients = message.getAllRecipients();
            if (recipients == null || recipients.length == 0) {
                throw new IllegalArgumentException("Email cannot be sent because no recipients (to, cc, or bcc) were specified");
            }
        } catch(IOException ex) {
            ExceptionHelper.raiseUnchecked(ex);
        } catch(MessagingException ex) {
            ExceptionHelper.raiseUnchecked(ex);
        } catch (MimeTypeParseException ex) {
            ExceptionHelper.raiseUnchecked(ex);
        } catch(URISyntaxException ex) {
            ExceptionHelper.raiseUnchecked(ex);
        } finally {
            cursor.destroy();
        }

        return message;
    }

    /**
     * Sets the given recipient email addresses on the given Message.
     *
     * @param message               The Message to set the recipients on.
     * @param recipientType         The type of recipient to be set.
     * @param addresses             The list of recipient email addresses to be set.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setRecipients(Message message, Message.RecipientType recipientType, String[] addresses) throws MessagingException {
        if (message != null && recipientType != null && addresses != null) {
            for (String address : addresses) {
                setRecipient(message, recipientType, address);
            }
        }
    }

    /**
     * Sets the given recipient email address on the given Message.
     *
     * @param message               The Message to set the recipients on.
     * @param recipientType         The type of recipient to be set.
     * @param address               The recipient email address to be set.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setRecipient(Message message, Message.RecipientType recipientType, String address) throws MessagingException {
        if (message != null && recipientType != null && address != null) {
            message.addRecipients(recipientType, InternetAddress.parse(address, false));
        }
    }

    /**
     * Sets the given from email address on the given Message.
     *
     * @param message               The Message to set the from email address on.
     * @param from                  The from email address to set. If null, it will be defaulted.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setFrom(Message message, String from) throws MessagingException {
        if (message != null && message.getFrom() == null) {
            if (from == null) {
                from = getDefaultFrom();
            }
            message.addFrom(InternetAddress.parse(from, false));
        }
    }

    /**
     * Sets the given subject on the given Message.
     *
     * @param message               The Message to set the subject on.
     * @param subject               The subject to be set.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setSubject(MimeMessage message, String subject) throws MessagingException {
        if (message != null && subject != null) {
            message.setSubject(subject, CharsetHelper.DEFAULT_CHARSET_NAME);
        }
    }

    /**
     * Sets the body content directly on the given Message, not using a Multipart message.
     *
     * @param message               The Message to set the body content on.
     * @param body                  The body content.
     * @param contentType           The MIME type of the body content.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setBody(Message message, String body, String contentType) throws MessagingException {
        if (message != null) {
            if (body == null) body = "";
            if (contentType == null) contentType = "text/plain";

            contentType = normalizeContentType(contentType);
            message.setContent(body, contentType);
            message.setHeader("Content-Transfer-Encoding", getTransferEncoding(contentType, false));
        }
    }

    /**
     * Adds a new BodyPart representing the given email body to the given Multipart object.
     *
     * @param multipart                 The Multipart object to add the BodyPart to.
     * @param body                      The content of the email body.
     * @param contentType               The MIME type of the email body content.
     * @throws MessagingException       If a messaging exception occurs.
     * @throws MimeTypeParseException   If the contentType cannot be parsed as a MIME type.
     */
    private static void setBody(Multipart multipart, String body, String contentType) throws MessagingException, MimeTypeParseException {
        if (multipart != null && body != null && contentType != null) {
            multipart.addBodyPart(createBody(body, contentType));
        }
    }

    /**
     * Returns a new BodyPart object represent the given email body.
     *
     * @param body                      The content of the email body.
     * @param contentType               The MIME type of the email body content.
     * @return                          A new BodyPart object representing the email body.
     * @throws MessagingException       If a messaging exception occurs.
     */
    private static BodyPart createBody(String body, String contentType) throws MessagingException {
        MimeBodyPart bodyPart = null;
        if (body != null && contentType != null) {
            contentType = normalizeContentType(contentType);
            bodyPart = new MimeBodyPart();
            bodyPart.setContent(body, contentType);
            bodyPart.setHeader("Content-Transfer-Encoding", getTransferEncoding(contentType, false));
        }
        return bodyPart;
    }

    /**
     * Adds new BodyPart objects representing the attachments specified in the given IData[] document list to the given
     * Multipart object.
     *
     * @param multipart             The Multipart to add the new BodyPart objects to.
     * @param attachments           The IData[] document list containing the attachments.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setAttachments(Multipart multipart, IData[] attachments) throws IOException, MessagingException {
        if (multipart != null && attachments != null) {
            for (IData attachment : attachments) {
                setAttachment(multipart, attachment);
            }
        }
    }

    /**
     * Adds a new BodyPart representing the attachment specified in the given IData document to the given Multipart
     * object.
     *
     * @param multipart             The Multipart to add the new BodyPart to.
     * @param attachment            The IData document containing the attachment attributes.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setAttachment(Multipart multipart, IData attachment) throws IOException, MessagingException {
        if (multipart != null && attachment != null) {
            BodyPart bodyPart = createAttachment(attachment);
            if (bodyPart != null) multipart.addBodyPart(bodyPart);
        }
    }

    /**
     * Returns a new BodyPart object representing the attachment specified in the given IData document.
     *
     * @param attachment            The IData document containing the attachment attributes.
     * @return                      A new BodyPart object that represents the attachment.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static BodyPart createAttachment(IData attachment) throws IOException, MessagingException {
        BodyPart bodyPart = null;
        if (attachment != null) {
            IDataCursor cursor = attachment.getCursor();
            try {
                String name = IDataHelper.get(cursor, "name", String.class);
                Object content = IDataHelper.get(cursor, "content");
                String contentType = IDataHelper.getOrDefault(cursor, "content.type", String.class, MIMETypeHelper.DEFAULT_MIME_TYPE_STRING);
                CompressionType compressionType = IDataHelper.getOrDefault(cursor, "compression", CompressionType.class, CompressionType.NONE);
                IData headers = IDataHelper.get(cursor, "headers", IData.class);

                bodyPart = createAttachment(name, content, contentType, compressionType, headers);
            } finally {
                cursor.destroy();
            }
        }
        return bodyPart;
    }

    /**
     * Returns a new BodyPart object representing the given attachment.
     *
     * @param name                  The filename of the attachment.
     * @param content               The content of the attachment.
     * @param contentType           The MIME type of the content.
     * @param headers               The optional headers to be set on the BodyPart.
     * @return                      A new BodyPart object representing the attachment.
     * @throws MessagingException   If a messaging error occurs.
     */
    private static BodyPart createAttachment(String name, Object content, String contentType, CompressionType compressionType, IData headers) throws IOException, MessagingException {
        BodyPart bodyPart = null;
        if (name != null && content != null) {
            contentType = normalizeContentType(contentType);
            bodyPart = new MimeBodyPart();
            bodyPart.setFileName(normalizeFileName(name, compressionType));
            bodyPart.setContent(compressIfRequired(name, content, contentType, compressionType), contentType);
            bodyPart.setHeader("Content-Transfer-Encoding", getTransferEncoding(contentType, true));
            setHeaders(bodyPart, headers);
        }
        return bodyPart;
    }

    /**
     * Regular expression pattern which matches characters that should be removed
     */
    private static final Pattern ILLEGAL_FILENAME_CHARACTER_PATTERN = Pattern.compile("[^\\w\\u00A0-\\uFFFF ~!@#$%^&()_+={}\\[\\];',.-]");

    /**
     * Normalizes the given content name as an email attachment filename.
     *
     * @param name              The content name.
     * @param compressionType   The type of compression used on the content.
     * @return                  The filename to use when attaching the content to an email message.
     */
    private static String normalizeFileName(String name, CompressionType compressionType) {
        String filename;

        if (name != null && !name.trim().isEmpty()) {
            filename = name.trim();
        } else {
            filename = "attachment.dat";
        }

        // strip all illegal filename characters
        filename = StringHelper.replace(filename, ILLEGAL_FILENAME_CHARACTER_PATTERN, "");

        if (compressionType == CompressionType.GZIP) {
            filename = filename + ".gz";
        } else if (compressionType == CompressionType.ZIP) {
            filename = filename + ".zip";
        }

        return filename;
    }


    /**
     * Compresses the given content if required by the give compression type.
     *
     * @param contentName       The name of the content, usually filename.
     * @param content           The content to be compressed.
     * @param contentType       The MIME type of the content containing the charset parameter set to the character
     *                          encoding to use to encode the data as bytes if it is provided as a String.
     * @param compressionType   The type of compression to use.
     * @return                  The given contentStream compressed using the given compressionType if GZIP or ZIP was
     *                          specified, otherwise the contentStream is returned with no changes made.
     * @throws IOException      If an IO error occurs.
     */
    private static InputStream compressIfRequired(String contentName, Object content, String contentType, CompressionType compressionType) throws IOException {
        return compressIfRequired(contentName, content, MIMETypeHelper.of(contentType), compressionType);
    }

    /**
     * Compresses the given content if required by the give compression type.
     *
     * @param contentName       The name of the content, usually filename.
     * @param content           The content to be compressed.
     * @param contentType       The MIME type of the content containing the charset parameter set to the character
     *                          encoding to use to encode the data as bytes if it is provided as a String.
     * @param compressionType   The type of compression to use.
     * @return                  The given contentStream compressed using the given compressionType if GZIP or ZIP was
     *                          specified, otherwise the contentStream is returned with no changes made.
     * @throws IOException      If an IO error occurs.
     */
    private static InputStream compressIfRequired(String contentName, Object content, MimeType contentType, CompressionType compressionType) throws IOException {
        return compressIfRequired(contentName, InputStreamHelper.normalize(content, contentType), compressionType);
    }

    /**
     * Compresses the given content if required by the give compression type.
     *
     * @param contentName       The name of the content, usually filename.
     * @param contentStream     The content to be compressed.
     * @param compressionType   The type of compression to use.
     * @return                  The given contentStream compressed using the given compressionType if GZIP or ZIP was
     *                          specified, otherwise the contentStream is returned with no changes made.
     * @throws IOException      If an IO error occurs.
     */
    private static InputStream compressIfRequired(String contentName, InputStream contentStream, CompressionType compressionType) throws IOException {
        InputStream compressedStream = contentStream;
        if (contentStream != null) {
            if (compressionType == CompressionType.GZIP) {
                compressedStream = GzipHelper.compress(contentStream);
            } else if (compressionType == CompressionType.ZIP) {
                ZipEntryWithData entry = new ZipEntryWithData(contentName, contentStream);
                compressedStream = ZipHelper.compress(entry);
            }
        }
        return compressedStream;
    }

    /**
     * Sets the given Multipart as the content of the given Message.
     *
     * @param message               The Message whose content is to be set.
     * @param multipart             The Multipart to be set as the content of the Message.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setMultipart(Message message, Multipart multipart) throws MessagingException {
        if (message != null && multipart != null) {
            message.setContent(multipart);
        }
    }

    /**
     * Sets headers specified in the given IData document on the given Message.
     *
     * @param message               The Message the headers will be set on.
     * @param headers               The IData document containing the header key value pairs to be set.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setHeaders(Message message, IData headers) throws MessagingException {
        if (message != null && headers != null) {
            IDataCursor headerCursor = headers.getCursor();
            try {
                while(headerCursor.next()) {
                    String key = headerCursor.getKey();
                    Object value = headerCursor.getValue();
                    if (value != null) {
                        message.setHeader(key, value.toString());
                    }
                }
            } finally {
                headerCursor.destroy();
            }
        }
    }

    /**
     * Sets headers specified in the given IData document on the given BodyPart.
     *
     * @param bodyPart              The BodyPart the headers will be set on.
     * @param headers               The IData document containing the header key value pairs to be set.
     * @throws MessagingException   If a messaging exception occurs.
     */
    private static void setHeaders(BodyPart bodyPart, IData headers) throws MessagingException {
        if (bodyPart != null && headers != null) {
            IDataCursor headerCursor = headers.getCursor();
            try {
                while(headerCursor.next()) {
                    String key = headerCursor.getKey();
                    Object value = headerCursor.getValue();
                    if (value != null) {
                        bodyPart.setHeader(key, value.toString());
                    }
                }
            } finally {
                headerCursor.destroy();
            }
        }
    }

    /**
     * Returns the properties to be used to create a new SMTP session and transport.
     */
    private static Properties getProperties(IData document) {
        Properties properties = new Properties();

        String host = null, port = null, user = null, password = null, token = null, trustStore = null;
        EncryptionType encryptionType = null;
        AuthenticationType authenticationType = null;

        if (document != null) {
            IDataCursor cursor = document.getCursor();
            try {
                IData transport = IDataHelper.get(cursor, "transport", IData.class);
                if (transport != null) {
                    IDataCursor transportCursor = transport.getCursor();
                    try {
                        host = IDataHelper.get(transportCursor, "host", String.class);
                        port = IDataHelper.get(transportCursor, "port", String.class);

                        IData auth = IDataHelper.get(transportCursor, "auth", IData.class);
                        if (auth != null) {
                            IDataCursor authCursor = auth.getCursor();
                            try {
                                authenticationType = IDataHelper.get(authCursor, "type", AuthenticationType.class);
                                user = IDataHelper.get(authCursor, "user", String.class);
                                if (user != null && !user.isEmpty()) {
                                    password = IDataHelper.first(authCursor, String.class, "password", "pass");
                                }
                                token = IDataHelper.get(authCursor, "token", String.class);
                            } finally {
                                authCursor.destroy();
                            }
                        }

                        IData secure = IDataHelper.get(transportCursor, "secure", IData.class);
                        if (secure != null) {
                            IDataCursor secureCursor = secure.getCursor();
                            try {
                                encryptionType = IDataHelper.first(secureCursor, EncryptionType.class, "encryption", "transportLayerSecurity");
                                trustStore = IDataHelper.first(secureCursor, String.class, "truststore", "trustStoreAlias");
                            } finally {
                                secureCursor.destroy();
                            }
                        }
                    } finally {
                        transportCursor.destroy();
                    }
                }
            } finally {
                cursor.destroy();
            }
        }

        // fallbacks for properties if not specified in message to be sent
        if (host == null || host.isEmpty()) host = Config.getProperty("watt.server.smtpServer");
        if (host == null || host.isEmpty()) host = System.getProperty("mail.smtp.host");

        if (port == null || port.isEmpty()) port = Config.getProperty("watt.server.smtpServerPort");
        if (port == null || port.isEmpty()) port = System.getProperty("mail.smtp.port", "25");

        if (user == null || user.isEmpty()) {
            user = Config.getProperty("watt.server.smtp.userName");
            if (user != null && !user.isEmpty()) {
                password = PasswordManagerHelper.get("wm.server.admin.resource:smtp:" + user);
                if (password == null || password.isEmpty()) password = Config.getProperty("watt.server.smtp.password");
            }
        }
        if (user == null || user.isEmpty()) user = System.getProperty("mail.smtp.user");
        if (user != null && user.isEmpty()) user = null;

        if (password != null && password.isEmpty()) password = null;
        if (encryptionType == null) encryptionType = EnumHelper.normalize(Config.getProperty("watt.server.smtpTransportSecurity"), EncryptionType.class);
        if (trustStore == null || trustStore.isEmpty()) trustStore = Config.getProperty("watt.server.smtpTrustStoreAlias");
        if (authenticationType == null) authenticationType = EnumHelper.normalize(Config.getProperty("Basic", "watt.server.smtp.authentication.type"), AuthenticationType.class);

        // set properties for sending message
        if (host != null && !host.isEmpty()) properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);
        if (user != null) properties.setProperty("mail.smtp.user", user);
        if (password != null) properties.setProperty("mail.smtp.password", password);
        if ((user != null && authenticationType == AuthenticationType.BASIC) || authenticationType == AuthenticationType.BEARER) {
            properties.setProperty("mail.smtp.auth", "true");
        }
        String pass = password;
        if (authenticationType == AuthenticationType.BEARER) {
            properties.setProperty("mail.smtp.auth.mechanisms", "XOAUTH2");
            pass = token;
            if (pass != null && pass.isEmpty()) pass = null;
        }
        properties.put("mail.smtp.uri", new URLName("smtp", host, Integer.parseInt(port), null, user, pass));
        properties.put("mail.smtp.timeout", DEFAULT_SMTP_TIMEOUT);
        properties.setProperty("mail.smtp.starttls.required", "false");
        properties.setProperty("mail.smtp.ssl.enable", "false");
        properties.setProperty("mail.smtp.socketFactory.fallback", "true");
        if (encryptionType == EncryptionType.EXPLICIT || encryptionType == EncryptionType.IMPLICIT) {
            if (encryptionType == EncryptionType.EXPLICIT) {
                properties.setProperty("mail.smtp.starttls.required", "true");
                properties.setProperty("mail.smtp.ssl.enable", "false");
            } else {
                properties.setProperty("mail.smtp.ssl.enable", "true");
                properties.setProperty("mail.smtp.socketFactory.fallback", "false");
            }
            Values context = new Values();
            if (trustStore != null && !trustStore.isEmpty()) context.put("trustStore", trustStore);
            properties.put("mail.smtp.socketFactory", new EmailSocketFactory(context, encryptionType));
        }

        return properties;
    }

    /**
     * Returns the transfer encoding to use given the MIME type of the content to be encoded.
     *
     * @param contentType   The MIME type of the content being encoded.
     * @param attachment    Whether this transfer encoding relates to an attachment or not.
     * @return              The value of the `Content-Transfer-Encoding` header to use.
     */
    private static String getTransferEncoding(String contentType, boolean attachment) {
        String transferEncoding;
        if (MIMETypeHelper.isText(MIMETypeHelper.of(contentType)) && !attachment) {
            transferEncoding = "quoted-printable";
        } else {
            transferEncoding = "base64";
        }
        return transferEncoding;
    }

    /**
     * Returns the default from address to use when no from was specified.
     * @return The default from address to use when no from was specified.
     */
    private static String getDefaultFrom() {
        String from = Config.getProperty("tn.mail.from");
        if (from == null) {
            from = System.getProperty("mail.smtp.from");
        }
        if (from == null) {
            String hostName;
            try {
                // TODO: is there an alternative that doesn't leak implementation details?
                hostName = NameHelper.InternetAddress.localhost().getDomain();
            } catch (java.net.UnknownHostException e) {
                hostName = "webmethods";
            }
            from = "Integration-Server@" + hostName.toLowerCase();
        }
        return from;
    }

    /**
     * Returns the given message as a String.
     *
     * @param message               The message to stringify.
     * @return                      The messages as a String.
     * @throws MessagingException   If a messaging error occurs.
     * @throws IOException          If an IO error occurs.
     */
    private static String stringify(Message message) throws MessagingException, IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        message.writeTo(stream);
        return stream.toString();
    }

    /**
     * The parameter named used for specifying a character set in a MIME type.
     */
    private static final String MIME_TYPE_CHARSET_PARAMETER_NAME = "charset";

    /**
     * Normalizes the given MIME type by ensuring a character set is specified for text types. If no character set
     * is specified already, it will be set to the default character set "UTF-8".
     *
     * @param contentType   The MIME type to normalize.
     * @return              The normalized MIME type.
     */
    private static String normalizeContentType(String contentType) {
        MimeType mimeType = MIMETypeHelper.of(contentType);
        if (MIMETypeHelper.isText(mimeType)) {
            String charset = mimeType.getParameter(MIME_TYPE_CHARSET_PARAMETER_NAME);
            if (charset == null) {
                mimeType.setParameter(MIME_TYPE_CHARSET_PARAMETER_NAME, CharsetHelper.DEFAULT_CHARSET_NAME);
            }
        }
        return mimeType.toString();
    }

    /**
     * An SSLSocketFactory to be used to create sockets when sending email via SMTP using TLS.
     */
    private static final class EmailSocketFactory extends EmailSSLSocketFactory {
        protected boolean explicit;

        public EmailSocketFactory(Values context, EncryptionType encryptionType) {
            super(context, null);
            this.explicit = encryptionType == EncryptionType.EXPLICIT;
        }

        public Socket createSocket() throws IOException {
            Socket socket;
            if (explicit) {
                socket = new Socket();
            } else {
                socket = super.createSocket();
            }
            return socket;
        }
    }

    /**
     * The types of SMTP transport encryption that is supported.
     */
    public enum EncryptionType {
        NONE, EXPLICIT, IMPLICIT;
    }

    /**
     * The types of SMTP transport authentication that is supported.
     */
    public enum AuthenticationType {
        NONE, BASIC, BEARER;
    }

    /**
     * The types of email attachment compression that is supported.
     */
    public enum CompressionType {
        NONE, GZIP, ZIP;
    }
}
