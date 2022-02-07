/*
MIT License

Copyright (c) 2021 apace100

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package io.github.merchantpug.apugli.util;

import io.github.merchantpug.apugli.Apugli;
import net.minecraft.util.Identifier;
import java.util.HashSet;

public final class ApugliNamespaceAlias {

    private static final HashSet<String> aliasedNamespaces = new HashSet<>();

    public static void addAlias(String namespace) {
        aliasedNamespaces.add(namespace);
    }

    public static boolean isAlias(String namespace) {
        return aliasedNamespaces.contains(namespace);
    }

    public static boolean isAlias(Identifier identifier) {
        return isAlias(identifier.getNamespace());
    }

    public static Identifier resolveAlias(Identifier original) {
        return new Identifier(Apugli.MODID, original.getPath());
    }
}