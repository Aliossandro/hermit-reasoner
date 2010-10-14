package org.semanticweb.HermiT.reasoner;

public class RIARegularityTest extends AbstractReasonerTest {
	
	 public RIARegularityTest(String name) {
	        super(name);
	 }
    
	 // TODO This hierarchy is actually irregular because the first axiom means that 
	 // loves -> topObjectProperty must hold with -> the simple/composite relations, but 
     // the chain requires topObjectProperty < loves, which cannot be achieved when
	 // loves -> topObjectProperty is required. At the moment HermiT deletes 
	 // SubObjectPropertyOf(anyProperty topObjectProperty) and 
	 // SubObjectPropertyOf(bottomObjectProperty anyProperty) axioms, so we miss the 
	 // loves -> topObjectProperty constraint. Should we change that? Protege introduces
	 // tons of SubObjectPropertyOf(anyProperty topObjectProperty) axioms without telling 
	 // the user....
//    public void testRIARegularity0() throws Exception{
//         String axioms = "SubObjectPropertyOf(:loves owl:topObjectProperty) " +
//                         "SubObjectPropertyOf(ObjectPropertyChain(:pHuman owl:topObjectProperty :pCat) :loves) ";
//         assertRegular(axioms,false);
//     }
	 public void testRIARegularity1() throws Exception{
		 String axioms = "SubObjectPropertyOf(:A :B) " +
		 				 "SubObjectPropertyOf(:B :C) " +
		 				 "SubObjectPropertyOf(:C :D) " +
		 				 "SubObjectPropertyOf(:D :A) ";
	     assertRegular(axioms,true);
	 }
	 public void testRIARegularity2() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q) :P) " +
		 				 "InverseObjectProperties(:P :Q) ";
	     assertRegular(axioms,false);
	 }
//	 The following is in disagreement with FaCT++
	 public void testRIARegularity3() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R ObjectInverseOf(:Q)) :P) " +
		 				 "InverseObjectProperties(:P :Q) ";
	     assertRegular(axioms,false);
	 }
	 public void testRIARegularity4() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q :P) :P) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:P :S) :Q) " +
		 				 "SubObjectPropertyOf(:Q :R) ";
	     assertRegular(axioms,false);
	 }
	 public void testRIARegularity5() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q :P) :P) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:P :S) :L) " +
		 				 "SubObjectPropertyOf(:L :R) " +
		 				 "SubObjectPropertyOf(:R :L) ";
	     assertRegular(axioms,true);
	 }
	 public void testRIARegularity6() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:P ObjectInverseOf(:P) :P) :P) ";
	     assertRegular(axioms,false);
	 }
	 public void testRIARegularity7() throws Exception{
		 String axioms = "InverseObjectProperties(:P :P-) "+
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:L :P-) :L) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:R :L) :P) ";
	     assertRegular(axioms,false);
	 }
	 public void testRIARegularity8() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R4 :R1) :R1) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:R1 :R2) :R2) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:R2 :R3) :R3) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:R3 :R4) :R4) " +
		 				 "EquivalentObjectProperties( :R1 :R2 )" +
		 				 "EquivalentObjectProperties( :R2 :R3 )" +
		 				 "EquivalentObjectProperties( :R3 :R4 )" +
		 				 "EquivalentObjectProperties( :R4 :R1 )";
	     assertRegular(axioms,true);
	 }
	 public void testRIARegularity9() throws Exception{
		 String axioms = 	"SubObjectPropertyOf(ObjectPropertyChain(:R1 :R2 :R3) :R) " +
		 					"EquivalentObjectProperties( :R2 :R )";
	     assertRegular(axioms,false);
	 }
}
