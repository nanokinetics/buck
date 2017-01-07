/*
 * Copyright 2016-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.jvm.java.abi.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.facebook.buck.testutil.CompilerTreeApiParameterized;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

@RunWith(CompilerTreeApiParameterized.class)
public class TreeBackedTypesTest extends CompilerTreeApiParameterizedTest {
  @Test
  public void testGetDeclaredTypeTopLevelNoGenerics() throws IOException {
    compile("class Foo { }");

    TypeElement fooElement = elements.getTypeElement("Foo");
    TypeMirror fooTypeMirror = types.getDeclaredType(fooElement);

    assertEquals(TypeKind.DECLARED, fooTypeMirror.getKind());
    DeclaredType fooDeclaredType = (DeclaredType) fooTypeMirror;
    assertNotSame(fooElement.asType(), fooDeclaredType);
    assertSame(fooElement, fooDeclaredType.asElement());
    assertEquals(0, fooDeclaredType.getTypeArguments().size());

    TypeMirror enclosingType = fooDeclaredType.getEnclosingType();
    assertEquals(TypeKind.NONE, enclosingType.getKind());
    assertTrue(enclosingType instanceof NoType);
  }

  @Test
  public void testIsSameTypeTopLevelNoGenerics() throws IOException {
    compile("class Foo { }");

    TypeElement fooElement = elements.getTypeElement("Foo");
    TypeMirror fooTypeMirror = types.getDeclaredType(fooElement);
    TypeMirror fooTypeMirror2 = types.getDeclaredType(fooElement);

    assertSameType(fooTypeMirror, fooTypeMirror2);
  }

  @Test
  public void testIsNotSameTypeStaticNoGenerics() throws IOException {
    compile(ImmutableMap.of(
        "Foo.java",
        "class Foo { }",
        "Bar.java",
        "class Bar { }"));

    TypeMirror fooType = elements.getTypeElement("Foo").asType();
    TypeMirror barType = elements.getTypeElement("Bar").asType();

    assertNotSameType(fooType, barType);
  }

  @Test
  public void testIsSameTypeJavacTypeTopLevelNoGenerics() throws IOException {
    initCompiler();

    TypeElement objectElement = elements.getTypeElement("java.lang.Object");
    TypeMirror objectTypeMirror = types.getDeclaredType(objectElement);
    TypeMirror objectTypeMirror2 = types.getDeclaredType(objectElement);

    assertSameType(objectTypeMirror, objectTypeMirror2);
  }

  @Test
  public void testIsNotSameTypeJavacTypeTopLevelNoGenerics() throws IOException {
    initCompiler();

    TypeMirror objectType = elements.getTypeElement("java.lang.Object").asType();
    TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();

    assertNotSameType(objectType, stringType);
  }

  @Test
  public void testIsSameTypeParameterizedType() throws IOException {
    initCompiler();

    TypeElement listElement = elements.getTypeElement("java.util.List");
    TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
    TypeMirror listStringType1 = types.getDeclaredType(listElement, stringType);
    TypeMirror listStringType2 = types.getDeclaredType(listElement, stringType);

    assertNotSame(listStringType1, listStringType2);
    assertSameType(listStringType1, listStringType2);
  }

  @Test
  public void testIsSameTypeMultiParameterizedType() throws IOException {
    initCompiler();

    TypeElement mapElement = elements.getTypeElement("java.util.Map");
    TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
    TypeMirror integerType = elements.getTypeElement("java.lang.Integer").asType();
    TypeMirror mapStringIntType1 = types.getDeclaredType(mapElement, stringType, integerType);
    TypeMirror mapStringIntType2 = types.getDeclaredType(mapElement, stringType, integerType);

    assertNotSame(mapStringIntType1, mapStringIntType2);
    assertSameType(mapStringIntType1, mapStringIntType2);
  }

  @Test
  public void testIsNotSameTypeParameterizedType() throws IOException {
    initCompiler();

    TypeElement listElement = elements.getTypeElement("java.util.List");
    TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
    TypeMirror integerType = elements.getTypeElement("java.lang.Integer").asType();
    TypeMirror listStringType = types.getDeclaredType(listElement, stringType);
    TypeMirror listIntType = types.getDeclaredType(listElement, integerType);

    assertNotSameType(listStringType, listIntType);
  }

  @Test
  public void testIsNotSameTypeMultiParameterizedType() throws IOException {
    initCompiler();

    TypeElement mapElement = elements.getTypeElement("java.util.Map");
    TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
    TypeMirror integerType = elements.getTypeElement("java.lang.Integer").asType();
    TypeMirror mapStringIntType = types.getDeclaredType(mapElement, stringType, integerType);
    TypeMirror mapStringStringType = types.getDeclaredType(mapElement, stringType, stringType);

    assertNotSameType(mapStringIntType, mapStringStringType);
  }

  @Test
  public void testIsSameTypeArrayType() throws IOException {
    compile("class Foo { }");

    TypeElement fooElement = elements.getTypeElement("Foo");
    ArrayType fooArray = types.getArrayType(fooElement.asType());
    ArrayType fooArray2 = types.getArrayType(fooElement.asType());

    assertSameType(fooArray, fooArray2);
    assertNotSameType(fooArray, fooElement.asType());
  }

  @Test
  public void testIsNotSameTypeArrayType() throws IOException {
    compile(Joiner.on('\n').join(
        "class Foo { }",
        "class Bar { }"));

    TypeMirror fooType = elements.getTypeElement("Foo").asType();
    TypeMirror barType = elements.getTypeElement("Bar").asType();
    ArrayType fooArray = types.getArrayType(fooType);
    ArrayType barArray = types.getArrayType(barType);

    assertNotSameType(fooArray, barArray);
  }

  @Test
  public void testIsSameTypePrimitiveType() throws IOException {
    initCompiler();

    for (TypeKind typeKind : TypeKind.values()) {
      if (typeKind.isPrimitive()) {
        PrimitiveType primitiveType = types.getPrimitiveType(typeKind);
        PrimitiveType primitiveType2 = types.getPrimitiveType(typeKind);

        assertSameType(primitiveType, primitiveType2);
      }
    }
  }

  @Test
  public void testIsNotSameTypePrimitiveType() throws IOException {
    initCompiler();

    PrimitiveType intType = types.getPrimitiveType(TypeKind.INT);
    PrimitiveType longType = types.getPrimitiveType(TypeKind.LONG);

    assertNotSameType(intType, longType);
  }

  @Test
  public void testIsNotSameTypeDifferentTypes() throws IOException {
    initCompiler();

    PrimitiveType intType = types.getPrimitiveType(TypeKind.INT);
    ArrayType intArrayType = types.getArrayType(intType);

    assertNotSameType(intType, intArrayType);
  }

  @Test
  public void testIsSameTypeNullType() throws IOException {
    initCompiler();

    assertSameType(types.getNullType(), types.getNullType());
  }
}
