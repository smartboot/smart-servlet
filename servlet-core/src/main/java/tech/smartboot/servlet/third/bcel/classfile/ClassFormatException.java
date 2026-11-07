/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package tech.smartboot.servlet.third.bcel.classfile;

/**
 * Thrown when the BCEL attempts to read a class file and determines
 * that the file is malformed or otherwise cannot be interpreted as a
 * class file.
 */
public class ClassFormatException extends RuntimeException {

    private static final long serialVersionUID = 3243149520175287759L;

    public ClassFormatException() {
        super();
    }


    public ClassFormatException(final String s) {
        super(s);
    }
}
