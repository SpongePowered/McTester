/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mctester.internal;

import org.apache.commons.lang3.ClassUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class Utils {

    public static DataContainer arbitraryToData(Object object) {
        if (object instanceof DataSerializable) {
            return ((DataSerializable) object).toContainer();
        }
        DataTranslator<Object> translator = (DataTranslator<Object>) Sponge.getDataManager().getTranslator(((Object) object).getClass()).get();
        return translator.translate(object);
    }

    public static Optional<Object> dataToArbitrary(DataView view, Class<?> type) {
        Optional<DataBuilder<?>> builder = (Optional) Sponge.getDataManager().getBuilder((Class<DataSerializable>) type);
        Optional<DataTranslator<?>> translator = (Optional) Sponge.getDataManager().getTranslator(type);
        if (builder.isPresent()) {
            return Optional.of(builder.get().build(view).get());
        } else if (translator.isPresent()) {
            return Optional.of(translator.get().translate(view));
        }
        return Optional.empty();
    }

    public static Class<?> classForName(String name) throws InvalidDataException {
        try {
            return ClassUtils.getClass(name); // Handles primitives
        } catch (ClassNotFoundException e) {
            throw new InvalidDataException(e);
        }
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    public static File getTimestampedPNGFileForDirectory(String baseName, File gameDirectory)
    {
        String s = baseName + DATE_FORMAT.format(new Date()).toString();
        int i = 1;

        while (true)
        {
            File file1 = new File(gameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");

            if (!file1.exists())
            {
                return file1;
            }

            ++i;
        }
    }
}
