/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailListener {
    public void onBeforeSend(GoMailMessage mail);
    public void onAfterSend(GoMailMessage mail);
    public void onSendError(GoMailMessage mail,Throwable exc);
}
