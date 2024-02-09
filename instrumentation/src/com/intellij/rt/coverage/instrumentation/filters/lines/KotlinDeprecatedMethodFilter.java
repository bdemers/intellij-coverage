/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.rt.coverage.instrumentation.filters.lines;

import com.intellij.rt.coverage.instrumentation.data.InstrumentationData;
import com.intellij.rt.coverage.instrumentation.data.Key;
import com.intellij.rt.coverage.instrumentation.filters.KotlinUtils;
import com.intellij.rt.coverage.instrumentation.filters.branches.KotlinDefaultArgsBranchFilter;
import org.jetbrains.coverage.org.objectweb.asm.AnnotationVisitor;
import org.jetbrains.coverage.org.objectweb.asm.MethodVisitor;
import org.jetbrains.coverage.org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

/**
 * Filter methods marked with deprecated annotation.
 */
public class KotlinDeprecatedMethodFilter extends CoverageFilter {
  private boolean myShouldIgnore;

  @Override
  public boolean isApplicable(InstrumentationData context) {
    return KotlinUtils.isKotlinClass(context);
  }

  @Override
  public void initFilter(MethodVisitor methodVisitor, InstrumentationData context) {
    super.initFilter(methodVisitor, context);
    String name = context.getMethodName();
    if (name.endsWith(KotlinDefaultArgsBranchFilter.DEFAULT_ARGS_SUFFIX)) {
      Set<String> deprecatedMethods = myContext.get(Key.DEPRECATED_METHODS);
      if (deprecatedMethods != null) {
        final String originalName = name.substring(0, name.length() - KotlinDefaultArgsBranchFilter.DEFAULT_ARGS_SUFFIX.length());
        if (deprecatedMethods.contains(originalName)) {
          myContext.setIgnoreSection(true);
          myShouldIgnore = true;
        }
      }
    }
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    final AnnotationVisitor av = super.visitAnnotation(descriptor, visible);
    if (!"Lkotlin/Deprecated;".equals(descriptor)) return av;
    return new AnnotationVisitor(Opcodes.API_VERSION, av) {
      @Override
      public void visitEnum(String name, String descriptor, String value) {
        super.visitEnum(name, descriptor, value);
        if (!"Lkotlin/DeprecationLevel;".equals(descriptor)) return;
        if ("ERROR".equals(value) || "HIDDEN".equals(value)) {
          if (!myShouldIgnore) {
            myContext.setIgnoreSection(true);
            myShouldIgnore = true;
          }
          Set<String> deprecatedMethods = myContext.get(Key.DEPRECATED_METHODS);
          if (deprecatedMethods == null) {
            deprecatedMethods = new HashSet<String>();
            myContext.put(Key.DEPRECATED_METHODS, deprecatedMethods);
          }
          deprecatedMethods.add(myContext.getMethodName());
        }
      }
    };
  }

  @Override
  public void visitCode() {
    super.visitCode();
    if (myShouldIgnore) {
      myContext.getProjectContext().getIgnoredStorage().addIgnoredMethod(myContext.get(Key.CLASS_NAME), myContext.getMethodName(), myContext.getMethodDesc());
    }
  }

  @Override
  public void visitEnd() {
    super.visitEnd();
    if (myShouldIgnore) {
      myContext.setIgnoreSection(false);
    }
  }
}
