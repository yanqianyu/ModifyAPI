import org.eclipse.jdt.core.dom.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;

public class MyVisitor extends ASTVisitor{
    static AtomicInteger id = new AtomicInteger();
    private Connection conn;
    private PreparedStatement stmt = null;
    private String sql;

    MyVisitor( Connection conn, String sql) {
        this.conn = conn;
        this.sql = sql;
    }

    static String[] keyWord = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "continue",
            "default", "do", "double", "else", "enum", "exports", "extends", "final", "finally", "float", "for",
            "if", "implements", "import", "instanceof", "int", "interface", "long", "long", "module", "native", "new",
            "package", "private", "protected", "public", "requires", "return", "short", "static", "strictfp", "super", "switch", "synchronized",
            "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "null", "false", "var",
            "const", "goto"
    };
    static String stopwords[] = {
            "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yourself", "yourselves",
            "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their",
            "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is",
            "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing",
            "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with",
            "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from",
            "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there",
            "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such",
            "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don",
            "should", "now", "lbrace", "rbrace", "dot", "comma", "eq", "semi", "lparen", "rparen", "colon", "lbracket", "rbracket",
            "lt", "gt", "{", "}", "(", ")", "[", "]", ",", "."
    };
    static String jdkPrefix[] = {
            "java.applet", "java.awt", "java.awt.color", "java.awt.datatransfer", "java.awt.dnd",
            "java.awt.event", "java.awt.font", "java.awt.geom", "java.awt.im", "java.awt.image", "java.awt.image.renderable",
            "java.awt.im.spi", "java.awt.print", "java.beans", "java.beans.beancontext", "java.io", "java.lang",
            "java.lang.annotation", "java.lang.instrument", "java.lang.invoke", "java.lang.management", "java.lang.ref",
            "java.lang.reflect", "java.math", "java.net", "java.nio", "java.nio.channels", "java.nio.channels.spi",
            "java.nio.charset", "java.nio.charset.spi", "java.nio.file", "java.nio.file.attribute", "java.nio.file.spi",
            "java.rmi", "java.rmi.activation", "java.rmi.dgc", "java.rmi.registry", "java.rmi.server", "java.security",
            "java.security.acl", "java.security.cert", "java.security.interfaces", "java.security.spec", "java.sql",
            "java.text", "java.text.spi", "java.util", "java.util.concurrent", "java.util.concurrent.atomic",
            "java.util.concurrent.locks", "java.util.jar", "java.util.logging", "java.util.prefs", "java.util.regex",
            "java.util.spi", "java.util.zip", "javax.accessibility", "javax.activation", "javax.activity", "javax.annotation",
            "javax.annotation.processing", "javax.crypto", "javax.crypto.interfaces", "javax.crypto.spec", "javax.imageio",
            "javax.imageio.event", "javax.imageio.metadata", "javax.imageio.plugins.bmp", "javax.imageio.plugins.jpeg",
            "javax.imageio.spi", "javax.imageio.stream", "javax.jws", "javax.jws.soap", "javax.lang.model",
            "javax.lang.model.element", "javax.lang.model.type", "javax.lang.model.util", "javax.management",
            "javax.management.loading", "javax.management.modelmbean", "javax.management.monitor",
            "javax.management.openmbean", "javax.management.relation", "javax.management.remote",
            "javax.management.remote.rmi", "javax.management.timer", "javax.naming", "javax.naming.directory",
            "javax.naming.event", "javax.naming.ldap", "javax.naming.spi", "javax.net", "javax.net.ssl", "javax.print",
            "javax.print.attribute", "javax.print.attribute.standard", "javax.print.event", "javax.rmi", "javax.rmi.CORBA",
            "javax.rmi.ssl", "javax.script", "javax.security.auth", "javax.security.auth.callback",
            "javax.security.auth.kerberos", "javax.security.auth.login", "javax.security.auth.spi",
            "javax.security.auth.x500", "javax.security.cert", "javax.security.sasl", "javax.sound.midi",
            "javax.sound.midi.spi", "javax.sound.sampled", "javax.sound.sampled.spi", "javax.sql", "javax.sql.rowset",
            "javax.sql.rowset.serial", "javax.sql.rowset.spi", "javax.swing", "javax.swing.border", "javax.swing.colorchooser",
            "javax.swing.event", "javax.swing.filechooser", "javax.swing.plaf", "javax.swing.plaf.basic",
            "javax.swing.plaf.metal", "javax.swing.plaf.multi", "javax.swing.plaf.nimbus", "javax.swing.plaf.synth",
            "javax.swing.table", "javax.swing.text", "javax.swing.text.html", "javax.swing.text.html.parser",
            "javax.swing.text.rtf", "javax.swing.tree", "javax.swing.undo", "javax.tools", "javax.transaction",
            "javax.transaction.xa", "javax.xml", "javax.xml.bind", "javax.xml.bind.annotation",
            "javax.xml.bind.annotation.adapters", "javax.xml.bind.attachment", "javax.xml.bind.helpers",
            "javax.xml.bind.util", "javax.xml.crypto", "javax.xml.crypto.dom", "javax.xml.crypto.dsig",
            "javax.xml.crypto.dsig.dom", "javax.xml.crypto.dsig.keyinfo", "javax.xml.crypto.dsig.spec",
            "javax.xml.datatype", "javax.xml.namespace", "javax.xml.parsers", "javax.xml.soap", "javax.xml.stream",
            "javax.xml.stream.events", "javax.xml.stream.util", "javax.xml.transform", "javax.xml.transform.dom",
            "javax.xml.transform.sax", "javax.xml.transform.stax", "javax.xml.transform.stream", "javax.xml.validation",
            "javax.xml.ws", "javax.xml.ws.handler", "javax.xml.ws.handler.soap", "javax.xml.ws.http", "javax.xml.ws.soap",
            "javax.xml.ws.spi", "javax.xml.ws.spi.http", "javax.xml.ws.wsaddressing", "javax.xml.xpath",
            "org.ietf.jgss", "org.omg.CORBA", "org.omg.CORBA_2_3", "org.omg.CORBA_2_3.portable", "org.omg.CORBA.DynAnyPackage",
            "org.omg.CORBA.ORBPackage", "org.omg.CORBA.portable", "org.omg.CORBA.TypeCodePackage", "org.omg.CosNaming",
            "org.omg.CosNaming.NamingContextExtPackage", "org.omg.CosNaming.NamingContextPackage", "org.omg.Dynamic",
            "org.omg.DynamicAny", "org.omg.DynamicAny.DynAnyFactoryPackage", "org.omg.DynamicAny.DynAnyPackage",
            "org.omg.IOP", "org.omg.IOP.CodecFactoryPackage", "org.omg.IOP.CodecPackage", "org.omg.Messaging",
            "org.omg.PortableInterceptor", "org.omg.PortableInterceptor.ORBInitInfoPackage", "org.omg.PortableServer",
            "org.omg.PortableServer.CurrentPackage", "org.omg.PortableServer.POAManagerPackage",
            "org.omg.PortableServer.POAPackage", "org.omg.PortableServer.portable", "org.omg.PortableServer.ServantLocatorPackage",
            "org.omg.SendingContext", "org.omg.stub.java.rmi", "org.w3c.dom", "org.w3c.dom.bootstrap", "org.w3c.dom.events",
            "org.w3c.dom.ls", "org.xml.sax", "org.xml.sax.ext",
            "org.xml.sax.helpers"
    };

    public static boolean isJdkApi(String s){
        for(String si: jdkPrefix){
            if(s.startsWith(si)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration node) {

        Block block = node.getBody();

        ArrayList<String> apiseq = new ArrayList<>();
        ArrayList<String> JDKapiseq = new ArrayList<>();

        StringBuffer APIseq = new StringBuffer();
        StringBuffer JDKAPIseq = new StringBuffer();

        block.accept(new ASTVisitor() {
            @Override
            public void endVisit(MethodInvocation node){
                // 处理o.m()
                Expression expr = node.getExpression();
                if(expr != null){
                    ITypeBinding typeBinding = expr.resolveTypeBinding();
                    if(typeBinding != null){
                        String qualifiedName = typeBinding.getQualifiedName();
                        Pattern p = Pattern.compile("<|>|,");
                        Matcher m = p.matcher(qualifiedName);
                        String[] className = p.split(qualifiedName);
                        StringBuffer api = new StringBuffer();

                        matcher(api, m, className);
                        api.append(" ");
                        api.append(node.getName());
                        apiseq.add(api.toString().replaceAll(" +", " ").trim());

                        if(isJdkApi(qualifiedName)) {
                            JDKapiseq.add(api.toString().replaceAll(" +", " ").trim());
                        }
                        //System.out.println("api: " + api.toString().replaceAll(" +", " "));
//                        if(isJdkApi(qualifiedName)){
//                            ret.add(qualifiedName + " " + node.getName());
//                        }
                    }
                }
            }

            public void matcher(StringBuffer api, Matcher m, String[] className){
                for(int i = 0; i < className.length; i++){
                    String[] packageName = className[i].split("\\.");
                    api.append(packageName[packageName.length - 1]);
                    if(m.find()){
                        api.append(" ");
                        api.append(m.group());
                        api.append(" ");
                    }
                }
            }

            // 处理new o
            @Override
            public void endVisit(ClassInstanceCreation node){
                if (node == null){
                    return;
                }
                String qualifiedName = node.getType().resolveBinding().getQualifiedName();
                Pattern p = Pattern.compile("<|>|,");
                Matcher m = p.matcher(qualifiedName);
                String[] className = p.split(qualifiedName);
                StringBuffer api = new StringBuffer();

                matcher(api, m, className);

                api.append(" new");
                apiseq.add(api.toString().replaceAll(" +", " ").trim());

                if(isJdkApi(qualifiedName)){
                    JDKapiseq.add(api.toString().replaceAll(" +", " ").trim());
                }

                //System.out.println("api: " + api.toString().replaceAll(" +", " "));
//                if(isJdkApi(qualifiedName)){
//                    ret.add(qualifiedName + " " + "new");
//                }
            }
        });

        for(String api: apiseq){
            APIseq.append(api);
            APIseq.append(" ");
        }

        for(String api: JDKapiseq){
            JDKAPIseq.append(api);
            JDKAPIseq.append(" ");
        }
        String api = APIseq.toString().trim();
        String jdkapi = JDKAPIseq.toString().trim();

        int key = id.getAndIncrement();
        // 写入数据库
        try {
            stmt = conn.prepareStatement(sql);
            // stmt.setInt(1, key);
            stmt.setString(1, api);
            stmt.setString(2, jdkapi);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}

