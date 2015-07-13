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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import sun.reflect.*;
import java.lang.reflect.*;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

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

}

// http://www.javaspecialists.eu/archive/Issue161.html
class EnumBuster<E extends Enum<E>> {
    private static final Class[] EMPTY_CLASS_ARRAY =
        new Class[0];
    private static final Object[] EMPTY_OBJECT_ARRAY =
        new Object[0];

    private static final String VALUES_FIELD = "$VALUES";
    private static final String ORDINAL_FIELD = "ordinal";

    private final ReflectionFactory reflection =
        ReflectionFactory.getReflectionFactory();

    private final Class<E> clazz;

    private final Collection<Field> switchFields;

    private final Deque<Memento> undoStack =
        new LinkedList<Memento>();

    /**
     * Construct an EnumBuster for the given enum class and keep
     * the switch statements of the classes specified in
     * switchUsers in sync with the enum values.
     */
    public EnumBuster(Class<E> clazz, Class... switchUsers) {
      try {
        this.clazz = clazz;
        switchFields = findRelatedSwitchFields(switchUsers);
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Could not create the class", e);
      }
    }

    /**
     * Make a new enum instance, without adding it to the values
     * array and using the default ordinal of 0.
     */
    public E make(String value) {
      return make(value, 0,
          EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Make a new enum instance with the given ordinal.
     */
    public E make(String value, int ordinal) {
      return make(value, ordinal,
          EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Make a new enum instance with the given value, ordinal and
     * additional parameters.  The additionalTypes is used to match
     * the constructor accurately.
     */
    public E make(String value, int ordinal,
                  Class[] additionalTypes, Object[] additional) {
      try {
        undoStack.push(new Memento());
        ConstructorAccessor ca = findConstructorAccessor(
            additionalTypes, clazz);
        return constructEnum(clazz, ca, value,
            ordinal, additional);
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Could not create enum", e);
      }
    }

    /**
     * This method adds the given enum into the array
     * inside the enum class.  If the enum already
     * contains that particular value, then the value
     * is overwritten with our enum.  Otherwise it is
     * added at the end of the array.
     *
     * In addition, if there is a constant field in the
     * enum class pointing to an enum with our value,
     * then we replace that with our enum instance.
     *
     * The ordinal is either set to the existing position
     * or to the last value.
     *
     * Warning: This should probably never be called,
     * since it can cause permanent changes to the enum
     * values.  Use only in extreme conditions.
     *
     * @param e the enum to add
     */
    public void addByValue(E e) {
      try {
        undoStack.push(new Memento());
        Field valuesField = findValuesField();

        // we get the current Enum[]
        E[] values = values();
        for (int i = 0; i < values.length; i++) {
          E value = values[i];
          if (value.name().equals(e.name())) {
            setOrdinal(e, value.ordinal());
            values[i] = e;
            replaceConstant(e);
            return;
          }
        }

        // we did not find it in the existing array, thus
        // append it to the array
        E[] newValues =
            Arrays.copyOf(values, values.length + 1);
        newValues[newValues.length - 1] = e;
        ReflectionHelper.setStaticFinalField(
            valuesField, newValues);

        int ordinal = newValues.length - 1;
        setOrdinal(e, ordinal);
        addSwitchCase();
      } catch (Exception ex) {
        throw new IllegalArgumentException(
            "Could not set the enum", ex);
      }
    }

    /**
     * We delete the enum from the values array and set the
     * constant pointer to null.
     *
     * @param e the enum to delete from the type.
     * @return true if the enum was found and deleted;
     *         false otherwise
     */
    public boolean deleteByValue(E e) {
      if (e == null) throw new NullPointerException();
      try {
        undoStack.push(new Memento());
        // we get the current E[]
        E[] values = values();
        for (int i = 0; i < values.length; i++) {
          E value = values[i];
          if (value.name().equals(e.name())) {
            E[] newValues =
                Arrays.copyOf(values, values.length - 1);
            System.arraycopy(values, i + 1, newValues, i,
                values.length - i - 1);
            for (int j = i; j < newValues.length; j++) {
              setOrdinal(newValues[j], j);
            }
            Field valuesField = findValuesField();
            ReflectionHelper.setStaticFinalField(
                valuesField, newValues);
            removeSwitchCase(i);
            blankOutConstant(e);
            return true;
          }
        }
      } catch (Exception ex) {
        throw new IllegalArgumentException(
            "Could not set the enum", ex);
      }
      return false;
    }

    /**
     * Undo the state right back to the beginning when the
     * EnumBuster was created.
     */
    public void restore() {
      while (undo()) {
        //
      }
    }

    /**
     * Undo the previous operation.
     */
    public boolean undo() {
      try {
        Memento memento = undoStack.poll();
        if (memento == null) return false;
        memento.undo();
        return true;
      } catch (Exception e) {
        throw new IllegalStateException("Could not undo", e);
      }
    }

    private ConstructorAccessor findConstructorAccessor(
        Class[] additionalParameterTypes,
        Class<E> clazz) throws NoSuchMethodException {
      Class[] parameterTypes =
          new Class[additionalParameterTypes.length + 2];
      parameterTypes[0] = String.class;
      parameterTypes[1] = int.class;
      System.arraycopy(
          additionalParameterTypes, 0,
          parameterTypes, 2,
          additionalParameterTypes.length);
      Constructor<E> cstr = clazz.getDeclaredConstructor(
          parameterTypes
      );
      return reflection.newConstructorAccessor(cstr);
    }

    private E constructEnum(Class<E> clazz,
                            ConstructorAccessor ca,
                            String value, int ordinal,
                            Object[] additional)
        throws Exception {
      Object[] parms = new Object[additional.length + 2];
      parms[0] = value;
      parms[1] = ordinal;
      System.arraycopy(
          additional, 0, parms, 2, additional.length);
      return clazz.cast(ca.newInstance(parms));
    }

    /**
     * The only time we ever add a new enum is at the end.
     * Thus all we need to do is expand the switch map arrays
     * by one empty slot.
     */
    private void addSwitchCase() {
      try {
        for (Field switchField : switchFields) {
          int[] switches = (int[]) switchField.get(null);
          switches = Arrays.copyOf(switches, switches.length + 1);
          ReflectionHelper.setStaticFinalField(
              switchField, switches
          );
        }
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Could not fix switch", e);
      }
    }

    private void replaceConstant(E e)
        throws IllegalAccessException, NoSuchFieldException {
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        if (field.getName().equals(e.name())) {
          ReflectionHelper.setStaticFinalField(
              field, e
          );
        }
      }
    }


    private void blankOutConstant(E e)
        throws IllegalAccessException, NoSuchFieldException {
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        if (field.getName().equals(e.name())) {
          ReflectionHelper.setStaticFinalField(
              field, null
          );
        }
      }
    }

    private void setOrdinal(E e, int ordinal)
        throws NoSuchFieldException, IllegalAccessException {
      Field ordinalField = Enum.class.getDeclaredField(
          ORDINAL_FIELD);
      ordinalField.setAccessible(true);
      ordinalField.set(e, ordinal);
    }

    /**
     * Method to find the values field, set it to be accessible,
     * and return it.
     *
     * @return the values array field for the enum.
     * @throws NoSuchFieldException if the field could not be found
     */
    private Field findValuesField()
        throws NoSuchFieldException {
      // first we find the static final array that holds
      // the values in the enum class
      Field valuesField = clazz.getDeclaredField(
          VALUES_FIELD);
      // we mark it to be public
      valuesField.setAccessible(true);
      return valuesField;
    }

    private Collection<Field> findRelatedSwitchFields(
        Class[] switchUsers) {
      Collection<Field> result = new ArrayList<Field>();
      try {
        for (Class switchUser : switchUsers) {
          Class[] clazzes = switchUser.getDeclaredClasses();
          for (Class suspect : clazzes) {
            Field[] fields = suspect.getDeclaredFields();
            for (Field field : fields) {
              if (field.getName().startsWith("$SwitchMap$" +
                  clazz.getSimpleName())) {
                field.setAccessible(true);
                result.add(field);
              }
            }
          }
        }
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Could not fix switch", e);
      }
      return  result;
    }

    private void removeSwitchCase(int ordinal) {
      try {
        for (Field switchField : switchFields) {
          int[] switches = (int[]) switchField.get(null);
          int[] newSwitches = Arrays.copyOf(
              switches, switches.length - 1);
          System.arraycopy(switches, ordinal + 1, newSwitches,
              ordinal, switches.length - ordinal - 1);
          ReflectionHelper.setStaticFinalField(
              switchField, newSwitches
          );
        }
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Could not fix switch", e);
      }
    }

    @SuppressWarnings("unchecked")
    private E[] values()
        throws NoSuchFieldException, IllegalAccessException {
      Field valuesField = findValuesField();
      return (E[]) valuesField.get(null);
    }

    private class Memento {
      private final E[] values;
      private final Map<Field, int[]> savedSwitchFieldValues =
          new HashMap<Field, int[]>();

      private Memento() throws IllegalAccessException {
        try {
          values = values().clone();
          for (Field switchField : switchFields) {
            int[] switchArray = (int[]) switchField.get(null);
            savedSwitchFieldValues.put(switchField,
                switchArray.clone());
          }
        } catch (Exception e) {
          throw new IllegalArgumentException(
              "Could not create the class", e);
        }
      }

      private void undo() throws
          NoSuchFieldException, IllegalAccessException {
        Field valuesField = findValuesField();
        ReflectionHelper.setStaticFinalField(valuesField, values);

        for (int i = 0; i < values.length; i++) {
          setOrdinal(values[i], i);
        }

        // reset all of the constants defined inside the enum
        Map<String, E> valuesMap =
            new HashMap<String, E>();
        for (E e : values) {
          valuesMap.put(e.name(), e);
        }
        Field[] constantEnumFields = clazz.getDeclaredFields();
        for (Field constantEnumField : constantEnumFields) {
          E en = valuesMap.get(constantEnumField.getName());
          if (en != null) {
            ReflectionHelper.setStaticFinalField(
                constantEnumField, en
            );
          }
        }

        for (Map.Entry<Field, int[]> entry :
            savedSwitchFieldValues.entrySet()) {
          Field field = entry.getKey();
          int[] mappings = entry.getValue();
          ReflectionHelper.setStaticFinalField(field, mappings);
        }
      }
    }
  }