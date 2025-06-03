package net.thevpc.nmail.expr;

public class IfExpr extends AbstractExpr {

    private Branch[] branches;
    private Expr elseValue;


    public IfExpr(Branch[] branches, Expr elseValue) {
        this.branches = branches;
        this.elseValue = elseValue;
    }

    public Branch[] getBranches() {
        return branches;
    }

    public Expr getElseValue() {
        return elseValue;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder("if");
        sb.append(" ").append(branches[0].condition);
        sb.append(" then ").append(branches[0].value);
        for (int i = 1; i < branches.length; i++) {
            new StringBuilder(" else if");
            sb.append(" ").append(branches[i].condition);
            sb.append(" then ").append(branches[i].value);
        }
        if(elseValue !=null){
            sb.append(" else ").append(elseValue);
        }
        sb.append(" end");
        return sb.toString();
    }

    @Override
    public String asString() {
        return String.valueOf(toString());
    }
    public static class Branch{
        private Expr condition;
        private Expr value;

        public Branch(Expr condition, Expr value) {
            this.condition = condition;
            this.value = value;
        }

        public Expr getCondition() {
            return condition;
        }

        public Expr getValue() {
            return value;
        }
    }

}
