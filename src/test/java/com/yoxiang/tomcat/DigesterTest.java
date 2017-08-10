package com.yoxiang.tomcat;

import org.apache.commons.digester3.Digester;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * Author: Rivers
 * Date: 2017/7/26 07:17
 */
public class DigesterTest {

    @Test
    public void testDigester() {

        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("department", Department.class);
        digester.addSetProperties("department");
        digester.addObjectCreate("department/user", User.class);
        digester.addSetProperties("department/user");
        digester.addSetNext("department/user", "addUser");
        digester.addCallMethod("department/extension", "putExtension", 2);
        digester.addCallParam("department/extension/property-name", 0);
        digester.addCallParam("department/extension/property-value", 0);

        try {
            System.out.println(System.getProperty("user.dir"));
            Department department = digester.parse(new File(System.getProperty("user.dir") + "/src/main/resources/dept-config.xml"));
            System.out.printf(department.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}
