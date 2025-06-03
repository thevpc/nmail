/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import net.thevpc.nmail.expr.Expr;

/**
 *
 * @author vpc
 */
public interface NMailDataSourceFactory {

    SupportedValue<NMailDataSource> create(Expr arg);
}
