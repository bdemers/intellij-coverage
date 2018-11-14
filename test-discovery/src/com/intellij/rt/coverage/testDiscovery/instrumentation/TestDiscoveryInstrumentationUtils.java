/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

package com.intellij.rt.coverage.testDiscovery.instrumentation;

import com.intellij.rt.coverage.data.TestDiscoveryProjectData;
import org.jetbrains.coverage.org.objectweb.asm.Type;

final class TestDiscoveryInstrumentationUtils {
  public static final String SEPARATOR = "/";

  private TestDiscoveryInstrumentationUtils() {
  }

  /**
   * @param name      method name
   * @param signature description of method arguments and return type
   * @return generated method id
   */
  static String[] getMethodId(String name, String signature) {
    if (TestDiscoveryProjectData.getProtocolVersion() < 4) {
      return new String[]{name + SEPARATOR + signature};
    }
    Type methodType = Type.getMethodType(signature);
    Type[] argumentTypes = methodType.getArgumentTypes();
    String[] result = new String[argumentTypes.length + 2];
    result[0] = name;
    result[1] = methodType.getReturnType().getInternalName();
    for (int i = 0; i < argumentTypes.length; i++) {
      result[i + 2] = argumentTypes[i].getInternalName();
    }
    return result;
  }
}
