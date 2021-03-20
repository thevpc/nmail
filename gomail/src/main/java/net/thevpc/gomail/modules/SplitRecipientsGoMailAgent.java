/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.modules;

import net.thevpc.gomail.*;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class SplitRecipientsGoMailAgent implements GoMailAgent {
    public static final GoMailAgent INSTANCE=new SplitRecipientsGoMailAgent(DefaultGoMailAgent.INSTANCE);
    private GoMailAgent base;

    public SplitRecipientsGoMailAgent(GoMailAgent base) {
        this.base = base;
    }

    @Override
    public int sendMessage(GoMailMessage mail, Properties roProperties, GoMailContext mailContext) throws IOException {
        int max=-1;
        final Properties properties = new Properties();
        if (roProperties != null) {
            properties.putAll(roProperties);
        }
        if (mail.getProperties() != null) {
            properties.putAll(mail.getProperties());
        }

        String maxRecipients = properties.getProperty("gomail.max-recipients");
        if(maxRecipients!=null){
            max=Integer.parseInt(maxRecipients);
        }
        if(max<=0){
            return base.sendMessage(mail, roProperties, mailContext);
        }
        int recipients = mail.to().size() + mail.bcc().size() + mail.cc().size();
        if(recipients >max){
            List<GoMailMessage> splittedMails=new ArrayList<>();
            LinkedList<Recipient> recipientsList=new LinkedList<>();
            int count=0;
            for (String s : mail.to()) {
                recipientsList.add(new Recipient(RecipientType.TO,s));
            }
            for (String s : mail.cc()) {
                recipientsList.add(new Recipient(RecipientType.CC,s));
            }
            for (String s : mail.bcc()) {
                recipientsList.add(new Recipient(RecipientType.BCC,s));
            }

            GoMailMessage m0= mail.copy();
            m0.cc().clear();
            m0.to().clear();
            m0.cc().clear();
            m0.bcc().clear();

            while(!recipientsList.isEmpty()){
                count=max;
                GoMailMessage m2 = m0.copy();
                while(!recipientsList.isEmpty() && count>0){
                    count--;
                    Recipient recipient = recipientsList.removeFirst();
                    m2.addRecipients(recipient.getType(),recipient.getValue());
                }
                splittedMails.add(m2);
            }
            int x=0;
            for (GoMailMessage other : splittedMails) {
                x+=base.sendMessage(other, roProperties, mailContext);
            }
            return x;
        }else{
            return base.sendMessage(mail, roProperties, mailContext);
        }
    }
}
