/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailListener {
    default void onBeforeSend(GoMailMessage mail){

    }
    default void onAfterSend(GoMailMessage mail){

    }
    default void onSendError(GoMailMessage mail,Throwable exc){

    }
}
