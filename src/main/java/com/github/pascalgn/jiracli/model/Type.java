/**
 * Copyright 2016 Pascal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pascalgn.jiracli.model;

public abstract class Type {
    Type() {
        // only allow subclasses in this package
    }

    public abstract <T> T accept(TypeVisitor<T> visitor);

    @SuppressWarnings("unchecked")
    <T extends Type> Data<T> accept(DataConverter dataConverter) {
        return (Data<T>) accept((TypeVisitor<?>) dataConverter);
    }
}
