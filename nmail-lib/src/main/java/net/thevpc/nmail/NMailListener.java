/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface NMailListener {
    default void onBeforeSend(NMailMessage mail){

    }
    default void onAfterSend(NMailMessage mail){

    }
    default void onSendError(NMailMessage mail, Throwable exc){

    }
}
