/*
 * Copyright 2015 Bay of Many
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * This project is derived from code in the Tinkerpop project under the following license:
 *
 *    Tinkerpop3
 *    http://www.apache.org/licenses/LICENSE-2.0
 */

package peapod.property;

import peapod.annotations.Property;
import peapod.annotations.Vertex;

@Vertex
public abstract class Person {

    public abstract String getS();

    public abstract void setS(String s);

    @Property(hidden = true)
    public abstract String getHidden();

    @Property(hidden = true)
    public abstract void setHidden(String s);

    public abstract Boolean getB1();

    public abstract void setB1(Boolean b1);

    public abstract boolean getB2();

    public abstract void setB2(boolean b2);

    public abstract Short getS1();

    public abstract void setS1(Short s1);

    public abstract short getS2();

    public abstract void setS2(short i2);

    public abstract Integer getI1();

    public abstract void setI1(Integer i1);

    public abstract int getI2();

    public abstract void setI2(int i2);

    public abstract Long getL1();

    public abstract void setL1(Long l1);

    public abstract long getL2();

    public abstract void setL2(long l2);

    public abstract Float getF1();

    public abstract void setF1(Float f1);

    public abstract float getF2();

    public abstract void setF2(float f2);

    public abstract Double getD1();

    public abstract void setD1(Double d1);

    public abstract double getD2();

    public abstract void setD2(double d2);

    public abstract Character getC1();

    public abstract void setC1(Character d1);

    public abstract char getC2();

    public abstract void setC2(char c2);

    public abstract Byte getBy1();

    public abstract void setBy1(Byte by1);

    public abstract byte getBy2();

    public abstract void setBy2(byte by2);


}
