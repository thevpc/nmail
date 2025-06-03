package net.thevpc.nmail.datasource;

import net.thevpc.nmail.expr.Token;
import net.thevpc.nmail.expr.TokenTType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {
    private List<RowWithId> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>();

    public CsvParser(Reader reader) {
        try {
            BufferedReader bReader=(reader instanceof BufferedReader)?(BufferedReader) reader:new BufferedReader(reader);
            String line;
            line = bReader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("missing headers");
            }
            columns = readLine(line);
            if (columns == null || columns.size() == 0) {
                throw new IllegalArgumentException("missing headers");
            }
            int index=0;
            while ((line = bReader.readLine()) != null) {
                List<String> a = readLine(line);
                index++;
                if (a != null) {
                    while (a.size() < columns.size()) {
                        a.add("");
                    }
                    rows.add(new RowWithId(index,a));
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private List<String> readLine(String line) {
        if (line == null || line.isEmpty() || line.startsWith("#")) {
            return null;
        }
        StringReader rr = new StringReader(line);
        Token was = null;
        Token t;

        List<String> r = new ArrayList<>();
        while ((t = readColumnOrSep(rr)) != null) {
            if (was == null || was.getTtype() == TokenTType.COMMA) {
                if (t.getTtype() == TokenTType.COMMA) {
                    r.add("");
                } else {
                    r.add(t.getSval());
                }
            } else {
                //last is a string
                if (t.getTtype() == TokenTType.COMMA) {
                    // do nothing
                } else {
                    r.add(t.getSval());
                }
            }
            was = t;
        }
        return r;
    }


    private Token readColumnOrSep(StringReader rr) {
        try {
            int a = rr.read();
            if (a < 0) {
                return null;
            }
            if (a == ',' || a == ';' || a == '\t' || a == '\n' || a == '\r') {
                return new Token(
                        TokenTType.COMMA,
                        String.valueOf((char) a),
                        0,
                        '\0'
                );
            }
            if (a == '\'') {
                StringBuilder sb = new StringBuilder();
                boolean end = false;
                while (!end && (a = rr.read()) > 0) {
                    switch (a) {
                        case '\'': {
                            end = true;
                            break;
                        }
                        case '\\': {
                            a = rr.read();
                            switch (a) {
                                case -1: {
                                    end = true;
                                    break;
                                }
                                case '\'': {
                                    sb.append('\'');
                                    break;
                                }
                                case '\\': {
                                    sb.append('\\');
                                    break;
                                }
                                case 'n': {
                                    sb.append('\n');
                                    break;
                                }
                                case 'r': {
                                    sb.append('\r');
                                    break;
                                }
                                case 'f': {
                                    sb.append('\f');
                                    break;
                                }
                                case 't': {
                                    sb.append('\r');
                                    break;
                                }
                                default: {
                                    sb.append('\\');
                                    sb.append((char) a);
                                }
                            }
                        }
                        default: {
                            sb.append((char) a);
                        }
                    }
                }
                return new Token(
                        TokenTType.SQ_STRING,
                        sb.toString(),
                        0,
                        '\0'
                );
            } else if (a == '\"') {
                StringBuilder sb = new StringBuilder();
                boolean end = false;
                while (!end && (a = rr.read()) > 0) {
                    switch (a) {
                        case '\"': {
                            end = true;
                            break;
                        }
                        case '\\': {
                            a = rr.read();
                            switch (a) {
                                case -1: {
                                    end = true;
                                    break;
                                }
                                case '\'': {
                                    sb.append('\'');
                                    break;
                                }
                                case '\\': {
                                    sb.append('\\');
                                    break;
                                }
                                case 'n': {
                                    sb.append('\n');
                                    break;
                                }
                                case 'r': {
                                    sb.append('\r');
                                    break;
                                }
                                case 'f': {
                                    sb.append('\f');
                                    break;
                                }
                                case 't': {
                                    sb.append('\r');
                                    break;
                                }
                                default: {
                                    sb.append('\\');
                                    sb.append((char) a);
                                }
                            }
                        }
                        default: {
                            sb.append((char) a);
                        }
                    }
                }
                return new Token(
                        TokenTType.DQ_STRING,
                        sb.toString(),
                        0,
                        '\0'
                );
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append((char) a);
                boolean end = false;
                while (!end) {
                    rr.mark(1);
                    a = rr.read();
                    if (a < 0) {
                        break;
                    }
                    switch (a) {
                        case '\t':
                        case ',':
                        case ';': {
                            end = true;
                            rr.reset();
                            break;
                        }
                        case '\\': {
                            a = rr.read();
                            switch (a) {
                                case -1: {
                                    end = true;
                                    break;
                                }
                                case '\'': {
                                    sb.append('\'');
                                    break;
                                }
                                case '\\': {
                                    sb.append('\\');
                                    break;
                                }
                                case 'n': {
                                    sb.append('\n');
                                    break;
                                }
                                case 'r': {
                                    sb.append('\r');
                                    break;
                                }
                                case 'f': {
                                    sb.append('\f');
                                    break;
                                }
                                case 't': {
                                    sb.append('\r');
                                    break;
                                }
                                default: {
                                    sb.append('\\');
                                    sb.append((char) a);
                                }
                            }
                        }
                        default: {
                            sb.append((char) a);
                        }
                    }
                }
                return new Token(
                        TokenTType.WORD,
                        sb.toString().trim(),
                        0,
                        '\0'
                );
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<RowWithId> getRows() {
        return rows;
    }

    public List<String> getColumns() {
        return columns;
    }
}
