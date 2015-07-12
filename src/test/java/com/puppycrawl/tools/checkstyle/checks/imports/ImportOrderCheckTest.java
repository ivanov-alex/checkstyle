////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks.imports;

import static com.puppycrawl.tools.checkstyle.checks.imports.ImportOrderCheck.MSG_ORDERING;
import static com.puppycrawl.tools.checkstyle.checks.imports.ImportOrderCheck.MSG_SEPARATION;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import sun.reflect.ConstructorAccessor;
import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;
import antlr.CommonHiddenStreamToken;

import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class ImportOrderCheckTest extends BaseCheckTestSupport {
    @Test
    public void testDefault() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        final String[] expected = {
            "5: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "9: " + getCheckMessage(MSG_ORDERING, "javax.swing.JComponent"),
            "11: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
            "13: " + getCheckMessage(MSG_ORDERING, "java.io.IOException"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder.java"), expected);
    }

    @Test
    public void testGroups() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("groups", "java.awt");
        checkConfig.addAttribute("groups", "javax.swing");
        checkConfig.addAttribute("groups", "java.io");
        final String[] expected = {
            "5: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "13: " + getCheckMessage(MSG_ORDERING, "java.io.IOException"),
            "16: " + getCheckMessage(MSG_ORDERING, "javax.swing.WindowConstants.*"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder.java"), expected);
    }

    @Test
    public void testGroupsRegexp() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("groups", "java, /^javax?\\.(awt|swing)\\./");
        checkConfig.addAttribute("ordered", "false");
        final String[] expected = {
            "11: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder.java"), expected);
    }

    @Test
    public void testSeparated() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("groups", "java.awt, javax.swing, java.io, java.util");
        checkConfig.addAttribute("separated", "true");
        checkConfig.addAttribute("ordered", "false");
        final String[] expected = {
            "9: " + getCheckMessage(MSG_SEPARATION, "javax.swing.JComponent"),
            "11: " + getCheckMessage(MSG_SEPARATION, "java.io.File"),
            "16: " + getCheckMessage(MSG_ORDERING, "javax.swing.WindowConstants.*"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder.java"), expected);
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("caseSensitive", "false");
        final String[] expected = {
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrderCaseInsensitive.java"), expected);
    }

    @Test(expected = CheckstyleException.class)
    public void testInvalidOption() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "invalid_option");
        final String[] expected = {};

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_Top.java"), expected);
    }

    @Test
    public void testTop() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "top");
        final String[] expected = {
            "4: " + getCheckMessage(MSG_ORDERING, "java.awt.Button.ABORT"),
            "18: " + getCheckMessage(MSG_ORDERING, "java.io.File.*"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_Top.java"), expected);
    }

    @Test
    public void testAbove() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "above");
        final String[] expected = {
            "5: " + getCheckMessage(MSG_ORDERING, "java.awt.Button.ABORT"),
            "8: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "13: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
            "14: " + getCheckMessage(MSG_ORDERING, "java.io.File.createTempFile"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_Above.java"), expected);
    }

    @Test
    public void testInFlow() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "inflow");
        final String[] expected = {
            "6: " + getCheckMessage(MSG_ORDERING, "java.awt.Dialog"),
            "11: " + getCheckMessage(MSG_ORDERING,
                     "javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE"),
            "12: " + getCheckMessage(MSG_ORDERING, "javax.swing.WindowConstants.*"),
            "13: " + getCheckMessage(MSG_ORDERING, "javax.swing.JTable"),
            "15: " + getCheckMessage(MSG_ORDERING, "java.io.File.createTempFile"),
            "16: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_InFlow.java"), expected);
    }

    @Test
    public void testUnder() throws Exception {
        // is default (testDefault)
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "under");
        final String[] expected = {
            "5: Wrong order for 'java.awt.Dialog' import.",
            "11: " + getCheckMessage(MSG_ORDERING, "java.awt.Button.ABORT"),
            "14: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_Under.java"), expected);
    }

    @Test
    public void testBottom() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "bottom");
        final String[] expected = {
            "15: " + getCheckMessage(MSG_ORDERING, "java.io.File"),
            "18: " + getCheckMessage(MSG_ORDERING, "java.awt.Button.ABORT"),
            "21: " + getCheckMessage(MSG_ORDERING, "java.io.Reader"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_Bottom.java"), expected);
    }

    @Test
    public void testHonorsTokenProperty() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("tokens", "IMPORT");
        final String[] expected = {
            "6: " + getCheckMessage(MSG_ORDERING, "java.awt.Button"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_HonorsTokensProperty.java"), expected);
    }

    @Test
    public void testWildcard() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("groups", "com,*,java");
        final String[] expected = {
            "9: " + getCheckMessage(MSG_ORDERING, "javax.crypto.Cipher"),
        };

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_Wildcard.java"), expected);
    }

    @Test
    public void testWildcardUnspecified() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(ImportOrderCheck.class);

        /*
        <property name="ordered" value="true"/>
        <property name="separated" value="true"/>
        */
        checkConfig.addAttribute("groups", "java,javax,org");
        final String[] expected = {};

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_WildcardUnspecified.java"), expected);
    }

    @Test
    public void testNoFailureForRedundantImports() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_NoFailureForRedundantImports.java"), expected);
    }

    @Test
    public void testStaticGroupsAlphabeticalOrder() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "top");
        checkConfig.addAttribute("groups", "org, java");
        checkConfig.addAttribute("sortStaticImportsAlphabetically", "true");
        final String[] expected = {};
        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrderStaticGroupOrder.java"), expected);
    }

    @Test
    public void testStaticGroupsOrder() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "top");
        checkConfig.addAttribute("groups", "org, java");
        final String[] expected = {
            "4: " + getCheckMessage(MSG_ORDERING, "org.abego.treelayout.Configuration.AlignmentInLevel"),
        };
        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrderStaticGroupOrder.java"), expected);
    }

    @Test
    public void testStaticGroupsAlphabeticalOrderBottom() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "bottom");
        checkConfig.addAttribute("groups", "org, java");
        checkConfig.addAttribute("sortStaticImportsAlphabetically", "true");
        final String[] expected = {};
        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrderStaticGroupOrderBottom.java"), expected);
    }

    @Test
    public void testStaticGroupsOrderBottom() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "bottom");
        checkConfig.addAttribute("groups", "org, java");
        final String[] expected = {
            "8: " + getCheckMessage(MSG_ORDERING, "org.abego.treelayout.Configuration.AlignmentInLevel"),
        };
        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrderStaticGroupOrderBottom.java"), expected);
    }

    @Test
    public void testStaticGroupsOrderAbove() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "above");
        checkConfig.addAttribute("groups", "org, java, sun");
        checkConfig.addAttribute("sortStaticImportsAlphabetically", "true");
        final String[] expected = {
            "7: " + getCheckMessage(MSG_ORDERING, "java.lang.Math.PI"),
            "8: " + getCheckMessage(MSG_ORDERING, "org.abego.treelayout.Configuration.AlignmentInLevel"),
        };
        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrderStaticGroupOrderBottom.java"), expected);
    }

    @Test
    public void testStaticOnDemandGroupsOrder() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "top");
        checkConfig.addAttribute("groups", "org, java");
        final String[] expected = {
            "4: " + getCheckMessage(MSG_ORDERING, "org.abego.treelayout.Configuration.*"),
            "9: " + getCheckMessage(MSG_ORDERING, "org.junit.Test"),
        };
        verify(checkConfig, getPath("imports" + File.separator
                 + "InputImportOrderStaticOnDemandGroupOrder.java"), expected);
    }

    @Test
    public void testStaticOnDemandGroupsAlphabeticalOrder() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "top");
        checkConfig.addAttribute("groups", "org, java");
        checkConfig.addAttribute("sortStaticImportsAlphabetically", "true");
        final String[] expected = {
            "9: " + getCheckMessage(MSG_ORDERING, "org.junit.Test"),
        };
        verify(checkConfig, getPath("imports" + File.separator
                 + "InputImportOrderStaticOnDemandGroupOrder.java"), expected);
    }

    @Test
    public void testStaticOnDemandGroupsOrderBottom() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "bottom");
        checkConfig.addAttribute("groups", "org, java");
        final String[] expected = {
            "8: " + getCheckMessage(MSG_ORDERING, "org.abego.treelayout.Configuration.*"),
        };
        verify(checkConfig, getPath("imports" + File.separator
                 + "InputImportOrderStaticOnDemandGroupOrderBottom.java"), expected);
    }

    @Test
    public void testStaticOnDemandGroupsAlphabeticalOrderBottom() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "bottom");
        checkConfig.addAttribute("groups", "org, java");
        checkConfig.addAttribute("sortStaticImportsAlphabetically", "true");
        final String[] expected = {};
        verify(checkConfig, getPath("imports" + File.separator
                 + "InputImportOrderStaticOnDemandGroupOrderBottom.java"), expected);
    }

    @Test
    public void testStaticOnDemandGroupsOrderAbove() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("option", "above");
        checkConfig.addAttribute("groups", "org, java");
        checkConfig.addAttribute("sortStaticImportsAlphabetically", "true");
        final String[] expected = {
            "7: " + getCheckMessage(MSG_ORDERING, "java.lang.Math.*"),
            "8: " + getCheckMessage(MSG_ORDERING, "org.abego.treelayout.Configuration.*"),
        };
        verify(checkConfig, getPath("imports" + File.separator
                 + "InputImportOrderStaticOnDemandGroupOrderBottom.java"), expected);
    }

    @Test(expected = CheckstyleException.class)
    public void testGroupWithSlashes() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("groups", "/^javax");
        final String[] expected = {};

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder.java"), expected);
    }

    @Test
    public void testGroupWithDot() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("groups", "java.awt.");
        final String[] expected = {};

        verify(checkConfig, getPath("imports" + File.separator + "InputImportOrder_NoFailureForRedundantImports.java"), expected);
    }

    @Test
    public void testMultiplePatternMatches() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(ImportOrderCheck.class);
        checkConfig.addAttribute("groups", "/java/,/rga/,/myO/,/org/,/organ./");
        final String[] expected = {};

        verify(checkConfig, new File("src/test/resources-noncompilable/com/puppycrawl/tools/"
                + "checkstyle/imports/"
                + "InputImportOrder_MultiplePatternMatches.java").getCanonicalPath(), expected);
    }

    @Test
    public void testVisitTokenSwitchReflection() throws Exception {
        DynamicEnumExtender.addEnum(ImportOrderOption.class, "NEW_OPTION_FOR_UT");
        ImportOrderCheck check = new ImportOrderCheck ();
        check.setOption("NEW_OPTION_FOR_UT");
        
        DetailAST astImport = mockAST(TokenTypes.IMPORT, "import", "mockfile", 0, 0);
        DetailAST astIdent = mockAST(TokenTypes.IDENT, "myTestImport", "mockfile", 0, 0);
        astImport.addChild(astIdent);
        DetailAST astSemi = mockAST(TokenTypes.SEMI, ";", "mockfile", 0, 0);
        astIdent.addNextSibling(astSemi);

        check.visitToken(astImport);
    }

    /**
     * Creates MOCK lexical token and returns AST node for this token
     * @param tokenType type of token
     * @param tokenText text of token
     * @param tokenFileName file name of token
     * @param tokenRow token position in a file (row)
     * @param tokenColumn token position in a file (column)
     * @return AST node for the token
     */
    private static DetailAST mockAST(final int tokenType, final String tokenText,
            final String tokenFileName, final int tokenRow, final int tokenColumn)
    {
        CommonHiddenStreamToken tokenImportSemi = new CommonHiddenStreamToken();
        tokenImportSemi.setType(tokenType);
        tokenImportSemi.setText(tokenText);
        tokenImportSemi.setLine(tokenRow);
        tokenImportSemi.setColumn(tokenColumn);
        tokenImportSemi.setFilename(tokenFileName);
        DetailAST astSemi = new DetailAST();
        astSemi.initialize(tokenImportSemi);
        return astSemi;
    }

}

class DynamicEnumExtender {

    private static ReflectionFactory reflectionFactory =
        ReflectionFactory.getReflectionFactory();

    private static void setFailsafeFieldValue(Field field, Object target, Object value)
        throws NoSuchFieldException, IllegalAccessException
    {
        // let's make the field accessible
        field.setAccessible(true);

        // next we change the modifier in the Field instance to
        // not be final anymore, thus tricking reflection into
        // letting us modify the static final field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        int modifiers = modifiersField.getInt(field);

        // blank out the final bit in the modifiers int
        modifiers &= ~Modifier.FINAL;
        modifiersField.setInt(field, modifiers);

        FieldAccessor fa = reflectionFactory.newFieldAccessor(field, false);
        fa.set(target, value);
    }

    private static void blankField(Class<?> enumClass, String fieldName)
        throws NoSuchFieldException, IllegalAccessException
    {
        for (Field field : Class.class.getDeclaredFields()) {
            if (field.getName().contains(fieldName)) {
                AccessibleObject.setAccessible(new Field[] { field }, true);
                setFailsafeFieldValue(field, enumClass, null);
                break;
            }
        }
    }

    private static void cleanEnumCache(Class<?> enumClass)
        throws NoSuchFieldException, IllegalAccessException
    {
        blankField(enumClass, "enumConstantDirectory"); // Sun (Oracle?!?) JDK 1.5/6
        blankField(enumClass, "enumConstants"); // IBM JDK
    }

    private static ConstructorAccessor getConstructorAccessor(Class<?> enumClass, Class<?>[] additionalParameterTypes)
        throws NoSuchMethodException
    {
        Class<?>[] parameterTypes = new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = int.class;
        System.arraycopy(additionalParameterTypes, 0, parameterTypes, 2, additionalParameterTypes.length);
        return reflectionFactory.newConstructorAccessor(enumClass .getDeclaredConstructor(parameterTypes));
    }

    private static Object makeEnum(Class<?> enumClass, String value, int ordinal, Class<?>[] additionalTypes, Object[] additionalValues)
        throws Exception
    {
        Object[] parms = new Object[additionalValues.length + 2];
        parms[0] = value;
        parms[1] = Integer.valueOf(ordinal);
        System.arraycopy(additionalValues, 0, parms, 2, additionalValues.length);
        return enumClass.cast(getConstructorAccessor(enumClass, additionalTypes).newInstance(parms));
    }

    /**
     * Add an enum instance to the enum class given as argument
     *
     * @param <T> the type of the enum (implicit)
     * @param enumType the class of the enum to be modified
     * @param enumName the name of the new enum instance to be added to the class
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> void addEnum(Class<T> enumType, String enumName) {
        // 0. Sanity checks
        if (!Enum.class.isAssignableFrom(enumType))
            throw new RuntimeException("class " + enumType + " is not an instance of Enum");

        // 1. Lookup "$VALUES" holder in enum class and get previous enum
        // instances
        Field valuesField = null;
        Field[] fields = enumType.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().contains("$VALUES")) {
                valuesField = field;
                break;
            }
        }
        AccessibleObject.setAccessible(new Field[] { valuesField }, true);

        try {

            // 2. Copy it
            T[] previousValues = (T[]) valuesField.get(enumType);
            List<T> values = new ArrayList<T>(Arrays.asList(previousValues));

            // 3. build new enum
            T newValue = (T) makeEnum(
                enumType,                         // The target enum class
                enumName,                         // THE NEW ENUM INSTANCE TO BE DYNAMICALLY ADDED
                values.size(), new Class<?>[] {}, // could be used to pass values to the enum constuctor if needed
                new Object[] {}                   // could be used to pass values to the enum constuctor if needed
            );

            // 4. add new value
            values.add(newValue);

            // 5. Set new values field
            setFailsafeFieldValue(valuesField, null, values.toArray((T[]) Array.newInstance(enumType, 0)));

            // 6. Clean enum cache
            cleanEnumCache(enumType);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}