/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import net.thevpc.gomail.expr.Expr;
import net.thevpc.gomail.util.SerializedForm;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
 *
 * @author vpc
 */
public interface GoMailDataSourceFactory {

    SupportedValue<GoMailDataSource> create(String type, Expr[] args);
}
