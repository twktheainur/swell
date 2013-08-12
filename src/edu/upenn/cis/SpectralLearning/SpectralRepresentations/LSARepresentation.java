package edu.upenn.cis.SpectralLearning.SpectralRepresentations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import Jama.Matrix;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import edu.upenn.cis.SpectralLearning.Data.Corpus;
import edu.upenn.cis.SpectralLearning.Data.Document;
import edu.upenn.cis.SpectralLearning.IO.Options;
import edu.upenn.cis.SpectralLearning.IO.ReadDataFile;

public class LSARepresentation extends SpectralRepresentation implements Serializable  {

	//private Corpus _corpus;
	private int _vocab_size;
	double[][] X;
	private ReadDataFile _rin;
	SparseDoubleMatrix2D XMatrix,XMatrixT;
	static final long serialVersionUID = 42L;
	ArrayList<ArrayList<Integer>> _allDocs;
	long _numTokens;
	
	public LSARepresentation(Options opt, long numTokens,ReadDataFile rin,ArrayList<ArrayList<Integer>> all_Docs) {
		super(opt, numTokens);
		_allDocs=all_Docs;
		_numTokens=numTokens;
		_rin=rin;
		_vocab_size=super._opt.vocabSize;
		XMatrix = new SparseDoubleMatrix2D(_vocab_size+1,_allDocs.size());
		XMatrixT = new SparseDoubleMatrix2D(_allDocs.size(),_vocab_size+1);
	}

	public void populateTermDocMatrix(){
		
		int doc_idx=-1,idx=0,tok_idx;
		HashMap<Double,Double> hMCounts=new HashMap<Double,Double>();
		
		while(idx<_allDocs.size()){
				ArrayList<Integer> doc=_allDocs.get(idx++);
				doc_idx++;
				tok_idx=0;
				while(tok_idx<doc.size()){
					int _tok=doc.get(tok_idx++);
					if(hMCounts.get((double)_tok) !=null)
						hMCounts.put((double) _tok, 1+hMCounts.get((double)_tok));
					else
						hMCounts.put((double) _tok, 1.0);
				}	
		}
		idx=0;doc_idx=-1;
		while(idx<_allDocs.size()){
			ArrayList<Integer> doc=_allDocs.get(idx++);
			doc_idx++;
			tok_idx=0;
			while(tok_idx<doc.size()){
				int _tok=doc.get(tok_idx++);
				double xCount=XMatrix.get(_tok, doc_idx);
				XMatrix.setQuick(_tok,doc_idx,xCount+ 1/hMCounts.get((double)_tok));
				XMatrixT.setQuick(doc_idx,_tok,xCount+ 1/hMCounts.get((double)_tok));
			}	
		}
		
		
	}
	
	public DenseDoubleMatrix2D getOmegaMatrix(){//Refer Tropp's notation
		Random r= new Random();
		DenseDoubleMatrix2D Omega= new DenseDoubleMatrix2D(_allDocs.size(),_num_hidden+20);//Oversampled the rank k
		for (int i=0;i<_allDocs.size();i++){
			for (int j=0;j<_num_hidden+20;j++)
				Omega.set(i,j,r.nextGaussian());
		}
		return Omega;
	}
	
	public SparseDoubleMatrix2D getTermDocMatrix(){
		return XMatrix;
	}

	public SparseDoubleMatrix2D getTermDocMatrixT(){
		return XMatrixT;
	}

	public Matrix getContextOblEmbeddings(Matrix eigenFeatDict) {
		Matrix WProjectionMatrix;
		
		WProjectionMatrix=generateWProjections(_allDocs,_rin.getSortedWordList(),eigenFeatDict);
		
		
		return WProjectionMatrix;

	}
	
	
	public void serializeLSARepresentation() {
		File f= new File(_opt.serializeRep);
		
		try{
			ObjectOutput lsaRep=new ObjectOutputStream(new FileOutputStream(f));
			lsaRep.writeObject(this);
			
			System.out.println("=======Serialized the LSA Representation=======");
		}
		catch (IOException ioe){
			System.out.println(ioe.getMessage());
		}
		
	}
	
	
}