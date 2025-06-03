/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import net.thevpc.nmail.expr.ExprVars;

import java.util.Properties;

/**
 * sends a NON EXPANDABLE mail
 *
 * @author taha.bensalah@gmail.com
 */
public interface NMailAgent {

    public int sendMessage(NMailMessage mail, Properties properties, NMailContext mailContext, ExprVars vars) ;
}
