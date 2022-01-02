package net.thevpc.gomail.test;

import net.thevpc.gomail.expr.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestExpr {
    @Test
    void test01() {
        Expr a = new ExprParser("a+b").parseStatementList();
        Assertions.assertTrue("(a + b)".equals(a.toString()));
        Assertions.assertTrue(a instanceof OpExpr);
        Assertions.assertTrue(((OpExpr) a).getOp() == TokenTType.PLUS);
    }

    @Test
    void test02() {
        OpExpr ee = (OpExpr) new ExprParser("(a+b)*3").parseStatementList();
        Assertions.assertTrue("((a + b) * 3.0)".equals(ee.toString()));
        Assertions.assertEquals(2, ee.getArguments().length);
        OpExpr aPlusB = (OpExpr) ee.getArguments()[0];
        Assertions.assertTrue(aPlusB.getArguments().length == 2);
        WordExpr a = (WordExpr) aPlusB.getArguments()[0];
        WordExpr b = (WordExpr) aPlusB.getArguments()[1];
        NumberExpr three = (NumberExpr) ee.getArguments()[1];
        Assertions.assertTrue(three.getValue() == 3.0);
    }

    @Test
    void test03() {
        OpExpr ee = (OpExpr) new ExprParser("(a + if a<0 then 1 else 2 end)*3").parseStatementList();
        Assertions.assertTrue("((a + if (a < 0.0) then 1.0 else 2.0 end) * 3.0)".equals(ee.toString()));
    }
}
