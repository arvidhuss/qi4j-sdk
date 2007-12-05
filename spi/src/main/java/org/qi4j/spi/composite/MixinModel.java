/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.spi.composite;

/**
 * A mixin is an implementation of a particular interface,
 * and is used as a fragment in a composite.
 */
public final class MixinModel
    extends FragmentModel
{
    private Iterable<ConstraintModel> constraintModels;
    private Iterable<ConcernModel> concernModels;
    private Iterable<SideEffectModel> sideEffectModels;

    public MixinModel( Class fragmentClass, Iterable<ConstructorModel> constructors, Iterable<FieldModel> fieldDependencies, Iterable<MethodModel> methodDependencies, Class[] appliesTo, Iterable<ConstraintModel> constraintModels, Iterable<ConcernModel> concernModels, Iterable<SideEffectModel> sideEffectModels )
    {
        super( fragmentClass, constructors, fieldDependencies, methodDependencies, appliesTo );
        this.constraintModels = constraintModels;
        this.sideEffectModels = sideEffectModels;
        this.concernModels = concernModels;
    }

    public Iterable<ConstraintModel> getConstraintModels()
    {
        return constraintModels;
    }

    public Iterable<ConcernModel> getConcernModels()
    {
        return concernModels;
    }

    public Iterable<SideEffectModel> getSideEffectModels()
    {
        return sideEffectModels;
    }
}
