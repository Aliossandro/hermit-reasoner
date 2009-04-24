// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.rdftext;

import java.util.Collection;

public class RDFTextLengthInterval {
    public static final int CHARACTER_COUNT=1112033;
    public static enum LanguageTagMode { PRESENT,ABSENT };
    
    protected final LanguageTagMode m_languageTagMode;
    protected final int m_minLength;
    protected final int m_maxLength;

    public RDFTextLengthInterval(LanguageTagMode languageTagMode,int minLength,int maxLength) {
        assert !isIntervalEmpty(languageTagMode,minLength,maxLength);
        m_languageTagMode=languageTagMode;
        m_minLength=minLength;
        m_maxLength=maxLength;
    }
    /**
     * Computes the intersection of this interval with the supplied one. If the two intervals do not intersect, the result is null.
     */
    public RDFTextLengthInterval intersectWith(RDFTextLengthInterval that) {
        if (m_languageTagMode!=that.m_languageTagMode)
            return null;
        int newMinLength=Math.max(m_minLength,that.m_minLength);
        int newMaxLength=Math.min(m_maxLength,that.m_maxLength);
        if (isIntervalEmpty(m_languageTagMode,newMinLength,newMaxLength))
            return null;
        else if (isEqual(m_languageTagMode,newMinLength,newMaxLength))
            return this;
        else if (that.isEqual(m_languageTagMode,newMinLength,newMaxLength))
            return that;
        else
            return new RDFTextLengthInterval(m_languageTagMode,newMinLength,newMaxLength);
    }
    protected boolean isEqual(LanguageTagMode languageTagMode,int minLength,int maxLength) {
        return m_languageTagMode==languageTagMode && m_minLength==minLength && m_maxLength==maxLength;
    }
    public int subtractSizeFrom(int argument) {
        if (argument<=0 || m_maxLength==Integer.MAX_VALUE || m_languageTagMode==LanguageTagMode.ABSENT)
            return 0;
        // If m_minLength or m_maxLength is more than 4, then the number of
        // values exceeds the range of long. 
        if (m_minLength>=4 || m_maxLength>=4)
            return 0;
        // We now compute the actual number of values.
        long size=getNumberOfValuesOfLength(m_maxLength)-getNumberOfValuesOfLength(m_minLength-1);
        return (int)Math.max(argument-size,0L);
    }
    protected long getNumberOfValuesOfLength(int length) {
        if (length<0)
            return 0L;
        else {
            long valuesOfLength=1L;
            long total=1L;
            for (int i=1;i<=length;i++) {
                valuesOfLength*=(long)CHARACTER_COUNT;
                total+=valuesOfLength;
            }
            return total;
        }
    }
    public boolean contains(String value) {
        return m_languageTagMode==LanguageTagMode.ABSENT && m_minLength<=value.length() && value.length()<=m_maxLength;
    }
    public boolean contains(RDFTextDataValue value) {
        return m_languageTagMode==LanguageTagMode.PRESENT && m_minLength<=value.getString().length() && value.getString().length()<=m_maxLength;
    }
    public void enumerateValues(Collection<Object> values) {
        if (m_maxLength==Integer.MAX_VALUE || m_languageTagMode==LanguageTagMode.PRESENT)
            throw new IllegalStateException("Internal error: the data range is infinite!");
        if (m_minLength==0)
            values.add("");
        char[] temp=new char[m_maxLength];
        processPosition(temp,values,0);
    }
    protected void processPosition(char[] temp,Collection<Object> values,int position) {
        if (position<m_maxLength) {
            for (int c=0;c<=0xFFFF;c++)
                if (RDFTextDatatypeHandler.isRDFTextCharacter((char)c)) {
                    temp[position]=(char)c;
                    if (m_minLength<=position+1)
                        values.add(new String(temp,0,position+1));
                    processPosition(temp,values,position+1);
                }
        }
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append('[');
        buffer.append(m_minLength);
        buffer.append("..");
        if (m_maxLength==Integer.MAX_VALUE)
            buffer.append("+INF");
        else
            buffer.append(m_maxLength);
        buffer.append(']');
        return buffer.toString();
    }
    protected static boolean isIntervalEmpty(LanguageTagMode languageTagMode,int minLength,int maxLength) {
        return minLength>maxLength;
    }
}
