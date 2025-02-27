/**
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.core.tests.compiler;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.Test;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@SuppressWarnings("all")
public class CompilerBug441096Test extends AbstractXtendCompilerTest {
  @Test
  public void test_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.xtend.lib.annotations.Accessors");
    _builder.newLine();
    _builder.append("class C {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("@Accessors(PUBLIC_GETTER) String string");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def dispatch void m(Void expr, String seq) {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def dispatch void m(Void expr, StringBuilder seq) {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def void m(I i) {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("val x = new I {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("override m() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("interface I {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("def void m();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.Arrays;");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtend.lib.annotations.AccessorType;");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtend.lib.annotations.Accessors;");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.xbase.lib.Pure;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@SuppressWarnings(\"all\")");
    _builder_1.newLine();
    _builder_1.append("public class C {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("public interface I {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("void m();");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("@Accessors(AccessorType.PUBLIC_GETTER)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("private String string;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("protected void _m(final Void expr, final String seq) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("protected void _m(final Void expr, final StringBuilder seq) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("public void m(final C.I i) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("private final C.I x = new C.I() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("@Override");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("public void m() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("};");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("public void m(final Object expr, final Object seq) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("if (expr == null");
    _builder_1.newLine();
    _builder_1.append("         ");
    _builder_1.append("&& seq instanceof StringBuilder) {");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("_m((Void)null, (StringBuilder)seq);");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("return;");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("} else if (expr == null");
    _builder_1.newLine();
    _builder_1.append("         ");
    _builder_1.append("&& seq instanceof String) {");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("_m((Void)null, (String)seq);");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("return;");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("} else {");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("throw new IllegalArgumentException(\"Unhandled parameter types: \" +");
    _builder_1.newLine();
    _builder_1.append("        ");
    _builder_1.append("Arrays.<Object>asList(expr, seq).toString());");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("@Pure");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("public String getString() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return this.string;");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertCompilesTo(_builder, _builder_1);
  }
}
