// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for abbreviating URIs. The resulting URIs can be either<br>
 * 1) &lt;uri&gt; or<br> 
 * 2) prefix-name:local-name where prefix-name can be empty.<br>
 * Forms 1 and 2 are dependent upon a set of prefix declarations that associates prefix names with prefix IRIs.
 * A URI abbreviated using form 2 that uses an unregistered prefix is invalid---expanding it will result in an exception.
 * Neither prefixes nor local names may contain colon characters.
 */
public class Prefixes implements Serializable {
    private static final long serialVersionUID=-158185482289831766L;
    
    protected static final Pattern s_localNameChecker=Pattern.compile("([a-zA-Z_])+");
    public static final Map<String,String> s_semanticWebPrefixes;
    static {
        s_semanticWebPrefixes=new HashMap<String,String>();
        s_semanticWebPrefixes.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        s_semanticWebPrefixes.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
        s_semanticWebPrefixes.put("owl","http://www.w3.org/2002/07/owl#");
        s_semanticWebPrefixes.put("xsd","http://www.w3.org/2001/XMLSchema#");
        s_semanticWebPrefixes.put("swrl","http://www.w3.org/2003/11/swrl#");
        s_semanticWebPrefixes.put("swrlb","http://www.w3.org/2003/11/swrlb#");
        s_semanticWebPrefixes.put("swrlx","http://www.w3.org/2003/11/swrlx#");
        s_semanticWebPrefixes.put("ruleml","http://www.w3.org/2003/11/ruleml#");
    }
    @SuppressWarnings("serial")
    public static final Prefixes EMPTY=new Prefixes() {
        protected boolean declarePrefixRaw(String prefixName,String prefixIRI) {
            throw new UnsupportedOperationException("The well-known empty Prefix instance cannot be modified.");
        }
    };

    protected final Map<String,String> m_prefixIRIsByPrefixName;
    protected final Map<String,String> m_prefixNamesByPrefixIRI;
    protected Pattern m_prefixIRIMathingPattern;

    public Prefixes() {
        m_prefixIRIsByPrefixName=new TreeMap<String,String>();
        m_prefixNamesByPrefixIRI=new TreeMap<String,String>();
        buildPrefixIRIMatchingPattern();
    }
    protected void buildPrefixIRIMatchingPattern() {
        List<String> list=new ArrayList<String>(m_prefixNamesByPrefixIRI.keySet());
        // Sort the prefix IRIs, longest first
        Collections.sort(list,new Comparator<String>() {
            public int compare(String lhs,String rhs) {
                return rhs.length()-lhs.length();
            }
        });
        StringBuilder pattern=new StringBuilder("^(");
        boolean didOne=false;
        for (String prefixIRI : list) {
            if (didOne)
                pattern.append("|(");
            else {
                pattern.append("(");
                didOne=true;
            }
            pattern.append(Pattern.quote(prefixIRI));
            pattern.append(")");
        }
        pattern.append(")");
        if (didOne)
            m_prefixIRIMathingPattern=Pattern.compile(pattern.toString());
        else
            m_prefixIRIMathingPattern=null;
    }
    public String abbreviateURI(String uri) {
        if (m_prefixIRIMathingPattern!=null) {
            Matcher matcher=m_prefixIRIMathingPattern.matcher(uri);
            if (matcher.find()) {
                String localName=uri.substring(matcher.end());
                if (isValidLocalName(localName)) {
                    String prefix=m_prefixNamesByPrefixIRI.get(matcher.group(1));
                    if (prefix==null || prefix.length()==0)
                        return ":"+localName;
                    else
                        return prefix+":"+localName;
                }
            }
        }
        return "<"+uri+">";
    }
    /**
     * Expands a full URI from the abbreviated one, which is of one of the following forms:
     * 'prefix:name', where 'prefix' is a registered prefix name (can be empty), or
     * '&lt;uri&gt;', where 'uri' is a URI.
     */
    public String expandAbbreviatedURI(String abbreviation) {
        if (abbreviation.length()>0 && abbreviation.charAt(0)=='<') {
            if (abbreviation.charAt(abbreviation.length()-1)!='>')
                throw new IllegalArgumentException("The string '"+abbreviation+"' is not a valid abbreviation: URIs must be enclosed in '<' and '>'.");
            return abbreviation.substring(1,abbreviation.length()-1);
        }
        else {
            int pos=abbreviation.indexOf(':');
            if (pos!=-1) {
                String prefix=abbreviation.substring(0,pos);
                String ns=m_prefixIRIsByPrefixName.get(prefix);
                if (ns==null) {
                    // Catch the common error of not quoting URIs starting with http:
                    if (prefix=="http")
                        throw new IllegalArgumentException("The URI '"+abbreviation+"' must be enclosed in '<' and '>' to be used as an abbreviation.");
                    throw new IllegalArgumentException("The string '"+prefix+"' is not a registered prefix name.");
                }
                return ns+abbreviation.substring(pos+1);
            }
            else
                throw new IllegalArgumentException("The abbreviation '"+abbreviation+"' is not valid (it does not start with a colon).");
        }
    }
    public boolean declarePrefix(String prefixName,String prefixIRI) {
        boolean containsPrefix=declarePrefixRaw(prefixName,prefixIRI);
        buildPrefixIRIMatchingPattern();
        return containsPrefix;
    }
    protected boolean declarePrefixRaw(String prefixName,String prefixIRI) {
        String existingPrefixName=m_prefixNamesByPrefixIRI.get(prefixIRI);
        if (existingPrefixName!=null && !prefixName.equals(existingPrefixName))
            throw new IllegalArgumentException("The prefix IRI '"+prefixIRI+"'has already been associated with the prefix name '"+existingPrefixName+"'.");
        m_prefixNamesByPrefixIRI.put(prefixIRI,prefixName);
        return m_prefixIRIsByPrefixName.put(prefixName,prefixIRI)==null;
    }
    public boolean declareDefaultPrefix(String defaultPrefixIRI) {
        return declarePrefix("",defaultPrefixIRI);
    }
    public Map<String,String> getPrefixIRIsByPrefixName() {
        return java.util.Collections.unmodifiableMap(m_prefixIRIsByPrefixName);
    }
    public String getPrefixIRI(String prefixName) {
        return m_prefixIRIsByPrefixName.get(prefixName);
    }
    public String getPrefixName(String prefixIRI) {
        return m_prefixNamesByPrefixIRI.get(prefixIRI);
    }
    /**
     * Registers HermiT's internal prefixes with this object.
     * 
     * @param individualURIs    the collection of URIs used in individuals (used for registering nominal prefix names)
     * @return                  'true' if this object already contained one of the internal prefix names
     */
    public boolean declareInternalPrefixes(Collection<String> individualURIs) {
        boolean containsPrefix=false;
        if (declarePrefixRaw("def","internal:def#"))
            containsPrefix=true;
        if (declarePrefixRaw("nnq","internal:nnq#"))
            containsPrefix=true;
        if (declarePrefixRaw("amq","internal:amq#"))
            containsPrefix=true;
        if (declarePrefixRaw("all","internal:all#"))
            containsPrefix=true;
        int individualURIsIndex=1;
        for (String uri : individualURIs) {
            if (declarePrefixRaw("nom"+(individualURIsIndex==1 ? "" : String.valueOf(individualURIsIndex)),"internal:nom#"+uri))
                containsPrefix=true;
            individualURIsIndex++;
        }
        if (declarePrefixRaw("nam","internal:nam#"))
            containsPrefix=true;
        if (declarePrefixRaw("grd","internal:grd#"))
            containsPrefix=true;
        buildPrefixIRIMatchingPattern();
        return containsPrefix;
    }
    /**
     * Registers the well-known Semantic Web prefixes.
     * 
     * @return                  'true' if this object already contained one of the well-known prefixes
     */
    public boolean declareSemanticWebPrefixes() {
        boolean containsPrefix=false;
        for (Map.Entry<String,String> entry : s_semanticWebPrefixes.entrySet())
            if (declarePrefixRaw(entry.getKey(),entry.getValue()))
                containsPrefix=true;
        buildPrefixIRIMatchingPattern();
        return containsPrefix;
    }
    /**
     * Registers all the prefixes from the supplied object.
     * 
     * @param prefixes          the object from which the prefixes are taken
     * @return                  'true' if this object already contained one of the prefixes from the supplied object
     */
    public boolean addPrefixes(Prefixes prefixes) {
        boolean containsPrefix=false;
        for (Map.Entry<String,String> entry : prefixes.m_prefixIRIsByPrefixName.entrySet())
            if (declarePrefixRaw(entry.getKey(),entry.getValue()))
                containsPrefix=true;
        buildPrefixIRIMatchingPattern();
        return containsPrefix;
    }
    /**
     * Determines whether the supplied URI is used internally by HermiT.
     */
    public static boolean isInternalURI(String uri) {
        return uri.startsWith("internal:");
    }
    /**
     * Determines whether the supplied string is a valid local name.
     */
    public static boolean isValidLocalName(String localName) {
        return s_localNameChecker.matcher(localName).matches();
    }
}
